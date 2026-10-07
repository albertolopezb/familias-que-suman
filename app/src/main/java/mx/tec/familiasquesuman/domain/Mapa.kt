package mx.tec.familiasquesuman.domain

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// Kotlin puro, igual que Modelos.kt. Vive en su propio archivo para no tocar
// Modelos.kt mientras el equipo hace el merge; si conviene, se junta después.

/** Un lugar en el mapa. */
data class Coordenada(val latitud: Double, val longitud: Double) {

    /** Distancia en línea recta, en kilómetros (fórmula de haversine). */
    fun distanciaKmA(otra: Coordenada): Double {
        val radioTierraKm = 6371.0
        val dLat = Math.toRadians(otra.latitud - latitud)
        val dLng = Math.toRadians(otra.longitud - longitud)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(latitud)) * cos(Math.toRadians(otra.latitud)) * sin(dLng / 2).pow(2)
        return 2 * radioTierraKm * asin(sqrt(a))
    }
}

/** Las tres cosas que enseña el mapa "cerca de ti". Cada una trae sus textos. */
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
    val coordenada: Coordenada,
    val distanciaKm: Double     // desde la ubicación de la familia
) {
    /** Único entre los tres tipos: una actividad y un proyecto pueden repetir id. */
    val clave: String get() = "${tipo.name}:$id"
}

/** Lo que necesita la vista de mapa: dónde está la familia y qué hay cerca. */
data class MapaCercano(
    val tuUbicacion: Coordenada,
    val puntos: List<PuntoEnMapa>   // del más cercano al más lejano
)
