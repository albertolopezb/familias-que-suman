"""Autenticación (OWASP A07 · API2): solo pasan tokens firmados por Auth0, para esta API, vigentes."""

import time

import jwt
import pytest

from app import identidad

from .conftest import AUDIENCIA, EMISOR, KID, OTRA_LLAVE, PERFILES, claims_de, firmar


def _pedir(cliente, token: str | None):
    headers = {"Authorization": f"Bearer {token}"} if token is not None else {}
    return cliente.get("/v1/yo", headers=headers)


def test_sin_token_es_401_y_dice_como_autenticarse(cliente):
    r = _pedir(cliente, None)
    assert r.status_code == 401
    assert r.json()["codigo"] == "falta_token"
    assert r.headers["www-authenticate"].startswith("Bearer")


@pytest.mark.parametrize("basura", ["", "abc", "a.b", "a.b.c", "x" * 9000])
def test_token_mal_formado(cliente, basura):
    assert _pedir(cliente, basura).status_code == 401


def test_token_valido_crea_la_cuenta_la_primera_vez(cliente, familia):
    r = cliente.get("/v1/yo", headers=familia.headers)
    assert r.status_code == 200
    cuerpo = r.json()
    assert cuerpo["correo"].endswith("@pruebas.mx")
    assert cuerpo["es_admin"] is False
    assert "auth0_sub" not in cuerpo                       # no se exponen identificadores internos


def test_alg_none_se_rechaza(cliente):
    # El ataque clásico: quitar la firma y decir "alg": "none".
    sin_firma = jwt.encode(claims_de("auth0|atacante"), key="", algorithm="none")
    assert _pedir(cliente, sin_firma).status_code == 401


def test_confusion_hs256_con_la_llave_publica_se_rechaza(cliente):
    # Otro clásico: firmar con HMAC usando la llave PÚBLICA como si fuera el secreto.
    token = jwt.encode(claims_de("auth0|atacante"), key="cualquier-cosa", algorithm="HS256", headers={"kid": KID})
    assert _pedir(cliente, token).status_code == 401


def test_firmado_con_otra_llave_se_rechaza(cliente):
    falsificado = firmar(claims_de("auth0|atacante", ("administrar:catalogo",)), llave=OTRA_LLAVE)
    assert _pedir(cliente, falsificado).status_code == 401


def test_kid_desconocido_se_rechaza(cliente):
    token = firmar(claims_de("auth0|x"), headers={"kid": "otra-llave"})
    assert _pedir(cliente, token).status_code == 401


def test_contenido_alterado_rompe_la_firma(cliente, familia):
    encabezado, contenido, firma = familia.token.split(".")
    otro = jwt.utils.base64url_encode(jwt.utils.base64url_decode(contenido).replace(b'"permissions":[]', b'"permissions":["administrar:catalogo"]'))
    assert _pedir(cliente, f"{encabezado}.{otro.decode()}.{firma}").status_code == 401


def test_token_vencido(cliente):
    ahora = int(time.time())
    token = firmar(claims_de("auth0|viejo", iat=ahora - 7200, exp=ahora - 3600))
    r = _pedir(cliente, token)
    assert r.status_code == 401 and r.json()["codigo"] == "token_expirado"


@pytest.mark.parametrize("cambio", [{"aud": "https://otra-api"}, {"iss": "https://otro-tenant.auth0.com/"}])
def test_audiencia_o_emisor_ajenos(cliente, cambio):
    assert _pedir(cliente, firmar(claims_de("auth0|x", **cambio))).status_code == 401


def test_faltan_claims_obligatorios(cliente):
    claims = claims_de("auth0|x")
    del claims["exp"]
    assert _pedir(cliente, firmar(claims)).status_code == 401


def test_cerrar_sesion_invalida_el_token_de_inmediato(cliente, familia):
    assert cliente.post("/v1/sesion/cerrar", headers=familia.headers).status_code == 204
    r = cliente.get("/v1/yo", headers=familia.headers)
    assert r.status_code == 401 and r.json()["codigo"] == "sesion_cerrada"


def test_correo_existente_sin_verificar_no_se_vincula(cliente, familia):
    """Si alguien se registra en Auth0 con el correo de otra familia (sin verificarlo), no se
    queda con su cuenta."""
    correo = cliente.get("/v1/yo", headers=familia.headers).json()["correo"]
    intruso = "auth0|intruso-" + correo[:8]
    PERFILES.perfiles[intruso] = identidad.PerfilAuth0(intruso, correo, False, "Intruso")
    r = _pedir(cliente, firmar(claims_de(intruso)))
    assert r.status_code == 409 and r.json()["codigo"] == "correo_sin_verificar"


def test_los_permisos_vienen_del_token_no_del_cuerpo(cliente, familia):
    r = cliente.patch("/v1/yo", headers=familia.headers, json={"es_admin": True})
    assert r.status_code == 422                              # mass assignment: campo no permitido
    assert cliente.get("/v1/yo", headers=familia.headers).json()["es_admin"] is False


def test_vinculacion_manual_de_una_cuenta_del_seed(cliente):
    """La familia de prueba existe sin usuario de Auth0; la herramienta la une a su `sub` y desde
    entonces ese token entra a esa cuenta (con sus datos), sin crear otra."""
    import uuid as _uuid

    from app.herramientas import vincular

    from .conftest import conectar_como_dueno

    correo = f"seed-{_uuid.uuid4().hex[:8]}@correo.com"
    with conectar_como_dueno() as c:
        c.execute("INSERT INTO cuentas (correo, nombre_familia) VALUES (%s, 'Familia del Seed')", (correo,))
    sub = f"auth0|{_uuid.uuid4().hex}"
    assert vincular(correo, "esto no es un sub") == 2
    assert vincular(correo, sub) == 0
    assert vincular(correo, f"auth0|{_uuid.uuid4().hex}") == 1          # ya no se puede volver a vincular
    r = cliente.get("/v1/yo", headers={"Authorization": f"Bearer {firmar(claims_de(sub))}"})
    assert r.status_code == 200 and r.json()["nombre_familia"] == "Familia del Seed"
