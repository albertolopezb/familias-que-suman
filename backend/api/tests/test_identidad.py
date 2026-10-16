"""Identificación (OWASP A07 · API2): cuentas con contraseña en Argon2id, tokens JWT HS256 que
emite y verifica la API, y nada más. Varias pruebas son las de la guía de clase (s23, §5)."""

import secrets
import time
import uuid

import jwt
import pytest
from fastapi.testclient import TestClient

from app.herramientas import cambiar_contrasena, dar_permisos
from app.main import app

from .conftest import AUDIENCIA, CONTRASENA, EMISOR, OTRO_SECRETO, claims_de, conectar_como_dueno, crear_cuenta, firmar


def _pedir(cliente, token: str | None):
    headers = {"Authorization": f"Bearer {token}"} if token is not None else {}
    return cliente.get("/v1/yo", headers=headers)


def _cliente_propio() -> TestClient:
    """Una "IP" solo para la prueba: el límite por IP del inicio de sesión no se mezcla entre pruebas."""
    return TestClient(app, client=(f"198.18.{secrets.randbelow(250)}.{secrets.randbelow(250) + 1}", 40000))


def _registro(**cambios) -> dict:
    return {"correo": f"{secrets.token_hex(6)}@pruebas.mx", "contrasena": CONTRASENA,
            "nombre_familia": "Familia Nueva", "acepta_aviso": True, **cambios}


# ─── Registro ────────────────────────────────────────────────────────────────

def test_registro_crea_la_cuenta_y_entra():
    c = _cliente_propio()
    datos = _registro()
    r = c.post("/v1/sesion/registro", json=datos)
    assert r.status_code == 201, r.text
    cuerpo = r.json()
    assert cuerpo["token_type"] == "bearer" and cuerpo["expira_en"] > 0
    yo = _pedir(c, cuerpo["access_token"]).json()
    assert yo["correo"] == datos["correo"] and yo["nombre_familia"] == "Familia Nueva"
    assert yo["es_admin"] is False and yo["permisos"] == [] and yo["aviso_aceptado"] is True
    assert "contrasena" not in str(yo) and "hash" not in str(yo)


def test_la_contrasena_se_guarda_como_hash_argon2id():
    c = _cliente_propio()
    datos = _registro()
    assert c.post("/v1/sesion/registro", json=datos).status_code == 201
    with conectar_como_dueno() as db:
        guardado = db.execute("SELECT contrasena_hash FROM cuentas WHERE correo = %s", (datos["correo"],)).fetchone()[0]
    assert guardado.startswith("$argon2id$") and CONTRASENA not in guardado


def test_nadie_se_registra_como_admin():
    """Asignación masiva (OWASP API3): el cuerpo no acepta campos que no son del formulario."""
    c = _cliente_propio()
    r = c.post("/v1/sesion/registro", json=_registro(permisos=["administrar:catalogo"], rol="admin"))
    assert r.status_code == 422


def test_registro_con_correo_ocupado():
    c = _cliente_propio()
    datos = _registro()
    assert c.post("/v1/sesion/registro", json=datos).status_code == 201
    r = c.post("/v1/sesion/registro", json={**datos, "correo": datos["correo"].upper()})
    assert r.status_code == 409 and r.json()["codigo"] == "correo_ocupado"


@pytest.mark.parametrize("contrasena", ["corta", "12345678", "aaaaaaaa", " " * 10, "x" * 129])
def test_contrasenas_debiles_o_absurdas(contrasena):
    r = _cliente_propio().post("/v1/sesion/registro", json=_registro(contrasena=contrasena))
    assert r.status_code == 422


def test_registro_pide_el_aviso_de_privacidad():
    r = _cliente_propio().post("/v1/sesion/registro", json=_registro(acepta_aviso=False))
    assert r.status_code == 422 and r.json()["codigo"] == "aviso_requerido"


# ─── Iniciar sesión (el /login de la guía) ───────────────────────────────────

def test_iniciar_sesion_da_un_jwt_con_los_atributos_de_la_cuenta():
    persona = crear_cuenta("moderar:contenido")
    r = _cliente_propio().post("/v1/sesion/iniciar", json={"correo": persona.correo, "contrasena": CONTRASENA})
    assert r.status_code == 200, r.text
    token = r.json()["access_token"]
    # El contenido se puede LEER sin el secreto (solo es Base64): por eso no lleva nada privado.
    claims = jwt.decode(token, options={"verify_signature": False})
    assert jwt.get_unverified_header(token)["alg"] == "HS256"
    assert claims["sub"] == persona.sub and claims["permisos"] == ["moderar:contenido"] and claims["rol"] == "admin"
    assert claims["iss"] == EMISOR and claims["aud"] == AUDIENCIA and claims["exp"] > time.time()
    assert "correo" not in claims and persona.correo not in token


def test_mismo_mensaje_si_el_correo_no_existe_o_la_contrasena_esta_mal():
    """Nadie puede averiguar qué correos tienen cuenta (enumeración de usuarios)."""
    persona = crear_cuenta()
    c = _cliente_propio()
    mala = c.post("/v1/sesion/iniciar", json={"correo": persona.correo, "contrasena": "no es esta"})
    nadie = c.post("/v1/sesion/iniciar", json={"correo": "nadie-tiene-este@pruebas.mx", "contrasena": "no es esta"})
    assert mala.status_code == nadie.status_code == 401
    assert mala.json()["codigo"] == nadie.json()["codigo"] == "credenciales_invalidas"
    assert mala.json()["error"] == nadie.json()["error"]


def test_fuerza_bruta_bloquea_el_correo_aunque_cambie_de_ip():
    persona = crear_cuenta()
    for _ in range(5):
        r = _cliente_propio().post("/v1/sesion/iniciar", json={"correo": persona.correo, "contrasena": "adivinando"})
        assert r.status_code == 401
    # Con la contraseña BUENA y desde otra IP: igual se espera.
    r = _cliente_propio().post("/v1/sesion/iniciar", json={"correo": persona.correo, "contrasena": CONTRASENA})
    assert r.status_code == 429 and r.json()["codigo"] == "demasiados_intentos"
    assert int(r.headers["retry-after"]) > 0


def test_limite_por_ip_del_inicio_de_sesion():
    c = _cliente_propio()
    codigos = [c.post("/v1/sesion/iniciar", json={"correo": f"{n}@pruebas.mx", "contrasena": "x"}).status_code for n in range(11)]
    assert codigos[:10] == [401] * 10 and codigos[10] == 429


def test_recuperar_responde_igual_exista_o_no():
    c = _cliente_propio()
    persona = crear_cuenta()
    assert c.post("/v1/sesion/recuperar", json={"correo": persona.correo}).status_code == 202
    assert c.post("/v1/sesion/recuperar", json={"correo": "nadie@pruebas.mx"}).status_code == 202


def test_la_contrasena_nueva_cierra_las_sesiones_viejas():
    persona = crear_cuenta()
    c = _cliente_propio()
    assert _pedir(c, persona.token).status_code == 200
    assert cambiar_contrasena(persona.correo, "otra frase distinta 2026") == 0
    r = _pedir(c, persona.token)
    assert r.status_code == 401 and r.json()["codigo"] == "sesion_cerrada"
    assert c.post("/v1/sesion/iniciar", json={"correo": persona.correo, "contrasena": CONTRASENA}).status_code == 401
    assert c.post("/v1/sesion/iniciar", json={"correo": persona.correo, "contrasena": "otra frase distinta 2026"}).status_code == 200


def test_quitar_permisos_invalida_el_token_que_los_traia():
    """Los permisos viajan DENTRO del JWT: al quitarlos, el token viejo tiene que dejar de servir."""
    jefa = crear_cuenta("administrar:catalogo")
    c = _cliente_propio()
    assert c.post("/v1/admin/asociaciones", headers=jefa.headers, json={"nombre": "Antes", "categoria": "Pruebas"}).status_code == 201
    assert dar_permisos(jefa.correo, ["ninguno"]) == 0
    assert c.post("/v1/admin/asociaciones", headers=jefa.headers, json={"nombre": "Después", "categoria": "Pruebas"}).status_code == 401
    nuevo = c.post("/v1/sesion/iniciar", json={"correo": jefa.correo, "contrasena": CONTRASENA}).json()["access_token"]
    r = c.post("/v1/admin/asociaciones", headers={"Authorization": f"Bearer {nuevo}"}, json={"nombre": "Después", "categoria": "Pruebas"})
    assert r.status_code == 403


def test_herramienta_de_permisos_rechaza_permisos_inventados():
    persona = crear_cuenta()
    assert dar_permisos(persona.correo, ["superusuario"]) == 2
    assert dar_permisos("nadie@pruebas.mx", ["todos"]) == 1


# ─── Verificar el token: lo que un atacante intentaría ───────────────────────

def test_sin_token_es_401_y_dice_como_autenticarse(cliente):
    r = _pedir(cliente, None)
    assert r.status_code == 401
    assert r.json()["codigo"] == "falta_token"
    assert r.headers["www-authenticate"].startswith("Bearer")


@pytest.mark.parametrize("basura", ["", "abc", "a.b", "a.b.c", "x" * 5000])
def test_token_mal_formado(cliente, basura):
    assert _pedir(cliente, basura).status_code == 401


def test_alg_none_se_rechaza(cliente, familia):
    # El ataque clásico: quitar la firma y decir "alg": "none".
    sin_firma = jwt.encode(claims_de(familia.sub, ("administrar:catalogo",)), key=None, algorithm="none")
    assert _pedir(cliente, sin_firma).status_code == 401


def test_otro_algoritmo_con_el_mismo_secreto_se_rechaza(cliente, familia):
    # Solo HS256: ni siquiera HS512 con el secreto correcto (el algoritmo no lo elige quien llama).
    assert _pedir(cliente, firmar(claims_de(familia.sub), algoritmo="HS512")).status_code == 401


def test_firmado_con_otro_secreto_se_rechaza(cliente, familia):
    falsificado = firmar(claims_de(familia.sub, ("administrar:catalogo",)), secreto=OTRO_SECRETO)
    assert _pedir(cliente, falsificado).status_code == 401


def test_contenido_alterado_rompe_la_firma(cliente, familia):
    """La demostración de la guía (§5.4): cambiar un claim sin volver a firmar no sirve."""
    encabezado, contenido, firma = familia.token.split(".")
    alterado = jwt.utils.base64url_decode(contenido).replace(b'"permisos":[]', b'"permisos":["administrar:catalogo"]')
    assert alterado != jwt.utils.base64url_decode(contenido)
    otro = jwt.utils.base64url_encode(alterado).decode()
    r = _pedir(cliente, f"{encabezado}.{otro}.{firma}")
    assert r.status_code == 401 and r.json()["codigo"] == "token_invalido"


def test_token_vencido(cliente, familia):
    ahora = int(time.time())
    r = _pedir(cliente, firmar(claims_de(familia.sub, iat=ahora - 7200, exp=ahora - 3600)))
    assert r.status_code == 401 and r.json()["codigo"] == "token_expirado"


@pytest.mark.parametrize("cambio", [{"aud": "otra-app"}, {"iss": "otro-sistema"}])
def test_audiencia_o_emisor_ajenos(cliente, familia, cambio):
    assert _pedir(cliente, firmar(claims_de(familia.sub, **cambio))).status_code == 401


@pytest.mark.parametrize("falta", ["exp", "iat", "jti", "sub", "aud"])
def test_faltan_claims_obligatorios(cliente, familia, falta):
    claims = claims_de(familia.sub)
    del claims[falta]
    assert _pedir(cliente, firmar(claims)).status_code == 401


@pytest.mark.parametrize("cambio", [{"permisos": ["superusuario"]}, {"permisos": "administrar:catalogo"}, {"rol": "root"}, {"sub": "no-es-un-uuid"}])
def test_claims_raros_aunque_esten_bien_firmados(cliente, familia, cambio):
    """Aunque alguien consiguiera firmar, la forma de cada claim se revisa (defensa en profundidad)."""
    assert _pedir(cliente, firmar(claims_de(familia.sub, **cambio))).status_code == 401


def test_token_de_una_cuenta_que_no_existe(cliente):
    assert _pedir(cliente, firmar(claims_de(str(uuid.uuid4())))).status_code == 401


def test_cerrar_sesion_invalida_el_token_de_inmediato(cliente, familia):
    assert cliente.post("/v1/sesion/cerrar", headers=familia.headers).status_code == 204
    r = _pedir(cliente, familia.token)
    assert r.status_code == 401 and r.json()["codigo"] == "sesion_cerrada"


def test_los_permisos_vienen_del_token_no_del_cuerpo(cliente, familia):
    r = cliente.patch("/v1/yo", headers=familia.headers, json={"es_admin": True})
    assert r.status_code == 422                              # mass assignment: campo no permitido
    assert cliente.get("/v1/yo", headers=familia.headers).json()["es_admin"] is False


# ─── Refresh tokens: renovar sin volver a pedir la contraseña ───────────────

def _entrar(c, persona) -> dict:
    r = c.post("/v1/sesion/iniciar", json={"correo": persona.correo, "contrasena": CONTRASENA})
    assert r.status_code == 200, r.text
    return r.json()


def test_iniciar_sesion_da_un_jwt_corto_y_un_refresh_token():
    t = _entrar(_cliente_propio(), crear_cuenta())
    assert t["expira_en"] == 15 * 60 and t["refresh_expira_en"] == 30 * 24 * 3600
    assert t["refresh_token"].count(".") == 0                   # no es un JWT: es un número al azar
    with conectar_como_dueno() as db:
        guardado = db.execute("SELECT huella FROM sesiones ORDER BY creada_en DESC LIMIT 1").fetchone()[0]
    assert guardado != t["refresh_token"] and len(guardado) == 64   # solo la huella SHA-256


def test_renovar_da_tokens_nuevos_y_el_viejo_ya_no_sirve():
    c = _cliente_propio()
    t = _entrar(c, crear_cuenta())
    r = c.post("/v1/sesion/renovar", json={"refresh_token": t["refresh_token"]})
    assert r.status_code == 200
    nuevo = r.json()
    assert nuevo["refresh_token"] != t["refresh_token"] and nuevo["access_token"] != t["access_token"]
    assert _pedir(c, nuevo["access_token"]).status_code == 200


def test_reusar_un_refresh_token_cierra_toda_la_sesion():
    """Si alguien robó el refresh token y la app ya lo había cambiado (o al revés), se corta todo."""
    c = _cliente_propio()
    t = _entrar(c, crear_cuenta())
    segundo = c.post("/v1/sesion/renovar", json={"refresh_token": t["refresh_token"]}).json()
    reuso = c.post("/v1/sesion/renovar", json={"refresh_token": t["refresh_token"]})
    assert reuso.status_code == 401 and reuso.json()["codigo"] == "sesion_cerrada"
    # El que sí era bueno también deja de servir: hay que volver a entrar con contraseña.
    r = c.post("/v1/sesion/renovar", json={"refresh_token": segundo["refresh_token"]})
    assert r.status_code == 401 and r.json()["codigo"] == "sesion_cerrada"


def test_cerrar_sesion_revoca_tambien_el_refresh_token():
    c = _cliente_propio()
    t = _entrar(c, crear_cuenta())
    r = c.post("/v1/sesion/cerrar", headers={"Authorization": f"Bearer {t['access_token']}"},
               json={"refresh_token": t["refresh_token"]})
    assert r.status_code == 204
    assert c.post("/v1/sesion/renovar", json={"refresh_token": t["refresh_token"]}).status_code == 401


def test_no_se_cierra_la_sesion_de_otra_cuenta_con_su_refresh_token():
    c = _cliente_propio()
    ana, beto = _entrar(c, crear_cuenta()), _entrar(c, crear_cuenta())
    c.post("/v1/sesion/cerrar", headers={"Authorization": f"Bearer {ana['access_token']}"}, json={"refresh_token": beto["refresh_token"]})
    assert c.post("/v1/sesion/renovar", json={"refresh_token": beto["refresh_token"]}).status_code == 200


def test_cambiar_la_contrasena_corta_los_refresh_tokens():
    c = _cliente_propio()
    persona = crear_cuenta()
    t = _entrar(c, persona)
    assert cambiar_contrasena(persona.correo, "otra frase distinta 2026") == 0
    assert c.post("/v1/sesion/renovar", json={"refresh_token": t["refresh_token"]}).status_code == 401


@pytest.mark.parametrize("basura", ["corto", "x" * 300, "con espacios y signos!!", "a.b.c" * 10])
def test_refresh_token_basura(basura):
    r = _cliente_propio().post("/v1/sesion/renovar", json={"refresh_token": basura})
    assert r.status_code in (401, 422)


def test_refresh_token_vencido():
    c = _cliente_propio()
    t = _entrar(c, crear_cuenta())
    with conectar_como_dueno() as db:
        db.execute("UPDATE sesiones SET creada_en = now() - interval '40 days', expira_en = now() - interval '1 day' "
                   "WHERE huella = encode(sha256(%s::bytea), 'hex')", (t["refresh_token"],))
    r = c.post("/v1/sesion/renovar", json={"refresh_token": t["refresh_token"]})
    assert r.status_code == 401 and r.json()["codigo"] == "token_expirado"
