"""Encabezados de seguridad, Host, tamaños, rate limit, errores sin trazas y lo que no se publica
por el túnel (OWASP A02 · A10 · API4 · API8 · API9)."""

import uuid

from fastapi.testclient import TestClient

from app.config import ajustes
from app.main import app

DESDE_EL_TUNEL = (ajustes.ip_tunel, 51000)


def test_encabezados_de_seguridad_en_todas_las_respuestas(cliente):
    for ruta in ["/v1/salud", "/v1/actividades", "/no-existe"]:
        h = cliente.get(ruta).headers
        assert h["strict-transport-security"].startswith("max-age=31536000")
        assert h["x-content-type-options"] == "nosniff"
        assert h["x-frame-options"] == "DENY"
        assert "default-src 'none'" in h["content-security-policy"]
        assert h["referrer-policy"] == "no-referrer"
        assert h["cache-control"] == "no-store"
        assert "server" not in h                       # no se anuncia ni el servidor ni su versión
        assert "x-request-id" in h


def test_host_ajeno_se_rechaza(cliente):
    r = cliente.get("/v1/salud", headers={"Host": "evil.example.com"})
    assert r.status_code == 400 and r.json()["codigo"] == "host_invalido"


def test_cuerpo_demasiado_grande(cliente, familia):
    r = cliente.post("/v1/yo/acompanantes", headers={**familia.headers, "Content-Type": "application/json"},
                     content=b'{"nombre": "' + b"a" * 1_100_000 + b'"}')
    assert r.status_code == 413


def test_errores_sin_trazas_ni_detalles_internos(cliente):
    r = cliente.get("/v1/actividades/no-es-un-uuid")
    assert r.status_code == 422
    texto = r.text.lower()
    for fuga in ["traceback", "sqlalchemy", "psycopg", "select ", "file \"", "uvicorn"]:
        assert fuga not in texto
    assert set(r.json()) >= {"error", "codigo"}


def test_rate_limit_en_un_flujo_publico():
    c = TestClient(app, client=("203.0.113.9", 40000))     # una IP solo para esta prueba
    cuerpo = {"tipo": "CENTRO", "nombre": "Centro de prueba", "descripcion": "Un centro que alguien sugiere.",
              "sugerida_por": "Prueba", "contacto": "prueba@correo.com"}
    codigos = [c.post("/v1/sugerencias", json=cuerpo).status_code for _ in range(7)]
    assert codigos[:5] == [201] * 5
    assert codigos[5] == 429
    ultimo = c.post("/v1/sugerencias", json=cuerpo)
    assert ultimo.headers.get("retry-after")


def test_la_ip_real_solo_se_cree_si_viene_del_tunel():
    """Desde fuera, poner CF-Connecting-IP no sirve para cambiar de IP y saltarse el límite."""
    falso = TestClient(app, client=("203.0.113.50", 40000))
    cuerpo = {"tipo": "CENTRO", "nombre": "Centro", "descripcion": "Otra sugerencia de prueba.", "sugerida_por": "Prueba", "contacto": "p@correo.com"}
    codigos = [falso.post("/v1/sugerencias", json=cuerpo, headers={"CF-Connecting-IP": f"10.9.9.{n}"}).status_code for n in range(7)]
    assert 429 in codigos


def test_inventario_solo_desde_la_red_interna(cliente):
    assert cliente.get("/openapi.json").status_code == 200
    por_tunel = TestClient(app, client=DESDE_EL_TUNEL)
    for ruta in ["/openapi.json", "/docs", "/v1/salud/dependencias"]:
        assert por_tunel.get(ruta, headers={"CF-Connecting-IP": "198.51.100.1"}).status_code == 404


def test_sin_cors(cliente):
    r = cliente.options("/v1/actividades", headers={"Origin": "https://evil.example", "Access-Control-Request-Method": "GET"})
    assert "access-control-allow-origin" not in r.headers


def test_assetlinks_para_app_links(cliente):
    r = cliente.get("/.well-known/assetlinks.json")
    assert r.status_code == 200
    destino = r.json()[0]["target"]
    assert destino["package_name"] == "mx.tec.familiasquesuman"
    assert destino["namespace"] == "android_app"


def test_enlace_de_actividad_inexistente(cliente):
    assert cliente.get(f"/v1/actividades/{uuid.uuid4()}/enlace").status_code == 404
    assert cliente.get(f"/a/{uuid.uuid4()}").status_code == 404


def test_limite_global_por_ip(monkeypatch):
    """Todas las rutas (aunque no tengan límite propio) se frenan por IP. Esta prueba se agregó
    porque, al verificar, el middleware de slowapi NO aplicaba el límite global con FastAPI 0.143."""
    from limits import parse

    from app import limites

    monkeypatch.setattr(limites, "regla_global", parse("5/minute"))
    c = TestClient(app, client=("203.0.113.77", 40000))
    codigos = [c.get("/v1/ciudades").status_code for _ in range(7)]
    assert codigos[:5] == [200] * 5 and codigos[5:] == [429, 429]
    r = c.get("/v1/ciudades")
    assert r.json()["codigo"] == "demasiadas_solicitudes" and int(r.headers["retry-after"]) >= 1
    # Otra IP no se ve afectada, y la salud nunca se frena (la usa Docker).
    assert TestClient(app, client=("203.0.113.78", 40000)).get("/v1/ciudades").status_code == 200
    assert c.get("/v1/salud").status_code == 200
