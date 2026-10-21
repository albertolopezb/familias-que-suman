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
 * "Buscar cerca de" (RF-02): de lo más cercano a lo más lejano a [aqui]. Lo que no tiene ubicación
 * va al final, en el orden que traía (sortedBy es estable). Se calcula en el teléfono.
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

/** "a 850 m", "a 2.3 km", "a 14 km": lo que se lee junto a cada lugar al buscar cerca de algo. */
fun textoDeDistancia(km: Double): String = when {
    km < 1.0 -> "a ${((km * 1000).toInt() / 50 * 50).coerceAtLeast(50)} m"
    km < 10.0 -> "a ${"%.1f".format(java.util.Locale.ROOT, km)} km"
    else -> "a ${km.toInt()} km"
}

/** El punto desde el que se busca: un municipio, una dirección o un punto que se tocó en el mapa. */
data class LugarDeReferencia(val nombre: String, val coordenada: Coordenada)

/**
 * Los municipios de las dos ciudades, para que "Apodaca" o "San Nicolás" se encuentren al momento
 * y sin internet. Lo demás (colonias, calles) se busca en OpenStreetMap (ver BuscadorDeLugares).
 * El centro de cada municipio es aproximado: sirve para ordenar por cercanía, no para llegar.
 */
private val MUNICIPIOS: List<Pair<List<String>, LugarDeReferencia>> = listOf(
    listOf("monterrey", "centro de monterrey", "macroplaza") to LugarDeReferencia("Monterrey", Coordenada(25.6694, -100.3098)),
    listOf("apodaca", "ciudad apodaca") to LugarDeReferencia("Apodaca", Coordenada(25.7817, -100.1886)),
    listOf("guadalupe") to LugarDeReferencia("Guadalupe", Coordenada(25.6775, -100.2597)),
    listOf("san nicolas", "san nicolas de los garza") to LugarDeReferencia("San Nicolás de los Garza", Coordenada(25.7417, -100.3022)),
    listOf("escobedo", "general escobedo") to LugarDeReferencia("General Escobedo", Coordenada(25.7972, -100.3330)),
    listOf("santa catarina") to LugarDeReferencia("Santa Catarina", Coordenada(25.6733, -100.4583)),
    listOf("san pedro", "san pedro garza garcia") to LugarDeReferencia("San Pedro Garza García", Coordenada(25.6573, -100.4023)),
    listOf("garcia") to LugarDeReferencia("García", Coordenada(25.8144, -100.5856)),
    listOf("juarez", "ciudad benito juarez") to LugarDeReferencia("Juárez", Coordenada(25.6475, -100.0956)),
    listOf("santiago") to LugarDeReferencia("Santiago", Coordenada(25.4247, -100.1517)),
    listOf("cadereyta", "cadereyta jimenez") to LugarDeReferencia("Cadereyta Jiménez", Coordenada(25.5917, -99.9833)),
    listOf("pesqueria") to LugarDeReferencia("Pesquería", Coordenada(25.7853, -100.0500)),
    listOf("salinas victoria") to LugarDeReferencia("Salinas Victoria", Coordenada(25.9608, -100.2917)),
    listOf("cienega de flores") to LugarDeReferencia("Ciénega de Flores", Coordenada(25.9539, -100.1586)),
    listOf("zuazua", "general zuazua") to LugarDeReferencia("General Zuazua", Coordenada(25.8950, -100.1089)),
    listOf("hermosillo", "centro de hermosillo") to LugarDeReferencia("Hermosillo", Coordenada(29.0729, -110.9559)),
    listOf("bahia de kino", "kino") to LugarDeReferencia("Bahía de Kino", Coordenada(28.8217, -111.9411)),
    listOf("miguel aleman", "poblado miguel aleman") to LugarDeReferencia("Poblado Miguel Alemán", Coordenada(28.8406, -111.4747))
)

/** Sin acentos, en minúsculas y sin ", N.L." / ", Sonora" al final: "San Nicolás, NL" → "san nicolas". */
internal fun normalizarLugar(texto: String): String =
    java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
        .replace(Regex("""\p{Mn}+"""), "")
        .lowercase()
        .replace(Regex("[.]"), "")
        .replace(Regex(""",?\s*(nl|nuevo leon|son|sonora|mexico|mx)\s*$"""), "")
        .replace(Regex("""\s+"""), " ")
        .trim()

/** Un municipio de la lista si [texto] es su nombre; si no, null (y se busca en OpenStreetMap). */
fun municipioConocido(texto: String): LugarDeReferencia? {
    val buscado = normalizarLugar(texto)
    if (buscado.isEmpty()) return null
    return MUNICIPIOS.firstOrNull { (nombres, _) -> buscado in nombres }?.second
}

/** Las cuatro cosas que enseña el mapa de la ciudad. Cada una trae sus textos. */
enum class TipoDePunto(
    val etiqueta: String,       // "Actividad"
    val plural: String,         // "Actividades"
    val textoBoton: String      // el botón del mini menú
) {
    ACTIVIDAD("Actividad", "Actividades", "Ver actividad"),
    PROYECTO("Proyecto", "Proyectos", "Ver proyecto"),
    DONACION("Donación", "Donaciones", "Ver donación"),
    DIRECTORIO("Centro", "Directorio", "Ver centro")
}

/**
 * Un puntero del mapa: lo mínimo de una actividad, un proyecto, una campaña de donación o un
 * centro del Directorio para pintarlo y llenar su mini menú. Lo demás se ve en su detalle.
 */
data class PuntoEnMapa(
    val id: String,             // el id de la actividad, proyecto, campaña o centro
    val tipo: TipoDePunto,
    val titulo: String,
    val descripcion: String,    // breve; el mini menú la corta a tres renglones
    val cuando: String,         // cuándo se realiza o hasta cuándo se necesita
    val lugar: String,
    val coordenada: Coordenada
) {
    /** Único entre los cuatro tipos: una actividad y un proyecto pueden repetir id. */
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
