"""¿Está viva la API? Para Docker (healthcheck), Prometheus y quien opere el servidor.

Lo público solo dice "ok": no publica versiones, nombres de servidores ni dependencias (eso
sería darle un mapa a quien busca CVEs). El detalle se ve solo desde la red interna.
"""

from fastapi import APIRouter, Request
from sqlalchemy import text

from .. import almacen
from ..config import ajustes
from ..db import engine
from ..errores import ApiError
from ..identidad import obtener_sesiones

router = APIRouter(tags=["Salud"])


@router.get("/v1/salud")
def salud() -> dict:
    return {"estado": "ok"}


@router.get("/v1/salud/dependencias", include_in_schema=False)
def dependencias(request: Request) -> dict:
    if request.state.via_tunel:
        raise ApiError(404, "Esa ruta no existe.", "ruta_inexistente")
    try:
        with engine.connect() as c:
            base = c.execute(text("select 1")).scalar() == 1
    except Exception:  # noqa: BLE001
        base = False
    return {
        "base_de_datos": base,
        "redis": obtener_sesiones().disponible(),
        "almacen": almacen.disponible(),
        "entorno": ajustes.entorno,
    }
