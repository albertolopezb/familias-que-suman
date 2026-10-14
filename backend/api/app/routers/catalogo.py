"""Lo público: lo que cualquiera ve sin cuenta (como en familiasquesuman.com).

Solo lectura (GET). Aun así: los filtros se validan, las listas tienen tope y nada personal
sale de aquí (un testimonio dice qué familia lo escribió, nunca su correo).
"""

import re
import uuid
from typing import Annotated, Literal

from fastapi import APIRouter, Depends, Path, Query, Request
from fastapi.responses import Response
from sqlalchemy import or_, select
from sqlalchemy.orm import Session

from .. import almacen, convertir, esquemas as e, politicas
from ..db import obtener_db
from ..errores import ApiError, no_encontrado
from ..identidad import Sujeto, usuario_opcional
from ..modelos import Actividad, Asociacion, Asistencia, Campana, Centro, Ciudad, Cuenta, Proyecto, Testimonio

router = APIRouter(prefix="/v1", tags=["Catálogo (público)"])

FiltroCiudad = Annotated[str | None, Query(max_length=60, pattern=r"^[\w .,'-]+$", description="Solo lo de esa ciudad")]


@router.get("/ciudades", response_model=list[e.CiudadSalida])
def ciudades(db: Session = Depends(obtener_db)):
    return db.scalars(select(Ciudad).order_by(Ciudad.nombre)).all()


@router.get("/asociaciones", response_model=list[e.AsociacionSalida])
def asociaciones(
    ciudad: FiltroCiudad = None,
    todas: bool = Query(False, description="true = también las que solo organizan (no salen en el directorio)"),
    db: Session = Depends(obtener_db),
):
    consulta = select(Asociacion).order_by(Asociacion.nombre).limit(500)
    if not todas:
        consulta = consulta.where(Asociacion.en_directorio.is_(True))
    if ciudad:
        consulta = consulta.where(Asociacion.ciudad == ciudad)
    return db.scalars(consulta).all()


@router.get("/asociaciones/{asociacion_id}", response_model=e.AsociacionSalida)
def asociacion(asociacion_id: uuid.UUID, db: Session = Depends(obtener_db)):
    a = db.get(Asociacion, asociacion_id)
    if a is None:
        raise no_encontrado("Esa asociación")
    return a


@router.get("/actividades", response_model=list[e.ActividadSalida])
def actividades(
    ciudad: FiltroCiudad = None,
    cuando: Literal["todas", "proximas", "pasadas"] = "todas",
    db: Session = Depends(obtener_db),
):
    """Primero las próximas (de la más cercana a la más lejana) y luego las que ya pasaron."""
    consulta = select(Actividad).limit(500)
    if ciudad:
        consulta = consulta.where(Actividad.ciudad == ciudad)
    lista = list(db.scalars(consulta).unique())
    ahora = convertir.ahora()
    proximas = sorted((a for a in lista if not convertir.ya_paso(a, ahora)), key=lambda a: a.inicia_en)
    pasadas = sorted((a for a in lista if convertir.ya_paso(a, ahora)), key=lambda a: a.inicia_en, reverse=True)
    elegidas = {"todas": proximas + pasadas, "proximas": proximas, "pasadas": pasadas}[cuando]
    return convertir.actividades(db, elegidas)


@router.get("/actividades/{actividad_id}", response_model=e.ActividadSalida)
def actividad(actividad_id: uuid.UUID, db: Session = Depends(obtener_db)):
    a = db.get(Actividad, actividad_id)
    if a is None:
        raise no_encontrado("Esa actividad")
    return convertir.actividades(db, [a])[0]


@router.get("/campanas", response_model=list[e.CampanaSalida])
def campanas(ciudad: FiltroCiudad = None, db: Session = Depends(obtener_db)):
    consulta = select(Campana).order_by(Campana.urgente.desc(), Campana.titulo).limit(500)
    if ciudad:
        consulta = consulta.where(Campana.ciudad == ciudad)
    return convertir.campanas(db, list(db.scalars(consulta).unique()))


@router.get("/campanas/{campana_id}", response_model=e.CampanaSalida)
def campana(campana_id: uuid.UUID, db: Session = Depends(obtener_db)):
    c = db.get(Campana, campana_id)
    if c is None:
        raise no_encontrado("Esa campaña")
    return convertir.campanas(db, [c])[0]


@router.get("/proyectos", response_model=list[e.ProyectoSalida])
def proyectos(db: Session = Depends(obtener_db)):
    return [convertir.proyecto(p) for p in db.scalars(select(Proyecto).order_by(Proyecto.nombre).limit(500)).unique()]


@router.get("/proyectos/{proyecto_id}", response_model=e.ProyectoSalida)
def proyecto(proyecto_id: uuid.UUID, db: Session = Depends(obtener_db)):
    p = db.get(Proyecto, proyecto_id)
    if p is None:
        raise no_encontrado("Ese proyecto")
    return convertir.proyecto(p)


@router.get("/centros", response_model=list[e.CentroSalida])
def centros(ciudad: FiltroCiudad = None, db: Session = Depends(obtener_db)):
    consulta = select(Centro).order_by(Centro.nombre).limit(500)
    if ciudad:
        consulta = consulta.where(Centro.ciudad == ciudad)
    return [convertir.centro(c) for c in db.scalars(consulta)]


@router.get("/centros/{centro_id}", response_model=e.CentroSalida)
def centro(centro_id: uuid.UUID, db: Session = Depends(obtener_db)):
    c = db.get(Centro, centro_id)
    if c is None:
        raise no_encontrado("Ese centro")
    return convertir.centro(c)


def testimonio_salida(t: Testimonio, familia: str, cuenta_id: uuid.UUID, sujeto: Sujeto | None) -> e.TestimonioSalida:
    return e.TestimonioSalida(
        id=t.id,
        participacion_id=t.asistencia_id,
        actividad_id=t.asistencia.actividad_id,
        actividad_titulo=t.asistencia.actividad.titulo,
        familia=familia,
        experiencia=t.experiencia,
        foto_url=convertir.foto_url(t.foto_clave),
        fecha=t.creado_en.date(),
        es_mio=sujeto is not None and sujeto.cuenta_id == cuenta_id,
    )


@router.get("/testimonios", response_model=list[e.TestimonioSalida])
def testimonios(
    limite: int = Query(100, ge=1, le=200),
    sujeto: Sujeto | None = Depends(usuario_opcional),
    db: Session = Depends(obtener_db),
):
    filas = db.execute(
        select(Testimonio, Cuenta.nombre_familia, Cuenta.id)
        .join(Asistencia, Testimonio.asistencia_id == Asistencia.id)
        .join(Cuenta, Asistencia.cuenta_id == Cuenta.id)
        .order_by(Testimonio.creado_en.desc())
        .limit(limite)
    ).unique()
    return [testimonio_salida(t, familia, cuenta_id, sujeto) for t, familia, cuenta_id in filas]


# ─── Fotos ──────────────────────────────────────────────────────────────────

_NOMBRE = re.compile(r"[0-9a-f]{32}\.jpg")


def responder_foto(clave: str, privada: bool) -> Response:
    objeto = almacen.abrir(clave)
    if objeto is None:
        raise no_encontrado("Esa foto")
    datos = objeto["Body"].read(8_000_000)
    return Response(
        content=datos,
        media_type="image/jpeg",
        headers={
            "Cache-Control": ("private" if privada else "public") + ", max-age=86400",
            "ETag": objeto.get("ETag", ""),
            "Content-Disposition": "inline",
            # Aunque alguien lograra colar HTML en una "foto", el navegador no ejecutaría nada.
            "Content-Security-Policy": "default-src 'none'; sandbox",
            "Cross-Origin-Resource-Policy": "cross-origin",
        },
    )


@router.get("/fotos/{carpeta}/{nombre}", response_class=Response, responses={200: {"content": {"image/jpeg": {}}}})
def foto(
    carpeta: Literal["testimonios", "perfil"],
    nombre: Annotated[str, Path(max_length=40)],
    sujeto: Sujeto | None = Depends(usuario_opcional),
    db: Session = Depends(obtener_db),
):
    """Las fotos de testimonios son públicas (los testimonios se publican). La de perfil, solo
    la ve su dueña o dueño."""
    if not _NOMBRE.fullmatch(nombre):
        raise no_encontrado("Esa foto")
    clave = f"{carpeta}/{nombre}"
    if carpeta == "perfil":
        if sujeto is None:
            raise ApiError(401, "Necesitas iniciar sesión para esto.", "falta_token")
        duena = db.scalar(select(Cuenta.id).where(Cuenta.foto_clave == clave))
        politicas.exigir(politicas.puede_ver_foto_perfil(sujeto, duena))
        return responder_foto(clave, privada=True)
    publicada = db.scalar(select(Testimonio.id).where(Testimonio.foto_clave == clave))
    if publicada is None:
        raise no_encontrado("Esa foto")
    return responder_foto(clave, privada=False)
