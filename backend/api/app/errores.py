"""Los errores, con UNA sola forma para toda la API:

    { "error": "texto para la persona", "codigo": "slug_para_la_app", "solicitud": "id" }

OWASP A10:2025 (manejo de condiciones excepcionales): cuando algo truena, la API
  · responde un mensaje genérico (nunca la traza, la consulta SQL ni la versión de nada),
  · guarda el detalle en la bitácora del servidor con el id de la solicitud,
  · y FALLA CERRADO: si no puede verificar algo (la base, Redis), niega; no deja pasar.
"""

import logging
from typing import Any

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from slowapi.errors import RateLimitExceeded
from sqlalchemy.exc import DBAPIError, OperationalError
from starlette.exceptions import HTTPException as StarletteHTTPException

from . import metricas

log = logging.getLogger("familias.errores")


class CuerpoMuyGrande(Exception):
    """La lanza seguridad.LimiteCuerpo cuando un cuerpo sin Content-Length pasa del límite."""


class ApiError(Exception):
    """Un error que la API decide responder a propósito (401, 403, 404, 409…)."""

    def __init__(self, status: int, error: str, codigo: str, datos: dict[str, Any] | None = None) -> None:
        super().__init__(error)
        self.status = status
        self.error = error
        self.codigo = codigo
        self.datos = datos


def no_encontrado(que: str = "Eso") -> ApiError:
    # 404 y no 403 cuando algo es de otra cuenta: así no se confirma ni que existe (OWASP API1).
    return ApiError(404, f"{que} no existe o no es tuyo.", "no_encontrado")


def _cuerpo(request: Request, error: str, codigo: str, datos: dict | None = None) -> dict:
    cuerpo: dict[str, Any] = {"error": error, "codigo": codigo}
    if datos:
        cuerpo["datos"] = datos
    solicitud = getattr(request.state, "solicitud", None)
    if solicitud:
        cuerpo["solicitud"] = solicitud
    return cuerpo


def instalar(app: FastAPI) -> None:
    @app.exception_handler(ApiError)
    async def _api_error(request: Request, e: ApiError) -> JSONResponse:
        headers: dict[str, str] = {}
        if e.status == 401:
            # El estándar (RFC 6750): un 401 dice cómo autenticarse.
            headers["WWW-Authenticate"] = f'Bearer realm="familias", error="{e.codigo}"'
            metricas.evento_seguridad(e.codigo)
        elif e.status == 403:
            metricas.evento_seguridad("prohibido")
        elif e.status == 429:
            headers["Retry-After"] = str((e.datos or {}).get("reintentar_en_s", 60))
            metricas.evento_seguridad("limite_excedido")
        return JSONResponse(_cuerpo(request, e.error, e.codigo, e.datos), status_code=e.status, headers=headers)

    @app.exception_handler(RequestValidationError)
    async def _validacion(request: Request, e: RequestValidationError) -> JSONResponse:
        # Se dice QUÉ campo está mal, pero no se repite lo que mandaron (no se refleja la entrada).
        primero = e.errors()[0] if e.errors() else {}
        campo = ".".join(str(p) for p in primero.get("loc", [])[1:]) or "cuerpo"
        mensaje = str(primero.get("msg", "dato inválido")).removeprefix("Value error, ")
        metricas.evento_seguridad("entrada_invalida")
        return JSONResponse(
            {**_cuerpo(request, f"Revisa '{campo}': {mensaje}", "dato_invalido"), "campo": campo},
            status_code=422,
        )

    @app.exception_handler(StarletteHTTPException)
    async def _http(request: Request, e: StarletteHTTPException) -> JSONResponse:
        # FastAPI envuelve cualquier falla al leer el cuerpo en un 400; si la causa fue el
        # límite de tamaño, se responde lo que de verdad pasó.
        if isinstance(e.__cause__, CuerpoMuyGrande):
            metricas.evento_seguridad("cuerpo_muy_grande")
            return JSONResponse(_cuerpo(request, "El archivo o los datos son demasiado grandes.", "cuerpo_muy_grande"), status_code=413)
        if e.status_code == 400:
            return JSONResponse(_cuerpo(request, "No se pudo leer la solicitud.", "solicitud_invalida"), status_code=400)
        codigos = {404: ("Esa ruta no existe.", "ruta_inexistente"), 405: ("Ese método no se permite aquí.", "metodo_no_permitido")}
        error, codigo = codigos.get(e.status_code, ("No se pudo atender la solicitud.", "error_http"))
        return JSONResponse(_cuerpo(request, error, codigo), status_code=e.status_code)

    @app.exception_handler(RateLimitExceeded)
    async def _limite(request: Request, e: RateLimitExceeded) -> JSONResponse:
        metricas.evento_seguridad("limite_excedido")
        return JSONResponse(
            _cuerpo(request, "Hiciste demasiadas solicitudes seguidas. Espera un momento.", "demasiadas_solicitudes"),
            status_code=429,
            headers={"Retry-After": "60"},
        )

    @app.exception_handler(OperationalError)
    @app.exception_handler(DBAPIError)
    async def _base(request: Request, e: Exception) -> JSONResponse:
        log.error("Falla de la base de datos", exc_info=e, extra={"solicitud": getattr(request.state, "solicitud", None)})
        return JSONResponse(
            _cuerpo(request, "El servicio no está disponible. Intenta en unos minutos.", "servicio_no_disponible"),
            status_code=503,
            headers={"Retry-After": "30"},
        )

    @app.exception_handler(Exception)
    async def _inesperado(request: Request, e: Exception) -> JSONResponse:
        log.error("Error inesperado", exc_info=e, extra={"solicitud": getattr(request.state, "solicitud", None)})
        return JSONResponse(_cuerpo(request, "Algo salió mal de nuestro lado.", "error_interno"), status_code=500)
