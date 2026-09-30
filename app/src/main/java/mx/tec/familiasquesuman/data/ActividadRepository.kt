package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.Asociacion

/**
 * La única puerta a actividades y asociaciones.
 *
 * Las funciones son `suspend` desde hoy aunque los datos estén en memoria:
 * cuando entre Retrofit van a tardar, y así ningún ViewModel tiene que cambiar.
 * Android Studio avisa "redundant suspend modifier"; es a propósito.
 */
class ActividadRepository {

    suspend fun getActividades(): List<Actividad> = DatosDePrueba.actividades

    suspend fun getActividad(id: String): Actividad =
        DatosDePrueba.actividades.first { it.id == id }

    suspend fun getAsociaciones(): List<Asociacion> = DatosDePrueba.asociaciones

    suspend fun getAsociacion(id: String): Asociacion =
        DatosDePrueba.asociaciones.first { it.id == id }

    /**
     * Las actividades con el nombre de quién las organiza, que es lo que la
     * tarjeta de la lista necesita. El cruce se hace aquí y no en la pantalla:
     * cuando entre el backend esto será un solo endpoint y nada de arriba cambia.
     */
    suspend fun getActividadesConAsociacion(): List<ActividadConAsociacion> {
        val porId = organizadoresPorId()
        return DatosDePrueba.actividades.mapNotNull { actividad ->
            porId[actividad.asociacionId]?.let { ActividadConAsociacion(actividad, it) }
        }
    }

    suspend fun getActividadConAsociacion(id: String): ActividadConAsociacion {
        val actividad = getActividad(id)
        return ActividadConAsociacion(actividad, organizadoresPorId().getValue(actividad.asociacionId))
    }

    /** Quien organiza puede ser una asociación del directorio o uno de los organizadores del sitio. */
    private fun organizadoresPorId(): Map<String, Asociacion> =
        (DatosDePrueba.asociaciones + DatosDePrueba.organizadores).associateBy { it.id }
}
