"""Rate limiting: cuántas veces se puede llamar algo en un tiempo (OWASP API4 y API6).

Se usa slowapi, como en la clase, con los contadores en Redis: así los límites se respetan
aunque la API se reinicie o corran varias copias. Ventana DESLIZANTE (moving window): no hay
"minuto nuevo" en el que un bot pueda disparar el doble justo en el cambio.

Dos llaves de conteo:
  · por IP (la real, ver seguridad.ip_real) para lo que es público;
  · por CUENTA para lo que pide sesión: un bot con muchas IPs pero una sola cuenta igual se frena.

¿Y la fuerza bruta contra el inicio de sesión? Dos frenos que se suman:
  · por IP: INICIAR_SESION (abajo) — un bot desde una máquina no puede probar mil contraseñas;
  · por CORREO: tras 5 contraseñas equivocadas seguidas, ese correo se bloquea 15 minutos venga
    de la IP que venga (identidad.SesionesCerradas.anotar_fallo) — un bot con muchas IPs tampoco.

Si Redis se cae, slowapi sigue contando en la memoria del proceso con un límite más estricto:
se protege la disponibilidad sin quedarse sin freno.
"""

from slowapi import Limiter
from starlette.requests import Request

from .config import ajustes


def por_ip(request: Request) -> str:
    return f"ip:{getattr(request.state, 'ip_cliente', None) or 'desconocida'}"


def por_cuenta(request: Request) -> str:
    """Las rutas con sesión ya pasaron por identidad.usuario_actual, que dejó aquí la cuenta."""
    cuenta = getattr(request.state, "cuenta_id", None)
    return f"cuenta:{cuenta}" if cuenta else por_ip(request)


limitador = Limiter(
    key_func=por_ip,
    default_limits=[ajustes.limite_global],
    storage_uri=ajustes.redis_url,
    strategy="moving-window",
    key_prefix="limite",
    in_memory_fallback_enabled=True,
    in_memory_fallback=["60/minute"],
    headers_enabled=False,
)

# Los límites de cada flujo sensible, en un solo lugar para poder revisarlos (y explicarlos).
INICIAR_SESION = "10/minute;60/hour"  # por IP; el bloqueo por correo va aparte (identidad.py)
REGISTRARSE = "10/hour"             # crear cuentas en masa (bots, spam) desde una IP
RECUPERAR = "5/hour"
RESTABLECER = "10/hour"             # escribir el código: además se quema al 5.º intento (recuperacion.py)
RENOVAR = "30/minute"               # la app renueva cada 15 min; más que esto desde una IP no es normal
SUGERIR = "5/hour"                  # público y anónimo: lo más estricto
INSCRIBIR = "30/hour"               # inscribirse, modificar y cancelar
APORTAR = "30/hour"
SUBIR_FOTO = "10/hour"
ENCUESTA = "30/hour"
INVITAR = "60/hour"
ESCRIBIR_PERFIL = "60/hour"
ADMIN = "300/hour"
ENLACE_PUBLICO = "60/minute"


# ─── El límite GLOBAL (todas las rutas) ─────────────────────────────────────
# slowapi trae un middleware para esto, pero al verificar el backend se encontró que con FastAPI
# 0.143 no encuentra las rutas de los routers incluidos y, cuando no encuentra la ruta, NO aplica
# el límite (125 de 125 peticiones pasaban con un límite de 120/minuto). Por eso el límite global
# lo aplica este middleware, con la misma librería (`limits`) y los mismos contadores en Redis.
# Los límites por ruta (los decoradores @limitador.limit) sí funcionan y se quedan.

import time  # noqa: E402

import anyio  # noqa: E402
from limits import parse  # noqa: E402
from limits.storage import MemoryStorage  # noqa: E402
from limits.strategies import MovingWindowRateLimiter  # noqa: E402
from starlette.types import ASGIApp, Receive, Scope, Send  # noqa: E402

from . import metricas  # noqa: E402

regla_global = parse(ajustes.limite_global)
_respaldo = MovingWindowRateLimiter(MemoryStorage())
_regla_respaldo = parse("60/minute")

# Lo que no cuenta: el healthcheck de Docker y Prometheus (vienen de dentro del contenedor).
_EXENTAS = ("/v1/salud",)


def _contar(ip: str) -> tuple[bool, int]:
    """Cuenta una petición de esta IP. Devuelve (se permite, segundos para reintentar)."""
    try:
        permitido = limitador.limiter.hit(regla_global, "global", ip)
        reintentar = 0 if permitido else max(1, int(limitador.limiter.get_window_stats(regla_global, "global", ip).reset_time - time.time()))
        return permitido, reintentar
    except Exception:  # noqa: BLE001 — sin Redis: se sigue frenando, en memoria y más estricto
        return _respaldo.hit(_regla_respaldo, "global", ip), 60


class LimiteGlobal:
    def __init__(self, app: ASGIApp) -> None:
        self.app = app

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http" or scope.get("path", "").startswith(_EXENTAS):
            return await self.app(scope, receive, send)
        ip = scope.get("state", {}).get("ip_cliente") or "desconocida"
        permitido, reintentar = await anyio.to_thread.run_sync(_contar, ip)
        if permitido:
            return await self.app(scope, receive, send)
        metricas.evento_seguridad("limite_excedido")
        cuerpo = b'{"error":"Hiciste demasiadas solicitudes seguidas. Espera un momento.","codigo":"demasiadas_solicitudes"}'
        await send({"type": "http.response.start", "status": 429, "headers": [
            (b"content-type", b"application/json"), (b"content-length", str(len(cuerpo)).encode()),
            (b"retry-after", str(reintentar).encode()),
        ]})
        await send({"type": "http.response.body", "body": cuerpo})
