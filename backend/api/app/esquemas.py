"""Lo que entra y lo que sale de la API, con Pydantic.

ENTRADA (validación, OWASP A05 · CWE-20):
  · `extra="forbid"`: un campo que no está declarado es un error. Así nadie se agrega
    `"rol": "admin"` o `"cuenta_id": "<otra>"` en el cuerpo (mass assignment, OWASP API3).
  · Cada texto tiene largo máximo, y se rechazan caracteres de control (un \\x00 rompería
    PostgreSQL; otros ensucian la bitácora: log injection).
  · Números con rango, fechas con zona horaria, ids que tienen que ser UUID.
  · Los enlaces que la app abre (Instagram) tienen que ser https:// de un dominio permitido:
    nada de `javascript:` ni `intent:`.

SALIDA (exposición de datos, OWASP API3):
  · Cada respuesta declara sus campos. Lo que no está aquí no sale, aunque esté en la tabla:
    por ejemplo, un testimonio dice el nombre de la familia, pero NUNCA su correo.
"""

import re
import uuid
from datetime import date, datetime
from typing import Annotated, Literal
from urllib.parse import urlparse

from pydantic import AfterValidator, AwareDatetime, BaseModel, ConfigDict, Field, StringConstraints

# ─── Tipos reutilizables ────────────────────────────────────────────────────

_CONTROL = re.compile(r"[\x00-\x08\x0b\x0c\x0e-\x1f\x7f]")


def _sin_control(v: str) -> str:
    if _CONTROL.search(v):
        raise ValueError("tiene caracteres no permitidos")
    return v


def Texto(maximo: int, minimo: int = 0):  # noqa: N802 — se usa como tipo
    return Annotated[str, StringConstraints(strip_whitespace=True, min_length=minimo, max_length=maximo), AfterValidator(_sin_control)]


def _telefono(v: str) -> str:
    v = re.sub(r"[\s\-()]", "", v)
    if v and not re.fullmatch(r"\+?[0-9]{7,15}", v):
        raise ValueError("debe tener solo dígitos (7 a 15)")
    return v


def _correo(v: str) -> str:
    if v and not re.fullmatch(r"[^@\s]{1,64}@[^@\s]{1,190}\.[A-Za-z]{2,24}", v):
        raise ValueError("no es un correo válido")
    return v.lower()


_DOMINIOS_ENLACE = {"instagram.com", "www.instagram.com", "facebook.com", "www.facebook.com"}


def _enlace_seguro(v: str) -> str:
    if not v:
        return v
    url = urlparse(v)
    if url.scheme != "https" or (url.hostname or "").lower() not in _DOMINIOS_ENLACE:
        raise ValueError("debe ser un enlace https:// de Instagram o Facebook")
    return v


def _recurso(v: str) -> str:
    # El nombre de una imagen que ya viene dentro de la app (res/drawable): letras, números y _.
    if v and not re.fullmatch(r"[a-z0-9_]{1,64}", v):
        raise ValueError("solo letras minúsculas, números y _")
    return v


Telefono = Annotated[str, StringConstraints(strip_whitespace=True, max_length=24), AfterValidator(_telefono)]
Correo = Annotated[str, StringConstraints(strip_whitespace=True, max_length=254), AfterValidator(_correo)]
EnlaceSeguro = Annotated[str, StringConstraints(strip_whitespace=True, max_length=300), AfterValidator(_enlace_seguro)]
Recurso = Annotated[str, StringConstraints(strip_whitespace=True, max_length=64), AfterValidator(_recurso)]
Ciudad = Texto(60, 2)
Sexo = Literal["MASCULINO", "FEMENINO", "OTRO", "NO_ESPECIFICA"]
Latitud = Annotated[float, Field(ge=-90, le=90)]
Longitud = Annotated[float, Field(ge=-180, le=180)]


class Entrada(BaseModel):
    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)


class Salida(BaseModel):
    model_config = ConfigDict(from_attributes=True)


# ─── Catálogo (público) ─────────────────────────────────────────────────────

class CiudadSalida(Salida):
    nombre: str
    estado: str
    zona_horaria: str
    latitud: float
    longitud: float


class AsociacionSalida(Salida):
    id: uuid.UUID
    nombre: str
    categoria: str
    descripcion: str
    direccion: str
    telefono: str
    whatsapp: str
    correo: str
    ciudad: str
    en_directorio: bool


class AportacionActividad(BaseModel):
    tipo: Literal["ninguna", "especie", "monetaria"]
    monto: str
    detalle: str


class ActividadSalida(BaseModel):
    id: uuid.UUID
    titulo: str
    asociacion: AsociacionSalida
    inicia_en: datetime
    termina_en: datetime | None
    direccion: str
    edad_minima: int | None
    descripcion: str
    cupo_total: int
    lugares_disponibles: int
    municipio: str
    tema: str
    aportacion: AportacionActividad
    punto_encuentro: str
    acerca_del_proyecto: str
    que_haremos: str
    que_incluye: str
    que_llevar: str
    recomendaciones: str
    imagen: str | None
    ya_paso: bool
    ciudad: str
    latitud: float | None
    longitud: float | None


class ArticuloSalida(BaseModel):
    id: uuid.UUID
    nombre: str
    meta: int
    apartados: int


class OpcionSalida(BaseModel):
    nombre: str
    precio: str


class PuntoSalida(BaseModel):
    direccion: str
    colonia: str


class CampanaSalida(BaseModel):
    id: uuid.UUID
    titulo: str
    asociacion_id: uuid.UUID | None
    categoria: str
    cierra_en: date | None
    urgente: bool
    descripcion: str
    unidad_meta: str
    meta_total: int
    completados: int
    articulos: list[ArticuloSalida]
    imagen: str | None
    ciudad: str
    texto_boton: str
    telefono: str | None
    whatsapp: str | None
    contacto_nombre: str | None
    meta_texto: str | None
    descripcion_larga: str
    como_ayudar: str
    opciones_donacion: list[OpcionSalida]
    puntos_entrega: list[PuntoSalida]
    instagram: str | None
    latitud: float | None
    longitud: float | None


class FormaApoyoSalida(Salida):
    titulo: str
    detalle: str
    icono: str


class ProyectoSalida(BaseModel):
    id: uuid.UUID
    nombre: str
    descripcion: str
    beneficiarios: str | None
    ciudad: str
    vigencia: str | None
    logo: str | None
    activo: bool
    resumen: str
    acerca_de: str
    formas_apoyo: list[FormaApoyoSalida]
    como_ayudar: str
    telefono: str | None
    whatsapp: str | None
    instagram: str | None
    latitud: float | None
    longitud: float | None


class CentroSalida(BaseModel):
    id: uuid.UUID
    nombre: str
    tipo: str
    resumen: str
    informacion: str
    necesidades: list[str]
    direccion: str
    logo: str | None
    verificado: bool
    como_ayudar: str
    recomendaciones: str
    telefono: str | None
    whatsapp: str | None
    instagram: str | None
    ciudad: str
    latitud: float | None = None
    longitud: float | None = None


class TestimonioSalida(BaseModel):
    id: uuid.UUID
    participacion_id: uuid.UUID
    actividad_id: uuid.UUID
    actividad_titulo: str
    familia: str                      # el nombre de la familia; el correo NUNCA sale
    experiencia: str
    foto_url: str | None
    fecha: date
    es_mio: bool
    estado: Literal["pendiente", "aprobado", "rechazado"] = "aprobado"   # lo público siempre es "aprobado"


class RevisionTestimonioEntrada(Entrada):
    estado: Literal["aprobado", "rechazado"]


class EnlaceSalida(BaseModel):
    url: str


# ─── La cuenta ──────────────────────────────────────────────────────────────

class Impacto(BaseModel):
    actividades_realizadas: int
    horas_de_servicio: int
    campanas_apoyadas: int


class PersonaSalida(BaseModel):
    nombre: str
    fecha_nacimiento: date | None
    edad: int | None
    sexo: Sexo | None


class YoSalida(BaseModel):
    id: uuid.UUID
    correo: str
    correo_verificado: bool
    nombre_familia: str
    ciudad: str
    titular: PersonaSalida
    es_admin: bool
    permisos: list[str]
    tiene_foto: bool
    invitaciones: int
    aviso_aceptado: bool
    impacto: Impacto


class YoEntrada(Entrada):
    nombre_familia: Texto(80, 3) | None = None
    ciudad: Ciudad | None = None
    titular_nombre: Texto(80, 3) | None = None
    titular_nacimiento: date | None = None
    titular_sexo: Sexo | None = None
    aceptar_aviso: bool | None = None


# ─── Sesión: correo y contraseña → JWT ──────────────────────────────────────
# Las contraseñas NO se recortan ni se pasan a minúsculas (por eso no heredan de Entrada): una
# contraseña con un espacio al final es otra contraseña. Se limita el largo para que nadie mande
# un "contraseña" de 1 MB a que la API la pase por Argon2 (OWASP API4).

_MUY_COMUNES = {
    "12345678", "123456789", "1234567890", "87654321", "11111111", "00000000", "password", "password1",
    "contraseña", "contrasena", "qwertyui", "qwerty123", "abcdefgh", "abc12345", "iloveyou", "monterrey",
}


def _contrasena_nueva(v: str) -> str:
    if _CONTROL.search(v):
        raise ValueError("tiene caracteres no permitidos")
    if not v.strip() or len(set(v)) < 4 or v.lower() in _MUY_COMUNES:
        raise ValueError("es demasiado fácil de adivinar; usa una frase o mezcla palabras y números")
    return v


CorreoObligatorio = Annotated[str, StringConstraints(strip_whitespace=True, min_length=6, max_length=254), AfterValidator(_correo)]


class EntradaSesion(BaseModel):
    model_config = ConfigDict(extra="forbid")


class InicioSesionEntrada(EntradaSesion):
    correo: CorreoObligatorio
    contrasena: Annotated[str, StringConstraints(min_length=1, max_length=128)]


class RegistroEntrada(EntradaSesion):
    correo: CorreoObligatorio
    contrasena: Annotated[str, StringConstraints(min_length=8, max_length=128), AfterValidator(_contrasena_nueva)]
    nombre_familia: Texto(80, 3)
    acepta_aviso: bool


class RecuperarEntrada(EntradaSesion):
    correo: CorreoObligatorio


class RestablecerEntrada(EntradaSesion):
    """El código de 6 dígitos que llegó por correo y la contraseña nueva (con las mismas reglas del registro)."""

    correo: CorreoObligatorio
    codigo: Annotated[str, StringConstraints(strip_whitespace=True, pattern=r"^[0-9]{6}$")]
    contrasena: Annotated[str, StringConstraints(min_length=8, max_length=128), AfterValidator(_contrasena_nueva)]


class AvisoEntrada(Entrada):
    titulo: Texto(80, 3)
    cuerpo: Texto(300, 3)
    ciudad: Ciudad | None = None          # sin ciudad: para todas


class AvisoSalida(BaseModel):
    id: int
    tipo: Literal["urgencia", "actividad_nueva"]
    ciudad: str | None
    titulo: str
    cuerpo: str
    actividad_id: uuid.UUID | None
    creado_en: datetime


class TokenSalida(BaseModel):
    """Lo que responde el /login de la guía (el JWT y su tipo), más cuánto dura (segundos) y el
    refresh token para renovarlo sin volver a pedir la contraseña."""

    access_token: str
    token_type: Literal["bearer"] = "bearer"
    expira_en: int
    refresh_token: str
    refresh_expira_en: int


RefreshToken = Annotated[str, StringConstraints(min_length=20, max_length=200, pattern=r"^[A-Za-z0-9_-]+$")]


class RenovarEntrada(EntradaSesion):
    refresh_token: RefreshToken


class CerrarSesionEntrada(EntradaSesion):
    refresh_token: RefreshToken | None = None


class AcompananteSalida(BaseModel):
    id: uuid.UUID
    nombre: str
    fecha_nacimiento: date | None
    edad: int | None
    sexo: Sexo | None


class PersonaEntrada(Entrada):
    nombre: Texto(80, 3)
    fecha_nacimiento: date | None = None
    edad: Annotated[int, Field(ge=0, le=120)] | None = None
    sexo: Sexo | None = None


class PreferenciasEntrada(Entrada):
    recordatorio_actividades: bool
    avisos_favoritos: bool
    urgencias_ciudad: bool


class PreferenciasSalida(Salida):
    recordatorio_actividades: bool
    avisos_favoritos: bool
    urgencias_ciudad: bool


class FavoritoSalida(BaseModel):
    objeto_id: uuid.UUID
    tipo: str


class InvitacionEntrada(Entrada):
    actividad_id: uuid.UUID


class ConteoSalida(BaseModel):
    total: int


# ─── Inscripciones ──────────────────────────────────────────────────────────

class InscripcionEntrada(Entrada):
    # Quién va además de la persona titular. 20 es más de lo que cabe en cualquier actividad
    # y evita que alguien mande una lista de un millón (OWASP API4).
    acompanantes: Annotated[list[PersonaEntrada], Field(max_length=20)] = []
    consentimiento_menores: bool = False


class InscripcionSalida(BaseModel):
    actividad_id: uuid.UUID
    personas: int
    acompanantes: list[PersonaSalida]
    creada_en: datetime
    actualizada_en: datetime


class InscripcionResultado(BaseModel):
    inscripcion: InscripcionSalida
    lugares_disponibles: int


class CancelacionSalida(BaseModel):
    liberados: int
    lugares_antes: int
    lugares_disponibles: int


# ─── Participación ──────────────────────────────────────────────────────────

class ParticipacionSalida(BaseModel):
    id: uuid.UUID                      # la asistencia
    actividad_id: uuid.UUID
    titulo_actividad: str
    nombre_asociacion: str
    inicia_en: datetime
    termina_en: datetime | None
    encuesta_final_contestada: bool
    encuesta_pendiente: bool
    testimonio_id: uuid.UUID | None


class RespuestaPregunta(Entrada):
    numero: Annotated[int, Field(ge=1, le=50)]
    pregunta: Texto(300, 1)
    respuesta: Texto(300, 1)


class EncuestaEntrada(Entrada):
    respuestas: Annotated[list[RespuestaPregunta], Field(min_length=1, max_length=20)]


class EncuestaSalida(BaseModel):
    id: uuid.UUID
    actividad_id: uuid.UUID
    participacion_id: uuid.UUID | None
    actividad_titulo: str
    familia: str
    correo: str | None = None          # solo en la consulta de moderación
    momento: Literal["antes", "despues"]
    respuestas: list[dict]
    fecha: date


# ─── Campañas ───────────────────────────────────────────────────────────────

class AportacionEntrada(Entrada):
    # La app genera este id: si la red falla y reintenta, la misma aportación no se cuenta
    # dos veces (como los números de secuencia del transporte confiable). Idempotencia.
    id: uuid.UUID
    cantidad: Annotated[int, Field(ge=1, le=1000)]


class AportacionSalida(BaseModel):
    articulo: ArticuloSalida
    cantidad: int
    completados_campana: int


# ─── Sugerencias y privacidad ───────────────────────────────────────────────

class SugerenciaEntrada(Entrada):
    tipo: Literal["ACTIVIDAD", "CAMPANA", "PROYECTO", "CENTRO"]
    nombre: Texto(120, 3)
    descripcion: Texto(2000, 10)
    detalle_extra: Texto(500) = ""
    ciudad: Ciudad = "Monterrey"
    organizacion: Texto(120) = ""
    contacto_causa: Texto(120) = ""
    sugerida_por: Texto(80, 3)
    contacto: Texto(120, 3)


class SugerenciaSalida(BaseModel):
    id: uuid.UUID
    tipo: str
    nombre: str
    descripcion: str
    detalle_extra: str
    ciudad: str
    organizacion: str
    contacto_causa: str
    sugerida_por: str
    contacto: str
    estado: str
    fecha: date


class EstadoSugerenciaEntrada(Entrada):
    estado: Literal["PENDIENTE", "APROBADA", "DESCARTADA"]


class SolicitudEliminacionSalida(BaseModel):
    id: uuid.UUID
    folio: str
    estado: str
    correo: str | None = None          # solo en la bandeja de privacidad
    creada_en: datetime


# ─── Administración ─────────────────────────────────────────────────────────

class AsociacionEntrada(Entrada):
    nombre: Texto(120, 2)
    categoria: Texto(60, 2)
    descripcion: Texto(2000) = ""
    direccion: Texto(200) = ""
    telefono: Telefono = ""
    whatsapp: Telefono = ""
    correo: Correo = ""
    ciudad: Ciudad = "Monterrey"
    en_directorio: bool = True


class ActividadEntrada(Entrada):
    titulo: Texto(160, 3)
    asociacion_id: uuid.UUID
    inicia_en: AwareDatetime
    termina_en: AwareDatetime | None = None
    direccion: Texto(200) = ""
    cupo_total: Annotated[int, Field(ge=0, le=10_000)] = 0
    # Si se manda, la API calcula cuántos lugares se apartaron por fuera de la app.
    lugares_disponibles: Annotated[int, Field(ge=0, le=10_000)] | None = None
    edad_minima: Annotated[int, Field(ge=0, le=99)] | None = None
    descripcion: Texto(4000) = ""
    ciudad: Ciudad = "Monterrey"
    municipio: Texto(80) = ""
    tema: Literal["SALUD", "CELEBRACION", "MEDIO_AMBIENTE", "GENERAL"] = "GENERAL"
    aportacion_tipo: Literal["ninguna", "especie", "monetaria"] = "ninguna"
    aportacion_monto: Texto(40) = ""
    aportacion_detalle: Texto(1000) = ""
    punto_encuentro: Texto(200) = ""
    acerca_del_proyecto: Texto(4000) = ""
    que_haremos: Texto(4000) = ""
    que_incluye: Texto(4000) = ""
    que_llevar: Texto(4000) = ""
    recomendaciones: Texto(4000) = ""
    imagen: Recurso = ""
    latitud: Latitud | None = None
    longitud: Longitud | None = None


class ArticuloEntrada(Entrada):
    nombre: Texto(120, 2)
    meta: Annotated[int, Field(ge=1, le=100_000)]


class OpcionEntrada(Entrada):
    nombre: Texto(120, 2)
    precio: Texto(40, 1)


class PuntoEntrada(Entrada):
    direccion: Texto(200, 3)
    colonia: Texto(120) = ""


class CampanaEntrada(Entrada):
    titulo: Texto(160, 3)
    asociacion_id: uuid.UUID | None = None
    categoria: Texto(60) = ""
    cierra_en: date | None = None
    urgente: bool = False
    descripcion: Texto(4000) = ""
    unidad_meta: Texto(40) = ""
    meta_total: Annotated[int, Field(ge=0, le=1_000_000)] = 0
    completados: Annotated[int, Field(ge=0, le=1_000_000)] | None = None
    ciudad: Ciudad = "Monterrey"
    texto_boton: Texto(40, 2) = "Quiero ayudar"
    telefono: Telefono = ""
    whatsapp: Telefono = ""
    contacto_nombre: Texto(120) = ""
    meta_texto: Texto(80) = ""
    descripcion_larga: Texto(8000) = ""
    como_ayudar: Texto(4000) = ""
    instagram: EnlaceSeguro = ""
    imagen: Recurso = ""
    latitud: Latitud | None = None
    longitud: Longitud | None = None
    articulos: Annotated[list[ArticuloEntrada], Field(max_length=20)] | None = None
    opciones_donacion: Annotated[list[OpcionEntrada], Field(max_length=20)] | None = None
    puntos_entrega: Annotated[list[PuntoEntrada], Field(max_length=20)] | None = None


class FormaApoyoEntrada(Entrada):
    titulo: Texto(200, 2)
    detalle: Texto(2000) = ""
    icono: Literal["CORAZON", "LIBRO", "DINERO"] = "CORAZON"


class ProyectoEntrada(Entrada):
    nombre: Texto(160, 2)
    descripcion: Texto(4000) = ""
    beneficiarios: Texto(120) = ""
    ciudad: Texto(80, 2) = "Monterrey"
    vigencia: Texto(80) = ""
    activo: bool = True
    resumen: Texto(1000) = ""
    acerca_de: Texto(4000) = ""
    como_ayudar: Texto(4000) = ""
    telefono: Telefono = ""
    whatsapp: Telefono = ""
    instagram: EnlaceSeguro = ""
    logo: Recurso = ""
    latitud: Latitud | None = None
    longitud: Longitud | None = None
    formas_apoyo: Annotated[list[FormaApoyoEntrada], Field(max_length=20)] | None = None


class CentroEntrada(Entrada):
    nombre: Texto(160, 2)
    tipo: Literal["Asilos", "Casas hogar", "Comedores"]
    resumen: Texto(1000) = ""
    informacion: Texto(4000) = ""
    necesidades: Annotated[list[Texto(300, 1)], Field(max_length=30)] = []
    direccion: Texto(200) = ""
    verificado: bool = False
    como_ayudar: Texto(4000) = ""
    recomendaciones: Texto(4000) = ""
    telefono: Telefono = ""
    whatsapp: Telefono = ""
    instagram: EnlaceSeguro = ""
    logo: Recurso = ""
    ciudad: Ciudad = "Monterrey"
    latitud: Latitud | None = None
    longitud: Longitud | None = None


class AsistenciaEntrada(Entrada):
    cuenta_id: uuid.UUID
    actividad_id: uuid.UUID


class InscritoSalida(BaseModel):
    """Una familia inscrita a una actividad, con quienes van. Sin correo ni teléfono: a quien
    organiza le basta saber a quién esperar (minimización, LFPDPPP)."""

    cuenta_id: uuid.UUID
    familia: str
    personas: int
    integrantes: list[str]
    hay_menores: bool
    asistencia_registrada: bool
    inscrita_en: datetime


class AsistenciaSalida(Salida):
    id: uuid.UUID
    cuenta_id: uuid.UUID
    actividad_id: uuid.UUID
    registrada_en: datetime



class ResumenSalida(BaseModel):
    """Los números del panel de administración. Solo conteos: ningún dato de una persona."""

    familias: int                   # cuentas de familia (sin permisos de administración)
    administradores: int            # cuentas con al menos un permiso
    acompanantes: int               # personas que las familias registraron además de la titular
    actividades_proximas: int
    actividades_pasadas: int
    proyectos_activos: int
    campanas: int
    centros: int
    asociaciones: int
    inscripciones_vigentes: int     # sin cancelar, de actividades que no han pasado
    personas_inscritas: int         # cuántas personas suman esas inscripciones
    aportaciones: int               # artículos apartados para campañas
    testimonios_publicados: int
    testimonios_por_revisar: int
    sugerencias: dict[str, int]     # las propuestas de las familias por tipo: actividad, campana, proyecto, centro
    sugerencias_por_revisar: int
    eliminaciones_pendientes: int
