"""Autenticación: ¿quién eres? La responde Auth0; aquí solo se VERIFICA.

Cómo funciona (OAuth 2.0 + OpenID Connect):
  1. La app abre la página de inicio de sesión de Auth0 (Universal Login) con PKCE.
  2. Auth0 revisa correo y contraseña (que guarda con hash bcrypt y protege contra fuerza bruta)
     y le da a la app un ACCESS TOKEN: un JWT firmado con la llave PRIVADA RSA de Auth0 (RS256).
  3. La app manda ese token en cada petición: `Authorization: Bearer <token>`.
  4. Esta API descarga las llaves PÚBLICAS de Auth0 (JWKS, https://<tenant>/.well-known/jwks.json)
     y comprueba la firma. Con la llave pública se puede VERIFICAR, pero no FIRMAR: aunque alguien
     robara esta API completa, no podría fabricar tokens. Es la diferencia HS256 vs RS256 de clase.

Qué se revisa de cada token, y por qué:
  · alg == RS256 y nada más        → cierra el ataque "alg: none" y la confusión RS256/HS256.
  · la firma con la llave del `kid` → nadie alteró el contenido (integridad y autenticidad).
  · iss == https://<tenant>/        → lo emitió NUESTRO tenant, no otro de Auth0.
  · aud == esta API                 → el token era para nosotros, no para otra API.
  · exp / iat                       → no está vencido (tolerancia de 30 s por relojes).
  · azp (opcional)                  → lo pidió nuestra app Android.
  · no está en la lista de sesiones cerradas (Redis).

Si Auth0 no responde, la API NO deja pasar a nadie: responde 503 (falla cerrado, OWASP A10).
"""

import hashlib
import json
import logging
import time
import urllib.error
import urllib.request
import uuid
from dataclasses import dataclass
from datetime import UTC, datetime, timedelta

import jwt
import redis
from fastapi import Depends, Request
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import func, select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session

from .config import ajustes
from .db import obtener_db
from .errores import ApiError
from .modelos import Cuenta, Preferencias

log = logging.getLogger("familias.identidad")

# Los permisos que da Auth0 (RBAC de la API). Cada función de administración pide el suyo:
# quien solo registra asistencias no puede borrar campañas (mínimo privilegio, OWASP API5).
ADMINISTRAR_CATALOGO = "administrar:catalogo"
MODERAR_CONTENIDO = "moderar:contenido"
REGISTRAR_ASISTENCIAS = "registrar:asistencias"
ATENDER_PRIVACIDAD = "atender:privacidad"
PERMISOS_ADMIN = frozenset({ADMINISTRAR_CATALOGO, MODERAR_CONTENIDO, REGISTRAR_ASISTENCIAS, ATENDER_PRIVACIDAD})

MAX_LARGO_TOKEN = 8192


@dataclass(frozen=True)
class Sujeto:
    """Quién hace la petición, con los ATRIBUTOS que usan las políticas ABAC (politicas.py)."""

    cuenta_id: uuid.UUID
    sub: str
    correo: str
    nombre_familia: str
    permisos: frozenset[str]
    huella_token: str          # SHA-256 del token: para cerrarlo sin guardar el token
    expira_en: int

    def puede(self, permiso: str) -> bool:
        return permiso in self.permisos

    @property
    def es_admin(self) -> bool:
        return bool(self.permisos & PERMISOS_ADMIN)


def huella(token: str) -> str:
    return hashlib.sha256(token.encode("ascii", errors="ignore")).hexdigest()


# ─── Verificar la firma ──────────────────────────────────────────────────────

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


class Verificador:
    """Verifica tokens de Auth0 con sus llaves públicas (JWKS), que se guardan en memoria una
    hora y se vuelven a pedir si llega un `kid` nuevo (Auth0 rota sus llaves)."""

    def __init__(self, dominio: str, audiencia: str, cliente_app: str = "") -> None:
        self.emisor = f"https://{dominio}/"
        self.audiencia = audiencia
        self.cliente_app = cliente_app
        self._jwks = jwt.PyJWKClient(
            f"https://{dominio}/.well-known/jwks.json", cache_keys=True, lifespan=3600, timeout=5
        )

    def _llave(self, token: str):
        try:
            return self._jwks.get_signing_key_from_jwt(token).key
        except jwt.PyJWKClientConnectionError as e:
            log.error("No se pudo leer el JWKS de Auth0", exc_info=e)
            raise IdentidadNoDisponible() from e
        except (jwt.PyJWKClientError, jwt.DecodeError) as e:
            raise _token_invalido() from e

    def verificar(self, token: str) -> dict:
        if len(token) > MAX_LARGO_TOKEN or token.count(".") != 2:
            raise _token_invalido()
        try:
            encabezado = jwt.get_unverified_header(token)
        except jwt.DecodeError as e:
            raise _token_invalido() from e
        if encabezado.get("alg") != "RS256" or not encabezado.get("kid"):
            raise _token_invalido()
        llave = self._llave(token)
        try:
            claims = jwt.decode(
                token,
                llave,
                algorithms=["RS256"],
                audience=self.audiencia,
                issuer=self.emisor,
                leeway=30,
                options={"require": ["exp", "iat", "iss", "aud", "sub"]},
            )
        except jwt.ExpiredSignatureError as e:
            raise _token_invalido("token_expirado") from e
        except jwt.InvalidTokenError as e:
            raise _token_invalido() from e
        if self.cliente_app and claims.get("azp") != self.cliente_app:
            raise _token_invalido()
        if not isinstance(claims.get("sub"), str) or not 1 <= len(claims["sub"]) <= 255:
            raise _token_invalido()
        return claims


# ─── El perfil de Auth0 (solo la primera vez) ────────────────────────────────

@dataclass(frozen=True)
class PerfilAuth0:
    sub: str
    correo: str
    verificado: bool
    nombre: str


class ProveedorPerfil:
    """Le pregunta a Auth0 (/userinfo) el correo de la persona. Solo se usa la primera vez
    que una cuenta llega a la API, para crearla aquí.

    OWASP API10 (consumo inseguro de APIs): la respuesta de Auth0 tampoco se cree a ciegas.
    Se pide con HTTPS y tiempo límite, y se revisan los tipos, los largos y que el `sub`
    sea el mismo del token.
    """

    def __init__(self, dominio: str) -> None:
        self.url = f"https://{dominio}/userinfo"

    def obtener(self, token: str) -> PerfilAuth0:
        solicitud = urllib.request.Request(self.url, headers={"Authorization": f"Bearer {token}", "Accept": "application/json"})
        try:
            with urllib.request.urlopen(solicitud, timeout=5) as r:  # noqa: S310 — URL fija de la configuración
                if r.status != 200:
                    raise IdentidadNoDisponible()
                datos = json.loads(r.read(64_000))
        except urllib.error.HTTPError as e:
            if e.code in (401, 403):
                raise _token_invalido() from e
            raise IdentidadNoDisponible() from e
        except (urllib.error.URLError, TimeoutError, ValueError) as e:
            raise IdentidadNoDisponible() from e
        sub, correo = datos.get("sub"), datos.get("email")
        if not isinstance(sub, str) or not isinstance(correo, str) or not 3 <= len(correo) <= 254 or "@" not in correo:
            raise ApiError(422, "Tu cuenta de Auth0 no tiene un correo válido.", "perfil_incompleto")
        nombre = datos.get("name") if isinstance(datos.get("name"), str) else ""
        return PerfilAuth0(sub=sub, correo=correo.strip().lower(), verificado=datos.get("email_verified") is True, nombre=nombre[:80])


# ─── Sesiones cerradas (Redis) ───────────────────────────────────────────────

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


# ─── Dependencias para las rutas ─────────────────────────────────────────────
# Se pueden sustituir en las pruebas (app.dependency_overrides) sin tocar el código de producción:
# no existe ningún "modo de prueba" que acepte tokens falsos en el servidor real.

_verificador: Verificador | None = None
_proveedor: ProveedorPerfil | None = None
_sesiones: SesionesCerradas | None = None


def obtener_verificador() -> Verificador:
    global _verificador
    if not ajustes.auth0_configurado:
        raise ApiError(503, "El inicio de sesión todavía no está configurado en el servidor.", "identidad_sin_configurar")
    if _verificador is None:
        _verificador = Verificador(ajustes.auth0_dominio, ajustes.auth0_audiencia, ajustes.auth0_cliente_app)
    return _verificador


def obtener_proveedor_perfil() -> ProveedorPerfil:
    global _proveedor
    if _proveedor is None:
        _proveedor = ProveedorPerfil(ajustes.auth0_dominio)
    return _proveedor


def obtener_sesiones() -> SesionesCerradas:
    global _sesiones
    if _sesiones is None:
        _sesiones = SesionesCerradas(ajustes.redis_url)
    return _sesiones


_bearer = HTTPBearer(auto_error=False, description="El access token de Auth0")


def _cuenta_de(db: Session, claims: dict, token: str, proveedor: ProveedorPerfil) -> Cuenta:
    """La cuenta local de este `sub`. Si es la primera vez, se crea (o se vincula)."""
    sub = claims["sub"]
    cuenta = db.scalar(select(Cuenta).where(Cuenta.auth0_sub == sub))
    if cuenta is not None:
        return cuenta

    perfil = proveedor.obtener(token)
    if perfil.sub != sub:
        raise _token_invalido()

    existente = db.scalar(select(Cuenta).where(func.lower(Cuenta.correo) == perfil.correo))
    if existente is not None:
        # Una cuenta con ese correo ya existía (por ejemplo, la de prueba del seed). Solo se
        # vincula si Auth0 asegura que el correo es de quien entra; si no, cualquiera podría
        # registrarse con el correo de otra familia y quedarse con sus datos.
        if existente.auth0_sub is None and perfil.verificado:
            existente.auth0_sub = sub
            existente.correo_verificado = True
            db.commit()
            return existente
        log.warning("Cuenta sin vincular: el correo ya existe", extra={"evento": f"sub:{sub}"})
        raise ApiError(
            409,
            "Ya existe una cuenta con ese correo. Confirma tu correo desde el mensaje que te mandamos y vuelve a entrar.",
            "correo_sin_verificar",
        )

    db.execute(
        insert(Cuenta)
        .values(
            auth0_sub=sub,
            correo=perfil.correo,
            correo_verificado=perfil.verificado,
            nombre_familia=perfil.nombre or "Mi familia",
            titular_nombre=perfil.nombre or None,
        )
        .on_conflict_do_nothing(index_elements=["auth0_sub"])   # dos primeras peticiones a la vez
    )
    db.flush()
    cuenta = db.scalar(select(Cuenta).where(Cuenta.auth0_sub == sub))
    db.execute(insert(Preferencias).values(cuenta_id=cuenta.id).on_conflict_do_nothing())
    db.commit()
    return cuenta


def _sujeto(request: Request, credenciales: HTTPAuthorizationCredentials | None, db: Session,
            proveedor: ProveedorPerfil, sesiones: SesionesCerradas) -> Sujeto:
    if credenciales is None or credenciales.scheme.lower() != "bearer":
        raise _token_invalido("falta_token")
    # El verificador se pide hasta que hay token: sin token es 401 siempre, esté o no Auth0
    # configurado. (Las pruebas lo sustituyen con dependency_overrides.)
    fabrica = request.app.dependency_overrides.get(obtener_verificador, obtener_verificador)
    token = credenciales.credentials
    claims = fabrica().verificar(token)
    huella_token = huella(token)
    if sesiones.esta_cerrada(huella_token):
        raise _token_invalido("sesion_cerrada")

    cuenta = _cuenta_de(db, claims, token, proveedor)
    permisos = frozenset(p for p in claims.get("permissions", []) if isinstance(p, str))

    # Se refleja el rol en la base (para consultas y la bitácora) y la última vez que entró,
    # pero sin escribir en cada petición: como mucho una vez cada 10 minutos.
    rol = "admin" if permisos & PERMISOS_ADMIN else "familia"
    ahora = datetime.now(UTC)
    if cuenta.rol != rol or cuenta.ultimo_acceso_en is None or ahora - cuenta.ultimo_acceso_en > timedelta(minutes=10):
        cuenta.rol = rol
        cuenta.ultimo_acceso_en = ahora
        db.commit()

    request.state.cuenta_id = cuenta.id
    return Sujeto(
        cuenta_id=cuenta.id,
        sub=claims["sub"],
        correo=cuenta.correo,
        nombre_familia=cuenta.nombre_familia,
        permisos=permisos,
        huella_token=huella_token,
        expira_en=int(claims["exp"]),
    )


def usuario_actual(
    request: Request,
    credenciales: HTTPAuthorizationCredentials | None = Depends(_bearer),
    db: Session = Depends(obtener_db),
    proveedor: ProveedorPerfil = Depends(obtener_proveedor_perfil),
    sesiones: SesionesCerradas = Depends(obtener_sesiones),
) -> Sujeto:
    """Exige sesión. Sin token o con uno inválido: 401."""
    return _sujeto(request, credenciales, db, proveedor, sesiones)


def usuario_opcional(
    request: Request,
    credenciales: HTTPAuthorizationCredentials | None = Depends(_bearer),
    db: Session = Depends(obtener_db),
    sesiones: SesionesCerradas = Depends(obtener_sesiones),
    proveedor: ProveedorPerfil = Depends(obtener_proveedor_perfil),
) -> Sujeto | None:
    """Para rutas públicas que cambian un poco con sesión (por ejemplo, "este testimonio es
    tuyo"). Sin token: None. Con token: se verifica igual de estricto (un token malo es 401,
    no "sin sesión")."""
    if credenciales is None:
        return None
    return _sujeto(request, credenciales, db, proveedor, sesiones)


def requiere(permiso: str):
    """Autorización a nivel de FUNCIÓN (OWASP API5): la app puede esconder el botón de
    administrar, pero el servidor no confía en eso y revisa el permiso del token."""

    def _dependencia(sujeto: Sujeto = Depends(usuario_actual)) -> Sujeto:
        if not sujeto.puede(permiso):
            raise ApiError(403, "Tu cuenta no tiene permiso para hacer esto.", "sin_permiso")
        return sujeto

    return _dependencia
