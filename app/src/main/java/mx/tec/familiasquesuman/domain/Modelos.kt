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
    val lugaresDisponibles: Int,
    // Parte 2: lo que muestra familiasquesuman.com/actividades. Todo lleva valor
    // por defecto para que nadie que ya construya una Actividad tenga que cambiar.
    val municipio: String = "",
    val tema: TemaActividad = TemaActividad.GENERAL,
    val aportacion: Aportacion = Aportacion.Ninguna,
    val puntoDeEncuentro: String = "",
    val acercaDelProyecto: String = "",
    val queHaremos: String = "",
    val queIncluye: String = "",
    val queLlevar: String = "",
    val recomendaciones: String = "",
    val foto: String? = null,       // nombre del drawable, sin extensión
    val yaPaso: Boolean = false
) {
    val ocupados: Int get() = cupoTotal - lugaresDisponibles
    val sinLugares: Boolean get() = lugaresDisponibles <= 0
    // Menos del 20 % libre: se pinta en ámbar
    val quedanPocos: Boolean get() = lugaresDisponibles in 1..(cupoTotal / 5).coerceAtLeast(1)
    // Algunas actividades del sitio no publican cupo; en esas no hay barra.
    val tieneCupo: Boolean get() = cupoTotal > 0
}

/** De qué va la actividad. Decide el ícono y su color, como en el sitio. */
enum class TemaActividad { SALUD, CELEBRACION, MEDIO_AMBIENTE, GENERAL }

/** Lo que cada familia lleva o paga para participar. */
sealed interface Aportacion {
    data object Ninguna : Aportacion
    data class EnEspecie(val detalle: String = "") : Aportacion
    data class Monetaria(val monto: String, val detalle: String = "") : Aportacion
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

/** Una opción de donación con su precio (Destellos de Luz). Solo informa: la app no cobra. */
data class OpcionDonacion(
    val nombre: String,
    val precio: String             // "$700"
)

/** Un punto donde se entrega lo recolectado (Suma a su Mesa). */
data class PuntoEntrega(
    val direccion: String,         // "Calle Cóndor 1001"
    val colonia: String            // "Fraccionamiento Azhara"
)

data class Campana(
    val id: String,
    val titulo: String,
    val asociacionId: String,
    val categoria: String,
    val cierra: String,            // "30 de diciembre"; vacío si la campaña no tiene fecha límite
    val urgente: Boolean,
    val descripcion: String,
    val unidadMeta: String,        // "kits", "despensas", "prendas"
    val metaTotal: Int,
    val completados: Int,
    val articulos: List<ArticuloMeta>,
    // --- Lo que trae cada campaña en familiasquesuman.com/donar. Todo opcional. ---
    val imagen: String? = null,               // nombre del drawable, sin extensión
    val ciudad: String = "Monterrey",
    val textoBoton: String = "Quiero ayudar", // "Quiero juntar" en Tapitas
    val telefono: String? = null,             // solo dígitos
    val whatsapp: String? = null,             // solo dígitos; sin WhatsApp, el botón llama
    val contactoNombre: String? = null,
    val metaTexto: String? = null,            // "250-300 cuentos"
    val descripcionLarga: String = "",
    val comoAyudar: String = "",
    val opcionesDonacion: List<OpcionDonacion> = emptyList(),
    val puntosEntrega: List<PuntoEntrega> = emptyList(),
    val instagram: String? = null             // enlace completo
) {
    val progreso: Float get() = if (metaTotal == 0) 0f else completados.toFloat() / metaTotal

    /** Solo las campañas con una meta de artículos se pueden apartar (RF-21). */
    val sePuedeApartar: Boolean get() = articulos.isNotEmpty()
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

// ---------------------------------------------------------------------------
// Proyectos y Directorio de Visiteo, como en familiasquesuman.com
// ---------------------------------------------------------------------------

/** Un proyecto con causa y objetivo específicos (RF-09). */
data class Proyecto(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val beneficiarios: String?,     // "25 Mujeres", "200 adultos mayores"
    val ciudad: String,
    val vigencia: String?,          // "Hasta 29 jun 2026"
    val logo: String?,              // nombre del drawable, sin extensión
    val activo: Boolean = true
)

/** Un centro verificado que se puede visitar en familia (RF-03). */
data class CentroVisiteo(
    val id: String,
    val nombre: String,
    val tipo: String,               // "Asilos", "Casas hogar", "Comedores"
    val resumen: String,
    val informacion: String,
    val necesidades: List<String>,
    val direccion: String,
    val logo: String?,
    val verificado: Boolean = true
)
