"""Apartar artículos de una campaña con meta (RF-21). La app no cobra: solo registra el compromiso.

Dos cuidados:
  · Igual que con el cupo, la fila del artículo se bloquea (`FOR UPDATE`) mientras se cuenta lo
    que falta: dos familias no pueden apartar los mismos últimos 10 cuentos.
  · Es IDEMPOTENTE: la app manda un id que ella misma genera. Si la red falla después de
    guardar y la app reintenta, el servidor reconoce el id y no lo cuenta dos veces. Es la idea
    de los números de secuencia en la transferencia confiable de datos (clase de redes).
"""

import uuid
from datetime import date

from fastapi import APIRouter, Depends, Request
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from .. import convertir, esquemas as e, limites, metricas, politicas
from ..db import obtener_db
from ..errores import ApiError, no_encontrado
from ..identidad import Sujeto, usuario_actual
from ..limites import limitador, por_cuenta
from ..modelos import Aportacion, ArticuloMeta, Campana

router = APIRouter(prefix="/v1/campanas", tags=["Campañas"])


def _apartados(db: Session, articulo_id: uuid.UUID) -> int:
    return db.scalar(select(func.coalesce(func.sum(Aportacion.cantidad), 0)).where(Aportacion.articulo_id == articulo_id)) or 0


def _resultado(db: Session, articulo: ArticuloMeta, cantidad: int) -> e.AportacionSalida:
    campana = db.get(Campana, articulo.campana_id)
    salida = convertir.campanas(db, [campana])[0]
    art = next(a for a in salida.articulos if a.id == articulo.id)
    return e.AportacionSalida(articulo=art, cantidad=cantidad, completados_campana=salida.completados)


@router.post("/articulos/{articulo_id}/aportaciones", response_model=e.AportacionSalida)
@limitador.limit(limites.APORTAR, key_func=por_cuenta)
def apartar(
    request: Request,
    articulo_id: uuid.UUID,
    datos: e.AportacionEntrada,
    sujeto: Sujeto = Depends(usuario_actual),
    db: Session = Depends(obtener_db),
):
    repetida = db.get(Aportacion, datos.id)
    if repetida is not None:
        if repetida.cuenta_id != sujeto.cuenta_id or repetida.articulo_id != articulo_id:
            raise ApiError(409, "Ese identificador ya se usó.", "id_repetido")
        articulo = db.get(ArticuloMeta, articulo_id)
        return _resultado(db, articulo, repetida.cantidad)            # reintento: misma respuesta

    articulo = db.scalar(select(ArticuloMeta).where(ArticuloMeta.id == articulo_id).with_for_update())
    if articulo is None:
        raise no_encontrado("Ese artículo")
    campana = db.get(Campana, articulo.campana_id)
    faltan = articulo.meta - articulo.apartados_fuera - _apartados(db, articulo_id)
    decision = politicas.puede_aportar(campana.cierra_en, date.today(), datos.cantidad, faltan)
    if not decision.permitido:
        db.rollback()
        metricas.operacion("aportacion", decision.codigo)
        politicas.exigir(decision)

    db.add(Aportacion(id=datos.id, cuenta_id=sujeto.cuenta_id, articulo_id=articulo_id, cantidad=datos.cantidad))
    db.commit()
    metricas.operacion("aportacion")
    return _resultado(db, articulo, datos.cantidad)
