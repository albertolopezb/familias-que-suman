"""Inyección y validación de entradas (OWASP A05:2025 · CWE-89 · CWE-79 · CWE-20)."""

import pytest
from fastapi.testclient import TestClient

from app.main import app

from .conftest import conectar_como_dueno

CARGAS_SQLI = [
    "' OR '1'='1",
    "'; DROP TABLE cuentas; --",
    "1; SELECT pg_sleep(5)",
    "\" OR \"\"=\"",
    "admin'--",
]


@pytest.mark.parametrize("carga", CARGAS_SQLI)
def test_sqli_en_filtros_de_busqueda(cliente, carga):
    r = cliente.get("/v1/actividades", params={"ciudad": carga})
    # O el patrón del filtro lo rechaza, o se busca literalmente esa "ciudad" y no hay nada.
    assert r.status_code in (200, 422)
    if r.status_code == 200:
        assert r.json() == []


@pytest.mark.parametrize("carga", CARGAS_SQLI)
def test_sqli_en_ids(cliente, carga):
    assert cliente.get(f"/v1/actividades/{carga}").status_code in (404, 422)


def test_sqli_en_el_cuerpo_se_guarda_como_texto(cliente):
    sin_limite = TestClient(app, client=("198.51.100.20", 40000))
    r = sin_limite.post("/v1/sugerencias", json={
        "tipo": "ACTIVIDAD", "nombre": "'; DROP TABLE cuentas; --", "descripcion": "Robert'); DROP TABLE sugerencias;--  ",
        "sugerida_por": "Bobby Tables", "contacto": "bobby@correo.com",
    })
    assert r.status_code == 201, r.text
    with conectar_como_dueno() as c:
        assert c.execute("SELECT count(*) FROM sugerencias WHERE nombre = %s", ("'; DROP TABLE cuentas; --",)).fetchone()[0] == 1
        assert c.execute("SELECT to_regclass('public.cuentas')").fetchone()[0] is not None       # la tabla sigue ahí


@pytest.mark.parametrize("texto", ["hola\x00mundo", "línea\x1b[31mroja", "\x7fborrar"])
def test_caracteres_de_control_se_rechazan(cliente, familia, texto):
    r = cliente.post("/v1/yo/acompanantes", headers=familia.headers, json={"nombre": texto})
    assert r.status_code == 422


def test_textos_con_tope(cliente, familia):
    assert cliente.post("/v1/yo/acompanantes", headers=familia.headers, json={"nombre": "A" * 81}).status_code == 422


def test_numeros_fuera_de_rango(cliente, admin, asociacion):
    r = cliente.post("/v1/admin/actividades", headers=admin.headers, json={
        "titulo": "Cupo imposible", "asociacion_id": asociacion["id"], "inicia_en": "2030-01-01T10:00:00-06:00", "cupo_total": -5,
    })
    assert r.status_code == 422


def test_fecha_sin_zona_horaria_se_rechaza(cliente, admin, asociacion):
    r = cliente.post("/v1/admin/actividades", headers=admin.headers, json={
        "titulo": "Sin zona", "asociacion_id": asociacion["id"], "inicia_en": "2030-01-01T10:00:00",
    })
    assert r.status_code == 422


@pytest.mark.parametrize("enlace", ["javascript:alert(1)", "intent://hack#Intent;end", "http://www.instagram.com/x", "https://evil.example/x"])
def test_enlaces_inseguros_se_rechazan(cliente, admin, enlace):
    r = cliente.post("/v1/admin/proyectos", headers=admin.headers, json={"nombre": "Proyecto", "instagram": enlace})
    assert r.status_code == 422


def test_xss_en_la_pagina_de_invitacion_se_escapa(cliente, crear_actividad):
    actividad = crear_actividad(titulo="<script>alert('xss')</script><img src=x onerror=alert(1)>")
    r = cliente.get(f"/a/{actividad['id']}")
    assert r.status_code == 200
    assert "<script>alert" not in r.text and "<img src=x" not in r.text
    assert "&lt;script&gt;" in r.text
    assert "script-src" not in r.headers["content-security-policy"]       # ningún script permitido
    assert "default-src 'none'" in r.headers["content-security-policy"]


def test_validacion_no_refleja_la_entrada(cliente, familia):
    r = cliente.post("/v1/yo/acompanantes", headers=familia.headers, json={"nombre": "<b>x</b>" * 20})
    assert r.status_code == 422
    assert "<b>" not in r.text
