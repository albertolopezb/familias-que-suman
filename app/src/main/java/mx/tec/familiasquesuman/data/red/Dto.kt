package mx.tec.familiasquesuman.data.red

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// Lo que manda y recibe la API, tal cual viaja en JSON. Los nombres en Kotlin van en camelCase y
// en el JSON en snake_case (lo convierte ClienteApi). Las fechas llegan como texto ISO-8601 y se
// convierten en Convertir.kt: aquí nada sabe de pantallas.
//
// Las respuestas pueden traer campos nuevos que la app todavía no conoce: se ignoran
// (ignoreUnknownKeys). Así una API más nueva no rompe una app vieja.

@Serializable
data class CiudadDto(val nombre: String, val estado: String, val zonaHoraria: String, val latitud: Double, val longitud: Double)

@Serializable
data class AsociacionDto(
    val id: String,
    val nombre: String,
    val categoria: String,
    val descripcion: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val whatsapp: String = "",
    val correo: String = "",
    val ciudad: String = "Monterrey",
    val enDirectorio: Boolean = true
)

@Serializable
data class AportacionActividadDto(val tipo: String = "ninguna", val monto: String = "", val detalle: String = "")

@Serializable
data class ActividadDto(
    val id: String,
    val titulo: String,
    val asociacion: AsociacionDto,
    val iniciaEn: String,
    val terminaEn: String? = null,
    val direccion: String = "",
    val edadMinima: Int? = null,
    val descripcion: String = "",
    val cupoTotal: Int = 0,
    val lugaresDisponibles: Int = 0,
    val municipio: String = "",
    val tema: String = "GENERAL",
    val aportacion: AportacionActividadDto = AportacionActividadDto(),
    val puntoEncuentro: String = "",
    val acercaDelProyecto: String = "",
    val queHaremos: String = "",
    val queIncluye: String = "",
    val queLlevar: String = "",
    val recomendaciones: String = "",
    val imagen: String? = null,
    val yaPaso: Boolean = false,
    val ciudad: String = "Monterrey",
    val latitud: Double? = null,
    val longitud: Double? = null
)

@Serializable
data class ArticuloDto(val id: String, val nombre: String, val meta: Int, val apartados: Int)

@Serializable
data class OpcionDto(val nombre: String, val precio: String)

@Serializable
data class PuntoDto(val direccion: String, val colonia: String = "")

@Serializable
data class CampanaDto(
    val id: String,
    val titulo: String,
    val asociacionId: String? = null,
    val categoria: String = "",
    val cierraEn: String? = null,
    val urgente: Boolean = false,
    val descripcion: String = "",
    val unidadMeta: String = "",
    val metaTotal: Int = 0,
    val completados: Int = 0,
    val articulos: List<ArticuloDto> = emptyList(),
    val imagen: String? = null,
    val ciudad: String = "Monterrey",
    val textoBoton: String = "Quiero ayudar",
    val telefono: String? = null,
    val whatsapp: String? = null,
    val contactoNombre: String? = null,
    val metaTexto: String? = null,
    val descripcionLarga: String = "",
    val comoAyudar: String = "",
    val opcionesDonacion: List<OpcionDto> = emptyList(),
    val puntosEntrega: List<PuntoDto> = emptyList(),
    val instagram: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

@Serializable
data class FormaApoyoDto(val titulo: String, val detalle: String = "", val icono: String = "CORAZON")

@Serializable
data class ProyectoDto(
    val id: String,
    val nombre: String,
    val descripcion: String = "",
    val beneficiarios: String? = null,
    val ciudad: String = "Monterrey",
    val vigencia: String? = null,
    val logo: String? = null,
    val activo: Boolean = true,
    val resumen: String = "",
    val acercaDe: String = "",
    val formasApoyo: List<FormaApoyoDto> = emptyList(),
    val comoAyudar: String = "",
    val telefono: String? = null,
    val whatsapp: String? = null,
    val instagram: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

@Serializable
data class CentroDto(
    val id: String,
    val nombre: String,
    val tipo: String,
    val resumen: String = "",
    val informacion: String = "",
    val necesidades: List<String> = emptyList(),
    val direccion: String = "",
    val logo: String? = null,
    val verificado: Boolean = false,
    val comoAyudar: String = "",
    val recomendaciones: String = "",
    val telefono: String? = null,
    val whatsapp: String? = null,
    val instagram: String? = null,
    val ciudad: String = "Monterrey"
)

@Serializable
data class TestimonioDto(
    val id: String,
    val participacionId: String,
    val actividadId: String,
    val actividadTitulo: String,
    val familia: String,
    val experiencia: String,
    val fotoUrl: String? = null,
    val fecha: String,
    val esMio: Boolean = false,
    val estado: String = "aprobado"
)

/** Lo que decide la moderación de un testimonio: "aprobado" o "rechazado". */
@Serializable
data class RevisionTestimonioCuerpo(val estado: String)

@Serializable
data class EnlaceDto(val url: String)

// ─── La cuenta ──────────────────────────────────────────────────────────────

@Serializable
data class ImpactoDto(val actividadesRealizadas: Int = 0, val horasDeServicio: Int = 0, val campanasApoyadas: Int = 0)

@Serializable
data class PersonaDto(val nombre: String, val fechaNacimiento: String? = null, val edad: Int? = null, val sexo: String? = null)

@Serializable
data class YoDto(
    val id: String,
    val correo: String,
    val correoVerificado: Boolean = false,
    val nombreFamilia: String,
    val ciudad: String = "Monterrey",
    val titular: PersonaDto,
    val esAdmin: Boolean = false,
    val permisos: List<String> = emptyList(),
    val tieneFoto: Boolean = false,
    val invitaciones: Int = 0,
    val avisoAceptado: Boolean = false,
    val impacto: ImpactoDto = ImpactoDto()
)

@Serializable
data class YoCambiosDto(
    val nombreFamilia: String? = null,
    val ciudad: String? = null,
    val titularNombre: String? = null,
    val aceptarAviso: Boolean? = null
)

// ─── Sesión (POST /v1/sesion/…) ───

@Serializable
data class InicioSesionCuerpo(val correo: String, val contrasena: String)

@Serializable
data class RegistroCuerpo(val correo: String, val contrasena: String, val nombreFamilia: String, val aceptaAviso: Boolean)

@Serializable
data class CorreoCuerpo(val correo: String)

/** El JWT que firma la API (dura minutos) y el refresh token para pedir otro (dura días). */
@Serializable
data class TokenDto(
    val accessToken: String,
    val tokenType: String = "bearer",
    val expiraEn: Long,
    val refreshToken: String,
    val refreshExpiraEn: Long
)

@Serializable
data class RenovarCuerpo(val refreshToken: String)

@Serializable
data class CerrarSesionCuerpo(val refreshToken: String? = null)

@Serializable
data class AcompananteDto(val id: String, val nombre: String, val fechaNacimiento: String? = null, val edad: Int? = null, val sexo: String? = null)

@Serializable
data class PreferenciasDto(val recordatorioActividades: Boolean, val avisosFavoritos: Boolean, val urgenciasCiudad: Boolean)

@Serializable
data class FavoritoDto(val objetoId: String, val tipo: String)

@Serializable
data class InvitacionDto(val actividadId: String)

@Serializable
data class ConteoDto(val total: Int)

// ─── Inscripciones ──────────────────────────────────────────────────────────

@Serializable
data class InscripcionCuerpo(val acompanantes: List<PersonaDto>, val consentimientoMenores: Boolean)

@Serializable
data class InscripcionDto(
    val actividadId: String,
    val personas: Int,
    val acompanantes: List<PersonaDto> = emptyList(),
    val creadaEn: String,
    val actualizadaEn: String
)

@Serializable
data class InscripcionResultadoDto(val inscripcion: InscripcionDto, val lugaresDisponibles: Int)

@Serializable
data class CancelacionDto(val liberados: Int, val lugaresAntes: Int, val lugaresDisponibles: Int)

// ─── Participación ──────────────────────────────────────────────────────────

@Serializable
data class ParticipacionDto(
    val id: String,
    val actividadId: String,
    val tituloActividad: String,
    val nombreAsociacion: String,
    val iniciaEn: String,
    val terminaEn: String? = null,
    val encuestaFinalContestada: Boolean = false,
    val encuestaPendiente: Boolean = false,
    val testimonioId: String? = null
)

@Serializable
data class RespuestaDto(val numero: Int, val pregunta: String, val respuesta: String)

@Serializable
data class EncuestaCuerpo(val respuestas: List<RespuestaDto>)

@Serializable
data class EncuestaDto(
    val id: String,
    val actividadId: String,
    val participacionId: String? = null,
    val actividadTitulo: String,
    val familia: String,
    val correo: String? = null,
    val momento: String,
    val respuestas: List<RespuestaDto>,
    val fecha: String
)

// ─── Campañas ───────────────────────────────────────────────────────────────

@Serializable
data class AportacionCuerpo(val id: String, val cantidad: Int)

@Serializable
data class AportacionDto(val articulo: ArticuloDto, val cantidad: Int, val completadosCampana: Int)

// ─── Sugerencias y privacidad ───────────────────────────────────────────────

@Serializable
data class SugerenciaCuerpo(
    val tipo: String,
    val nombre: String,
    val descripcion: String,
    val detalleExtra: String,
    val ciudad: String,
    val organizacion: String,
    val contactoCausa: String,
    val sugeridaPor: String,
    val contacto: String
)

@Serializable
data class SugerenciaDto(
    val id: String,
    val tipo: String,
    val nombre: String,
    val descripcion: String,
    val detalleExtra: String = "",
    val ciudad: String = "",
    val organizacion: String = "",
    val contactoCausa: String = "",
    val sugeridaPor: String,
    val contacto: String,
    val estado: String,
    val fecha: String
)

@Serializable
data class EstadoSugerenciaCuerpo(val estado: String)

@Serializable
data class SolicitudEliminacionDto(val id: String, val folio: String, val estado: String, val creadaEn: String)

// ─── Administración ─────────────────────────────────────────────────────────

@Serializable
data class AsociacionCuerpo(
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val direccion: String,
    val telefono: String,
    val whatsapp: String,
    val correo: String,
    val ciudad: String = "Monterrey",
    val enDirectorio: Boolean = true
)

/** Para crear y editar actividades, campañas, proyectos y centros: solo los campos que se mandan. */
typealias CuerpoLibre = JsonObject

/** El error que responde la API: `{ "error": "...", "codigo": "...", "datos": {...} }`. */
@Serializable
data class ErrorDto(val error: String = "", val codigo: String = "", val datos: JsonObject? = null)
