"""API de Familias que Suman.

    docker compose up -d                         (desde backend/)
    → http://localhost:8000/v1/salud
    → http://localhost:8000/docs                 (solo en desarrollo y solo desde tu máquina)

Cómo está armada (ver docs/guia-backend.md para el detalle de cada pieza):

    petición → Registro → EncabezadosSeguridad → HostValido → LimiteCuerpo → RateLimit global
             → CapturarErrores → ruta → [identidad: token de Auth0] → [políticas ABAC] → ORM → PostgreSQL
"""

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.openapi.docs import get_swagger_ui_html

from . import almacen, bitacora, errores, metricas
from .config import ajustes
from .errores import ApiError
from .limites import LimiteGlobal, limitador
from .routers import admin, aportaciones, catalogo, cuenta, enlaces, inscripciones, participacion, salud, sugerencias
from .seguridad import CapturarErrores, EncabezadosSeguridad, HostValido, LimiteCuerpo, Registro

log = logging.getLogger("familias")


@asynccontextmanager
async def arranque(_: FastAPI):
    bitacora.configurar()
    ajustes.revisar_para_produccion()          # en producción, con secretos de ejemplo NO arranca
    try:
        almacen.preparar()
    except Exception as e:  # noqa: BLE001 — sin almacén la API sigue; las fotos responden 503
        log.error("No se pudo preparar el almacén de fotos", exc_info=e)
    if ajustes.metricas_activas:
        metricas.publicar(ajustes.metricas_puerto)
    log.info("API lista", extra={"evento": f"entorno:{ajustes.entorno}"})
    yield


app = FastAPI(
    title="API · Familias que Suman",
    version="1.0.0",
    description="Catálogo, cuentas, inscripciones, testimonios, encuestas y campañas. Identidad con Auth0 (JWT RS256).",
    lifespan=arranque,
    # La documentación no se publica sola: ver /openapi.json y /docs abajo.
    openapi_url=None,
    docs_url=None,
    redoc_url=None,
)
app.state.limiter = limitador
errores.instalar(app)

# El orden importa: la última que se agrega es la más externa.
app.add_middleware(CapturarErrores)
app.add_middleware(LimiteGlobal)
app.add_middleware(LimiteCuerpo)
app.add_middleware(HostValido)
app.add_middleware(EncabezadosSeguridad)
app.add_middleware(Registro)
# Sin CORSMiddleware a propósito: la API la usa una app nativa, no un sitio web. Sin CORS, el
# navegador no deja que una página ajena llame a la API con los datos de quien la visita.

for modulo in (salud, catalogo, enlaces, cuenta, inscripciones, participacion, aportaciones, sugerencias, admin):
    app.include_router(modulo.router)


def _solo_interno(request: Request) -> None:
    """El inventario de la API (OWASP API9) existe y está al día, pero no se publica en
    internet: solo se ve desde la red de Docker o desde tu máquina, nunca por el túnel."""
    if request.state.via_tunel:
        raise ApiError(404, "Esa ruta no existe.", "ruta_inexistente")


@app.get("/openapi.json", include_in_schema=False)
def openapi(request: Request):
    _solo_interno(request)
    return app.openapi()


@app.get("/docs", include_in_schema=False)
def docs(request: Request):
    _solo_interno(request)
    if ajustes.es_produccion:
        raise ApiError(404, "Esa ruta no existe.", "ruta_inexistente")
    return get_swagger_ui_html(openapi_url="/openapi.json", title="API · Familias que Suman")
