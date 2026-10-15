"""Lo que comparten todas las pruebas.

    docker compose --profile pruebas run --rm pruebas

Las pruebas corren contra PostgreSQL, Redis y RustFS DE VERDAD (los del compose), no contra
versiones falsas: una prueba de permisos contra una base de mentira no demuestra que la real
los respete. Para no tocar los datos de desarrollo:

  · se crea una base aparte, `familias_pruebas`, desde cero, con las MISMAS migraciones;
  · Redis usa otra base lógica (/1) y el almacén otro bucket (familias-pruebas).

Los tokens: las personas de prueba se crean directo en la base (con su contraseña en Argon2id) y
su token lo firma el MISMO `identidad.emitir_token` de producción con el secreto de pruebas
(JWT_SECRETO del servicio `pruebas`). Así cada prueba no tiene que pasar por /v1/sesion/iniciar
(que tiene su propio límite por IP); el inicio de sesión se prueba aparte en test_identidad.py.
No hay ningún "modo de prueba" en el servidor: se verifica todo igual que en producción.
"""

import os
import secrets
import time
import uuid
from dataclasses import dataclass, field
from datetime import UTC, datetime, timedelta
from pathlib import Path
from types import SimpleNamespace

import jwt
import psycopg
import pytest
import redis


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

SECRETO = ajustes.jwt_secreto
OTRO_SECRETO = "el-secreto-que-adivino-el-atacante-0123456789"
EMISOR = ajustes.jwt_emisor
AUDIENCIA = ajustes.jwt_audiencia
CONTRASENA = "una frase larga de prueba 42"


def firmar(claims: dict, secreto: str | None = None, algoritmo: str = "HS256", headers: dict | None = None) -> str:
    """Para fabricar tokens a mano (los que un atacante intentaría)."""
    return jwt.encode(claims, secreto or SECRETO, algorithm=algoritmo, headers=headers)


def claims_de(cuenta_id: str, permisos: tuple[str, ...] = (), **cambios) -> dict:
    ahora = time.time()
    base = {
        "iss": EMISOR, "aud": AUDIENCIA, "sub": str(cuenta_id), "rol": identidad.rol_de(permisos),
        "permisos": list(permisos), "iat": ahora, "exp": int(ahora) + 600, "jti": uuid.uuid4().hex,
    }
    base.update(cambios)
    return base


# ─── 3. Personas ─────────────────────────────────────────────────────────────

@dataclass
class Persona:
    sub: str                  # el id de la cuenta (lo que va en el claim `sub`)
    correo: str
    token: str
    permisos: tuple[str, ...] = field(default_factory=tuple)
    contrasena: str = CONTRASENA

    @property
    def headers(self) -> dict[str, str]:
        return {"Authorization": f"Bearer {self.token}"}


@pytest.fixture(scope="session")
def cliente():
    with TestClient(app) as c:
        yield c


def crear_cuenta(*permisos: str, nombre: str = "Familia de Prueba", contrasena: str = CONTRASENA) -> Persona:
    correo = f"{secrets.token_hex(6)}@pruebas.mx"
    with conectar_como_dueno() as c:
        cuenta_id = c.execute(
            "INSERT INTO cuentas (correo, contrasena_hash, nombre_familia, rol, permisos, tokens_validos_desde) "
            "VALUES (%s, %s, %s, %s, %s, now() - interval '1 minute') RETURNING id",
            (correo, identidad.hash_contrasena(contrasena), nombre, identidad.rol_de(permisos), list(permisos)),
        ).fetchone()[0]
        c.execute("INSERT INTO preferencias (cuenta_id) VALUES (%s)", (cuenta_id,))
    token, _ = identidad.emitir_token(SimpleNamespace(id=cuenta_id, permisos=list(permisos)))
    return Persona(str(cuenta_id), correo, token, permisos, contrasena)


@pytest.fixture
def nueva_persona(cliente):
    def _crear(*permisos: str, nombre: str = "Familia de Prueba") -> Persona:
        persona = crear_cuenta(*permisos, nombre=nombre)
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
                "INSERT INTO asistencias (cuenta_id, actividad_id) VALUES (%s, %s) RETURNING id",
                (persona_id, actividad_id),
            ).fetchone()
        return str(fila[0])

    return _registrar
