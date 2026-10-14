package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.MapaDeCiudad
import mx.tec.familiasquesuman.domain.PuntoEnMapa
import mx.tec.familiasquesuman.domain.TipoDePunto

/**
 * Junta actividades, proyectos y campañas de donación de una ciudad en una sola lista de
 * punteros. La latitud y la longitud llegan de la API con cada cosa; lo que no tiene, no sale en
 * el mapa. No usa la ubicación del teléfono: el mapa es una vista general de la ciudad elegida.
 */
class MapaRepository(
    private val actividadRepository: ActividadRepository,
    private val campanaRepository: CampanaRepository
) {

    suspend fun getMapaDeCiudad(ciudad: String): MapaDeCiudad {
        val ciudades = runCatching { actividadRepository.getCiudades() }.getOrDefault(emptyList())
        val centro = (ciudades.firstOrNull { it.nombre == ciudad } ?: ciudades.firstOrNull())
            ?.let { Coordenada(it.latitud, it.longitud) }
            ?: Coordenada(25.6694, -100.3098)                  // Macroplaza, si la API no contestó

        val actividades = actividadRepository.getActividades()
            .filter { !it.yaPaso && it.ciudad == ciudad }
            .mapNotNull { actividad ->
                actividad.coordenada?.let { donde ->
                    PuntoEnMapa(
                        id = actividad.id,
                        tipo = TipoDePunto.ACTIVIDAD,
                        titulo = actividad.titulo,
                        descripcion = actividad.descripcion,
                        cuando = listOf(actividad.fecha, actividad.horario).filter { it.isNotBlank() }.joinToString(" · "),
                        lugar = actividad.direccion.ifBlank { actividad.municipio },
                        coordenada = donde
                    )
                }
            }

        // Los proyectos no siempre dicen la ciudad de Familias que Suman (algunos dicen el
        // municipio): salen los que tienen lugar en el mapa cerca de la ciudad elegida.
        val proyectos = actividadRepository.getProyectos()
            .filter { it.activo }
            .mapNotNull { proyecto ->
                proyecto.coordenada?.takeIf { cerca(it, centro) }?.let { donde ->
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
            .filter { it.ciudad == ciudad }
            .mapNotNull { campana ->
                campana.coordenada?.let { donde ->
                    PuntoEnMapa(
                        id = campana.id,
                        tipo = TipoDePunto.DONACION,
                        titulo = campana.titulo,
                        descripcion = campana.descripcion,
                        cuando = if (campana.cierra.isBlank()) "Sin fecha límite" else "Se necesita antes del ${campana.cierra}",
                        lugar = campana.puntosEntrega.firstOrNull()?.let { "${it.direccion}, ${it.colonia}" } ?: campana.ciudad,
                        coordenada = donde
                    )
                }
            }

        return MapaDeCiudad(ciudad = ciudad, centro = centro, puntos = actividades + proyectos + donaciones)
    }

    /** ~100 km alrededor del centro de la ciudad. */
    private fun cerca(a: Coordenada, b: Coordenada) =
        kotlin.math.abs(a.latitud - b.latitud) < 1.0 && kotlin.math.abs(a.longitud - b.longitud) < 1.0
}
