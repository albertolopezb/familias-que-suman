"""Inscribirse, modificar y cancelar.

La parte delicada es el CUPO. Sin cuidado, esto es una condición de carrera (TOCTOU, "time of
check to time of use", de la clase de vulnerabilidades de diseño):

    Familia A: ¿hay 4 lugares? sí            Familia B: ¿hay 4 lugares? sí
    Familia A: me inscribo con 4             Familia B: me inscribo con 4   → 8 en 4 lugares

La solución: antes de contar, se BLOQUEA la fila de la actividad (`SELECT … FOR UPDATE`).
La segunda familia espera a que la primera termine su transacción y, cuando cuenta, ya ve los
lugares que se ocuparon. La revisión y el uso pasan a ser una sola operación atómica.

El número de personas no lo dice la app: lo calcula el servidor (titular + acompañantes), y
la edad mínima y el consentimiento también se revisan aquí (lógica de negocio, OWASP A06).
"""

import uuid
from datetime import UTC, datetime

from fastapi import APIRouter, Depends, Request
from sqlalchemy import func, select
from sqlalchemy.orm import Session, noload

from .. import convertir, esquemas as e, limites, metricas, politicas
from ..db import obtener_db
from ..errores import no_encontrado
from ..identidad import Sujeto, usuario_actual
from ..limites import limitador, por_cuenta
from ..modelos import Acompanante, Actividad, Cuenta, Inscripcion, InscripcionPersona
from .cuenta import edad

router = APIRouter(prefix="/v1/yo/inscripciones", tags=["Inscripciones"])


def _salida(i: Inscripcion) -> e.InscripcionSalida:
    return e.InscripcionSalida(
        actividad_id=i.actividad_id,
        personas=i.personas,
        acompanantes=[
            e.PersonaSalida(nombre=p.nombre, fecha_nacimiento=p.fecha_nacimiento, edad=edad(p.fecha_nacimiento, p.edad), sexo=p.sexo)
            for p in i.integrantes
        ],
        creada_en=i.creada_en,
        actualizada_en=i.actualizada_en,
    )


def _vigente(db: Session, cuenta_id: uuid.UUID, actividad_id: uuid.UUID) -> Inscripcion | None:
    return db.scalar(
        select(Inscripcion).where(
            Inscripcion.cuenta_id == cuenta_id, Inscripcion.actividad_id == actividad_id, Inscripcion.cancelada_en.is_(None)
        )
    )


def _bloquear_actividad(db: Session, actividad_id: uuid.UUID) -> Actividad:
    actividad = db.scalar(
        select(Actividad).options(noload(Actividad.asociacion)).where(Actividad.id == actividad_id).with_for_update()
    )
    if actividad is None:
        raise no_encontrado("Esa actividad")
    return actividad


def _ocupados(db: Session, actividad_id: uuid.UUID) -> int:
    return db.scalar(
        select(func.coalesce(func.sum(Inscripcion.personas), 0))
        .where(Inscripcion.actividad_id == actividad_id, Inscripcion.cancelada_en.is_(None))
    ) or 0


@router.get("", response_model=list[e.InscripcionSalida])
def mis_inscripciones(sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    filas = db.scalars(
        select(Inscripcion).where(Inscripcion.cuenta_id == sujeto.cuenta_id, Inscripcion.cancelada_en.is_(None))
        .order_by(Inscripcion.creada_en)
    )
    return [_salida(i) for i in filas]


@router.put("/{actividad_id}", response_model=e.InscripcionResultado)
@limitador.limit(limites.INSCRIBIR, key_func=por_cuenta)
def inscribirse(
    request: Request,
    actividad_id: uuid.UUID,
    datos: e.InscripcionEntrada,
    sujeto: Sujeto = Depends(usuario_actual),
    db: Session = Depends(obtener_db),
):
    """PUT porque es idempotente: la misma petición dos veces (la red reintenta) deja la misma
    inscripción, no dos. Sirve para inscribirse y para modificar quién va."""
    ahora = datetime.now(UTC)
    actividad = _bloquear_actividad(db, actividad_id)          # ← aquí empieza la sección crítica
    cuenta = db.get(Cuenta, sujeto.cuenta_id)
    propia = _vigente(db, sujeto.cuenta_id, actividad_id)
    lugares_propios = propia.personas if propia else 0
    libres = actividad.cupo_total - actividad.ocupados_fuera - _ocupados(db, actividad_id)

    integrantes = [politicas.Integrante(cuenta.titular_nombre or cuenta.nombre_familia, edad(cuenta.titular_nacimiento))]
    integrantes += [politicas.Integrante(p.nombre, edad(p.fecha_nacimiento, p.edad)) for p in datos.acompanantes]
    personas = len(integrantes)

    decision = politicas.puede_guardar_inscripcion(
        actividad_inicia=actividad.inicia_en,
        ahora=ahora,
        tiene_cupo=actividad.cupo_total > 0,
        lugares_libres=libres,
        lugares_propios=lugares_propios,
        personas=personas,
        edad_minima=actividad.edad_minima,
        integrantes=integrantes,
        consentimiento=datos.consentimiento_menores,
    )
    if not decision.permitido:
        metricas.operacion("inscripcion", decision.codigo)
        db.rollback()
        politicas.exigir(decision)

    if propia is None:
        propia = Inscripcion(cuenta_id=sujeto.cuenta_id, actividad_id=actividad_id, personas=personas)
        db.add(propia)
    propia.personas = personas
    propia.actualizada_en = ahora
    hay_menores = any(i.edad is not None and i.edad < 18 for i in integrantes[1:])
    propia.consentimiento_menores_en = ahora if hay_menores else None
    propia.integrantes = [
        InscripcionPersona(nombre=p.nombre, fecha_nacimiento=p.fecha_nacimiento, edad=p.edad, sexo=p.sexo, orden=n)
        for n, p in enumerate(datos.acompanantes)
    ]

    # Quien va por primera vez queda en "Mi familia" para la próxima (como hace la app).
    conocidos = {
        n.lower() for n in db.scalars(select(Acompanante.nombre).where(Acompanante.cuenta_id == sujeto.cuenta_id))
    }
    for p in datos.acompanantes:
        if p.nombre.lower() not in conocidos:
            db.add(Acompanante(cuenta_id=sujeto.cuenta_id, nombre=p.nombre, fecha_nacimiento=p.fecha_nacimiento, edad=p.edad, sexo=p.sexo))
            conocidos.add(p.nombre.lower())

    db.commit()                                                  # ← aquí se libera el candado
    metricas.operacion("inscripcion", "modificada" if lugares_propios else "nueva")
    disponibles = max(actividad.cupo_total - actividad.ocupados_fuera - _ocupados(db, actividad_id), 0)
    db.refresh(propia)
    return e.InscripcionResultado(inscripcion=_salida(propia), lugares_disponibles=disponibles)


@router.delete("/{actividad_id}", response_model=e.CancelacionSalida)
@limitador.limit(limites.INSCRIBIR, key_func=por_cuenta)
def cancelar(request: Request, actividad_id: uuid.UUID, sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    ahora = datetime.now(UTC)
    actividad = _bloquear_actividad(db, actividad_id)
    propia = _vigente(db, sujeto.cuenta_id, actividad_id)
    if propia is None:
        db.rollback()
        raise no_encontrado("Esa inscripción")
    politicas.exigir(politicas.puede_cancelar(sujeto, propia.cuenta_id, actividad.inicia_en, ahora))
    antes = convertir.lugares_libres(actividad, _ocupados(db, actividad_id))
    propia.cancelada_en = ahora
    liberados = propia.personas
    db.commit()
    metricas.operacion("cancelacion")
    despues = convertir.lugares_libres(actividad, _ocupados(db, actividad_id))
    return e.CancelacionSalida(liberados=liberados, lugares_antes=antes, lugares_disponibles=despues)
