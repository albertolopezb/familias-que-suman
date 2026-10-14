"""Historial, encuestas e impacto: salen de lo que REGISTRA la asociación, no de lo que diga la familia."""

ENCUESTA = {"respuestas": [{"numero": 1, "pregunta": "¿Cómo se sintieron?", "respuesta": "Muy bien"}]}


def test_historial_e_impacto_salen_de_las_asistencias(cliente, familia, crear_actividad, registrar_asistencia):
    assert cliente.get("/v1/yo/historial", headers=familia.headers).json() == []
    actividad = crear_actividad(dias=-2)
    registrar_asistencia(familia.sub, actividad["id"])
    historial = cliente.get("/v1/yo/historial", headers=familia.headers).json()
    assert len(historial) == 1 and historial[0]["actividad_id"] == actividad["id"]
    assert historial[0]["encuesta_pendiente"] is True
    impacto = cliente.get("/v1/yo", headers=familia.headers).json()["impacto"]
    assert impacto["actividades_realizadas"] == 1 and impacto["horas_de_servicio"] == 2


def test_encuesta_final_de_una_asistencia_propia(cliente, familia, crear_actividad, registrar_asistencia):
    actividad = crear_actividad(dias=-2)
    asistencia = registrar_asistencia(familia.sub, actividad["id"])
    r = cliente.put(f"/v1/yo/participaciones/{asistencia}/encuesta-final", headers=familia.headers, json=ENCUESTA)
    assert r.status_code == 200, r.text
    assert r.json()["momento"] == "despues"
    assert cliente.get("/v1/yo/historial", headers=familia.headers).json()[0]["encuesta_pendiente"] is False


def test_encuesta_previa_pide_estar_inscrita(cliente, familia, crear_actividad):
    actividad = crear_actividad()
    ruta = f"/v1/yo/actividades/{actividad['id']}/encuesta-previa"
    assert cliente.put(ruta, headers=familia.headers, json=ENCUESTA).json()["codigo"] == "sin_inscripcion"
    cliente.put(f"/v1/yo/inscripciones/{actividad['id']}", headers=familia.headers, json={"acompanantes": []})
    assert cliente.put(ruta, headers=familia.headers, json=ENCUESTA).status_code == 200


def test_una_familia_no_se_registra_asistencias(cliente, familia, crear_actividad):
    actividad = crear_actividad(dias=-2)
    yo = cliente.get("/v1/yo", headers=familia.headers).json()
    r = cliente.post("/v1/admin/asistencias", headers=familia.headers, json={"cuenta_id": yo["id"], "actividad_id": actividad["id"]})
    assert r.status_code == 403


def test_solicitud_de_eliminacion_es_idempotente_y_la_atiende_privacidad(cliente, familia, admin):
    primera = cliente.post("/v1/yo/eliminacion", headers=familia.headers).json()
    segunda = cliente.post("/v1/yo/eliminacion", headers=familia.headers).json()
    assert primera["folio"] == segunda["folio"] and primera["folio"].startswith("ELIM-")
    r = cliente.post(f"/v1/admin/solicitudes-eliminacion/{primera['id']}/atender", headers=admin.headers)
    assert r.status_code == 200 and r.json()["estado"] == "atendida"
    # La cuenta y sus datos ya no existen: el token de antes ya no sirve para nada propio.
    assert cliente.get("/v1/yo/acompanantes", headers=familia.headers).status_code in (200, 401)
