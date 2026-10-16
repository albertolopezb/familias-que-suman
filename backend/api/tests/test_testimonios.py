"""RF-12 / HU-11: un testimonio nace EN REVISIÓN y solo se publica cuando la moderación lo aprueba."""

import io

from PIL import Image

from .conftest import conectar_como_dueno


def _png() -> bytes:
    salida = io.BytesIO()
    Image.new("RGB", (40, 30), (20, 120, 200)).save(salida, "PNG")
    return salida.getvalue()


def _publicar(cliente, persona, asistencia, texto="Fue una mañana preciosa ayudando en familia."):
    r = cliente.put(f"/v1/yo/participaciones/{asistencia}/testimonio", headers=persona.headers,
                    data={"experiencia": texto}, files={"foto": ("a.png", _png(), "image/png")})
    assert r.status_code == 200, r.text
    return r.json()


def test_un_testimonio_nuevo_no_es_publico_hasta_aprobarlo(cliente, familia, admin, crear_actividad, registrar_asistencia):
    asistencia = registrar_asistencia(familia.sub, crear_actividad(dias=-3)["id"])
    t = _publicar(cliente, familia, asistencia)
    assert t["estado"] == "pendiente"
    assert all(p["id"] != t["id"] for p in cliente.get("/v1/testimonios").json())
    # Su autora sí lo ve, marcado "en revisión".
    mios = cliente.get("/v1/yo/testimonios", headers=familia.headers).json()
    assert any(m["id"] == t["id"] and m["estado"] == "pendiente" for m in mios)

    r = cliente.patch(f"/v1/admin/testimonios/{t['id']}", headers=admin.headers, json={"estado": "aprobado"})
    assert r.status_code == 200 and r.json()["estado"] == "aprobado"
    assert any(p["id"] == t["id"] for p in cliente.get("/v1/testimonios").json())


def test_la_foto_en_revision_solo_la_ven_su_autora_y_la_moderacion(cliente, familia, otra_familia, admin, crear_actividad, registrar_asistencia):
    asistencia = registrar_asistencia(familia.sub, crear_actividad(dias=-3)["id"])
    foto = _publicar(cliente, familia, asistencia)["foto_url"]
    assert cliente.get(foto).status_code == 404                                  # sin sesión
    assert cliente.get(foto, headers=otra_familia.headers).status_code == 404
    assert cliente.get(foto, headers=familia.headers).status_code == 200
    assert cliente.get(foto, headers=admin.headers).status_code == 200


def test_rechazado_no_se_publica(cliente, familia, admin, crear_actividad, registrar_asistencia):
    asistencia = registrar_asistencia(familia.sub, crear_actividad(dias=-3)["id"])
    t = _publicar(cliente, familia, asistencia)
    assert cliente.patch(f"/v1/admin/testimonios/{t['id']}", headers=admin.headers, json={"estado": "rechazado"}).status_code == 200
    assert all(p["id"] != t["id"] for p in cliente.get("/v1/testimonios").json())
    assert cliente.get("/v1/yo/testimonios", headers=familia.headers).json()[0]["estado"] == "rechazado"


def test_cambiarlo_lo_regresa_a_revision(cliente, familia, admin, crear_actividad, registrar_asistencia):
    asistencia = registrar_asistencia(familia.sub, crear_actividad(dias=-3)["id"])
    t = _publicar(cliente, familia, asistencia)
    cliente.patch(f"/v1/admin/testimonios/{t['id']}", headers=admin.headers, json={"estado": "aprobado"})
    otra = _publicar(cliente, familia, asistencia, "Lo edito: también fue la primera vez de mi hija como voluntaria.")
    assert otra["estado"] == "pendiente"
    assert all(p["id"] != t["id"] for p in cliente.get("/v1/testimonios").json())


def test_solo_la_moderacion_revisa(cliente, familia, nueva_persona, crear_actividad, registrar_asistencia):
    asistencia = registrar_asistencia(familia.sub, crear_actividad(dias=-3)["id"])
    t = _publicar(cliente, familia, asistencia)
    catalogo = nueva_persona("administrar:catalogo")          # admin, pero no de contenido
    for persona in (familia, catalogo):
        assert cliente.get("/v1/admin/testimonios", headers=persona.headers).status_code == 403
        assert cliente.patch(f"/v1/admin/testimonios/{t['id']}", headers=persona.headers, json={"estado": "aprobado"}).status_code == 403
    assert cliente.patch(f"/v1/admin/testimonios/{t['id']}", headers=familia.headers, json={"estado": "publicado"}).status_code in (403, 422)


def test_la_bandeja_y_la_auditoria(cliente, familia, admin, crear_actividad, registrar_asistencia):
    asistencia = registrar_asistencia(familia.sub, crear_actividad(dias=-3)["id"])
    t = _publicar(cliente, familia, asistencia)
    bandeja = cliente.get("/v1/admin/testimonios", headers=admin.headers).json()
    assert any(b["id"] == t["id"] for b in bandeja)
    assert "correo" not in str(bandeja)
    cliente.patch(f"/v1/admin/testimonios/{t['id']}", headers=admin.headers, json={"estado": "aprobado"})
    with conectar_como_dueno() as c:
        quien = c.execute("SELECT actor_id::text FROM auditoria WHERE accion = 'testimonio.aprobado' AND objeto_id = %s", (t["id"],)).fetchone()
    assert quien is not None and quien[0] == admin.sub
