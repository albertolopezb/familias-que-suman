"""De las filas de la base a lo que responde la API, con lo que se CALCULA en el servidor:
lugares disponibles, lo apartado en cada meta, si una actividad ya pasó, y las horas en la
zona horaria de su ciudad.

Lo calculado nunca se acepta del teléfono: la app no puede decir "quedan 50 lugares".
"""

import uuid
from collections.abc import Iterable
from datetime import UTC, datetime
from functools import lru_cache
from zoneinfo import ZoneInfo

from sqlalchemy import func, select
from sqlalchemy.orm import Session

from . import esquemas as e
from .modelos import Actividad, Aportacion, ArticuloMeta, Campana, Centro, Ciudad, Inscripcion, Proyecto


def ahora() -> datetime:
    return datetime.now(UTC)


@lru_cache(maxsize=32)
def _zona(nombre: str) -> ZoneInfo:
    return ZoneInfo(nombre)


_zonas_por_ciudad: dict[str, str] = {}


def zona_de(db: Session, ciudad: str) -> ZoneInfo:
    if not _zonas_por_ciudad:
        _zonas_por_ciudad.update({c.nombre: c.zona_horaria for c in db.scalars(select(Ciudad))})
    return _zona(_zonas_por_ciudad.get(ciudad, "America/Monterrey"))


def ocupados_en_app(db: Session, actividad_ids: Iterable[uuid.UUID]) -> dict[uuid.UUID, int]:
    """Personas inscritas por la app en cada actividad (solo inscripciones vigentes)."""
    ids = list(actividad_ids)
    if not ids:
        return {}
    filas = db.execute(
        select(Inscripcion.actividad_id, func.coalesce(func.sum(Inscripcion.personas), 0))
        .where(Inscripcion.actividad_id.in_(ids), Inscripcion.cancelada_en.is_(None))
        .group_by(Inscripcion.actividad_id)
    )
    return {fila[0]: int(fila[1]) for fila in filas}


def lugares_libres(actividad: Actividad, ocupados_app: int) -> int:
    return max(actividad.cupo_total - actividad.ocupados_fuera - ocupados_app, 0)


def ya_paso(actividad: Actividad, momento: datetime) -> bool:
    return momento >= (actividad.termina_en or actividad.inicia_en)


def asociacion(a) -> e.AsociacionSalida:
    return e.AsociacionSalida.model_validate(a)


def actividad(db: Session, a: Actividad, ocupados_app: int, momento: datetime | None = None) -> e.ActividadSalida:
    momento = momento or ahora()
    zona = zona_de(db, a.ciudad)
    return e.ActividadSalida(
        id=a.id,
        titulo=a.titulo,
        asociacion=asociacion(a.asociacion),
        inicia_en=a.inicia_en.astimezone(zona),
        termina_en=a.termina_en.astimezone(zona) if a.termina_en else None,
        direccion=a.direccion,
        edad_minima=a.edad_minima,
        descripcion=a.descripcion,
        cupo_total=a.cupo_total,
        lugares_disponibles=lugares_libres(a, ocupados_app),
        municipio=a.municipio,
        tema=a.tema,
        aportacion=e.AportacionActividad(tipo=a.aportacion_tipo, monto=a.aportacion_monto, detalle=a.aportacion_detalle),
        punto_encuentro=a.punto_encuentro,
        acerca_del_proyecto=a.acerca_del_proyecto,
        que_haremos=a.que_haremos,
        que_incluye=a.que_incluye,
        que_llevar=a.que_llevar,
        recomendaciones=a.recomendaciones,
        imagen=a.imagen,
        ya_paso=ya_paso(a, momento),
        ciudad=a.ciudad,
        latitud=a.latitud,
        longitud=a.longitud,
    )


def actividades(db: Session, lista: list[Actividad]) -> list[e.ActividadSalida]:
    ocupados = ocupados_en_app(db, (a.id for a in lista))
    momento = ahora()
    return [actividad(db, a, ocupados.get(a.id, 0), momento) for a in lista]


def apartados_en_app(db: Session, articulo_ids: Iterable[uuid.UUID]) -> dict[uuid.UUID, int]:
    ids = list(articulo_ids)
    if not ids:
        return {}
    filas = db.execute(
        select(Aportacion.articulo_id, func.coalesce(func.sum(Aportacion.cantidad), 0))
        .where(Aportacion.articulo_id.in_(ids))
        .group_by(Aportacion.articulo_id)
    )
    return {fila[0]: int(fila[1]) for fila in filas}


def articulo(art: ArticuloMeta, apartados_app: int) -> e.ArticuloSalida:
    return e.ArticuloSalida(id=art.id, nombre=art.nombre, meta=art.meta, apartados=art.apartados_fuera + apartados_app)


def campana(c: Campana, apartados: dict[uuid.UUID, int]) -> e.CampanaSalida:
    articulos = [articulo(a, apartados.get(a.id, 0)) for a in sorted(c.articulos, key=lambda x: x.nombre)]
    return e.CampanaSalida(
        id=c.id,
        titulo=c.titulo,
        asociacion_id=c.asociacion_id,
        categoria=c.categoria,
        cierra_en=c.cierra_en,
        urgente=c.urgente,
        descripcion=c.descripcion,
        unidad_meta=c.unidad_meta,
        meta_total=c.meta_total,
        completados=c.completados_fuera + sum(a.apartados for a in articulos),
        articulos=articulos,
        imagen=c.imagen_clave,
        ciudad=c.ciudad,
        texto_boton=c.texto_boton,
        telefono=c.telefono,
        whatsapp=c.whatsapp,
        contacto_nombre=c.contacto_nombre,
        meta_texto=c.meta_texto,
        descripcion_larga=c.descripcion_larga,
        como_ayudar=c.como_ayudar,
        opciones_donacion=[e.OpcionSalida(nombre=o.nombre, precio=o.precio) for o in c.opciones],
        puntos_entrega=[e.PuntoSalida(direccion=p.direccion, colonia=p.colonia) for p in c.puntos],
        instagram=c.instagram,
        latitud=c.latitud,
        longitud=c.longitud,
    )


def campanas(db: Session, lista: list[Campana]) -> list[e.CampanaSalida]:
    apartados = apartados_en_app(db, (a.id for c in lista for a in c.articulos))
    return [campana(c, apartados) for c in lista]


def proyecto(p: Proyecto) -> e.ProyectoSalida:
    return e.ProyectoSalida(
        id=p.id,
        nombre=p.nombre,
        descripcion=p.descripcion,
        beneficiarios=p.beneficiarios,
        ciudad=p.ciudad,
        vigencia=p.vigencia,
        logo=p.logo_clave,
        activo=p.activo,
        resumen=p.resumen,
        acerca_de=p.acerca_de,
        formas_apoyo=[e.FormaApoyoSalida.model_validate(f) for f in p.formas_apoyo],
        como_ayudar=p.como_ayudar,
        telefono=p.telefono,
        whatsapp=p.whatsapp,
        instagram=p.instagram,
        latitud=p.latitud,
        longitud=p.longitud,
    )


def centro(c: Centro) -> e.CentroSalida:
    return e.CentroSalida(
        id=c.id,
        nombre=c.nombre,
        tipo=c.tipo,
        resumen=c.resumen,
        informacion=c.informacion,
        necesidades=list(c.necesidades or []),
        direccion=c.direccion,
        logo=c.logo_clave,
        verificado=c.verificado,
        como_ayudar=c.como_ayudar,
        recomendaciones=c.recomendaciones,
        telefono=c.telefono,
        whatsapp=c.whatsapp,
        instagram=c.instagram,
        ciudad=c.ciudad,
        latitud=c.latitud,
        longitud=c.longitud,
    )


def foto_url(clave: str | None) -> str | None:
    """La ruta (relativa a la API) desde donde la app descarga la foto de un testimonio."""
    if not clave:
        return None
    return f"/v1/fotos/{clave}"
