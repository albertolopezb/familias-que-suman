"""Avisos de Familias que Suman: urgencias de una ciudad y actividades nuevas (RF-16, RF-22).

Como en la Práctica 8 de startdroid, sin Firebase: la app los pide con su sesión
(`GET /v1/avisos?desde=N`, solo los que van después del último que ya vio) cuando se abre y cada
15 minutos con WorkManager aunque esté cerrada, y los muestra como notificación.

  · Leerlos pide sesión (JWT) y cada cuenta recibe solo los de SU ciudad y los generales.
  · Crearlos y borrarlos pide el permiso administrar:catalogo, y queda en la auditoría.
  · Crear una actividad genera solo su aviso de "actividad nueva" (routers/admin.py).
  · Un aviso no lleva datos de nadie: título, texto y, si acaso, el id de una actividad.
"""

import uuid
from datetime import UTC, datetime, timedelta

from fastapi import APIRouter, Depends, Query, Request, Response, status
from sqlalchemy import or_, select
from sqlalchemy.orm import Session

from .. import esquemas as e, limites
from ..bitacora import auditar
from ..db import obtener_db
from ..errores import ApiError, no_encontrado
from ..identidad import ADMINISTRAR_CATALOGO, Sujeto, requiere, usuario_actual
from ..limites import limitador, por_cuenta
from ..modelos import Aviso, Ciudad, Cuenta

router = APIRouter(prefix="/v1", tags=["Avisos"])

DIAS_VIGENTES = 7        # un aviso de hace más de una semana ya no se manda
MAX_POR_RESPUESTA = 50


def aviso_salida(a: Aviso) -> e.AvisoSalida:
    return e.AvisoSalida(id=a.id, tipo=a.tipo, ciudad=a.ciudad, titulo=a.titulo, cuerpo=a.cuerpo,
                         actividad_id=a.actividad_id, creado_en=a.creado_en)


@router.get("/avisos", response_model=list[e.AvisoSalida])
def mis_avisos(
    desde: int = Query(0, ge=0, le=2**62),
    sujeto: Sujeto = Depends(usuario_actual),
    db: Session = Depends(obtener_db),
):
    """Los avisos de la ciudad de la cuenta (y los generales) con id mayor que `desde`, del más viejo al más nuevo."""
    ciudad = db.scalar(select(Cuenta.ciudad).where(Cuenta.id == sujeto.cuenta_id))
    filas = db.scalars(
        select(Aviso)
        .where(Aviso.id > desde, or_(Aviso.ciudad.is_(None), Aviso.ciudad == ciudad),
               Aviso.creado_en > datetime.now(UTC) - timedelta(days=DIAS_VIGENTES))
        .order_by(Aviso.id).limit(MAX_POR_RESPUESTA)
    )
    return [aviso_salida(a) for a in filas]


@router.post("/admin/avisos", response_model=e.AvisoSalida, status_code=status.HTTP_201_CREATED, tags=["Administración"])
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def crear_aviso(request: Request, d: e.AvisoEntrada, s: Sujeto = Depends(requiere(ADMINISTRAR_CATALOGO)),
                db: Session = Depends(obtener_db)):
    """Una urgencia para las familias de una ciudad (o de todas, sin ciudad)."""
    if d.ciudad is not None and db.get(Ciudad, d.ciudad) is None:
        raise ApiError(422, "Esa ciudad no está disponible.", "ciudad_invalida")
    aviso = Aviso(tipo="urgencia", ciudad=d.ciudad, titulo=d.titulo, cuerpo=d.cuerpo, creado_por=s.cuenta_id)
    db.add(aviso)
    db.flush()
    auditar(db, "aviso.crear", "aviso", aviso.id, s.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    db.refresh(aviso)
    return aviso_salida(aviso)


@router.delete("/admin/avisos/{aviso_id}", status_code=status.HTTP_204_NO_CONTENT, tags=["Administración"])
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def borrar_aviso(request: Request, aviso_id: int, s: Sujeto = Depends(requiere(ADMINISTRAR_CATALOGO)),
                 db: Session = Depends(obtener_db)):
    aviso = db.get(Aviso, aviso_id)
    if aviso is None:
        raise no_encontrado("Ese aviso")
    db.delete(aviso)
    auditar(db, "aviso.borrar", "aviso", aviso_id, s.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    return Response(status_code=204)


def aviso_de_actividad_nueva(db: Session, actividad_id: uuid.UUID, titulo: str, ciudad: str, autor: uuid.UUID) -> None:
    """RF-22: "Se acaba de abrir TAL voluntariado". Lo agrega admin.py al crear una actividad."""
    db.add(Aviso(tipo="actividad_nueva", ciudad=ciudad, titulo=f"Nueva actividad en {ciudad}"[:80],
                 cuerpo=titulo[:300], actividad_id=actividad_id, creado_por=autor))
