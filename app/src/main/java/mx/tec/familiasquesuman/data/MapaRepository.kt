package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.MapaDeCiudad
import mx.tec.familiasquesuman.domain.PuntoEnMapa
import mx.tec.familiasquesuman.domain.TipoDePunto

/** Lo que se sabe de una ciudad en el mapa: hacia dónde mira y dónde está cada cosa. */
class UbicacionesDeCiudad(
    val centro: Coordenada,
    val actividades: Map<String, Coordenada> = emptyMap(),
    val proyectos: Map<String, Coordenada> = emptyMap(),
    val campanas: Map<String, Coordenada> = emptyMap()
)

/**
 * Coordenadas de prueba para el mapa del Inicio, por ciudad.
 *
 * Son aproximadas: alcanzan para ver punteros repartidos por la ciudad, no para
 * llegar a la puerta. Cuando exista el backend, la latitud y la longitud llegan con
 * cada actividad, proyecto y campaña, y este objeto se borra.
 *
 * Hermosillo todavía no tiene actividades, proyectos ni campañas con lugar: su mapa
 * se abre centrado en la ciudad y vacío hasta que se publique algo.
 */
object UbicacionesDePrueba {

    val porCiudad = mapOf(
        "Monterrey" to UbicacionesDeCiudad(
            centro = Coordenada(25.6694, -100.3098),    // Macroplaza
            actividades = mapOf(
                "act1" to Coordenada(25.6919, -100.2163),   // Hospital Materno Infantil, Guadalupe
                "act2" to Coordenada(25.6830, -100.4720),   // Colonia Sendero, Santa Catarina
                "act9" to Coordenada(25.7480, -100.3620),   // San Bernabé
                "act10" to Coordenada(25.6510, -100.3350),  // Casa de Día Los Robles
                "act11" to Coordenada(25.6870, -100.3220)   // Av. Colón
            ),
            proyectos = mapOf(
                "pr1" to Coordenada(25.7930, -100.3200),    // Escobedo
                "pr2" to Coordenada(25.6730, -100.3050),
                "pr3" to Coordenada(25.7100, -100.3400),
                "pr4" to Coordenada(25.6650, -100.3550),
                "pr5" to Coordenada(25.6931, -100.2148),    // Hospital Materno Infantil
                "pr6" to Coordenada(25.6480, -100.3200),
                "pr7" to Coordenada(25.6842, -100.4698),    // Colonia Sendero
                "pr8" to Coordenada(25.7000, -100.2800)
            ),
            campanas = mapOf(
                "c1" to Coordenada(25.6760, -100.3340),
                "c2" to Coordenada(25.6520, -100.2890),
                "c3" to Coordenada(25.6560, -100.3690),     // Río Amazonas, Del Valle
                "c4" to Coordenada(25.6400, -100.3100)
            )
        ),
        "Hermosillo" to UbicacionesDeCiudad(
            centro = Coordenada(29.0729, -110.9559)         // Plaza Zaragoza
        )
    )

    /** Una ciudad que no está en la lista se ve vacía, centrada en la primera. */
    fun de(ciudad: String): UbicacionesDeCiudad =
        porCiudad[ciudad] ?: UbicacionesDeCiudad(centro = porCiudad.getValue(CIUDADES.first()).centro)
}

/**
 * Junta actividades, proyectos y campañas de donación de una ciudad en una sola lista
 * de punteros. Lo que no tiene coordenada no sale en el mapa. No usa la ubicación del
 * teléfono: el mapa es una vista general de la ciudad que se eligió.
 */
class MapaRepository(
    private val actividadRepository: ActividadRepository,
    private val campanaRepository: CampanaRepository
) {

    suspend fun getMapaDeCiudad(ciudad: String): MapaDeCiudad {
        val lugares = UbicacionesDePrueba.de(ciudad)

        val actividades = actividadRepository.getActividades()
            .filterNot { it.yaPaso }
            .mapNotNull { actividad ->
                lugares.actividades[actividad.id]?.let { donde ->
                    PuntoEnMapa(
                        id = actividad.id,
                        tipo = TipoDePunto.ACTIVIDAD,
                        titulo = actividad.titulo,
                        descripcion = actividad.descripcion,
                        cuando = listOf(actividad.fecha, actividad.horario)
                            .filter { it.isNotBlank() }
                            .joinToString(" · "),
                        lugar = actividad.direccion.ifBlank { actividad.municipio },
                        coordenada = donde
                    )
                }
            }

        val proyectos = actividadRepository.getProyectos()
            .filter { it.activo }
            .mapNotNull { proyecto ->
                lugares.proyectos[proyecto.id]?.let { donde ->
                    PuntoEnMapa(
                        id = proyecto.id,
                        tipo = TipoDePunto.PROYECTO,
                        titulo = proyecto.nombre,
                        descripcion = proyecto.resumen.ifBlank { proyecto.descripcion },
                        cuando = proyecto.vigencia ?: "Proyecto permanente",
                        lugar = proyecto.ciudad,
                        coordenada = donde
                    )
                }
            }

        val donaciones = campanaRepository.getCampanas()
            .mapNotNull { campana ->
                lugares.campanas[campana.id]?.let { donde ->
                    PuntoEnMapa(
                        id = campana.id,
                        tipo = TipoDePunto.DONACION,
                        titulo = campana.titulo,
                        descripcion = campana.descripcion,
                        cuando = if (campana.cierra.isBlank()) "Sin fecha límite"
                        else "Se necesita antes del ${campana.cierra}",
                        lugar = campana.puntosEntrega.firstOrNull()
                            ?.let { "${it.direccion}, ${it.colonia}" }
                            ?: campana.ciudad,
                        coordenada = donde
                    )
                }
            }

        return MapaDeCiudad(
            ciudad = ciudad,
            centro = lugares.centro,
            puntos = actividades + proyectos + donaciones
        )
    }
}
