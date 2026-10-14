"""Sugerencias: cualquier persona propone una actividad, campaña, proyecto o centro, y la
moderación las revisa.

Es lo único que se puede ESCRIBIR sin cuenta, así que es lo más vigilado: 5 por hora por IP
(OWASP API6, flujos de negocio sensibles: sin esto, un bot llena la bandeja en minutos).
"""

import uuid
from datetime import UTC, datetime

from fastapi import APIRouter, Depends, Query, Request, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from .. import esquemas as e, limites
from ..bitacora import auditar
from ..db import obtener_db
from ..errores import no_encontrado
from ..identidad import MODERAR_CONTENIDO, Sujeto, requiere, usuario_opcional
from ..limites import limitador
from ..modelos import Sugerencia

router = APIRouter(prefix="/v1", tags=["Sugerencias"])


def _salida(s: Sugerencia) -> e.SugerenciaSalida:
    return e.SugerenciaSalida(
        id=s.id, tipo=s.tipo.upper(), nombre=s.nombre, descripcion=s.descripcion, detalle_extra=s.detalle_extra,
        ciudad=s.ciudad, organizacion=s.organizacion, contacto_causa=s.contacto_causa, sugerida_por=s.sugerida_por,
        contacto=s.contacto, estado=s.estado.upper(), fecha=s.creada_en.date(),
    )


@router.post("/sugerencias", status_code=status.HTTP_201_CREATED, response_model=e.ConteoSalida)
@limitador.limit(limites.SUGERIR)
def sugerir(request: Request, datos: e.SugerenciaEntrada, sujeto: Sujeto | None = Depends(usuario_opcional),
            db: Session = Depends(obtener_db)):
    """Responde solo un acuse: la persona que sugiere no puede leer la bandeja."""
    db.add(Sugerencia(
        tipo=datos.tipo.lower(), nombre=datos.nombre, descripcion=datos.descripcion, detalle_extra=datos.detalle_extra,
        ciudad=datos.ciudad, organizacion=datos.organizacion, contacto_causa=datos.contacto_causa,
        sugerida_por=datos.sugerida_por, contacto=datos.contacto, cuenta_id=sujeto.cuenta_id if sujeto else None,
    ))
    db.commit()
    return e.ConteoSalida(total=1)


@router.get("/admin/sugerencias", response_model=list[e.SugerenciaSalida], tags=["Administración"])
def bandeja(
    estado: str | None = Query(None, pattern="^(PENDIENTE|APROBADA|DESCARTADA)$"),
    sujeto: Sujeto = Depends(requiere(MODERAR_CONTENIDO)),
    db: Session = Depends(obtener_db),
):
    consulta = select(Sugerencia).order_by(Sugerencia.creada_en.desc()).limit(500)
    if estado:
        consulta = consulta.where(Sugerencia.estado == estado.lower())
    return [_salida(s) for s in db.scalars(consulta)]


@router.patch("/admin/sugerencias/{sugerencia_id}", response_model=e.SugerenciaSalida, tags=["Administración"])
def revisar(request: Request, sugerencia_id: uuid.UUID, datos: e.EstadoSugerenciaEntrada,
            sujeto: Sujeto = Depends(requiere(MODERAR_CONTENIDO)), db: Session = Depends(obtener_db)):
    s = db.get(Sugerencia, sugerencia_id)
    if s is None:
        raise no_encontrado("Esa sugerencia")
    s.estado = datos.estado.lower()
    s.revisada_en = datetime.now(UTC)
    auditar(db, "sugerencia.revisar", "sugerencia", s.id, sujeto.cuenta_id, {"estado": s.estado}, request.state.ip_cliente)
    db.commit()
    return _salida(s)
