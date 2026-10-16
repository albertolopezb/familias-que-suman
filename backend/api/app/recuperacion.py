"""Recuperar la contraseña con un código de un solo uso que llega por correo.

Lo mínimo que pide el anexo del reto ("con correo, hay un token de un solo uso"), con lo del
pizarrón:
  · Hashed: del código solo se guarda su HMAC-SHA256 (con HMAC_SECRETO). Un código de 6 dígitos
    con SHA-256 a secas se adivinaría probando el millón de combinaciones; con HMAC, sin la llave
    no se puede.
  · BF ⇒ rate limit: el código dura 15 minutos, sirve UNA vez y se quema al 5.º intento
    equivocado (5 de 1 000 000: 0.0005 %). Además, máximo 3 códigos por hora por cuenta y el rate
    limit por IP de las rutas.
  · Pedir un código nuevo anula los anteriores.
"""

import hashlib
import hmac
import secrets
from datetime import UTC, datetime, timedelta

from sqlalchemy import func, select, update
from sqlalchemy.orm import Session

from .config import ajustes
from .modelos import Cuenta, Recuperacion

MINUTOS = 15
MAX_INTENTOS = 5
MAX_POR_HORA = 3


def _huella(cuenta: Cuenta, codigo: str) -> str:
    mensaje = f"recuperacion:{cuenta.id}:{codigo}".encode()
    return hmac.new(ajustes.hmac_secreto.encode(), mensaje, hashlib.sha256).hexdigest()


def nuevo_codigo(db: Session, cuenta: Cuenta) -> str | None:
    """Un código nuevo (y anula los anteriores). None si ya pidió demasiados en la última hora."""
    ahora = datetime.now(UTC)
    recientes = db.scalar(
        select(func.count()).select_from(Recuperacion)
        .where(Recuperacion.cuenta_id == cuenta.id, Recuperacion.creada_en > ahora - timedelta(hours=1))
    ) or 0
    if recientes >= MAX_POR_HORA:
        return None
    db.execute(
        update(Recuperacion).where(Recuperacion.cuenta_id == cuenta.id, Recuperacion.usada_en.is_(None))
        .values(usada_en=ahora)
    )
    codigo = f"{secrets.randbelow(1_000_000):06d}"       # el generador criptográfico, no random
    db.add(Recuperacion(cuenta_id=cuenta.id, huella=_huella(cuenta, codigo), expira_en=ahora + timedelta(minutes=MINUTOS)))
    return codigo


def usar_codigo(db: Session, cuenta: Cuenta, codigo: str) -> bool:
    """True si el código es el vigente (y lo gasta). Si no, cuenta el intento; al 5.º se quema."""
    ahora = datetime.now(UTC)
    vigente = db.scalar(
        select(Recuperacion)
        .where(Recuperacion.cuenta_id == cuenta.id, Recuperacion.usada_en.is_(None), Recuperacion.expira_en > ahora)
        .order_by(Recuperacion.creada_en.desc()).limit(1)
        .with_for_update()       # dos intentos a la vez no pueden gastar el mismo código
    )
    if vigente is None:
        return False
    if hmac.compare_digest(vigente.huella, _huella(cuenta, codigo)):   # comparación en tiempo constante
        vigente.usada_en = ahora
        return True
    vigente.intentos += 1
    if vigente.intentos >= MAX_INTENTOS:
        vigente.usada_en = ahora
    return False
