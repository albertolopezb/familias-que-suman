"""La sesión: /v1/sesion/…  Crear cuenta, entrar, salir y pedir ayuda con la contraseña.

Es el `POST /login` de la guía de clase (s23), con lo que una app de verdad necesita además:
  · las contraseñas se guardan y se comparan como hash Argon2id (nunca en claro);
  · fuerza bruta frenada por IP y por correo (ver limites.py e identidad.py);
  · el mismo mensaje para "ese correo no existe" y "esa contraseña no es": así nadie averigua
    qué correos tienen cuenta (enumeración de usuarios, OWASP A07);
  · cerrar sesión invalida el token en el servidor, no solo en el teléfono;
  · el JWT dura 15 minutos y se renueva con un refresh token de un solo uso (rotación), como pide
    el anexo del reto en startdroid.
"""

import logging
from datetime import UTC, datetime

from fastapi import APIRouter, Depends, Request, Response, status
from sqlalchemy import func, select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from .. import esquemas as e, limites, metricas
from ..bitacora import auditar
from ..db import obtener_db
from ..errores import ApiError
from ..identidad import (
    SesionesCerradas, Sujeto, abrir_sesion, contrasena_correcta, emitir_token, hash_contrasena, necesita_rehash,
    obtener_sesiones, renovar_sesion, revocar_refresh, usuario_actual,
)
from ..limites import limitador
from ..modelos import Cuenta, Preferencias

log = logging.getLogger("familias.sesion")

router = APIRouter(prefix="/v1/sesion", tags=["Sesión"])


def _tokens(db: Session, cuenta: Cuenta) -> e.TokenSalida:
    """Un JWT nuevo y un refresh token nuevo (el refresh se guarda como huella; hay que hacer commit)."""
    refresh, refresh_segundos = abrir_sesion(db, cuenta)
    token, segundos = emitir_token(cuenta)
    return e.TokenSalida(access_token=token, expira_en=segundos, refresh_token=refresh, refresh_expira_en=refresh_segundos)


def _por_correo(db: Session, correo: str) -> Cuenta | None:
    return db.scalar(select(Cuenta).where(func.lower(Cuenta.correo) == correo.lower()))


@router.post("/registro", response_model=e.TokenSalida, status_code=status.HTTP_201_CREATED)
@limitador.limit(limites.REGISTRARSE)
def registrarse(request: Request, datos: e.RegistroEntrada, db: Session = Depends(obtener_db)):
    """Crea la cuenta de la familia y entra de una vez (responde el token)."""
    if not datos.acepta_aviso:
        raise ApiError(422, "Para crear tu cuenta, acepta el aviso de privacidad.", "aviso_requerido")
    if datos.contrasena.lower() == datos.correo or datos.contrasena.lower() == datos.correo.split("@")[0]:
        raise ApiError(422, "Tu contraseña no puede ser tu correo.", "contrasena_debil")
    if _por_correo(db, datos.correo) is not None:
        raise ApiError(409, "Ese correo ya tiene cuenta. Inicia sesión con él.", "correo_ocupado")

    ahora = datetime.now(UTC)
    cuenta = Cuenta(
        correo=datos.correo,
        contrasena_hash=hash_contrasena(datos.contrasena),
        nombre_familia=datos.nombre_familia,
        rol="familia",
        permisos=[],                 # nadie se registra como admin: los permisos se dan aparte
        aviso_aceptado_en=ahora,
        tokens_validos_desde=ahora,
        ultimo_acceso_en=ahora,
    )
    db.add(cuenta)
    try:
        db.flush()
    except IntegrityError as error:   # dos registros con el mismo correo al mismo tiempo
        db.rollback()
        raise ApiError(409, "Ese correo ya tiene cuenta. Inicia sesión con él.", "correo_ocupado") from error
    db.execute(insert(Preferencias).values(cuenta_id=cuenta.id).on_conflict_do_nothing())
    auditar(db, "cuenta.crear", "cuenta", cuenta.id, cuenta.id, ip=request.state.ip_cliente)
    tokens = _tokens(db, cuenta)
    db.commit()
    metricas.operacion("registro")
    return tokens


@router.post("/iniciar", response_model=e.TokenSalida)
@limitador.limit(limites.INICIAR_SESION)
def iniciar_sesion(
    request: Request,
    datos: e.InicioSesionEntrada,
    db: Session = Depends(obtener_db),
    sesiones: SesionesCerradas = Depends(obtener_sesiones),
):
    """Correo y contraseña → JWT firmado con los atributos de la cuenta (rol y permisos)."""
    espera = sesiones.segundos_de_bloqueo(datos.correo)
    if espera:
        raise ApiError(
            429, "Hubo demasiados intentos con este correo. Espera unos minutos o recupera tu contraseña.",
            "demasiados_intentos", {"reintentar_en_s": espera},
        )

    cuenta = _por_correo(db, datos.correo)
    # Se compara SIEMPRE (con un hash señuelo si el correo no existe): tarda lo mismo en los dos casos.
    if not contrasena_correcta(cuenta.contrasena_hash if cuenta else None, datos.contrasena):
        sesiones.anotar_fallo(datos.correo)
        log.warning("Inicio de sesión fallido", extra={"evento": "login_fallido"})
        raise ApiError(401, "El correo o la contraseña no coinciden.", "credenciales_invalidas")

    sesiones.limpiar_fallos(datos.correo)
    if necesita_rehash(cuenta.contrasena_hash):
        cuenta.contrasena_hash = hash_contrasena(datos.contrasena)
    cuenta.ultimo_acceso_en = datetime.now(UTC)
    tokens = _tokens(db, cuenta)
    db.commit()
    metricas.operacion("inicio_sesion")
    return tokens


@router.post("/renovar", response_model=e.TokenSalida)
@limitador.limit(limites.RENOVAR)
def renovar(request: Request, datos: e.RenovarEntrada, db: Session = Depends(obtener_db)):
    """Refresh token vigente → un JWT nuevo y un refresh token nuevo; el anterior deja de servir.
    No pide el JWT viejo: justo se usa cuando ya venció."""
    cuenta, refresh, refresh_segundos = renovar_sesion(db, datos.refresh_token, request.state.ip_cliente)
    token, segundos = emitir_token(cuenta)
    db.commit()
    metricas.operacion("renovacion")
    return e.TokenSalida(access_token=token, expira_en=segundos, refresh_token=refresh, refresh_expira_en=refresh_segundos)


@router.post("/cerrar", status_code=status.HTTP_204_NO_CONTENT)
def cerrar_sesion(
    datos: e.CerrarSesionEntrada | None = None,
    sujeto: Sujeto = Depends(usuario_actual),
    sesiones: SesionesCerradas = Depends(obtener_sesiones),
    db: Session = Depends(obtener_db),
):
    """El JWT de esta sesión deja de servir desde ya (no solo cuando venza) y, si la app manda su
    refresh token, también ese y toda su familia: ya no se puede renovar."""
    sesiones.cerrar(sujeto.huella_token, sujeto.expira_en)
    if datos is not None and datos.refresh_token:
        revocar_refresh(db, datos.refresh_token, sujeto.cuenta_id)
        db.commit()
    return Response(status_code=204)


@router.post("/recuperar", status_code=status.HTTP_202_ACCEPTED)
@limitador.limit(limites.RECUPERAR)
def recuperar(request: Request, datos: e.RecuperarEntrada, db: Session = Depends(obtener_db)):
    """Pedir ayuda con la contraseña. Responde lo MISMO exista o no el correo (nadie averigua quién
    tiene cuenta). El proyecto no manda correos: la solicitud queda en la auditoría y la
    administración asigna una contraseña temporal con `python -m app.herramientas contrasena`."""
    cuenta = _por_correo(db, datos.correo)
    if cuenta is not None:
        auditar(db, "sesion.recuperar_solicitada", "cuenta", cuenta.id, None, ip=request.state.ip_cliente)
        db.commit()
    return Response(status_code=202)
