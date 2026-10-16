"""RF-16 / RF-22: avisos de Familias que Suman que la app pide cada 15 minutos (sin Firebase)."""

from .conftest import conectar_como_dueno


def _aviso(cliente, admin, **cambios):
    cuerpo = {"titulo": "Urge ropa de invierno", "cuerpo": "El albergue de la colonia Sendero necesita cobijas.", **cambios}
    return cliente.post("/v1/admin/avisos", headers=admin.headers, json=cuerpo)


def test_sin_sesion_no_hay_avisos(cliente):
    assert cliente.get("/v1/avisos").status_code == 401


def test_cada_familia_recibe_los_de_su_ciudad_y_los_generales(cliente, familia, admin):
    with conectar_como_dueno() as c:
        c.execute("UPDATE cuentas SET ciudad = 'Monterrey' WHERE id = %s", (familia.sub,))
    de_mty = _aviso(cliente, admin, ciudad="Monterrey").json()
    de_hmo = _aviso(cliente, admin, ciudad="Hermosillo").json()
    general = _aviso(cliente, admin).json()
    ids = [a["id"] for a in cliente.get("/v1/avisos", headers=familia.headers).json()]
    assert de_mty["id"] in ids and general["id"] in ids and de_hmo["id"] not in ids
    assert ids == sorted(ids)                                     # del más viejo al más nuevo


def test_desde_solo_trae_lo_nuevo(cliente, familia, admin):
    primero = _aviso(cliente, admin).json()
    segundo = _aviso(cliente, admin).json()
    ids = [a["id"] for a in cliente.get(f"/v1/avisos?desde={primero['id']}", headers=familia.headers).json()]
    assert primero["id"] not in ids and segundo["id"] in ids
    assert cliente.get("/v1/avisos?desde=-1", headers=familia.headers).status_code == 422
    assert cliente.get("/v1/avisos?desde=1%20OR%201=1", headers=familia.headers).status_code == 422


def test_solo_el_catalogo_crea_y_borra_avisos(cliente, familia, admin, nueva_persona):
    moderacion = nueva_persona("moderar:contenido")
    for persona in (familia, moderacion):
        assert _aviso(cliente, persona).status_code == 403
    creado = _aviso(cliente, admin).json()
    assert cliente.delete(f"/v1/admin/avisos/{creado['id']}", headers=familia.headers).status_code == 403
    assert cliente.delete(f"/v1/admin/avisos/{creado['id']}", headers=admin.headers).status_code == 204
    with conectar_como_dueno() as c:
        acciones = [f[0] for f in c.execute(
            "SELECT accion FROM auditoria WHERE objeto_id = %s AND actor_id = %s", (str(creado["id"]), admin.sub))]
    assert acciones == ["aviso.crear", "aviso.borrar"]


def test_lo_que_entra_se_valida(cliente, admin):
    assert _aviso(cliente, admin, titulo="x" * 81).status_code == 422
    assert _aviso(cliente, admin, cuerpo="ok\x00").status_code == 422
    assert _aviso(cliente, admin, ciudad="Narnia").status_code == 422
    assert _aviso(cliente, admin, tipo="actividad_nueva").status_code == 422          # campo de más


def test_una_actividad_nueva_genera_su_aviso(cliente, familia, crear_actividad):
    actividad = crear_actividad()
    avisos = cliente.get("/v1/avisos", headers=familia.headers).json()
    assert any(a["tipo"] == "actividad_nueva" and a["actividad_id"] == actividad["id"]
               and a["cuerpo"] == actividad["titulo"] for a in avisos)
