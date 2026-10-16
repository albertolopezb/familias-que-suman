"""Lo que pasa después de inscribirse: historial (RF-11), testimonios (RF-12) y encuestas (RF-13).

El historial sale de las ASISTENCIAS que registra la asociación, no de lo que diga la familia:
nadie puede sumarse actividades (ni insignias) que no hizo. Por eso registrar asistencias pide
un permiso aparte (`registrar:asistencias`).
"""

import uuid
from typing import Literal
from datetime import UTC, datetime, timedelta

from fastapi import APIRouter, Depends, File, Form, Query, Request, Response, status
from fastapi import UploadFile
from sqlalchemy import select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session

from .. import almacen, esquemas as e, imagenes, limites, metricas, politicas
from ..bitacora import auditar
from ..db import obtener_db
from ..errores import ApiError, no_encontrado
from ..identidad import MODERAR_CONTENIDO, REGISTRAR_ASISTENCIAS, Sujeto, requiere, usuario_actual
from ..limites import limitador, por_cuenta
from ..modelos import Actividad, Asistencia, Cuenta, Inscripcion, RespuestaEncuesta, Testimonio
from .catalogo import testimonio_salida
from .cuenta import leer_subida

router = APIRouter(prefix="/v1", tags=["Participación"])

# La encuesta de después se pide mientras la actividad está fresca.
VENTANA_ENCUESTA = timedelta(days=21)


def _asistencia_propia(db: Session, sujeto: Sujeto, asistencia_id: uuid.UUID) -> Asistencia:
    asistencia = db.get(Asistencia, asistencia_id)
    if asistencia is None:
        raise no_encontrado("Esa participación")
    return asistencia


@router.get("/yo/historial", response_model=list[e.ParticipacionSalida], tags=["Mi cuenta"])
def historial(sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    asistencias = db.scalars(select(Asistencia).where(Asistencia.cuenta_id == sujeto.cuenta_id)).unique().all()
    finales = set(db.scalars(
        select(RespuestaEncuesta.asistencia_id)
        .where(RespuestaEncuesta.cuenta_id == sujeto.cuenta_id, RespuestaEncuesta.momento == "despues")
    ))
    testimonios = dict(db.execute(
        select(Testimonio.asistencia_id, Testimonio.id).where(Testimonio.asistencia_id.in_([a.id for a in asistencias]))
    ).all()) if asistencias else {}
    ahora = datetime.now(UTC)
    salida = []
    for a in sorted(asistencias, key=lambda x: x.actividad.inicia_en, reverse=True):
        act = a.actividad
        fin = act.termina_en or act.inicia_en
        contestada = a.id in finales
        salida.append(e.ParticipacionSalida(
            id=a.id,
            actividad_id=act.id,
            titulo_actividad=act.titulo,
            nombre_asociacion=act.asociacion.nombre,
            inicia_en=act.inicia_en,
            termina_en=act.termina_en,
            encuesta_final_contestada=contestada,
            encuesta_pendiente=not contestada and timedelta(0) <= ahora - fin <= VENTANA_ENCUESTA,
            testimonio_id=testimonios.get(a.id),
        ))
    return salida


# ─── Testimonios ────────────────────────────────────────────────────────────

@router.get("/yo/testimonios", response_model=list[e.TestimonioSalida], tags=["Mi cuenta"])
def mis_testimonios(sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    filas = db.scalars(
        select(Testimonio).join(Asistencia, Testimonio.asistencia_id == Asistencia.id)
        .where(Asistencia.cuenta_id == sujeto.cuenta_id).order_by(Testimonio.creado_en.desc())
    ).unique()
    return [testimonio_salida(t, sujeto.nombre_familia, sujeto.cuenta_id, sujeto) for t in filas]


@router.put("/yo/participaciones/{asistencia_id}/testimonio", response_model=e.TestimonioSalida)
@limitador.limit(limites.SUBIR_FOTO, key_func=por_cuenta)
def publicar_testimonio(
    request: Request,
    asistencia_id: uuid.UUID,
    experiencia: str = Form(..., min_length=20, max_length=500),
    foto: UploadFile = File(...),
    sujeto: Sujeto = Depends(usuario_actual),
    db: Session = Depends(obtener_db),
):
    """Un testimonio por participación: si ya había uno, se reemplaza (con su foto). Siempre queda
    EN REVISIÓN: solo se publica cuando la moderación lo aprueba (RF-12, HU-11)."""
    asistencia = _asistencia_propia(db, sujeto, asistencia_id)
    politicas.exigir(politicas.puede_publicar_testimonio(sujeto, asistencia.cuenta_id, asistencia.actividad.inicia_en, datetime.now(UTC)))
    texto = e._sin_control(experiencia.strip())
    if len(texto) < 20:
        raise ApiError(422, "Cuéntanos un poco más (mínimo 20 letras).", "dato_invalido")

    limpia = imagenes.procesar(leer_subida(foto))
    clave = imagenes.clave_nueva("testimonios")
    almacen.guardar(clave, limpia)

    anterior = db.scalar(select(Testimonio).where(Testimonio.asistencia_id == asistencia_id))
    foto_vieja = anterior.foto_clave if anterior else None
    if anterior is None:
        anterior = Testimonio(asistencia_id=asistencia_id, experiencia=texto, foto_clave=clave, estado="pendiente")
        db.add(anterior)
    else:
        anterior.experiencia, anterior.foto_clave, anterior.creado_en = texto, clave, datetime.now(UTC)
        # Cambiar el texto o la foto lo regresa a revisión: lo aprobado era otra cosa.
        anterior.estado, anterior.revisado_en, anterior.revisado_por = "pendiente", None, None
    db.commit()
    almacen.borrar(foto_vieja)
    metricas.operacion("testimonio")
    db.refresh(anterior)
    return testimonio_salida(anterior, sujeto.nombre_familia, sujeto.cuenta_id, sujeto)


@router.delete("/testimonios/{testimonio_id}", status_code=status.HTTP_204_NO_CONTENT)
def eliminar_testimonio(
    request: Request, testimonio_id: uuid.UUID, sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)
):
    """Su autora o autor, o la moderación. Si lo borra la moderación, queda en la auditoría."""
    t = db.get(Testimonio, testimonio_id)
    if t is None:
        raise no_encontrado("Ese testimonio")
    autor = t.asistencia.cuenta_id
    politicas.exigir(politicas.puede_eliminar_testimonio(sujeto, autor))
    foto = t.foto_clave
    db.delete(t)
    if autor != sujeto.cuenta_id:
        auditar(db, "testimonio.moderar_eliminar", "testimonio", testimonio_id, sujeto.cuenta_id, {"autor": str(autor)}, request.state.ip_cliente)
    db.commit()
    almacen.borrar(foto)
    return Response(status_code=204)


# ─── Revisión de testimonios (moderación) ───────────────────────────────────

@router.get("/admin/testimonios", response_model=list[e.TestimonioSalida], tags=["Administración"])
def testimonios_por_revisar(
    estado: Literal["pendiente", "aprobado", "rechazado"] = Query("pendiente"),
    sujeto: Sujeto = Depends(requiere(MODERAR_CONTENIDO)),
    db: Session = Depends(obtener_db),
):
    """La bandeja de la moderación: por omisión, lo que espera revisión (lo más viejo primero)."""
    filas = db.execute(
        select(Testimonio, Cuenta.nombre_familia, Cuenta.id)
        .join(Asistencia, Testimonio.asistencia_id == Asistencia.id)
        .join(Cuenta, Asistencia.cuenta_id == Cuenta.id)
        .where(Testimonio.estado == estado)
        .order_by(Testimonio.creado_en).limit(200)
    ).unique()
    return [testimonio_salida(t, familia, cuenta_id, sujeto) for t, familia, cuenta_id in filas]


@router.patch("/admin/testimonios/{testimonio_id}", response_model=e.TestimonioSalida, tags=["Administración"])
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def revisar_testimonio(
    request: Request,
    testimonio_id: uuid.UUID,
    datos: e.RevisionTestimonioEntrada,
    sujeto: Sujeto = Depends(requiere(MODERAR_CONTENIDO)),
    db: Session = Depends(obtener_db),
):
    """Aprobar (se publica) o rechazar (no se publica; su autora lo ve como rechazado y puede
    mandar otro). Queda en la auditoría quién lo decidió."""
    t = db.get(Testimonio, testimonio_id)
    if t is None:
        raise no_encontrado("Ese testimonio")
    t.estado, t.revisado_en, t.revisado_por = datos.estado, datetime.now(UTC), sujeto.cuenta_id
    auditar(db, f"testimonio.{datos.estado}", "testimonio", t.id, sujeto.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    metricas.operacion("revision_testimonio", datos.estado)
    familia = db.scalar(select(Cuenta.nombre_familia).where(Cuenta.id == t.asistencia.cuenta_id)) or ""
    return testimonio_salida(t, familia, t.asistencia.cuenta_id, sujeto)


# ─── Encuestas ──────────────────────────────────────────────────────────────

def encuesta_salida(r: RespuestaEncuesta, familia: str, correo: str | None = None) -> e.EncuestaSalida:
    return e.EncuestaSalida(
        id=r.id,
        actividad_id=r.actividad_id,
        participacion_id=r.asistencia_id,
        actividad_titulo=r.actividad.titulo,
        familia=familia,
        correo=correo,
        momento=r.momento,
        respuestas=r.respuestas,
        fecha=r.creada_en.date(),
    )


def _guardar_encuesta(db: Session, sujeto: Sujeto, actividad_id: uuid.UUID, momento: str, asistencia_id: uuid.UUID | None,
                      datos: e.EncuestaEntrada) -> RespuestaEncuesta:
    respuestas = [r.model_dump() for r in datos.respuestas]
    db.execute(
        insert(RespuestaEncuesta)
        .values(cuenta_id=sujeto.cuenta_id, actividad_id=actividad_id, momento=momento, asistencia_id=asistencia_id, respuestas=respuestas)
        .on_conflict_do_update(
            index_elements=["cuenta_id", "actividad_id", "momento"],
            set_={"respuestas": respuestas, "asistencia_id": asistencia_id, "creada_en": datetime.now(UTC)},
        )
    )
    db.commit()
    metricas.operacion(f"encuesta_{momento}")
    return db.scalar(select(RespuestaEncuesta).where(
        RespuestaEncuesta.cuenta_id == sujeto.cuenta_id, RespuestaEncuesta.actividad_id == actividad_id, RespuestaEncuesta.momento == momento
    ))


@router.get("/yo/encuestas", response_model=list[e.EncuestaSalida], tags=["Mi cuenta"])
def mis_encuestas(sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    filas = db.scalars(
        select(RespuestaEncuesta).where(RespuestaEncuesta.cuenta_id == sujeto.cuenta_id).order_by(RespuestaEncuesta.creada_en.desc())
    ).unique()
    return [encuesta_salida(r, sujeto.nombre_familia) for r in filas]


@router.put("/yo/actividades/{actividad_id}/encuesta-previa", response_model=e.EncuestaSalida)
@limitador.limit(limites.ENCUESTA, key_func=por_cuenta)
def encuesta_previa(request: Request, actividad_id: uuid.UUID, datos: e.EncuestaEntrada,
                    sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    actividad = db.get(Actividad, actividad_id)
    if actividad is None:
        raise no_encontrado("Esa actividad")
    inscrita = db.scalar(select(Inscripcion.id).where(
        Inscripcion.cuenta_id == sujeto.cuenta_id, Inscripcion.actividad_id == actividad_id, Inscripcion.cancelada_en.is_(None)
    )) is not None
    politicas.exigir(politicas.puede_contestar_previa(sujeto, inscrita, actividad.inicia_en, datetime.now(UTC)))
    return encuesta_salida(_guardar_encuesta(db, sujeto, actividad_id, "antes", None, datos), sujeto.nombre_familia)


@router.put("/yo/participaciones/{asistencia_id}/encuesta-final", response_model=e.EncuestaSalida)
@limitador.limit(limites.ENCUESTA, key_func=por_cuenta)
def encuesta_final(request: Request, asistencia_id: uuid.UUID, datos: e.EncuestaEntrada,
                   sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    asistencia = _asistencia_propia(db, sujeto, asistencia_id)
    politicas.exigir(politicas.puede_contestar_final(sujeto, asistencia.cuenta_id))
    return encuesta_salida(_guardar_encuesta(db, sujeto, asistencia.actividad_id, "despues", asistencia_id, datos), sujeto.nombre_familia)


@router.get("/admin/encuestas", response_model=list[e.EncuestaSalida], tags=["Administración"])
def todas_las_encuestas(
    actividad_id: uuid.UUID | None = Query(None),
    sujeto: Sujeto = Depends(usuario_actual),
    db: Session = Depends(obtener_db),
):
    politicas.exigir(politicas.puede_ver_todas_las_encuestas(sujeto))
    consulta = (
        select(RespuestaEncuesta, Cuenta.nombre_familia, Cuenta.correo)
        .join(Cuenta, RespuestaEncuesta.cuenta_id == Cuenta.id)
        .order_by(RespuestaEncuesta.creada_en.desc()).limit(500)
    )
    if actividad_id:
        consulta = consulta.where(RespuestaEncuesta.actividad_id == actividad_id)
    return [encuesta_salida(r, familia, correo) for r, familia, correo in db.execute(consulta).unique()]


@router.post("/admin/asistencias", response_model=e.AsistenciaSalida, status_code=status.HTTP_201_CREATED, tags=["Administración"])
def registrar_asistencia(request: Request, datos: e.AsistenciaEntrada,
                         sujeto: Sujeto = Depends(requiere(REGISTRAR_ASISTENCIAS)), db: Session = Depends(obtener_db)):
    """Lo registra la asociación el día de la actividad. Idempotente: registrar dos veces no duplica."""
    if db.get(Cuenta, datos.cuenta_id) is None or db.get(Actividad, datos.actividad_id) is None:
        raise no_encontrado("Esa cuenta o actividad")
    db.execute(insert(Asistencia).values(cuenta_id=datos.cuenta_id, actividad_id=datos.actividad_id).on_conflict_do_nothing())
    auditar(db, "asistencia.registrar", "asistencia", f"{datos.cuenta_id}:{datos.actividad_id}", sujeto.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    return db.scalar(select(Asistencia).where(Asistencia.cuenta_id == datos.cuenta_id, Asistencia.actividad_id == datos.actividad_id))
