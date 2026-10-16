package mx.tec.familiasquesuman.domain

// Kotlin puro, como Modelos.kt. Aquí viven RF-12 (testimonio con fotografía) y RF-13 (encuesta antes y después).

/** Antes de publicarse, cada testimonio lo revisa Familias que Suman (RF-12, HU-11). */
enum class EstadoRevision { PENDIENTE, APROBADO, RECHAZADO }

/** Un testimonio (RF-12): la foto y la experiencia de una familia sobre una actividad. */
data class Testimonio(
    val id: String,
    /** La participación (RF-11) de la que habla; una familia publica un testimonio por participación. */
    val participacionId: String,
    val actividadTitulo: String,
    val familia: String,
    val correo: String,
    val experiencia: String,
    /** Ruta del archivo de la foto en el almacenamiento de la app; null si no trae. */
    val foto: String?,
    val fecha: String,
    /** Lo dice el servidor: la cuenta con sesión es la que lo escribió. */
    val esMio: Boolean = false,
    /** Lo público siempre viene APROBADO; la autora y la moderación ven también los otros dos. */
    val estado: EstadoRevision = EstadoRevision.APROBADO
)

/**
 * Quién puede eliminar un testimonio: el admin (moderación) y la cuenta que lo escribió.
 * Ninguna otra cuenta, ni sin sesión.
 */
fun Testimonio.puedeEliminarlo(correoDeQuienPide: String?, esAdmin: Boolean): Boolean =
    esAdmin || esMio || (correoDeQuienPide != null && correo.isNotBlank() && correoDeQuienPide.equals(correo, ignoreCase = true))

enum class MomentoEncuesta(val etiqueta: String) {
    ANTES("Antes de la actividad"),
    DESPUES("Al terminar")
}

/** La pregunta y la respuesta tal como se leyeron: así se consultan aunque la encuesta cambie después. */
data class RespuestaPregunta(val numero: Int, val pregunta: String, val respuesta: String)

/**
 * Lo que una familia contestó en una encuesta, ligado a la actividad (RF-13).
 * [actividadId] es el de la actividad en la encuesta de antes y el de la participación en la de después.
 */
data class RespuestaEncuesta(
    val id: String,
    val actividadId: String,
    val actividadTitulo: String,
    val correo: String,
    val familia: String,
    val momento: MomentoEncuesta,
    val respuestas: List<RespuestaPregunta>,
    val fecha: String
)
