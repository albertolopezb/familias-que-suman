package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.MapaCercano
import mx.tec.familiasquesuman.domain.PuntoEnMapa
import mx.tec.familiasquesuman.domain.TipoDePunto

/**
 * Coordenadas de prueba para el mapa "cerca de ti".
 *
 * Son aproximadas: alcanzan para ver punteros repartidos por la zona metropolitana,
 * no para llegar a la puerta. Cuando exista el backend, la latitud y la longitud
 * llegan con cada actividad, proyecto y campaña, y este objeto se borra.
 */
object UbicacionesDePrueba {

    /** Dónde "está" la familia mientras no se pide el GPS: la Macroplaza. */
    val TU_UBICACION = Coordenada(25.6694, -100.3098)

    val actividades = mapOf(
        "act1" to Coordenada(25.6919, -100.2163),   // Hospital Materno Infantil, Guadalupe
        "act2" to Coordenada(25.6830, -100.4720),   // Colonia Sendero, Santa Catarina
        "act9" to Coordenada(25.7480, -100.3620),   // San Bernabé
        "act10" to Coordenada(25.6510, -100.3350),  // Casa de Día Los Robles
        "act11" to Coordenada(25.6870, -100.3220)   // Av. Colón
    )

    val proyectos = mapOf(
        "pr1" to Coordenada(25.7930, -100.3200),    // Escobedo
        "pr2" to Coordenada(25.6730, -100.3050),
        "pr3" to Coordenada(25.7100, -100.3400),
        "pr4" to Coordenada(25.6650, -100.3550),
        "pr5" to Coordenada(25.6931, -100.2148),    // Hospital Materno Infantil
        "pr6" to Coordenada(25.6480, -100.3200),
        "pr7" to Coordenada(25.6842, -100.4698),    // Colonia Sendero
        "pr8" to Coordenada(25.7000, -100.2800)
    )

    val campanas = mapOf(
        "c1" to Coordenada(25.6760, -100.3340),
        "c2" to Coordenada(25.6520, -100.2890),
        "c3" to Coordenada(25.6560, -100.3690),     // Río Amazonas, Del Valle
        "c4" to Coordenada(25.6400, -100.3100)
    )
}

/**
 * Junta actividades, proyectos y campañas de donación en una sola lista de
 * punteros, ordenada por cercanía. Lo que no tiene coordenada no sale en el mapa.
 */
class MapaRepository(
    private val actividadRepository: ActividadRepository,
    private val campanaRepository: CampanaRepository
) {

    /**
     * Hoy es un punto fijo. Aquí entra la ubicación real del teléfono cuando se
     * conecte el mapa: se pide el permiso y se devuelve lo que diga el GPS.
     */
    suspend fun getUbicacionDelUsuario(): Coordenada = UbicacionesDePrueba.TU_UBICACION

    suspend fun getMapaCercano(): MapaCercano {
        val tu = getUbicacionDelUsuario()

        val actividades = actividadRepository.getActividades()
            .filterNot { it.yaPaso }
            .mapNotNull { actividad ->
                UbicacionesDePrueba.actividades[actividad.id]?.let { donde ->
                    PuntoEnMapa(
                        id = actividad.id,
                        tipo = TipoDePunto.ACTIVIDAD,
                        titulo = actividad.titulo,
                        descripcion = actividad.descripcion,
                        cuando = listOf(actividad.fecha, actividad.horario)
                            .filter { it.isNotBlank() }
                            .joinToString(" · "),
                        lugar = actividad.direccion.ifBlank { actividad.municipio },
                        coordenada = donde,
                        distanciaKm = tu.distanciaKmA(donde)
                    )
                }
            }

        val proyectos = actividadRepository.getProyectos()
            .filter { it.activo }
            .mapNotNull { proyecto ->
                UbicacionesDePrueba.proyectos[proyecto.id]?.let { donde ->
                    PuntoEnMapa(
                        id = proyecto.id,
                        tipo = TipoDePunto.PROYECTO,
                        titulo = proyecto.nombre,
                        descripcion = proyecto.resumen.ifBlank { proyecto.descripcion },
                        cuando = proyecto.vigencia ?: "Proyecto permanente",
                        lugar = proyecto.ciudad,
                        coordenada = donde,
                        distanciaKm = tu.distanciaKmA(donde)
                    )
                }
            }

        val donaciones = campanaRepository.getCampanas()
            .mapNotNull { campana ->
                UbicacionesDePrueba.campanas[campana.id]?.let { donde ->
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
                        coordenada = donde,
                        distanciaKm = tu.distanciaKmA(donde)
                    )
                }
            }

        return MapaCercano(
            tuUbicacion = tu,
            puntos = (actividades + proyectos + donaciones).sortedBy { it.distanciaKm }
        )
    }
}
