"""Las capas que envuelven a TODAS las peticiones (middlewares ASGI). De afuera hacia adentro:

  1. Registro            id de solicitud, IP real, bitácora JSON y métricas.
  2. EncabezadosSeguridad los encabezados HTTP de seguridad (HSTS, CSP, nosniff, …).
  3. HostValido           rechaza un Host que no es nuestro (ataques de Host header).
  4. LimiteCuerpo         rechaza cuerpos enormes antes de leerlos (OWASP API4).
  5. (rate limit global de slowapi, ver limites.py)
  6. CapturarErrores      la red de seguridad: nada inesperado sale como traza.

Defensa en profundidad: cada capa es independiente; si una falla, las demás siguen.
"""

import json
import logging
import time
import uuid
from ipaddress import ip_address

from starlette.types import ASGIApp, Message, Receive, Scope, Send

from . import metricas
from .errores import CuerpoMuyGrande
from .bitacora import ip_hmac
from .config import ajustes

log = logging.getLogger("familias.acceso")
log_errores = logging.getLogger("familias.errores")


def _encabezado(scope: Scope, nombre: bytes) -> str | None:
    for k, v in scope.get("headers", []):
        if k == nombre:
            return v.decode("latin-1")
    return None


def ip_real(scope: Scope) -> tuple[str, bool]:
    """La IP de quien llama y si llegó por el túnel.

    Detrás de Cloudflare, la conexión TCP la abre el contenedor del túnel: su IP es la misma
    para todo el mundo. Cloudflare manda la IP real en `CF-Connecting-IP`, pero ese encabezado
    cualquiera lo puede escribir. Por eso SOLO se le cree cuando la conexión viene del túnel
    (su IP es fija en la red de Docker). Sin esto, el rate limit castigaría a todos por igual,
    o un atacante falsificaría su IP para saltárselo.
    """
    cliente = scope.get("client")
    par = cliente[0] if cliente else "0.0.0.0"
    try:
        if ip_address(par) == ajustes.ip_del_tunel:
            cf = _encabezado(scope, b"cf-connecting-ip")
            if cf:
                return str(ip_address(cf.strip())), True
            return par, True
    except ValueError:
        pass
    return par, False


async def _responder_json(send: Send, status: int, cuerpo: dict, extra: list[tuple[bytes, bytes]] | None = None) -> None:
    datos = json.dumps(cuerpo, ensure_ascii=False).encode("utf-8")
    headers = [(b"content-type", b"application/json"), (b"content-length", str(len(datos)).encode())]
    await send({"type": "http.response.start", "status": status, "headers": headers + (extra or [])})
    await send({"type": "http.response.body", "body": datos})


class Registro:
    """Pone un id a cada solicitud, resuelve la IP real y escribe una línea JSON al terminar.

    En la bitácora NUNCA va el token, ni el cuerpo, ni la IP en claro (va su HMAC).
    """

    def __init__(self, app: ASGIApp) -> None:
        self.app = app

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            return await self.app(scope, receive, send)
        inicio = time.perf_counter()
        solicitud = uuid.uuid4().hex[:16]
        ip, via_tunel = ip_real(scope)
        estado_http = {"codigo": 500}
        scope.setdefault("state", {}).update(solicitud=solicitud, ip_cliente=ip, via_tunel=via_tunel)

        async def enviar(mensaje: Message) -> None:
            if mensaje["type"] == "http.response.start":
                estado_http["codigo"] = mensaje["status"]
                mensaje.setdefault("headers", []).append((b"x-request-id", solicitud.encode()))
            await send(mensaje)

        try:
            await self.app(scope, receive, enviar)
        finally:
            ms = (time.perf_counter() - inicio) * 1000
            # La plantilla de la ruta ("/v1/actividades/{actividad_id}"), no la ruta real:
            # así la métrica no explota en mil etiquetas y no se guardan ids en Prometheus.
            ruta = getattr(scope.get("route"), "path", None) or "sin_ruta"
            metodo = scope.get("method", "-")
            metricas.peticiones.labels(metodo=metodo, ruta=ruta, estado=str(estado_http["codigo"])).inc()
            metricas.duracion.labels(metodo=metodo, ruta=ruta).observe(ms / 1000)
            estado = scope.get("state", {})
            log.info(
                "solicitud",
                extra={
                    "solicitud": solicitud,
                    "metodo": metodo,
                    "ruta": ruta,
                    "estado": estado_http["codigo"],
                    "ms": round(ms, 1),
                    "cuenta": estado.get("cuenta_id"),
                    "ip_hmac": ip_hmac(ip),
                    "via_tunel": via_tunel,
                },
            )


# Para JSON: no se permite cargar NADA, ni embeber la respuesta en un iframe.
CSP_API = "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"
# Solo para /docs en desarrollo: Swagger UI carga su JavaScript de jsDelivr.
CSP_DOCS = (
    "default-src 'none'; script-src 'self' https://cdn.jsdelivr.net 'unsafe-inline'; "
    "style-src 'self' https://cdn.jsdelivr.net 'unsafe-inline'; img-src 'self' data: https://fastapi.tiangolo.com; "
    "connect-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"
)

ENCABEZADOS_BASE: list[tuple[bytes, bytes]] = [
    # HSTS: el navegador solo vuelve a entrar por HTTPS, ni el primer clic va por HTTP (SSL stripping).
    (b"strict-transport-security", b"max-age=31536000; includeSubDomains"),
    # Que el navegador no "adivine" el tipo: un .jpg con HTML adentro no se ejecuta como página.
    (b"x-content-type-options", b"nosniff"),
    # Nadie puede meter la API ni la página de enlaces en un iframe (clickjacking).
    (b"x-frame-options", b"DENY"),
    (b"referrer-policy", b"no-referrer"),
    (b"permissions-policy", b"camera=(), microphone=(), geolocation=(), payment=(), usb=()"),
    (b"cross-origin-opener-policy", b"same-origin"),
    (b"cross-origin-resource-policy", b"same-origin"),
    (b"cross-origin-embedder-policy", b"require-corp"),
    (b"x-permitted-cross-domain-policies", b"none"),
]


class EncabezadosSeguridad:
    """Agrega los encabezados de seguridad a cada respuesta (también a los errores).

    Si la ruta ya puso uno (la página de enlaces pone su propia CSP), se respeta el suyo.
    """

    def __init__(self, app: ASGIApp) -> None:
        self.app = app

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            return await self.app(scope, receive, send)
        es_docs = scope.get("path", "").startswith("/docs")

        async def enviar(mensaje: Message) -> None:
            if mensaje["type"] == "http.response.start":
                headers = list(mensaje.get("headers", []))
                presentes = {k.lower() for k, _ in headers}
                for k, v in ENCABEZADOS_BASE:
                    if k not in presentes:
                        headers.append((k, v))
                if b"content-security-policy" not in presentes:
                    headers.append((b"content-security-policy", (CSP_DOCS if es_docs else CSP_API).encode()))
                if b"cache-control" not in presentes:
                    # Por omisión nada se guarda en caché: ni datos personales ni tokens en un proxy.
                    headers.append((b"cache-control", b"no-store"))
                # No decir qué servidor ni qué versión corre (huella para buscar CVEs).
                headers = [(k, v) for k, v in headers if k.lower() not in (b"server", b"x-powered-by")]
                mensaje["headers"] = headers
            await send(mensaje)

        await self.app(scope, receive, enviar)


class HostValido:
    """Solo se atienden peticiones dirigidas a nuestros nombres. Un Host falso podría usarse
    para envenenar enlaces o cachés. Acepta comodines tipo `*.trycloudflare.com`."""

    def __init__(self, app: ASGIApp) -> None:
        self.app = app
        self.exactos = {h.lower() for h in ajustes.lista_hosts if not h.startswith("*.")}
        self.sufijos = tuple(h[1:].lower() for h in ajustes.lista_hosts if h.startswith("*."))

    def _permitido(self, host: str) -> bool:
        nombre = host.rsplit(":", 1)[0].strip("[]").lower() if host.count(":") <= 1 else host.lower()
        return nombre in self.exactos or (bool(self.sufijos) and nombre.endswith(self.sufijos))

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            return await self.app(scope, receive, send)
        host = _encabezado(scope, b"host") or ""
        if not self._permitido(host):
            metricas.evento_seguridad("host_invalido")
            return await _responder_json(send, 400, {"error": "Solicitud inválida.", "codigo": "host_invalido"})
        await self.app(scope, receive, send)


class LimiteCuerpo:
    """Corta los cuerpos que pasan del límite: primero por Content-Length y, si no lo traen
    (transferencia por partes), contando los bytes conforme llegan. Así nadie llena la
    memoria del servidor mandando un archivo gigante (OWASP API4 · CWE-400)."""

    def __init__(self, app: ASGIApp) -> None:
        self.app = app

    @staticmethod
    def _limite(scope: Scope) -> int:
        tipo = (_encabezado(scope, b"content-type") or "").lower()
        if tipo.startswith("multipart/form-data"):
            return ajustes.max_foto_bytes + 64_000
        return ajustes.max_cuerpo_bytes

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            return await self.app(scope, receive, send)
        limite = self._limite(scope)
        largo = _encabezado(scope, b"content-length")
        respuesta_iniciada = False

        async def rechazar() -> None:
            metricas.evento_seguridad("cuerpo_muy_grande")
            await _responder_json(send, 413, {"error": "El archivo o los datos son demasiado grandes.", "codigo": "cuerpo_muy_grande"})

        if largo is not None:
            try:
                if int(largo) > limite:
                    return await rechazar()
            except ValueError:
                return await _responder_json(send, 400, {"error": "Solicitud inválida.", "codigo": "longitud_invalida"})

        recibidos = 0

        async def recibir() -> Message:
            nonlocal recibidos
            mensaje = await receive()
            if mensaje["type"] == "http.request":
                recibidos += len(mensaje.get("body", b""))
                if recibidos > limite:
                    raise CuerpoMuyGrande()
            return mensaje

        async def enviar(mensaje: Message) -> None:
            nonlocal respuesta_iniciada
            if mensaje["type"] == "http.response.start":
                respuesta_iniciada = True
            await send(mensaje)

        try:
            await self.app(scope, recibir, enviar)
        except CuerpoMuyGrande:
            if not respuesta_iniciada:
                await rechazar()


class CapturarErrores:
    """La última red: si algo se escapó de todos los manejadores, sale un 500 genérico con
    los encabezados de seguridad (porque esta capa está DENTRO de EncabezadosSeguridad)."""

    def __init__(self, app: ASGIApp) -> None:
        self.app = app

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            return await self.app(scope, receive, send)
        iniciada = False

        async def enviar(mensaje: Message) -> None:
            nonlocal iniciada
            if mensaje["type"] == "http.response.start":
                iniciada = True
            await send(mensaje)

        try:
            await self.app(scope, receive, enviar)
        except Exception as e:  # noqa: BLE001 — a propósito: es la red de seguridad
            if isinstance(e, CuerpoMuyGrande):
                raise
            solicitud = scope.get("state", {}).get("solicitud")
            log_errores.error("Error no manejado", exc_info=e, extra={"solicitud": solicitud})
            if not iniciada:
                await _responder_json(send, 500, {"error": "Algo salió mal de nuestro lado.", "codigo": "error_interno", "solicitud": solicitud})
