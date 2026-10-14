"""El usuario de la base con el que entra la API no puede hacer más de lo que necesita.

Si alguien lograra ejecutar SQL a través de la API, estas son las cosas que NO podría hacer.
"""

import psycopg
import pytest

from .conftest import conectar_como_api


@pytest.mark.parametrize("sql", [
    "DROP TABLE cuentas",
    "TRUNCATE actividades",
    "ALTER TABLE cuentas ADD COLUMN puerta_trasera text",
    "CREATE TABLE robada (x text)",
    "UPDATE auditoria SET accion = 'nada'",
    "DELETE FROM auditoria",
    "SELECT * FROM flyway_schema_history",
    "INSERT INTO ciudades VALUES ('Atlantis', 'X', 'UTC', 0, 0)",
    "CREATE ROLE intruso SUPERUSER",
])
def test_la_api_no_puede(sql):
    with conectar_como_api() as c, pytest.raises(psycopg.errors.InsufficientPrivilege):
        c.execute(sql)


def test_la_api_si_puede_lo_suyo():
    with conectar_como_api() as c:
        assert c.execute("SELECT count(*) >= 0 FROM actividades").fetchone()[0]
        c.execute("INSERT INTO auditoria (accion, objeto_tipo) VALUES ('prueba', 'prueba')")
