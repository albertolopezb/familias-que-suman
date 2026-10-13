package mx.tec.familiasquesuman.domain

// Kotlin puro, igual que Modelos.kt. Vive en su propio archivo para no tocar
// Modelos.kt mientras el equipo hace el merge; si conviene, se junta después.

/** Un lugar en el mapa: latitud y longitud. */
data class Coordenada(val latitud: Double, val longitud: Double)

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
