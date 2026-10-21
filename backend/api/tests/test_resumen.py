"""El resumen de la administración: cuenta bien, no deja ver a una familia y no trae datos personales.
Y los centros con ubicación (para que salgan en el mapa)."""

from app import identidad


def _resumen(cliente, persona):
    r = cliente.get("/v1/admin/resumen", headers=persona.headers)
    assert r.status_code == 200, r.text
    return r.json()


def test_el_resumen_cuenta_familias_e_inscripciones(cliente, admin, nueva_persona, crear_actividad):
    antes = _resumen(cliente, admin)
    familia = nueva_persona(nombre="Familia Resumen")
    actividad = crear_actividad(cupo=10)
    r = cliente.put(
        f"/v1/yo/inscripciones/{actividad['id']}", headers=familia.headers,
        json={"acompanantes": [{"nombre": "Lía Pérez", "edad": 30}]},
    )
    assert r.status_code == 200, r.text

    despues = _resumen(cliente, admin)
    assert despues["familias"] == antes["familias"] + 1
    assert despues["administradores"] == antes["administradores"]
    assert despues["actividades_proximas"] == antes["actividades_proximas"] + 1
    assert despues["inscripciones_vigentes"] == antes["inscripciones_vigentes"] + 1
    assert despues["personas_inscritas"] == antes["personas_inscritas"] + 2
    assert set(despues["sugerencias"]) == {"actividad", "campana", "proyecto", "centro"}


def test_el_resumen_son_solo_numeros(cliente, admin):
    for valor in _resumen(cliente, admin).values():
        assert isinstance(valor, int) or all(isinstance(v, int) for v in valor.values())


def test_una_familia_no_ve_el_resumen(cliente, familia):
    assert cliente.get("/v1/admin/resumen", headers=familia.headers).status_code == 403
    assert cliente.get("/v1/admin/resumen").status_code == 401


def test_quien_solo_modera_no_ve_el_resumen(cliente, nueva_persona):
    moderador = nueva_persona(identidad.MODERAR_CONTENIDO)
    assert cliente.get("/v1/admin/resumen", headers=moderador.headers).status_code == 403


def _centro(**extra):
    return {"nombre": "Comedor de prueba", "tipo": "Comedores", "direccion": "Cuautla 208, Monterrey", **extra}


def test_un_centro_guarda_su_ubicacion_y_la_publica(cliente, admin):
    r = cliente.post("/v1/admin/centros", headers=admin.headers, json=_centro(latitud=25.724, longitud=-100.344))
    assert r.status_code == 201, r.text
    centro = r.json()
    assert (centro["latitud"], centro["longitud"]) == (25.724, -100.344)
    publico = next(c for c in cliente.get("/v1/centros").json() if c["id"] == centro["id"])
    assert (publico["latitud"], publico["longitud"]) == (25.724, -100.344)


def test_un_centro_sin_ubicacion_sigue_valiendo(cliente, admin):
    r = cliente.post("/v1/admin/centros", headers=admin.headers, json=_centro())
    assert r.status_code == 201, r.text
    assert r.json()["latitud"] is None


def test_una_ubicacion_imposible_se_rechaza(cliente, admin):
    r = cliente.post("/v1/admin/centros", headers=admin.headers, json=_centro(latitud=125.0, longitud=-100.3))
    assert r.status_code == 422
