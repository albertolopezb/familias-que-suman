"""Identificación: ¿quién eres? Con JWT emitidos y verificados por esta misma API.

Es el esquema de la guía de clase (s23 · "Stateless Identity: JWT Authentication in FastAPI"),
con el access token corto + refresh token que pide el anexo del reto en startdroid:

  1. La app manda correo y contraseña UNA vez a `POST /v1/sesion/iniciar` (por HTTPS).
  2. La API busca la cuenta, compara la contraseña contra su hash Argon2id y, si coincide,
     firma un JWT con HMAC-SHA256 (HS256) usando un secreto que solo ella conoce.
  3. La app guarda ese token y lo manda en cada petición: `Authorization: Bearer <token>`.
     El JWT dura 15 minutos. Junto con él llega un REFRESH TOKEN (30 días) que solo sirve para
     pedir otro JWT en `POST /v1/sesion/renovar`; cada uso lo cambia por uno nuevo (rotación).
  4. La API vuelve a calcular la firma: si alguien cambió UN solo carácter del contenido (por
     ejemplo `"permisos": []` → `["administrar:catalogo"]`), la firma ya no coincide y es 401.
     Así la API no tiene que guardar sesiones: el token se defiende solo ("stateless").

Lo que lleva el token (claims):
  · iss / aud      quién lo emitió (esta API) y para quién es (la app). Un JWT de otro sistema
                   firmado con otro secreto no sirve, y uno nuestro no sirve en otro sistema.
  · sub            el id de la cuenta (UUID). Nada de correo ni nombre: el contenido de un JWT
                   se puede LEER (solo está en Base64), no se debe poner nada privado.
  · rol, permisos  los ATRIBUTOS del sujeto que usan las políticas ABAC (politicas.polar).
  · iat / exp      cuándo se emitió y cuándo vence.
  · jti            un id único: dos tokens nunca son iguales.

Qué se revisa de cada token, y por qué:
  · alg == HS256 y nada más        → cierra el ataque "alg: none" y la confusión de algoritmos.
  · la firma con NUESTRO secreto   → nadie alteró el contenido (integridad y autenticidad).
  · iss, aud, exp, iat, sub, jti   → obligatorios; vencido = 401 (tolerancia de 30 s por relojes).
  · que no esté en la lista de sesiones cerradas (Redis) y que sea más nuevo que el último
    cambio de contraseña o de permisos de la cuenta (`tokens_validos_desde`).

Si Redis no responde, la API NO deja pasar a nadie: responde 503 (falla cerrado, OWASP A10).
"""

import hashlib
import logging
import secrets
import time
import uuid
from dataclasses import dataclass
from datetime import UTC, datetime, timedelta

import jwt
import redis
from argon2 import PasswordHasher
from argon2.exceptions import InvalidHashError, VerificationError
from fastapi import Depends, Request
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import select, update
from sqlalchemy.orm import Session

from .bitacora import auditar
from .config import ajustes
from .db import obtener_db
from .errores import ApiError
from .modelos import Cuenta, Sesion

log = logging.getLogger("familias.identidad")

# Los permisos de administración. Cada función pide el suyo: quien solo registra asistencias no
# puede borrar campañas (mínimo privilegio, OWASP API5). Se guardan en la cuenta y viajan en el JWT.
ADMINISTRAR_CATALOGO = "administrar:catalogo"
MODERAR_CONTENIDO = "moderar:contenido"
REGISTRAR_ASISTENCIAS = "registrar:asistencias"
ATENDER_PRIVACIDAD = "atender:privacidad"
PERMISOS_ADMIN = frozenset({ADMINISTRAR_CATALOGO, MODERAR_CONTENIDO, REGISTRAR_ASISTENCIAS, ATENDER_PRIVACIDAD})

ALGORITMO = "HS256"
MAX_LARGO_TOKEN = 4096


@dataclass(frozen=True)
class Sujeto:
    """Quién hace la petición, con los ATRIBUTOS que usan las políticas ABAC (politicas.polar).
    `rol` y `permisos` salen del token (firmado); el correo y el nombre, de la base."""

    cuenta_id: uuid.UUID
    correo: str
    nombre_familia: str
    rol: str
    permisos: frozenset[str]
    huella_token: str          # SHA-256 del token: para cerrarlo sin guardar el token
    expira_en: int

    @property
    def id(self) -> str:
        """El id como texto: así lo compara Polar con el dueño de cada recurso."""
        return str(self.cuenta_id)

    def puede(self, permiso: str) -> bool:
        return permiso in self.permisos

    @property
    def es_admin(self) -> bool:
        return bool(self.permisos & PERMISOS_ADMIN)


def huella(token: str) -> str:
    return hashlib.sha256(token.encode("ascii", errors="ignore")).hexdigest()


def rol_de(permisos: frozenset[str] | set[str] | list[str]) -> str:
    return "admin" if set(permisos) & PERMISOS_ADMIN else "familia"


class IdentidadNoDisponible(ApiError):
    def __init__(self) -> None:
        super().__init__(503, "No pudimos verificar tu sesión en este momento. Intenta en un minuto.", "identidad_no_disponible")


def _token_invalido(codigo: str = "token_invalido") -> ApiError:
    mensajes = {
        "token_invalido": "Tu sesión no es válida. Vuelve a iniciar sesión.",
        "token_expirado": "Tu sesión expiró. Vuelve a iniciar sesión.",
        "sesion_cerrada": "Esta sesión ya se cerró. Vuelve a iniciar sesión.",
        "falta_token": "Necesitas iniciar sesión para esto.",
    }
    return ApiError(401, mensajes[codigo], codigo)


# ─── Contraseñas: Argon2id ───────────────────────────────────────────────────
# Argon2id es la primera opción de OWASP (Password Storage Cheat Sheet). Cada hash lleva su propia
# sal al azar, y calcularlo cuesta memoria a propósito: probar millones de contraseñas contra una
# tabla robada sale carísimo, incluso con tarjetas gráficas. Parámetros: la configuración mínima
# que recomienda OWASP (19 MiB, 2 pasadas, 1 hilo), para que la API (512 MB) aguante varios
# inicios de sesión a la vez.

_hasher = PasswordHasher(time_cost=2, memory_cost=19456, parallelism=1)
# Un hash de mentira para comparar cuando el correo NO existe: así tarda lo mismo que cuando sí
# existe y nadie puede averiguar qué correos tienen cuenta midiendo el tiempo de respuesta.
_HASH_SENUELO = _hasher.hash("contraseña-que-nadie-tiene")


def hash_contrasena(contrasena: str) -> str:
    return _hasher.hash(contrasena)


def contrasena_correcta(guardado: str | None, contrasena: str) -> bool:
    try:
        return _hasher.verify(guardado or _HASH_SENUELO, contrasena) and guardado is not None
    except (VerificationError, InvalidHashError):
        return False


def necesita_rehash(guardado: str) -> bool:
    """Si algún día se suben los parámetros, el hash viejo se rehace al entrar (con la contraseña en mano)."""
    return _hasher.check_needs_rehash(guardado)


# ─── Emitir y verificar el JWT ───────────────────────────────────────────────

def emitir_token(cuenta: Cuenta) -> tuple[str, int]:
    """Firma el token de una cuenta. Devuelve el token y cuántos segundos dura."""
    # `iat` con fracciones de segundo (el estándar JWT lo permite): así un token emitido justo
    # antes de un cambio de contraseña no "empata" con él y se invalida sin ventana de 1 s.
    ahora = time.time()
    segundos = ajustes.jwt_minutos * 60
    permisos = sorted(set(cuenta.permisos or ()) & PERMISOS_ADMIN)
    claims = {
        "iss": ajustes.jwt_emisor,
        "aud": ajustes.jwt_audiencia,
        "sub": str(cuenta.id),
        "rol": rol_de(permisos),
        "permisos": permisos,
        "iat": ahora,
        "exp": int(ahora) + segundos,
        "jti": uuid.uuid4().hex,
    }
    return jwt.encode(claims, ajustes.jwt_secreto, algorithm=ALGORITMO), segundos


def verificar_token(token: str) -> dict:
    """Las claims de un token válido, o 401. Ninguna parte del token se cree antes de verificar la firma."""
    if not token or len(token) > MAX_LARGO_TOKEN or token.count(".") != 2:
        raise _token_invalido()
    try:
        encabezado = jwt.get_unverified_header(token)
    except jwt.DecodeError as e:
        raise _token_invalido() from e
    # `algorithms=[HS256]` abajo ya lo exige; revisarlo antes deja claro que "none" ni se intenta.
    if encabezado.get("alg") != ALGORITMO:
        raise _token_invalido()
    try:
        claims = jwt.decode(
            token,
            ajustes.jwt_secreto,
            algorithms=[ALGORITMO],
            audience=ajustes.jwt_audiencia,
            issuer=ajustes.jwt_emisor,
            leeway=30,
            options={"require": ["exp", "iat", "iss", "aud", "sub", "jti"]},
        )
    except jwt.ExpiredSignatureError as e:
        raise _token_invalido("token_expirado") from e
    except jwt.InvalidTokenError as e:
        raise _token_invalido() from e
    # Aunque la firma sea buena, la forma de las claims se revisa igual (defensa en profundidad).
    try:
        claims["cuenta_id"] = uuid.UUID(claims["sub"])
    except (ValueError, TypeError, AttributeError) as e:
        raise _token_invalido() from e
    permisos = claims.get("permisos", [])
    if not isinstance(permisos, list) or not all(isinstance(p, str) and p in PERMISOS_ADMIN for p in permisos):
        raise _token_invalido()
    if claims.get("rol") not in ("familia", "admin") or isinstance(claims["iat"], bool) or not isinstance(claims["iat"], int | float):
        raise _token_invalido()
    return claims


# ─── Refresh tokens: sesiones que se renuevan sin volver a pedir la contraseña ──
# El refresh token es un número al azar de 384 bits (no un JWT): no lleva nada adentro y solo
# sirve contra la tabla `sesiones`, donde se guarda su HUELLA SHA-256, nunca el token. Así, aunque
# alguien se llevara la tabla, no tendría tokens que usar.
#
# Rotación y detección de reuso (OWASP, "refresh token rotation"): cada refresh token sirve UNA
# vez. Al usarlo se marca como usado y nace otro de la misma "familia". Si llega uno ya usado,
# alguien más lo tenía (la app legítima ya lo había cambiado): se cierra toda la familia y las
# dos partes tienen que volver a entrar con contraseña.

def _ahora() -> datetime:
    return datetime.now(UTC)


def abrir_sesion(db: Session, cuenta: Cuenta, familia: uuid.UUID | None = None) -> tuple[str, int]:
    """Crea un refresh token para la cuenta. Devuelve el token (solo esta vez se ve) y su duración."""
    refresh = secrets.token_urlsafe(48)
    segundos = ajustes.refresh_dias * 24 * 3600
    db.add(Sesion(
        cuenta_id=cuenta.id, familia=familia or uuid.uuid4(), huella=huella(refresh),
        expira_en=_ahora() + timedelta(seconds=segundos),
    ))
    return refresh, segundos


def revocar_familia(db: Session, familia: uuid.UUID) -> None:
    db.execute(update(Sesion).where(Sesion.familia == familia, Sesion.revocada_en.is_(None)).values(revocada_en=_ahora()))


def revocar_todas(db: Session, cuenta_id: uuid.UUID) -> None:
    """Cierra todas las sesiones de una cuenta (cambio de contraseña o de permisos)."""
    db.execute(update(Sesion).where(Sesion.cuenta_id == cuenta_id, Sesion.revocada_en.is_(None)).values(revocada_en=_ahora()))


def revocar_refresh(db: Session, refresh: str, cuenta_id: uuid.UUID) -> None:
    """Cerrar sesión: la familia de ESE refresh token, solo si es de quien cierra."""
    fila = db.scalar(select(Sesion).where(Sesion.huella == huella(refresh), Sesion.cuenta_id == cuenta_id))
    if fila is not None:
        revocar_familia(db, fila.familia)


def renovar_sesion(db: Session, refresh: str, ip: str | None = None) -> tuple[Cuenta, str, int]:
    """Cambia un refresh token vigente por otro nuevo. 401 si no existe, venció, se cerró o ya se usó."""
    # FOR UPDATE: dos renovaciones a la vez con el mismo token no pueden ganar las dos.
    fila = db.scalar(select(Sesion).where(Sesion.huella == huella(refresh)).with_for_update())
    if fila is None:
        raise _token_invalido()
    if fila.revocada_en is not None:
        raise _token_invalido("sesion_cerrada")
    if fila.usada_en is not None:
        revocar_familia(db, fila.familia)
        auditar(db, "sesion.reuso_de_refresh", "cuenta", fila.cuenta_id, fila.cuenta_id, ip=ip)
        db.commit()
        log.warning("Refresh token reusado: se cerró la sesión completa", extra={"evento": "refresh_reusado"})
        raise _token_invalido("sesion_cerrada")
    if fila.expira_en <= _ahora():
        raise _token_invalido("token_expirado")
    cuenta = db.get(Cuenta, fila.cuenta_id)
    fila.usada_en = _ahora()
    nuevo, segundos = abrir_sesion(db, cuenta, fila.familia)
    return cuenta, nuevo, segundos


# ─── Redis: sesiones cerradas e intentos fallidos ────────────────────────────

class SesionesCerradas:
    """El JWT es "sin estado": la API no guarda sesiones y cualquier réplica lo puede verificar
    (clase: stateless vs stateful). El costo es que un token vale hasta que vence. Para que
    "Cerrar sesión" sea de verdad, al cerrar se guarda la HUELLA del token (no el token) en
    Redis hasta su vencimiento, y la API la revisa en cada petición."""

    def __init__(self, url: str) -> None:
        self._redis = redis.Redis.from_url(url, socket_timeout=2, socket_connect_timeout=2)

    def cerrar(self, huella_token: str, expira_en: int) -> None:
        segundos = max(1, expira_en - int(time.time()) + 60)
        self._redis.set(f"sesion_cerrada:{huella_token}", "1", ex=segundos)

    def esta_cerrada(self, huella_token: str) -> bool:
        try:
            return bool(self._redis.exists(f"sesion_cerrada:{huella_token}"))
        except redis.RedisError as e:
            # Sin Redis no se puede saber si la sesión se cerró: se niega (falla cerrado).
            log.error("Redis no responde al revisar sesiones cerradas", exc_info=e)
            raise IdentidadNoDisponible() from e

    def disponible(self) -> bool:
        try:
            return bool(self._redis.ping())
        except redis.RedisError:
            return False

    # ── Fuerza bruta (OWASP A07 · API2) ──
    # El límite por IP (limites.INICIAR_SESION) frena a un bot que prueba muchas contraseñas desde
    # una sola máquina. Esto frena al que usa MUCHAS IPs contra UNA cuenta: tras N fallos seguidos,
    # ese correo queda bloqueado un rato, venga de donde venga. La llave es el SHA-256 del correo:
    # en Redis no queda ningún correo.

    @staticmethod
    def _llave_fallos(correo: str) -> str:
        return "fallos_login:" + hashlib.sha256(correo.strip().lower().encode()).hexdigest()

    def segundos_de_bloqueo(self, correo: str) -> int:
        try:
            llave = self._llave_fallos(correo)
            fallos = int(self._redis.get(llave) or 0)
            if fallos < ajustes.login_max_fallos:
                return 0
            return max(1, int(self._redis.ttl(llave)))
        except redis.RedisError as e:
            log.error("Redis no responde al revisar intentos de inicio de sesión", exc_info=e)
            raise IdentidadNoDisponible() from e

    def anotar_fallo(self, correo: str) -> None:
        try:
            llave = self._llave_fallos(correo)
            with self._redis.pipeline() as p:
                p.incr(llave)
                p.expire(llave, ajustes.login_bloqueo_minutos * 60)
                p.execute()
        except redis.RedisError as e:
            log.error("Redis no responde al anotar un intento fallido", exc_info=e)
            raise IdentidadNoDisponible() from e

    def limpiar_fallos(self, correo: str) -> None:
        try:
            self._redis.delete(self._llave_fallos(correo))
        except redis.RedisError:
            pass


# ─── Dependencias para las rutas ─────────────────────────────────────────────

_sesiones: SesionesCerradas | None = None


def obtener_sesiones() -> SesionesCerradas:
    global _sesiones
    if _sesiones is None:
        _sesiones = SesionesCerradas(ajustes.redis_url)
    return _sesiones


_bearer = HTTPBearer(auto_error=False, description="El JWT que da POST /v1/sesion/iniciar")


def _sujeto(request: Request, credenciales: HTTPAuthorizationCredentials | None, db: Session,
            sesiones: SesionesCerradas) -> Sujeto:
    if credenciales is None or credenciales.scheme.lower() != "bearer":
        raise _token_invalido("falta_token")
    token = credenciales.credentials
    claims = verificar_token(token)
    huella_token = huella(token)
    if sesiones.esta_cerrada(huella_token):
        raise _token_invalido("sesion_cerrada")

    cuenta = db.get(Cuenta, claims["cuenta_id"])
    if cuenta is None:   # se borró mientras el token seguía vigente
        raise _token_invalido()
    # Se cambió la contraseña o los permisos después de emitir este token: ya no sirve.
    if claims["iat"] < cuenta.tokens_validos_desde.timestamp():
        raise _token_invalido("sesion_cerrada")

    # La última vez que entró, sin escribir en cada petición: como mucho una vez cada 10 minutos.
    ahora = datetime.now(UTC)
    if cuenta.ultimo_acceso_en is None or ahora - cuenta.ultimo_acceso_en > timedelta(minutes=10):
        cuenta.ultimo_acceso_en = ahora
        db.commit()

    request.state.cuenta_id = cuenta.id
    return Sujeto(
        cuenta_id=cuenta.id,
        correo=cuenta.correo,
        nombre_familia=cuenta.nombre_familia,
        rol=claims["rol"],
        permisos=frozenset(claims.get("permisos", [])),
        huella_token=huella_token,
        expira_en=int(claims["exp"]),
    )


def usuario_actual(
    request: Request,
    credenciales: HTTPAuthorizationCredentials | None = Depends(_bearer),
    db: Session = Depends(obtener_db),
    sesiones: SesionesCerradas = Depends(obtener_sesiones),
) -> Sujeto:
    """Exige sesión (el `get_current_user` de la guía). Sin token o con uno inválido: 401."""
    return _sujeto(request, credenciales, db, sesiones)


def usuario_opcional(
    request: Request,
    credenciales: HTTPAuthorizationCredentials | None = Depends(_bearer),
    db: Session = Depends(obtener_db),
    sesiones: SesionesCerradas = Depends(obtener_sesiones),
) -> Sujeto | None:
    """Para rutas públicas que cambian un poco con sesión (por ejemplo, "este testimonio es
    tuyo"). Sin token: None. Con token: se verifica igual de estricto (un token malo es 401,
    no "sin sesión")."""
    if credenciales is None:
        return None
    return _sujeto(request, credenciales, db, sesiones)


def requiere(permiso: str):
    """Autorización a nivel de FUNCIÓN (OWASP API5): la app puede esconder el botón de
    administrar, pero el servidor no confía en eso y le pregunta a la política (Oso) si el
    sujeto del token tiene el permiso."""

    def _dependencia(sujeto: Sujeto = Depends(usuario_actual)) -> Sujeto:
        from . import politicas  # aquí y no arriba: politicas importa este módulo

        politicas.exigir(politicas.puede_usar_funcion(sujeto, permiso))
        return sujeto

    return _dependencia
