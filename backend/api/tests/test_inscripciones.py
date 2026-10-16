"""Inscripciones: cupo, edad mínima, consentimiento, modificar sin perder lugares, cancelar,
y la condición de carrera (TOCTOU) con varias familias a la vez."""

from concurrent.futures import ThreadPoolExecutor
from threading import Barrier

from fastapi.testclient import TestClient

from app.main import app


def _inscribir(cliente, persona, actividad_id, acompanantes=(), consentimiento=False):
    return cliente.put(
        f"/v1/yo/inscripciones/{actividad_id}", headers=persona.headers,
        json={"acompanantes": [{"nombre": n, "edad": e} for n, e in acompanantes], "consentimiento_menores": consentimiento},
    )


def test_inscribirse_descuenta_lugares(cliente, familia, crear_actividad):
    actividad = crear_actividad(cupo=10)
    r = _inscribir(cliente, familia, actividad["id"], [("Mateo Pérez", 30), ("Renata Pérez", 40)])
    assert r.status_code == 200, r.text
    assert r.json()["inscripcion"]["personas"] == 3
    assert r.json()["lugares_disponibles"] == 7
    assert cliente.get(f"/v1/actividades/{actividad['id']}").json()["lugares_disponibles"] == 7


def test_sin_lugares_suficientes(cliente, familia, crear_actividad):
    actividad = crear_actividad(cupo=2)
    r = _inscribir(cliente, familia, actividad["id"], [("Uno Pérez", 30), ("Dos Pérez", 30)])
    assert r.status_code == 409
    assert r.json()["codigo"] == "sin_lugares"
    assert r.json()["datos"]["lugares_disponibles"] == 2


def test_modificar_no_pierde_los_lugares_propios(cliente, familia, otra_familia, crear_actividad):
    actividad = crear_actividad(cupo=4)
    assert _inscribir(cliente, familia, actividad["id"], [("Uno Pérez", 30), ("Dos Pérez", 30)]).status_code == 200
    assert _inscribir(cliente, otra_familia, actividad["id"]).status_code == 200      # ya no queda nada libre
    # La primera familia quita a alguien y lo vuelve a poner: sus 3 lugares siguen siendo suyos.
    assert _inscribir(cliente, familia, actividad["id"], [("Uno Pérez", 30)]).status_code == 200
    assert _inscribir(cliente, familia, actividad["id"], [("Uno Pérez", 30), ("Dos Pérez", 30)]).status_code == 200


def test_edad_minima_se_revisa_en_el_servidor(cliente, familia, crear_actividad):
    actividad = crear_actividad(edad_minima=15)
    r = _inscribir(cliente, familia, actividad["id"], [("Niño Pérez", 9)], consentimiento=True)
    assert r.status_code == 422 and r.json()["codigo"] == "edad_minima"


def test_menores_piden_consentimiento(cliente, familia, crear_actividad):
    actividad = crear_actividad()
    r = _inscribir(cliente, familia, actividad["id"], [("Niña Pérez", 8)])
    assert r.status_code == 422 and r.json()["codigo"] == "falta_consentimiento"
    assert _inscribir(cliente, familia, actividad["id"], [("Niña Pérez", 8)], consentimiento=True).status_code == 200


def test_no_se_inscribe_a_algo_que_ya_paso(cliente, familia, crear_actividad):
    actividad = crear_actividad(dias=-1)
    r = _inscribir(cliente, familia, actividad["id"])
    assert r.status_code == 409 and r.json()["codigo"] == "actividad_iniciada"


def test_cancelar_libera_lugares(cliente, familia, crear_actividad):
    actividad = crear_actividad(cupo=5)
    _inscribir(cliente, familia, actividad["id"], [("Uno Pérez", 30)])
    r = cliente.delete(f"/v1/yo/inscripciones/{actividad['id']}", headers=familia.headers)
    assert r.status_code == 200
    assert r.json() == {"liberados": 2, "lugares_antes": 3, "lugares_disponibles": 5}


def test_put_es_idempotente(cliente, familia, crear_actividad):
    actividad = crear_actividad(cupo=10)
    for _ in range(3):
        r = _inscribir(cliente, familia, actividad["id"], [("Uno Pérez", 30)])
    assert r.json()["lugares_disponibles"] == 8                 # 2 personas, no 6


def test_toctou_varias_familias_a_la_vez_no_sobrepasan_el_cupo(familia, nueva_persona, crear_actividad):
    """Ocho familias de 1 persona se inscriben AL MISMO TIEMPO a una actividad con 3 lugares.
    Sin el candado (SELECT … FOR UPDATE) varias verían "hay lugar" a la vez."""
    actividad = crear_actividad(cupo=3)
    familias = [nueva_persona() for _ in range(8)]
    salida = Barrier(len(familias))

    def intento(persona):
        c = TestClient(app)          # un cliente (y un ciclo de eventos) por hilo: de verdad en paralelo
        salida.wait()                # todas arrancan en el mismo instante
        return _inscribir(c, persona, actividad["id"]).status_code

    with ThreadPoolExecutor(max_workers=len(familias)) as hilos:
        codigos = list(hilos.map(intento, familias))
    assert codigos.count(200) == 3
    assert codigos.count(409) == 5
    assert TestClient(app).get(f"/v1/actividades/{actividad['id']}").json()["lugares_disponibles"] == 0


def test_no_se_cancela_a_menos_de_12_horas(cliente, familia, crear_actividad):
    """RF-19 / HU-09: la familia cancela hasta 12 horas antes; después, la asociación ya contaba con ella."""
    actividad = crear_actividad(dias=0.3)                      # empieza en ~7 horas
    assert cliente.put(f"/v1/yo/inscripciones/{actividad['id']}", headers=familia.headers, json={"acompanantes": []}).status_code == 200
    r = cliente.delete(f"/v1/yo/inscripciones/{actividad['id']}", headers=familia.headers)
    assert r.status_code == 409 and r.json()["codigo"] == "fuera_de_plazo"
