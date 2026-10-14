"""Autorización: ¿puedes hacer ESTO con ESTO, AHORA? Control de acceso basado en ATRIBUTOS (ABAC).

En RBAC la pregunta es solo "¿qué rol tienes?". Aquí casi nada se decide así: que una familia
pueda cancelar depende de que la inscripción sea SUYA (atributo del recurso), de que la
actividad no haya empezado (atributo del entorno: la hora) y de quién es ella (atributo del
sujeto). Cada regla vive aquí, con nombre, en un solo lugar: se pueden leer, probar y auditar
juntas. Las rutas no deciden; preguntan.

Atributos que se usan:
  · Sujeto:   cuenta_id, permisos que da Auth0.
  · Recurso:  dueño (cuenta_id), fecha y cupo de la actividad, edad mínima, autor del
              testimonio, asistencia registrada, cierre de la campaña, lo que falta de la meta.
  · Entorno:  la hora actual del servidor (nunca la del teléfono, que se puede cambiar).

Cada política devuelve una Decision. `exigir()` la convierte en error si no se permite.
Lo que es de otra cuenta responde 404, no 403: así no se confirma ni que existe (OWASP API1).
"""

from dataclasses import dataclass, field
from datetime import date, datetime
from typing import Any, Callable

from . import identidad
from .errores import ApiError
from .identidad import Sujeto


@dataclass(frozen=True)
class Decision:
    permitido: bool
    status: int = 200
    codigo: str = "ok"
    mensaje: str = ""
    datos: dict[str, Any] = field(default_factory=dict)


PERMITIDO = Decision(True)


def _niega(status: int, codigo: str, mensaje: str, **datos: Any) -> Decision:
    return Decision(False, status, codigo, mensaje, datos)


def exigir(decision: Decision) -> None:
    if not decision.permitido:
        raise ApiError(decision.status, decision.mensaje, decision.codigo, decision.datos or None)


# El catálogo de políticas, para la guía y para las pruebas: nombre → descripción.
CATALOGO: dict[str, str] = {}


def politica(nombre: str, descripcion: str) -> Callable:
    def registrar(funcion: Callable) -> Callable:
        CATALOGO[nombre] = descripcion
        funcion.nombre_politica = nombre
        return funcion

    return registrar


# ─── Propiedad (de quién es) ────────────────────────────────────────────────

@politica("recurso.propio", "Solo la cuenta dueña ve o cambia lo suyo (BOLA/IDOR, OWASP API1).")
def es_propio(sujeto: Sujeto, cuenta_del_recurso: Any) -> Decision:
    if cuenta_del_recurso != sujeto.cuenta_id:
        return _niega(404, "no_encontrado", "Eso no existe o no es tuyo.")
    return PERMITIDO


# ─── Inscripciones ──────────────────────────────────────────────────────────

@dataclass(frozen=True)
class Integrante:
    nombre: str
    edad: int | None


@politica(
    "inscripcion.guardar",
    "Inscribirse o modificar: la actividad no ha empezado, tiene cupo para todos (contando los "
    "lugares que la familia ya tenía), cada persona con edad conocida cumple la edad mínima y, si "
    "van menores, la persona titular dio su consentimiento.",
)
def puede_guardar_inscripcion(
    actividad_inicia: datetime,
    ahora: datetime,
    tiene_cupo: bool,
    lugares_libres: int,
    lugares_propios: int,
    personas: int,
    edad_minima: int | None,
    integrantes: list[Integrante],
    consentimiento: bool,
) -> Decision:
    if ahora >= actividad_inicia:
        return _niega(409, "actividad_iniciada", "Esta actividad ya empezó o ya pasó.")
    if edad_minima is not None:
        menores = [i.nombre for i in integrantes if i.edad is not None and i.edad < edad_minima]
        if menores:
            return _niega(
                422, "edad_minima",
                f"La edad mínima para esta actividad es de {edad_minima} años.",
                edad_minima=edad_minima, personas=menores,
            )
    if any(i.edad is not None and i.edad < 18 for i in integrantes) and not consentimiento:
        return _niega(422, "falta_consentimiento", "Para inscribir a menores, acepta ser responsable de ellos.")
    if tiene_cupo and personas > lugares_libres + lugares_propios:
        return _niega(
            409, "sin_lugares", "Ya no hay lugares suficientes para todos.",
            lugares_disponibles=max(lugares_libres + lugares_propios, 0), personas=personas,
        )
    return PERMITIDO


@politica("inscripcion.cancelar", "Cancelar: la inscripción es de la cuenta y la actividad no ha empezado.")
def puede_cancelar(sujeto: Sujeto, cuenta_de_inscripcion: Any, actividad_inicia: datetime, ahora: datetime) -> Decision:
    propio = es_propio(sujeto, cuenta_de_inscripcion)
    if not propio.permitido:
        return propio
    if ahora >= actividad_inicia:
        return _niega(409, "actividad_iniciada", "Ya no se puede cancelar: la actividad ya empezó.")
    return PERMITIDO


# ─── Participación: testimonios y encuestas ─────────────────────────────────

@politica(
    "testimonio.publicar",
    "Publicar testimonio: solo de una asistencia registrada A NOMBRE de la cuenta (lo registra la "
    "asociación, no la familia) y de una actividad que ya empezó.",
)
def puede_publicar_testimonio(sujeto: Sujeto, cuenta_de_asistencia: Any, actividad_inicia: datetime, ahora: datetime) -> Decision:
    propio = es_propio(sujeto, cuenta_de_asistencia)
    if not propio.permitido:
        return propio
    if ahora < actividad_inicia:
        return _niega(409, "actividad_no_realizada", "Podrás contar tu experiencia cuando pase la actividad.")
    return PERMITIDO


@politica("testimonio.eliminar", "Eliminar testimonio: su autora o autor, o quien tenga 'moderar:contenido'.")
def puede_eliminar_testimonio(sujeto: Sujeto, autor: Any) -> Decision:
    if autor == sujeto.cuenta_id or sujeto.puede(identidad.MODERAR_CONTENIDO):
        return PERMITIDO
    return _niega(404, "no_encontrado", "Ese testimonio no existe o no es tuyo.")


@politica("encuesta.previa", "Encuesta de antes: la familia está inscrita y la actividad no ha empezado.")
def puede_contestar_previa(inscrita: bool, actividad_inicia: datetime, ahora: datetime) -> Decision:
    if not inscrita:
        return _niega(409, "sin_inscripcion", "Primero inscríbete a la actividad.")
    if ahora >= actividad_inicia:
        return _niega(409, "actividad_iniciada", "La encuesta de antes ya cerró: la actividad empezó.")
    return PERMITIDO


@politica("encuesta.final", "Encuesta de después: de una asistencia registrada a nombre de la cuenta.")
def puede_contestar_final(sujeto: Sujeto, cuenta_de_asistencia: Any) -> Decision:
    return es_propio(sujeto, cuenta_de_asistencia)


@politica("encuestas.ver_todas", "Ver las respuestas de todas las familias: solo con 'moderar:contenido'.")
def puede_ver_todas_las_encuestas(sujeto: Sujeto) -> Decision:
    if sujeto.puede(identidad.MODERAR_CONTENIDO):
        return PERMITIDO
    return _niega(403, "sin_permiso", "Tu cuenta no tiene permiso para ver las respuestas de otras familias.")


# ─── Campañas ───────────────────────────────────────────────────────────────

@politica(
    "aportacion.crear",
    "Apartar artículos de una campaña: la campaña no ha cerrado y no se aparta más de lo que falta "
    "(nadie puede 'comprometer' 10,000 cuentos para inflar la barra).",
)
def puede_aportar(cierra_en: date | None, hoy: date, cantidad: int, faltan: int) -> Decision:
    if cierra_en is not None and hoy > cierra_en:
        return _niega(409, "campana_cerrada", "Esta campaña ya cerró.")
    if faltan <= 0:
        return _niega(409, "meta_completa", "Esta meta ya se completó. ¡Gracias!", faltan=0)
    if cantidad > faltan:
        return _niega(409, "ya_no_alcanza", "Otras familias ya apartaron una parte. Ajusta la cantidad.", faltan=faltan)
    return PERMITIDO


# ─── Fotos ──────────────────────────────────────────────────────────────────

@politica("foto.perfil", "La foto de perfil solo la ve su dueña o dueño.")
def puede_ver_foto_perfil(sujeto: Sujeto, cuenta_de_la_foto: Any) -> Decision:
    return es_propio(sujeto, cuenta_de_la_foto)
