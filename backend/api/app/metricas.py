"""Las métricas que lee Prometheus y pinta Grafana (OWASP A09 · registro y alertas).

Se publican en un puerto aparte (9100) dentro de la red de monitoreo. El túnel de Cloudflare
solo lleva al puerto 8000, así que /metrics nunca queda en internet (OWASP API9: un endpoint
interno expuesto fue justo lo que pasó en Optus, 2022).

Además del tráfico normal se cuentan los EVENTOS DE SEGURIDAD: contraseñas equivocadas, tokens inválidos o vencidos,
403, 429, archivos rechazados… Un pico de cualquiera de ellos dispara una alerta
(monitoreo/prometheus/alertas.yml).
"""

from prometheus_client import Counter, Histogram, start_http_server

peticiones = Counter(
    "familias_http_peticiones_total",
    "Peticiones HTTP atendidas",
    ["metodo", "ruta", "estado"],
)
duracion = Histogram(
    "familias_http_duracion_segundos",
    "Cuánto tarda la API en responder",
    ["metodo", "ruta"],
    buckets=(0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1, 2.5, 5),
)
seguridad = Counter(
    "familias_eventos_seguridad_total",
    "Eventos de seguridad: credenciales_invalidas, token_invalido, token_expirado, sesion_cerrada, prohibido, "
    "limite_excedido, entrada_invalida, archivo_rechazado, cuerpo_muy_grande, host_invalido…",
    ["tipo"],
)
negocio = Counter(
    "familias_operaciones_total",
    "Operaciones de la app: inscripciones, cancelaciones, aportaciones, testimonios…",
    ["operacion", "resultado"],
)


def evento_seguridad(tipo: str) -> None:
    seguridad.labels(tipo=tipo).inc()


def operacion(nombre: str, resultado: str = "ok") -> None:
    negocio.labels(operacion=nombre, resultado=resultado).inc()


def publicar(puerto: int) -> None:
    """Abre el servidor de métricas en su propio hilo. Se llama una vez al arrancar."""
    from .resumen import registrar_en_prometheus  # importa la base: solo cuando sí hay métricas

    registrar_en_prometheus()
    start_http_server(puerto, addr="0.0.0.0")
