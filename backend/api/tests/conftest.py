"""Lo que comparten todas las pruebas.

    docker compose --profile pruebas run --rm pruebas

Las pruebas corren contra PostgreSQL, Redis y RustFS DE VERDAD (los del compose), no contra
versiones falsas: una prueba de permisos contra una base de mentira no demuestra que la real
los respete. Para no tocar los datos de desarrollo:

  · se crea una base aparte, `familias_pruebas`, desde cero, con las MISMAS migraciones;
  · Redis usa otra base lógica (/1) y el almacén otro bucket (familias-pruebas).

Auth0 no se usa: las pruebas tienen su propio par de llaves RSA y firman sus tokens con la
privada. Al servidor se le cambia (solo aquí, con dependency_overrides) de dónde saca la llave
pública. El código que verifica la firma, el emisor, la audiencia y la fecha es el mismo de
producción.
"""

import os
import time
import uuid
from dataclasses import dataclass, field
from datetime import UTC, datetime, timedelta
from pathlib import Path

import jwt
import psycopg
import pytest
import redis
from cryptography.hazmat.primitives.asymmetric import rsa


# ─── 1. La base de pruebas, de cero, ANTES de importar la app ────────────────

def _preparar_base() -> None:
    dueno = os.environ["DUENO_URL"]
    with psycopg.connect(dueno, autocommit=True) as c:
        c.execute("DROP DATABASE IF EXISTS familias_pruebas WITH (FORCE)")
        c.execute("CREATE DATABASE familias_pruebas")
    url = dueno.rsplit("/", 1)[0] + "/familias_pruebas"
    with psycopg.connect(url, autocommit=True) as c:
        # afterMigrate le quita a la API el permiso sobre el historial de Flyway: que exista.
        c.execute("CREATE TABLE flyway_schema_history (installed_rank int)")
        migraciones = sorted(Path("/migraciones").glob("V*__*.sql"), key=lambda p: int(p.name[1:].split("__")[0]))
        for archivo in migraciones:
            c.execute(archivo.read_text(encoding="utf-8"))
        despues = (Path("/migraciones/afterMigrate.sql").read_text(encoding="utf-8")
                   .replace("${api_usuario}", os.environ["DB_API_USUARIO"])
                   .replace("${api_contrasena}", os.environ["DB_API_CONTRASENA"])
                   .replace("${flyway:database}", "familias_pruebas"))
        c.execute(despues)


_preparar_base()
redis.Redis.from_url(os.environ["REDIS_URL"]).flushdb()

from fastapi.testclient import TestClient  # noqa: E402

from app import identidad  # noqa: E402
from app.config import ajustes  # noqa: E402
from app.main import app  # noqa: E402

# ─── 2. Tokens de prueba ─────────────────────────────────────────────────────

LLAVE = rsa.generate_private_key(public_exponent=65537, key_size=2048)
OTRA_LLAVE = rsa.generate_private_key(public_exponent=65537, key_size=2048)   # "la del atacante"
KID = "llave-de-pruebas"
EMISOR = ajustes.auth0_emisor
AUDIENCIA = ajustes.auth0_audiencia


class VerificadorDePrueba(identidad.Verificador):
    """El verificador de producción, pero la llave pública sale de aquí y no del JWKS de Auth0."""

    def __init__(self) -> None:
        super().__init__(ajustes.auth0_dominio, AUDIENCIA)

    def _llave(self, token: str):
        if jwt.get_unverified_header(token).get("kid") != KID:
            raise identidad._token_invalido()
        return LLAVE.public_key()


class PerfilesDePrueba(identidad.ProveedorPerfil):
    """En lugar de preguntarle a Auth0 /userinfo, cada prueba dice qué correo tiene cada sub."""

    def __init__(self) -> None:
        self.perfiles: dict[str, identidad.PerfilAuth0] = {}

    def obtener(self, token: str) -> identidad.PerfilAuth0:
        sub = jwt.decode(token, options={"verify_signature": False})["sub"]
        return self.perfiles.get(sub) or identidad.PerfilAuth0(sub, f"{sub.split('|')[-1]}@pruebas.mx", True, "Familia de Prueba")


PERFILES = PerfilesDePrueba()
app.dependency_overrides[identidad.obtener_verificador] = VerificadorDePrueba
app.dependency_overrides[identidad.obtener_proveedor_perfil] = lambda: PERFILES


def firmar(claims: dict, llave=None, headers: dict | None = None, algoritmo: str = "RS256") -> str:
    return jwt.encode(claims, llave or LLAVE, algorithm=algoritmo, headers=headers if headers is not None else {"kid": KID})


def claims_de(sub: str, permisos: tuple[str, ...] = (), **cambios) -> dict:
    ahora = int(time.time())
    base = {"iss": EMISOR, "aud": AUDIENCIA, "sub": sub, "iat": ahora, "exp": ahora + 600, "permissions": list(permisos)}
    base.update(cambios)
    return base


# ─── 3. Personas ─────────────────────────────────────────────────────────────

@dataclass
class Persona:
    sub: str
    token: str
    permisos: tuple[str, ...] = field(default_factory=tuple)

    @property
    def headers(self) -> dict[str, str]:
        return {"Authorization": f"Bearer {self.token}"}


@pytest.fixture(scope="session")
def cliente():
    with TestClient(app) as c:
        yield c


@pytest.fixture
def nueva_persona(cliente):
    def _crear(*permisos: str, nombre: str = "Familia de Prueba") -> Persona:
        sub = f"auth0|{uuid.uuid4().hex}"
        PERFILES.perfiles[sub] = identidad.PerfilAuth0(sub, f"{sub[6:]}@pruebas.mx", True, nombre)
        persona = Persona(sub, firmar(claims_de(sub, permisos)), permisos)
        r = cliente.get("/v1/yo", headers=persona.headers)
        assert r.status_code == 200, r.text
        return persona

    return _crear


@pytest.fixture
def familia(nueva_persona) -> Persona:
    return nueva_persona(nombre="Familia Ana")


@pytest.fixture
def otra_familia(nueva_persona) -> Persona:
    return nueva_persona(nombre="Familia Beto")


@pytest.fixture
def admin(nueva_persona) -> Persona:
    return nueva_persona(*sorted(identidad.PERMISOS_ADMIN), nombre="Administración")


# ─── 4. Catálogo de prueba (por la API de administración) ───────────────────

@pytest.fixture
def asociacion(cliente, admin) -> dict:
    r = cliente.post("/v1/admin/asociaciones", headers=admin.headers,
                     json={"nombre": f"Asociación {uuid.uuid4().hex[:6]}", "categoria": "Pruebas"})
    assert r.status_code == 201, r.text
    return r.json()


@pytest.fixture
def crear_actividad(cliente, admin, asociacion):
    def _crear(dias: float = 7, cupo: int = 10, **extra) -> dict:
        inicio = datetime.now(UTC) + timedelta(days=dias)
        cuerpo = {
            "titulo": f"Actividad de prueba {uuid.uuid4().hex[:6]}",
            "asociacion_id": asociacion["id"],
            "inicia_en": inicio.isoformat(),
            "termina_en": (inicio + timedelta(hours=2)).isoformat(),
            "cupo_total": cupo,
            **extra,
        }
        r = cliente.post("/v1/admin/actividades", headers=admin.headers, json=cuerpo)
        assert r.status_code == 201, r.text
        return r.json()

    return _crear


def conectar_como_api():
    """Una conexión directa con el usuario de la API (para probar el mínimo privilegio)."""
    return psycopg.connect(os.environ["DATABASE_URL"].replace("postgresql+psycopg", "postgresql"), autocommit=True)


def conectar_como_dueno():
    return psycopg.connect(os.environ["DUENO_URL"].rsplit("/", 1)[0] + "/familias_pruebas", autocommit=True)


@pytest.fixture
def registrar_asistencia(cliente, admin):
    def _registrar(persona_id: str, actividad_id: str) -> str:
        with conectar_como_dueno() as c:
            # La actividad tiene que haber pasado para que tenga sentido la asistencia.
            fila = c.execute(
                "INSERT INTO asistencias (cuenta_id, actividad_id) SELECT id, %s FROM cuentas WHERE auth0_sub = %s RETURNING id",
                (actividad_id, persona_id),
            ).fetchone()
        return str(fila[0])

    return _registrar
