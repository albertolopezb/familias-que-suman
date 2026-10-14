"""Autorización (OWASP A01 · API1 · API3 · API5): cada quien solo ve y cambia lo suyo, y las
funciones de administración piden su permiso."""

import uuid

import pytest


def test_bola_no_se_ven_ni_se_borran_acompanantes_ajenos(cliente, familia, otra_familia):
    r = cliente.post("/v1/yo/acompanantes", headers=familia.headers, json={"nombre": "Mateo", "edad": 9})
    assert r.status_code == 201
    acompanante = r.json()["id"]
    # La otra familia no lo ve en su lista…
    assert all(a["id"] != acompanante for a in cliente.get("/v1/yo/acompanantes", headers=otra_familia.headers).json())
    # …y si adivina el id, para ella "no existe" (404, no 403: ni se confirma que existe).
    r = cliente.delete(f"/v1/yo/acompanantes/{acompanante}", headers=otra_familia.headers)
    assert r.status_code == 404
    assert any(a["id"] == acompanante for a in cliente.get("/v1/yo/acompanantes", headers=familia.headers).json())


def test_bola_testimonio_sobre_asistencia_ajena(cliente, familia, otra_familia, crear_actividad, registrar_asistencia):
    actividad = crear_actividad(dias=-3)
    asistencia = registrar_asistencia(familia.sub, actividad["id"])
    foto = _png()
    r = cliente.put(
        f"/v1/yo/participaciones/{asistencia}/testimonio",
        headers=otra_familia.headers,
        data={"experiencia": "Quiero publicar en la asistencia de otra familia, a ver si se deja."},
        files={"foto": ("a.png", foto, "image/png")},
    )
    assert r.status_code == 404


def test_solo_la_autora_o_la_moderacion_borran_un_testimonio(cliente, familia, otra_familia, admin, crear_actividad, registrar_asistencia):
    actividad = crear_actividad(dias=-3)
    asistencia = registrar_asistencia(familia.sub, actividad["id"])
    r = cliente.put(
        f"/v1/yo/participaciones/{asistencia}/testimonio", headers=familia.headers,
        data={"experiencia": "Fue una mañana preciosa con mis hijos ayudando."}, files={"foto": ("a.png", _png(), "image/png")},
    )
    assert r.status_code == 200, r.text
    testimonio = r.json()["id"]
    assert cliente.delete(f"/v1/testimonios/{testimonio}", headers=otra_familia.headers).status_code == 404
    assert cliente.delete(f"/v1/testimonios/{testimonio}", headers=admin.headers).status_code == 204


@pytest.mark.parametrize("metodo,ruta", [
    ("post", "/v1/admin/asociaciones"),
    ("post", "/v1/admin/actividades"),
    ("post", "/v1/admin/campanas"),
    ("get", "/v1/admin/sugerencias"),
    ("get", "/v1/admin/solicitudes-eliminacion"),
    ("get", "/v1/admin/encuestas"),
])
def test_funciones_de_admin_piden_permiso(cliente, familia, metodo, ruta):
    r = getattr(cliente, metodo)(ruta, headers=familia.headers, **({"json": {}} if metodo == "post" else {}))
    assert r.status_code in (403, 422)
    if r.status_code == 422:
        # Con cuerpo vacío puede fallar la validación primero; con un cuerpo válido, 403.
        r = cliente.post(ruta, headers=familia.headers, json={"nombre": "X" * 5, "categoria": "Y" * 5})
        assert r.status_code == 403


def test_permiso_minimo_quien_registra_asistencias_no_administra_catalogo(cliente, nueva_persona):
    asociacion = nueva_persona("registrar:asistencias")
    r = cliente.post("/v1/admin/asociaciones", headers=asociacion.headers, json={"nombre": "Nueva", "categoria": "Otra"})
    assert r.status_code == 403


def test_mass_assignment_campos_extra_se_rechazan(cliente, familia, crear_actividad):
    actividad = crear_actividad()
    r = cliente.put(f"/v1/yo/inscripciones/{actividad['id']}", headers=familia.headers,
                    json={"acompanantes": [], "personas": 1000, "cuenta_id": str(uuid.uuid4())})
    assert r.status_code == 422


def test_los_testimonios_publicos_no_exponen_correos(cliente, familia, crear_actividad, registrar_asistencia):
    actividad = crear_actividad(dias=-3)
    asistencia = registrar_asistencia(familia.sub, actividad["id"])
    cliente.put(f"/v1/yo/participaciones/{asistencia}/testimonio", headers=familia.headers,
                data={"experiencia": "Un testimonio público que no debe llevar el correo."}, files={"foto": ("a.png", _png(), "image/png")})
    publicos = cliente.get("/v1/testimonios").json()
    assert publicos and all("correo" not in t for t in publicos)
    assert "@" not in str(publicos)


def test_respuestas_de_otras_familias_solo_con_moderacion(cliente, familia, admin):
    assert cliente.get("/v1/admin/encuestas", headers=familia.headers).status_code == 403
    assert cliente.get("/v1/admin/encuestas", headers=admin.headers).status_code == 200


def _png() -> bytes:
    import io

    from PIL import Image

    salida = io.BytesIO()
    Image.new("RGB", (40, 30), (200, 100, 50)).save(salida, "PNG")
    return salida.getvalue()
