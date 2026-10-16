"""RF-18: "¿Olvidaste tu contraseña?" con un código de 6 dígitos por correo, de un solo uso.

Los correos no salen: `buzon` reemplaza a correo.enviar y los guarda en una lista.
"""

import re
import secrets

import pytest
from fastapi.testclient import TestClient

from app import correo
from app.main import app

from .conftest import CONTRASENA, conectar_como_dueno, crear_cuenta

NUEVA = "una frase nueva y larga 2026"


def _cliente_propio() -> TestClient:
    """Una "IP" solo para la prueba: los límites por IP no se mezclan entre pruebas."""
    return TestClient(app, client=(f"198.19.{secrets.randbelow(250)}.{secrets.randbelow(250) + 1}", 40000))


@pytest.fixture
def buzon(monkeypatch):
    enviados: list[dict] = []
    monkeypatch.setattr(correo, "enviar", lambda para, asunto, texto: enviados.append({"para": para, "asunto": asunto, "texto": texto}))
    return enviados


def _codigo(buzon, para) -> str:
    texto = [m["texto"] for m in buzon if m["para"] == para][-1]
    return re.search(r"\b(\d{6})\b", texto).group(1)


def _restablecer(c, correo_, codigo, contrasena=NUEVA):
    return c.post("/v1/sesion/restablecer", json={"correo": correo_, "codigo": codigo, "contrasena": contrasena})


def test_responde_lo_mismo_exista_o_no_la_cuenta(buzon):
    c = _cliente_propio()
    persona = crear_cuenta()
    si = c.post("/v1/sesion/recuperar", json={"correo": persona.correo})
    no = c.post("/v1/sesion/recuperar", json={"correo": f"nadie-{secrets.token_hex(4)}@pruebas.mx"})
    assert si.status_code == no.status_code == 202 and si.content == no.content == b""
    assert [m["para"] for m in buzon] == [persona.correo]          # solo a quien sí tiene cuenta


def test_el_codigo_cambia_la_contrasena_una_sola_vez(buzon):
    c = _cliente_propio()
    persona = crear_cuenta()
    entrada = c.post("/v1/sesion/iniciar", json={"correo": persona.correo, "contrasena": CONTRASENA}).json()
    c.post("/v1/sesion/recuperar", json={"correo": persona.correo})
    codigo = _codigo(buzon, persona.correo)

    assert _restablecer(c, persona.correo, codigo).status_code == 204
    assert _restablecer(c, persona.correo, codigo, "otra frase distinta 2026").status_code == 400   # ya se usó
    # La contraseña vieja ya no entra y la nueva sí.
    assert c.post("/v1/sesion/iniciar", json={"correo": persona.correo, "contrasena": CONTRASENA}).status_code == 401
    assert c.post("/v1/sesion/iniciar", json={"correo": persona.correo, "contrasena": NUEVA}).status_code == 200
    # Las sesiones de antes se cerraron: ni el JWT ni el refresh token sirven.
    assert c.get("/v1/yo", headers={"Authorization": f"Bearer {entrada['access_token']}"}).status_code == 401
    assert c.post("/v1/sesion/renovar", json={"refresh_token": entrada["refresh_token"]}).status_code == 401
    # Y le avisamos por correo que cambió.
    assert buzon[-1]["asunto"] == "Tu contraseña cambió"


def test_el_codigo_no_se_guarda_en_claro(buzon):
    c = _cliente_propio()
    persona = crear_cuenta()
    c.post("/v1/sesion/recuperar", json={"correo": persona.correo})
    codigo = _codigo(buzon, persona.correo)
    with conectar_como_dueno() as db:
        huella, expira = db.execute(
            "SELECT huella, expira_en - creada_en FROM recuperaciones WHERE cuenta_id = %s", (persona.sub,)).fetchone()
    assert len(huella) == 64 and codigo not in huella
    assert abs(expira.total_seconds() - 15 * 60) < 2           # 15 minutos


def test_cinco_intentos_equivocados_queman_el_codigo(buzon):
    c = _cliente_propio()
    persona = crear_cuenta()
    c.post("/v1/sesion/recuperar", json={"correo": persona.correo})
    codigo = _codigo(buzon, persona.correo)
    malo = f"{(int(codigo) + 1) % 1_000_000:06d}"
    for _ in range(5):
        r = _restablecer(c, persona.correo, malo)
        assert r.status_code == 400 and r.json()["codigo"] == "codigo_invalido"
    assert _restablecer(c, persona.correo, codigo).status_code == 400      # el bueno ya tampoco sirve


def test_un_codigo_vencido_no_sirve(buzon):
    c = _cliente_propio()
    persona = crear_cuenta()
    c.post("/v1/sesion/recuperar", json={"correo": persona.correo})
    codigo = _codigo(buzon, persona.correo)
    with conectar_como_dueno() as db:
        db.execute("UPDATE recuperaciones SET creada_en = now() - interval '20 minutes', "
                   "expira_en = now() - interval '5 minutes' WHERE cuenta_id = %s", (persona.sub,))
    assert _restablecer(c, persona.correo, codigo).status_code == 400


def test_pedir_otro_anula_el_anterior_y_hay_tope_por_hora(buzon):
    c = _cliente_propio()
    persona = crear_cuenta()
    c.post("/v1/sesion/recuperar", json={"correo": persona.correo})
    primero = _codigo(buzon, persona.correo)
    c.post("/v1/sesion/recuperar", json={"correo": persona.correo})
    segundo = _codigo(buzon, persona.correo)
    c.post("/v1/sesion/recuperar", json={"correo": persona.correo})
    assert c.post("/v1/sesion/recuperar", json={"correo": persona.correo}).status_code == 202   # el 4.º: misma respuesta…
    assert len([m for m in buzon if m["para"] == persona.correo]) == 3                       # …pero ya no manda correo
    if primero != segundo:
        assert _restablecer(c, persona.correo, primero).status_code == 400


def test_el_codigo_de_una_cuenta_no_sirve_para_otra(buzon):
    c = _cliente_propio()
    ana, beto = crear_cuenta(), crear_cuenta()
    c.post("/v1/sesion/recuperar", json={"correo": ana.correo})
    c.post("/v1/sesion/recuperar", json={"correo": beto.correo})
    codigo_de_ana = _codigo(buzon, ana.correo)
    if codigo_de_ana != _codigo(buzon, beto.correo):
        assert _restablecer(c, beto.correo, codigo_de_ana).status_code == 400


def test_contrasena_debil_o_codigo_raro_no_gastan_el_codigo(buzon):
    c = _cliente_propio()
    persona = crear_cuenta()
    c.post("/v1/sesion/recuperar", json={"correo": persona.correo})
    codigo = _codigo(buzon, persona.correo)
    assert _restablecer(c, persona.correo, codigo, "12345678").status_code == 422
    for raro in ("12345", "1234567", "12a456", "' OR 1=1", ""):
        assert _restablecer(c, persona.correo, raro).status_code == 422
    assert _restablecer(c, persona.correo, codigo).status_code == 204      # sigue sirviendo
