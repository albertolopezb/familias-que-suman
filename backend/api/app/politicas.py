"""Autorización: ¿puedes hacer ESTO con ESTO, AHORA? Control de acceso basado en ATRIBUTOS (ABAC).

Como en la guía de clase (s23), la identidad sale del JWT y la decisión la toma Oso evaluando
reglas declarativas escritas en Polar (`politicas.polar`, junto a este archivo). Las rutas no
deciden: arman el recurso con sus atributos y le preguntan a la política.

En RBAC la pregunta es solo "¿qué rol tienes?". Aquí casi nada se decide así: que una familia
pueda cancelar depende de que la inscripción sea SUYA (atributo del recurso), de que la
actividad no haya empezado (atributo del entorno: la hora) y de quién es ella (atributo del
sujeto). Las reglas viven todas juntas en el .polar: se pueden leer, probar y auditar en un solo lugar.

Atributos que se usan:
  · Sujeto:   id de la cuenta, rol y permisos (claims del JWT firmado).
  · Recurso:  dueño, fecha y cupo de la actividad, edad mínima, autor del testimonio,
              asistencia registrada, cierre de la campaña, lo que falta de la meta.
  · Entorno:  la hora actual del servidor (nunca la del teléfono, que se puede cambiar).

Este archivo pone lo que Polar no debe saber: el texto que ve la persona y el código HTTP de
cada negación. Cada política devuelve una Decision; `exigir()` la convierte en error si no se permite.
"""

import threading
from dataclasses import dataclass, field
from datetime import date, datetime
from pathlib import Path
from typing import Any, Callable

from oso import Oso, Variable

from .errores import ApiError
from .identidad import MODERAR_CONTENIDO, Sujeto


@dataclass(frozen=True)
class Decision:
    permitido: bool
    status: int = 200
    codigo: str = "ok"
    mensaje: str = ""
    datos: dict[str, Any] = field(default_factory=dict)


PERMITIDO = Decision(True)


def exigir(decision: Decision) -> None:
    if not decision.permitido:
        raise ApiError(decision.status, decision.mensaje, decision.codigo, decision.datos or None)


# ─── Los recursos, con los atributos que leen las reglas ────────────────────
# Solo números, textos y booleanos: Polar los compara sin sorpresas. Los ids van como texto y
# las horas como segundos (timestamp).

def _id(valor: Any) -> str:
    return str(valor) if valor is not None else ""


def _segundos(momento: datetime) -> float:
    return momento.timestamp()


@dataclass(frozen=True)
class Funcion:
    permiso: str


@dataclass(frozen=True)
class Recurso:
    dueno: str


@dataclass(frozen=True)
class FotoPerfil:
    dueno: str


@dataclass(frozen=True)
class Integrante:
    nombre: str
    edad: int | None


@dataclass(frozen=True)
class Inscripcion:
    inicia: float
    ahora: float
    tiene_cupo: bool
    lugares_libres: int
    lugares_propios: int
    personas: int
    edad_minima: int | None
    integrantes: list[Integrante]
    consentimiento: bool


@dataclass(frozen=True)
class Cancelacion:
    dueno: str
    inicia: float
    ahora: float


@dataclass(frozen=True)
class Participacion:
    dueno: str
    inicia: float
    ahora: float


@dataclass(frozen=True)
class Testimonio:
    autor: str


@dataclass(frozen=True)
class EncuestaPrevia:
    inscrita: bool
    inicia: float
    ahora: float


@dataclass(frozen=True)
class Aportacion:
    cierra_en: int | None      # días (date.toordinal); None = no cierra
    hoy: int
    cantidad: int
    faltan: int


# ─── El motor (Oso) ──────────────────────────────────────────────────────────

_oso = Oso()
for _clase in (Sujeto, Funcion, Recurso, FotoPerfil, Integrante, Inscripcion, Cancelacion, Participacion,
               Testimonio, EncuestaPrevia, Aportacion):
    _oso.register_class(_clase)
_oso.load_files([str(Path(__file__).with_name("politicas.polar"))])
# Las rutas corren en varios hilos a la vez; una consulta a Oso dura microsegundos, así que se
# hacen de una en una (Oso no promete ser seguro entre hilos).
_candado = threading.Lock()


@dataclass(frozen=True)
class Motivo:
    codigo: str
    status: int
    mensaje: str | Callable[[Any], str]
    datos: Callable[[Any], dict] | None = None


@dataclass(frozen=True)
class Politica:
    nombre: str
    descripcion: str
    motivos: tuple[Motivo, ...]          # en orden de importancia: se responde el primero que aplique


# El catálogo de políticas, para la guía y para las pruebas: acción de Polar → política.
POLITICAS: dict[str, Politica] = {
    "usar": Politica(
        "funcion.permiso", "Las funciones de administración piden su permiso (claim `permisos` del JWT).",
        (Motivo("sin_permiso", 403, "Tu cuenta no tiene permiso para hacer esto."),),
    ),
    "ver": Politica(
        "recurso.propio", "Solo la cuenta dueña ve o cambia lo suyo (BOLA/IDOR, OWASP API1).",
        (Motivo("no_encontrado", 404, "Eso no existe o no es tuyo."),),
    ),
    "inscribirse": Politica(
        "inscripcion.guardar",
        "Inscribirse o modificar: la actividad no ha empezado, tiene cupo para todos (contando los "
        "lugares que la familia ya tenía), cada persona con edad conocida cumple la edad mínima y, si "
        "van menores, la persona titular dio su consentimiento.",
        (
            Motivo("actividad_iniciada", 409, "Esta actividad ya empezó o ya pasó."),
            Motivo(
                "edad_minima", 422,
                lambda r: f"La edad mínima para esta actividad es de {r.edad_minima} años.",
                lambda r: {"edad_minima": r.edad_minima,
                           "personas": [i.nombre for i in r.integrantes if i.edad is not None and i.edad < r.edad_minima]},
            ),
            Motivo("falta_consentimiento", 422, "Para inscribir a menores, acepta ser responsable de ellos."),
            Motivo(
                "sin_lugares", 409, "Ya no hay lugares suficientes para todos.",
                lambda r: {"lugares_disponibles": max(r.lugares_libres + r.lugares_propios, 0), "personas": r.personas},
            ),
        ),
    ),
    "cancelar": Politica(
        "inscripcion.cancelar", "Cancelar: la inscripción es de la cuenta y faltan al menos 12 horas para la actividad (RF-19).",
        (
            Motivo("no_encontrado", 404, "Eso no existe o no es tuyo."),
            Motivo("fuera_de_plazo", 409, "Solo se puede cancelar hasta 12 horas antes de la actividad."),
        ),
    ),
    "publicar_testimonio": Politica(
        "testimonio.publicar",
        "Publicar testimonio: solo de una asistencia registrada A NOMBRE de la cuenta (lo registra la "
        "asociación, no la familia) y de una actividad que ya empezó.",
        (
            Motivo("no_encontrado", 404, "Eso no existe o no es tuyo."),
            Motivo("actividad_no_realizada", 409, "Podrás contar tu experiencia cuando pase la actividad."),
        ),
    ),
    "eliminar": Politica(
        "testimonio.eliminar", f"Eliminar testimonio: su autora o autor, o quien tenga '{MODERAR_CONTENIDO}'.",
        (Motivo("no_encontrado", 404, "Ese testimonio no existe o no es tuyo."),),
    ),
    "ver_en_revision": Politica(
        "testimonio.ver_en_revision", f"Un testimonio en revisión (RF-12) solo lo ven su autora y quien tiene '{MODERAR_CONTENIDO}'.",
        (Motivo("no_encontrado", 404, "Esa foto no existe."),),
    ),
    "contestar_previa": Politica(
        "encuesta.previa", "Encuesta de antes: la familia está inscrita y la actividad no ha empezado.",
        (
            Motivo("sin_inscripcion", 409, "Primero inscríbete a la actividad."),
            Motivo("actividad_iniciada", 409, "La encuesta de antes ya cerró: la actividad empezó."),
        ),
    ),
    "contestar_final": Politica(
        "encuesta.final", "Encuesta de después: de una asistencia registrada a nombre de la cuenta.",
        (Motivo("no_encontrado", 404, "Eso no existe o no es tuyo."),),
    ),
    "ver_todas": Politica(
        "encuestas.ver_todas", f"Ver las respuestas de todas las familias: solo con '{MODERAR_CONTENIDO}'.",
        (Motivo("sin_permiso", 403, "Tu cuenta no tiene permiso para ver las respuestas de otras familias."),),
    ),
    "aportar": Politica(
        "aportacion.crear",
        "Apartar artículos de una campaña: la campaña no ha cerrado y no se aparta más de lo que falta "
        "(nadie puede 'comprometer' 10,000 cuentos para inflar la barra).",
        (
            Motivo("campana_cerrada", 409, "Esta campaña ya cerró."),
            Motivo("meta_completa", 409, "Esta meta ya se completó. ¡Gracias!", lambda r: {"faltan": 0}),
            Motivo("ya_no_alcanza", 409, "Otras familias ya apartaron una parte. Ajusta la cantidad.", lambda r: {"faltan": r.faltan}),
        ),
    ),
}
# La foto de perfil usa la misma acción "ver" con su propio tipo de recurso; para el catálogo:
CATALOGO: dict[str, str] = {p.nombre: p.descripcion for p in POLITICAS.values()}
CATALOGO["foto.perfil"] = "La foto de perfil solo la ve su dueña o dueño."


def decidir(sujeto: Sujeto, accion: str, recurso: Any) -> Decision:
    """Le pregunta a Oso. Si niega, busca POR QUÉ (las reglas `motivo`) para responder algo útil."""
    with _candado:
        if _oso.is_allowed(sujeto, accion, recurso):
            return PERMITIDO
        codigos = {r["bindings"]["motivo"] for r in _oso.query_rule("motivo", sujeto, accion, recurso, Variable("motivo"))}
    politica = POLITICAS.get(accion)
    for m in politica.motivos if politica else ():
        if m.codigo in codigos:
            mensaje = m.mensaje(recurso) if callable(m.mensaje) else m.mensaje
            return Decision(False, m.status, m.codigo, mensaje, m.datos(recurso) if m.datos else {})
    # Oso negó sin un motivo conocido (una acción que no tiene regla): se niega igual.
    return Decision(False, 403, "sin_permiso", "Tu cuenta no tiene permiso para hacer esto.")


# ─── Lo que llaman las rutas ─────────────────────────────────────────────────

def puede_usar_funcion(sujeto: Sujeto, permiso: str) -> Decision:
    return decidir(sujeto, "usar", Funcion(permiso))


def es_propio(sujeto: Sujeto, cuenta_del_recurso: Any) -> Decision:
    return decidir(sujeto, "ver", Recurso(_id(cuenta_del_recurso)))


def puede_guardar_inscripcion(
    sujeto: Sujeto,
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
    return decidir(sujeto, "inscribirse", Inscripcion(
        inicia=_segundos(actividad_inicia), ahora=_segundos(ahora), tiene_cupo=tiene_cupo,
        lugares_libres=lugares_libres, lugares_propios=lugares_propios, personas=personas,
        edad_minima=edad_minima, integrantes=list(integrantes), consentimiento=consentimiento,
    ))


def puede_cancelar(sujeto: Sujeto, cuenta_de_inscripcion: Any, actividad_inicia: datetime, ahora: datetime) -> Decision:
    return decidir(sujeto, "cancelar", Cancelacion(_id(cuenta_de_inscripcion), _segundos(actividad_inicia), _segundos(ahora)))


def puede_publicar_testimonio(sujeto: Sujeto, cuenta_de_asistencia: Any, actividad_inicia: datetime, ahora: datetime) -> Decision:
    return decidir(sujeto, "publicar_testimonio", Participacion(_id(cuenta_de_asistencia), _segundos(actividad_inicia), _segundos(ahora)))


def puede_eliminar_testimonio(sujeto: Sujeto, autor: Any) -> Decision:
    return decidir(sujeto, "eliminar", Testimonio(_id(autor)))


def puede_ver_testimonio_en_revision(sujeto: Sujeto, autor: Any) -> Decision:
    return decidir(sujeto, "ver_en_revision", Testimonio(_id(autor)))


def puede_contestar_previa(sujeto: Sujeto, inscrita: bool, actividad_inicia: datetime, ahora: datetime) -> Decision:
    return decidir(sujeto, "contestar_previa", EncuestaPrevia(inscrita, _segundos(actividad_inicia), _segundos(ahora)))


def puede_contestar_final(sujeto: Sujeto, cuenta_de_asistencia: Any) -> Decision:
    # La hora no importa aquí: la asistencia ya se registró, la actividad ya pasó.
    return decidir(sujeto, "contestar_final", Participacion(_id(cuenta_de_asistencia), 0.0, 0.0))


def puede_ver_todas_las_encuestas(sujeto: Sujeto) -> Decision:
    return decidir(sujeto, "ver_todas", "encuestas")


def puede_aportar(sujeto: Sujeto, cierra_en: date | None, hoy: date, cantidad: int, faltan: int) -> Decision:
    return decidir(sujeto, "aportar", Aportacion(cierra_en.toordinal() if cierra_en else None, hoy.toordinal(), cantidad, faltan))


def puede_ver_foto_perfil(sujeto: Sujeto, cuenta_de_la_foto: Any) -> Decision:
    return decidir(sujeto, "ver", FotoPerfil(_id(cuenta_de_la_foto)))
