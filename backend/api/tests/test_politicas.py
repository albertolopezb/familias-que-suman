"""Las reglas ABAC de politicas.polar, probadas directo contra Oso (sin HTTP), como los casos de
Alice, Bob y Charlie de la guía de clase (s23, §5.3): mismos atributos → misma decisión."""

import uuid
from datetime import UTC, date, datetime, timedelta

import pytest

from app import politicas
from app.identidad import PERMISOS_ADMIN, Sujeto

AHORA = datetime.now(UTC)
MANANA = AHORA + timedelta(days=1)
AYER = AHORA - timedelta(days=1)


def sujeto(*permisos: str) -> Sujeto:
    return Sujeto(uuid.uuid4(), "x@pruebas.mx", "Familia", "admin" if permisos else "familia", frozenset(permisos), "h", 0)


ANA, BETO = sujeto(), sujeto()
MODERACION = sujeto("moderar:contenido")


def _inscripcion(**cambios):
    base = dict(
        sujeto=ANA, actividad_inicia=MANANA, ahora=AHORA, tiene_cupo=True, lugares_libres=3, lugares_propios=0,
        personas=2, edad_minima=7, integrantes=[politicas.Integrante("Ana", 38), politicas.Integrante("Mateo", 9)],
        consentimiento=True,
    )
    base.update(cambios)
    return politicas.puede_guardar_inscripcion(**base)


def test_cada_politica_del_catalogo_tiene_descripcion():
    assert set(politicas.CATALOGO) >= {
        "funcion.permiso", "recurso.propio", "inscripcion.guardar", "inscripcion.cancelar", "testimonio.publicar",
        "testimonio.eliminar", "encuesta.previa", "encuesta.final", "encuestas.ver_todas", "aportacion.crear", "foto.perfil",
    }
    assert all(len(d) > 20 for d in politicas.CATALOGO.values())


@pytest.mark.parametrize("permiso", sorted(PERMISOS_ADMIN))
def test_las_funciones_de_admin_piden_su_permiso_y_solo_ese(permiso):
    assert politicas.puede_usar_funcion(sujeto(permiso), permiso).permitido
    otros = PERMISOS_ADMIN - {permiso}
    assert not politicas.puede_usar_funcion(sujeto(*otros), permiso).permitido
    negado = politicas.puede_usar_funcion(ANA, permiso)
    assert (negado.status, negado.codigo) == (403, "sin_permiso")


def test_lo_ajeno_no_existe():
    assert politicas.es_propio(ANA, ANA.cuenta_id).permitido
    negado = politicas.es_propio(ANA, BETO.cuenta_id)
    assert (negado.status, negado.codigo) == (404, "no_encontrado")
    assert not politicas.es_propio(ANA, None).permitido
    assert not politicas.puede_ver_foto_perfil(BETO, ANA.cuenta_id).permitido


def test_inscripcion_valida():
    assert _inscripcion().permitido


@pytest.mark.parametrize("cambios,codigo,status", [
    ({"ahora": MANANA + timedelta(minutes=1)}, "actividad_iniciada", 409),
    ({"integrantes": [politicas.Integrante("Ana", 38), politicas.Integrante("Bebé", 2)]}, "edad_minima", 422),
    ({"consentimiento": False}, "falta_consentimiento", 422),
    ({"personas": 4, "lugares_libres": 3}, "sin_lugares", 409),
])
def test_inscripcion_negada_con_su_motivo(cambios, codigo, status):
    decision = _inscripcion(**cambios)
    assert not decision.permitido and (decision.codigo, decision.status) == (codigo, status)


def test_el_motivo_mas_importante_va_primero():
    # Ya empezó Y hay un bebé Y no hay lugares: lo primero que se dice es que ya empezó.
    decision = _inscripcion(ahora=MANANA + timedelta(hours=1), personas=9,
                            integrantes=[politicas.Integrante("Bebé", 1)], consentimiento=False)
    assert decision.codigo == "actividad_iniciada"


def test_edad_minima_dice_quien_no_la_cumple():
    decision = _inscripcion(integrantes=[politicas.Integrante("Ana", 38), politicas.Integrante("Bebé", 2), politicas.Integrante("Sin edad", None)])
    assert decision.datos == {"edad_minima": 7, "personas": ["Bebé"]}


def test_sin_edad_minima_ni_cupo_pasa_cualquiera():
    assert _inscripcion(edad_minima=None, tiene_cupo=False, personas=50, lugares_libres=0,
                        integrantes=[politicas.Integrante("Bebé", 1)]).permitido


def test_los_lugares_propios_cuentan_al_modificar():
    assert _inscripcion(personas=4, lugares_libres=1, lugares_propios=3).permitido


def test_cancelar():
    assert politicas.puede_cancelar(ANA, ANA.cuenta_id, MANANA, AHORA).permitido
    assert politicas.puede_cancelar(ANA, BETO.cuenta_id, MANANA, AHORA).codigo == "no_encontrado"
    assert politicas.puede_cancelar(ANA, ANA.cuenta_id, AYER, AHORA).codigo == "actividad_iniciada"


def test_testimonios():
    assert politicas.puede_publicar_testimonio(ANA, ANA.cuenta_id, AYER, AHORA).permitido
    assert politicas.puede_publicar_testimonio(ANA, ANA.cuenta_id, MANANA, AHORA).codigo == "actividad_no_realizada"
    assert politicas.puede_publicar_testimonio(BETO, ANA.cuenta_id, AYER, AHORA).codigo == "no_encontrado"
    assert politicas.puede_eliminar_testimonio(ANA, ANA.cuenta_id).permitido
    assert politicas.puede_eliminar_testimonio(MODERACION, ANA.cuenta_id).permitido
    assert politicas.puede_eliminar_testimonio(BETO, ANA.cuenta_id).status == 404


def test_encuestas():
    assert politicas.puede_contestar_previa(ANA, True, MANANA, AHORA).permitido
    assert politicas.puede_contestar_previa(ANA, False, MANANA, AHORA).codigo == "sin_inscripcion"
    assert politicas.puede_contestar_previa(ANA, True, AYER, AHORA).codigo == "actividad_iniciada"
    assert politicas.puede_contestar_final(ANA, ANA.cuenta_id).permitido
    assert not politicas.puede_contestar_final(BETO, ANA.cuenta_id).permitido
    assert politicas.puede_ver_todas_las_encuestas(MODERACION).permitido
    assert politicas.puede_ver_todas_las_encuestas(ANA).status == 403


def test_aportaciones():
    hoy = date.today()
    assert politicas.puede_aportar(ANA, None, hoy, 2, 5).permitido
    assert politicas.puede_aportar(ANA, hoy, hoy, 2, 5).permitido                      # cierra hoy: todavía se puede
    assert politicas.puede_aportar(ANA, hoy - timedelta(days=1), hoy, 2, 5).codigo == "campana_cerrada"
    assert politicas.puede_aportar(ANA, None, hoy, 1, 0).codigo == "meta_completa"
    negado = politicas.puede_aportar(ANA, None, hoy, 6, 5)
    assert negado.codigo == "ya_no_alcanza" and negado.datos == {"faltan": 5}


def test_lo_que_no_tiene_regla_se_niega():
    """Oso niega por omisión: una acción sin `allow` en el .polar nunca se permite."""
    decision = politicas.decidir(sujeto(*PERMISOS_ADMIN), "borrar_todo", politicas.Recurso("x"))
    assert not decision.permitido and decision.status == 403
