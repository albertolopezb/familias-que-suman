package mx.tec.familiasquesuman.domain

// Kotlin puro, igual que Modelos.kt. Vive en su propio archivo para no tocar
// Modelos.kt mientras el equipo hace el merge; si conviene, se junta después.

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Un lugar en el mapa: latitud y longitud. */
data class Coordenada(val latitud: Double, val longitud: Double) {
    /** Como se escribe en el formulario de administración: "25.669400, -100.309800". */
    fun comoTexto(): String = "%.6f, %.6f".format(java.util.Locale.ROOT, latitud, longitud)

    /** Distancia en km en línea recta (fórmula del haversine). Para ordenar por cercanía basta. */
    fun distanciaKm(otra: Coordenada): Double {
        val dLat = Math.toRadians(otra.latitud - latitud)
        val dLng = Math.toRadians(otra.longitud - longitud)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(latitud)) * cos(Math.toRadians(otra.latitud)) * sin(dLng / 2).pow(2)
        return 2 * RADIO_TIERRA_KM * asin(sqrt(a))
    }

    private companion object {
        const val RADIO_TIERRA_KM = 6371.0
    }
}

/**
 * "Cerca de mí" (RF-02): de lo más cercano a lo más lejano. Lo que no tiene ubicación va al
 * final, en el orden que traía (sortedBy es estable). Se calcula en el teléfono: [aqui] nunca
 * sale de él.
 */
fun <T> List<T>.porCercania(aqui: Coordenada, dondeEsta: (T) -> Coordenada?): List<T> =
    sortedBy { dondeEsta(it)?.distanciaKm(aqui) ?: Double.MAX_VALUE }

/**
 * Lo que escribe la administración en el campo "Ubicación en el mapa", convertido en coordenada.
 * Acepta las coordenadas tal cual ("25.6694, -100.3098", lo que copia Google Maps al dejar el dedo
 * sobre un punto) o un enlace largo de Google Maps: primero el pin exacto (`!3d<lat>!4d<lng>`) y,
 * si no lo trae, el centro de la vista (`@<lat>,<lng>`). Los enlaces cortos (maps.app.goo.gl) no
 * traen coordenadas: con ellos, y con cualquier cosa fuera de rango, regresa null.
 */
fun coordenadaDeTexto(texto: String): Coordenada? {
    val limpio = texto.trim()
    if (limpio.isEmpty()) return null
    val numero = """(-?\d{1,3}(?:\.\d+)?)"""
    val patrones = listOf(
        Regex("""!3d$numero!4d$numero"""),
        Regex("""@$numero,\s*$numero"""),
        Regex("""^$numero\s*,\s*$numero$""")
    )
    for (patron in patrones) {
        val m = patron.find(limpio) ?: continue
        val lat = m.groupValues[1].toDoubleOrNull() ?: continue
        val lng = m.groupValues[2].toDoubleOrNull() ?: continue
        if (lat in -90.0..90.0 && lng in -180.0..180.0) return Coordenada(lat, lng)
    }
    return null
}

/** Las tres cosas que enseña el mapa de la ciudad. Cada una trae sus textos. */
enum class TipoDePunto(
    val etiqueta: String,       // "Actividad"
    val plural: String,         // "Actividades"
    val textoBoton: String      // el botón del mini menú
) {
    ACTIVIDAD("Actividad", "Actividades", "Ver actividad"),
    PROYECTO("Proyecto", "Proyectos", "Ver proyecto"),
    DONACION("Donación", "Donaciones", "Ver donación")
}

/**
 * Un puntero del mapa: lo mínimo de una actividad, un proyecto o una campaña
 * de donación para pintarlo y llenar su mini menú. Lo demás se ve en su detalle.
 */
data class PuntoEnMapa(
    val id: String,             // el id de la actividad, proyecto o campaña
    val tipo: TipoDePunto,
    val titulo: String,
    val descripcion: String,    // breve; el mini menú la corta a tres renglones
    val cuando: String,         // cuándo se realiza o hasta cuándo se necesita
    val lugar: String,
    val coordenada: Coordenada
) {
    /** Único entre los tres tipos: una actividad y un proyecto pueden repetir id. */
    val clave: String get() = "${tipo.name}:$id"
}

/**
 * Lo que necesita el mapa del Inicio: la ciudad elegida y sus punteros. No usa la
 * ubicación del teléfono: es una vista general de la ciudad, igual para todos.
 */
data class MapaDeCiudad(
    val ciudad: String,             // "Monterrey" o "Hermosillo"
    val centro: Coordenada,         // hacia dónde mira el mapa si no hay punteros
    val puntos: List<PuntoEnMapa>
)
