"""Campañas: no apartar más de lo que falta, campañas cerradas e idempotencia de reintentos."""

import uuid
from datetime import date, timedelta


def _campana(cliente, admin, meta=10, **extra):
    r = cliente.post("/v1/admin/campanas", headers=admin.headers, json={
        "titulo": f"Campaña {uuid.uuid4().hex[:6]}", "articulos": [{"nombre": "Cuentos", "meta": meta}], **extra,
    })
    assert r.status_code == 201, r.text
    return r.json()


def _apartar(cliente, persona, articulo_id, cantidad, id_=None):
    return cliente.post(f"/v1/campanas/articulos/{articulo_id}/aportaciones", headers=persona.headers,
                        json={"id": str(id_ or uuid.uuid4()), "cantidad": cantidad})


def test_apartar_suma_a_la_meta(cliente, admin, familia):
    articulo = _campana(cliente, admin)["articulos"][0]
    r = _apartar(cliente, familia, articulo["id"], 4)
    assert r.status_code == 200, r.text
    assert r.json()["articulo"]["apartados"] == 4


def test_no_se_aparta_mas_de_lo_que_falta(cliente, admin, familia, otra_familia):
    articulo = _campana(cliente, admin, meta=5)["articulos"][0]
    assert _apartar(cliente, familia, articulo["id"], 4).status_code == 200
    r = _apartar(cliente, otra_familia, articulo["id"], 3)
    assert r.status_code == 409 and r.json()["codigo"] == "ya_no_alcanza"
    assert r.json()["datos"]["faltan"] == 1


def test_reintento_con_el_mismo_id_no_cuenta_doble(cliente, admin, familia):
    articulo = _campana(cliente, admin)["articulos"][0]
    mismo = uuid.uuid4()
    for _ in range(3):
        r = _apartar(cliente, familia, articulo["id"], 2, mismo)
        assert r.status_code == 200
    assert r.json()["articulo"]["apartados"] == 2


def test_el_id_de_otra_familia_no_se_reusa(cliente, admin, familia, otra_familia):
    articulo = _campana(cliente, admin)["articulos"][0]
    mismo = uuid.uuid4()
    _apartar(cliente, familia, articulo["id"], 1, mismo)
    assert _apartar(cliente, otra_familia, articulo["id"], 1, mismo).status_code == 409


def test_campana_cerrada(cliente, admin, familia):
    articulo = _campana(cliente, admin, cierra_en=(date.today() - timedelta(days=1)).isoformat())["articulos"][0]
    r = _apartar(cliente, familia, articulo["id"], 1)
    assert r.status_code == 409 and r.json()["codigo"] == "campana_cerrada"


def test_cantidades_absurdas(cliente, admin, familia):
    articulo = _campana(cliente, admin)["articulos"][0]
    assert _apartar(cliente, familia, articulo["id"], 0).status_code == 422
    assert _apartar(cliente, familia, articulo["id"], 10_000).status_code == 422
