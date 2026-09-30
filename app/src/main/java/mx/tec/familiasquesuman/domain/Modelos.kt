package mx.tec.familiasquesuman.domain

// Kotlin puro: ningún import de Android, Retrofit ni Room en este archivo.
// Si falta un campo, se agrega aquí en un PR aparte; nadie define su propia versión.
// Las fechas son texto mientras todo está en memoria; con el backend pasan a LocalDate.

data class Asociacion(
    val id: String,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val direccion: String,
    val telefono: String,
    val whatsapp: String,
    val correo: String
)

data class Actividad(
    val id: String,
    val titulo: String,
    val asociacionId: String,
    val fecha: String,
    val horario: String,
    val direccion: String,
    val edadMinima: Int?,          // null = sin restricción de edad
    val descripcion: String,
    val cupoTotal: Int,
    val lugaresDisponibles: Int
) {
    val ocupados: Int get() = cupoTotal - lugaresDisponibles
    val sinLugares: Boolean get() = lugaresDisponibles <= 0
    // Menos del 20 % libre: se pinta en ámbar
    val quedanPocos: Boolean get() = lugaresDisponibles in 1..(cupoTotal / 5).coerceAtLeast(1)
}

data class ArticuloMeta(
    val id: String,
    val nombre: String,
    val meta: Int,
    val apartados: Int
) {
    val faltan: Int get() = (meta - apartados).coerceAtLeast(0)
    val completo: Boolean get() = apartados >= meta
}

data class Campana(
    val id: String,
    val titulo: String,
    val asociacionId: String,
    val categoria: String,
    val cierra: String,
    val urgente: Boolean,
    val descripcion: String,
    val unidadMeta: String,        // "kits", "despensas", "prendas"
    val metaTotal: Int,
    val completados: Int,
    val articulos: List<ArticuloMeta>
) {
    val progreso: Float get() = if (metaTotal == 0) 0f else completados.toFloat() / metaTotal
}

data class Acompanante(
    val nombre: String,
    val edad: Int
)

data class Familia(
    val id: String,
    val nombre: String,
    val ciudad: String,
    val correo: String
)

data class Impacto(
    val actividadesRealizadas: Int,
    val horasDeServicio: Int,
    val campanasApoyadas: Int
)

// ---------------------------------------------------------------------------
// Parte 2 · Actividades
// ---------------------------------------------------------------------------

/**
 * Una actividad junto con la asociación que la organiza, tal como se muestra
 * en la lista y en el detalle.
 *
 * Vive en el dominio y no en ui/: si viviera en ui/, la capa de datos tendría
 * que importar de la capa de arriba para poder devolverlo.
 */
data class ActividadConAsociacion(
    val actividad: Actividad,
    val asociacion: Asociacion
)

/** El pendiente que le queda a la familia después de participar. */
enum class EstadoParticipacion {
    SIN_PENDIENTES,
    ENCUESTA_PENDIENTE,
    TESTIMONIO_EN_REVISION,
    TESTIMONIO_PUBLICADO
}

/**
 * Una actividad en la que la familia ya participó (RF-11).
 * El historial se construye con asistencias registradas por la asociación,
 * no con lo que la familia declare por su cuenta.
 */
data class Participacion(
    val id: String,
    val tituloActividad: String,
    val nombreAsociacion: String,
    val fecha: String,
    val mes: String,
    val estado: EstadoParticipacion
)
