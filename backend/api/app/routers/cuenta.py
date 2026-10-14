"""Lo de cada familia: /v1/yo/…

Aquí NUNCA llega un id de cuenta desde el teléfono. La cuenta sale del token verificado
(sujeto.cuenta_id) y todas las consultas se filtran con ella. No existe /v1/cuentas/{id}:
no hay forma de pedir los datos de otra familia cambiando un número (BOLA/IDOR, OWASP API1).
"""

import uuid
from datetime import UTC, date, datetime

from fastapi import APIRouter, Depends, File, Request, Response, UploadFile, status
from sqlalchemy import delete, distinct, func, select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session

from .. import almacen, convertir, esquemas as e, imagenes, limites, metricas
from ..bitacora import auditar
from ..config import ajustes
from ..db import obtener_db
from ..errores import ApiError, no_encontrado
from ..identidad import SesionesCerradas, Sujeto, obtener_sesiones, usuario_actual
from ..limites import limitador, por_cuenta
from ..modelos import (
    Acompanante, Actividad, Aportacion, ArticuloMeta, Asistencia, Asociacion, Campana, Centro, Ciudad, Cuenta,
    Favorito, Invitacion, Preferencias, Proyecto, SolicitudEliminacion,
)
from .catalogo import responder_foto

router = APIRouter(prefix="/v1", tags=["Mi cuenta"])


def edad(nacimiento: date | None, guardada: int | None = None) -> int | None:
    if nacimiento is None:
        return guardada
    hoy = date.today()
    return hoy.year - nacimiento.year - ((hoy.month, hoy.day) < (nacimiento.month, nacimiento.day))


def _validar_nacimiento(nacimiento: date | None) -> None:
    if nacimiento is not None and not date(1900, 1, 1) <= nacimiento <= date.today():
        raise ApiError(422, "La fecha de nacimiento no es válida.", "dato_invalido")


def impacto(db: Session, cuenta_id: uuid.UUID) -> e.Impacto:
    asistidas = db.scalars(
        select(Actividad).join(Asistencia, Asistencia.actividad_id == Actividad.id).where(Asistencia.cuenta_id == cuenta_id)
    ).unique().all()
    horas = sum(((a.termina_en - a.inicia_en).total_seconds() / 3600 if a.termina_en else 2) for a in asistidas)
    campanas = db.scalar(
        select(func.count(distinct(ArticuloMeta.campana_id)))
        .join(Aportacion, Aportacion.articulo_id == ArticuloMeta.id)
        .where(Aportacion.cuenta_id == cuenta_id)
    )
    return e.Impacto(actividades_realizadas=len(asistidas), horas_de_servicio=round(horas), campanas_apoyadas=campanas or 0)


def yo_salida(db: Session, cuenta: Cuenta, sujeto: Sujeto) -> e.YoSalida:
    invitaciones = db.scalar(select(func.count()).select_from(Invitacion).where(Invitacion.cuenta_id == cuenta.id))
    return e.YoSalida(
        id=cuenta.id,
        correo=cuenta.correo,
        correo_verificado=cuenta.correo_verificado,
        nombre_familia=cuenta.nombre_familia,
        ciudad=cuenta.ciudad,
        titular=e.PersonaSalida(
            nombre=cuenta.titular_nombre or cuenta.nombre_familia,
            fecha_nacimiento=cuenta.titular_nacimiento,
            edad=edad(cuenta.titular_nacimiento),
            sexo=cuenta.titular_sexo,
        ),
        es_admin=sujeto.es_admin,
        permisos=sorted(sujeto.permisos),
        tiene_foto=cuenta.foto_clave is not None,
        invitaciones=invitaciones or 0,
        aviso_aceptado=cuenta.aviso_aceptado_en is not None,
        impacto=impacto(db, cuenta.id),
    )


def _cuenta(db: Session, sujeto: Sujeto) -> Cuenta:
    cuenta = db.get(Cuenta, sujeto.cuenta_id)
    if cuenta is None:   # se borró mientras el token seguía vigente
        raise ApiError(401, "Tu cuenta ya no existe.", "token_invalido")
    return cuenta


@router.get("/yo", response_model=e.YoSalida)
def yo(sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    return yo_salida(db, _cuenta(db, sujeto), sujeto)


@router.patch("/yo", response_model=e.YoSalida)
@limitador.limit(limites.ESCRIBIR_PERFIL, key_func=por_cuenta)
def editar_yo(request: Request, cambios: e.YoEntrada, sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    cuenta = _cuenta(db, sujeto)
    _validar_nacimiento(cambios.titular_nacimiento)
    if cambios.ciudad is not None:
        if db.get(Ciudad, cambios.ciudad) is None:
            raise ApiError(422, "Esa ciudad no está disponible.", "ciudad_invalida")
        cuenta.ciudad = cambios.ciudad
    if cambios.nombre_familia is not None:
        cuenta.nombre_familia = cambios.nombre_familia
    if cambios.titular_nombre is not None:
        cuenta.titular_nombre = cambios.titular_nombre
    if "titular_nacimiento" in cambios.model_fields_set:
        cuenta.titular_nacimiento = cambios.titular_nacimiento
    if "titular_sexo" in cambios.model_fields_set:
        cuenta.titular_sexo = cambios.titular_sexo
    if cambios.aceptar_aviso and cuenta.aviso_aceptado_en is None:
        cuenta.aviso_aceptado_en = datetime.now(UTC)
        auditar(db, "aviso_privacidad.aceptar", "cuenta", cuenta.id, cuenta.id, ip=request.state.ip_cliente)
    db.commit()
    return yo_salida(db, cuenta, sujeto)


# ─── Acompañantes ───────────────────────────────────────────────────────────

def _acompanante_salida(a: Acompanante) -> e.AcompananteSalida:
    return e.AcompananteSalida(id=a.id, nombre=a.nombre, fecha_nacimiento=a.fecha_nacimiento, edad=edad(a.fecha_nacimiento, a.edad), sexo=a.sexo)


@router.get("/yo/acompanantes", response_model=list[e.AcompananteSalida])
def acompanantes(sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    filas = db.scalars(select(Acompanante).where(Acompanante.cuenta_id == sujeto.cuenta_id).order_by(Acompanante.creado_en))
    return [_acompanante_salida(a) for a in filas]


@router.post("/yo/acompanantes", response_model=e.AcompananteSalida, status_code=status.HTTP_201_CREATED)
@limitador.limit(limites.ESCRIBIR_PERFIL, key_func=por_cuenta)
def agregar_acompanante(request: Request, persona: e.PersonaEntrada, sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    """Mismo nombre = misma persona: se actualiza en lugar de duplicarla (como hace la app)."""
    _validar_nacimiento(persona.fecha_nacimiento)
    existente = db.scalar(
        select(Acompanante).where(Acompanante.cuenta_id == sujeto.cuenta_id, func.lower(Acompanante.nombre) == persona.nombre.lower())
    )
    if existente is None:
        if db.scalar(select(func.count()).select_from(Acompanante).where(Acompanante.cuenta_id == sujeto.cuenta_id)) >= 30:
            raise ApiError(409, "Ya registraste el máximo de personas en tu familia.", "demasiados_acompanantes")
        existente = Acompanante(cuenta_id=sujeto.cuenta_id, nombre=persona.nombre)
        db.add(existente)
    existente.fecha_nacimiento = persona.fecha_nacimiento
    existente.edad = persona.edad
    existente.sexo = persona.sexo
    db.commit()
    return _acompanante_salida(existente)


@router.delete("/yo/acompanantes/{acompanante_id}", status_code=status.HTTP_204_NO_CONTENT)
@limitador.limit(limites.ESCRIBIR_PERFIL, key_func=por_cuenta)
def quitar_acompanante(request: Request, acompanante_id: uuid.UUID, sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    # El filtro por cuenta_id va EN la consulta: lo de otra cuenta simplemente "no existe".
    resultado = db.execute(delete(Acompanante).where(Acompanante.id == acompanante_id, Acompanante.cuenta_id == sujeto.cuenta_id))
    if resultado.rowcount == 0:
        raise no_encontrado("Esa persona")
    db.commit()
    return Response(status_code=204)


# ─── Favoritos ──────────────────────────────────────────────────────────────

_TIPOS_FAVORITO = (
    ("asociacion", Asociacion), ("actividad", Actividad), ("campana", Campana), ("proyecto", Proyecto), ("centro", Centro),
)


@router.get("/yo/favoritos", response_model=list[e.FavoritoSalida])
def favoritos(sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    filas = db.scalars(select(Favorito).where(Favorito.cuenta_id == sujeto.cuenta_id).order_by(Favorito.creado_en))
    return [e.FavoritoSalida(objeto_id=f.objeto_id, tipo=f.tipo) for f in filas]


@router.put("/yo/favoritos/{objeto_id}", response_model=e.FavoritoSalida)
@limitador.limit(limites.ESCRIBIR_PERFIL, key_func=por_cuenta)
def marcar_favorito(request: Request, objeto_id: uuid.UUID, sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    """PUT es idempotente: marcar dos veces deja lo mismo. Solo se aceptan cosas que existen."""
    tipo = next((nombre for nombre, modelo in _TIPOS_FAVORITO if db.get(modelo, objeto_id) is not None), None)
    if tipo is None:
        raise no_encontrado("Eso")
    db.execute(insert(Favorito).values(cuenta_id=sujeto.cuenta_id, tipo=tipo, objeto_id=objeto_id).on_conflict_do_nothing())
    db.commit()
    return e.FavoritoSalida(objeto_id=objeto_id, tipo=tipo)


@router.delete("/yo/favoritos/{objeto_id}", status_code=status.HTTP_204_NO_CONTENT)
@limitador.limit(limites.ESCRIBIR_PERFIL, key_func=por_cuenta)
def quitar_favorito(request: Request, objeto_id: uuid.UUID, sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    db.execute(delete(Favorito).where(Favorito.cuenta_id == sujeto.cuenta_id, Favorito.objeto_id == objeto_id))
    db.commit()
    return Response(status_code=204)


# ─── Preferencias ───────────────────────────────────────────────────────────

@router.get("/yo/preferencias", response_model=e.PreferenciasSalida)
def preferencias(sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    p = db.get(Preferencias, sujeto.cuenta_id)
    return p or e.PreferenciasSalida(recordatorio_actividades=True, avisos_favoritos=True, urgencias_ciudad=False)


@router.put("/yo/preferencias", response_model=e.PreferenciasSalida)
@limitador.limit(limites.ESCRIBIR_PERFIL, key_func=por_cuenta)
def guardar_preferencias(request: Request, nuevas: e.PreferenciasEntrada, sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    valores = nuevas.model_dump()
    db.execute(
        insert(Preferencias).values(cuenta_id=sujeto.cuenta_id, **valores)
        .on_conflict_do_update(index_elements=["cuenta_id"], set_=valores)
    )
    db.commit()
    return nuevas


# ─── Foto de perfil ─────────────────────────────────────────────────────────

def leer_subida(archivo: UploadFile) -> bytes:
    datos = archivo.file.read(ajustes.max_foto_bytes + 1)
    if len(datos) > ajustes.max_foto_bytes:
        metricas.evento_seguridad("archivo_rechazado")
        raise ApiError(413, "La foto pesa demasiado (máximo 6 MB).", "cuerpo_muy_grande")
    return datos


@router.get("/yo/foto", response_class=Response, responses={200: {"content": {"image/jpeg": {}}}})
def mi_foto(sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    cuenta = _cuenta(db, sujeto)
    if not cuenta.foto_clave:
        raise no_encontrado("Tu foto")
    return responder_foto(cuenta.foto_clave, privada=True)


@router.put("/yo/foto", response_model=e.YoSalida)
@limitador.limit(limites.SUBIR_FOTO, key_func=por_cuenta)
def cambiar_foto(request: Request, foto: UploadFile = File(...), sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    limpia = imagenes.procesar(leer_subida(foto))
    cuenta = _cuenta(db, sujeto)
    anterior, clave = cuenta.foto_clave, imagenes.clave_nueva("perfil")
    almacen.guardar(clave, limpia)
    cuenta.foto_clave = clave
    db.commit()
    almacen.borrar(anterior)
    return yo_salida(db, cuenta, sujeto)


@router.delete("/yo/foto", status_code=status.HTTP_204_NO_CONTENT)
def quitar_foto(sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    cuenta = _cuenta(db, sujeto)
    anterior = cuenta.foto_clave
    cuenta.foto_clave = None
    db.commit()
    almacen.borrar(anterior)
    return Response(status_code=204)


# ─── Invitaciones ───────────────────────────────────────────────────────────

@router.post("/yo/invitaciones", response_model=e.ConteoSalida, status_code=status.HTTP_201_CREATED)
@limitador.limit(limites.INVITAR, key_func=por_cuenta)
def registrar_invitacion(request: Request, datos: e.InvitacionEntrada, sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    if db.get(Actividad, datos.actividad_id) is None:
        raise no_encontrado("Esa actividad")
    db.add(Invitacion(cuenta_id=sujeto.cuenta_id, actividad_id=datos.actividad_id))
    db.commit()
    total = db.scalar(select(func.count()).select_from(Invitacion).where(Invitacion.cuenta_id == sujeto.cuenta_id))
    return e.ConteoSalida(total=total or 0)


# ─── Privacidad: solicitud para eliminar mis datos (HU-14) ──────────────────

def solicitud_salida(s: SolicitudEliminacion, con_correo: bool = False) -> e.SolicitudEliminacionSalida:
    return e.SolicitudEliminacionSalida(
        id=s.id, folio=f"ELIM-{s.folio:04d}", estado=s.estado, correo=s.correo if con_correo else None, creada_en=s.creada_en
    )


@router.get("/yo/eliminacion", response_model=e.SolicitudEliminacionSalida | None)
def mi_solicitud(sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    s = db.scalar(
        select(SolicitudEliminacion)
        .where(SolicitudEliminacion.cuenta_id == sujeto.cuenta_id, SolicitudEliminacion.estado == "pendiente")
    )
    return solicitud_salida(s) if s else None


@router.post("/yo/eliminacion", response_model=e.SolicitudEliminacionSalida, status_code=status.HTTP_201_CREATED)
@limitador.limit("3/day", key_func=por_cuenta)
def solicitar_eliminacion(request: Request, sujeto: Sujeto = Depends(usuario_actual), db: Session = Depends(obtener_db)):
    """Idempotente: si ya hay una pendiente, regresa la misma con su folio."""
    actual = db.scalar(
        select(SolicitudEliminacion)
        .where(SolicitudEliminacion.cuenta_id == sujeto.cuenta_id, SolicitudEliminacion.estado == "pendiente")
    )
    if actual is None:
        actual = SolicitudEliminacion(cuenta_id=sujeto.cuenta_id, correo=sujeto.correo)
        db.add(actual)
        db.flush()
        auditar(db, "privacidad.solicitar_eliminacion", "cuenta", sujeto.cuenta_id, sujeto.cuenta_id, ip=request.state.ip_cliente)
        db.commit()
        db.refresh(actual)
    return solicitud_salida(actual)


# ─── Cerrar sesión ──────────────────────────────────────────────────────────

@router.post("/sesion/cerrar", status_code=status.HTTP_204_NO_CONTENT, tags=["Sesión"])
def cerrar_sesion(sujeto: Sujeto = Depends(usuario_actual), sesiones: SesionesCerradas = Depends(obtener_sesiones)):
    """El token de esta sesión deja de servir desde ya (no solo cuando venza)."""
    sesiones.cerrar(sujeto.huella_token, sujeto.expira_en)
    return Response(status_code=204)
