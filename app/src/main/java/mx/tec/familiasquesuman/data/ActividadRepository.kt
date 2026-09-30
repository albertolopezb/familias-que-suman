package mx.tec.familiasquesuman.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.CentroVisiteo
import mx.tec.familiasquesuman.domain.Proyecto

/**
 * La única puerta a actividades y asociaciones.
 *
 * Las funciones son `suspend` desde hoy aunque los datos estén en memoria:
 * cuando entre Retrofit van a tardar, y así ningún ViewModel tiene que cambiar.
 * Android Studio avisa "redundant suspend modifier"; es a propósito.
 */
class ActividadRepository {

    /**
     * Las actividades viven aquí y no en DatosDePrueba para que el panel de
     * administración pueda crear, editar y borrar, y las familias lo vean al
     * momento. Todo es en memoria: al cerrar la app se pierde, como el resto.
     */
    private val _actividades = MutableStateFlow(DatosDePrueba.actividades)

    /** Cambia cada vez que el admin guarda o borra algo. */
    val actividades: StateFlow<List<Actividad>> = _actividades.asStateFlow()

    suspend fun getActividades(): List<Actividad> = _actividades.value

    suspend fun getActividad(id: String): Actividad =
        _actividades.value.first { it.id == id }

    suspend fun getAsociaciones(): List<Asociacion> = DatosDePrueba.asociaciones

    suspend fun getAsociacion(id: String): Asociacion =
        DatosDePrueba.asociaciones.first { it.id == id }

    /** Quienes pueden organizar una actividad: asociaciones y organizadores del sitio. */
    suspend fun getOrganizadores(): List<Asociacion> =
        DatosDePrueba.organizadores + DatosDePrueba.asociaciones

    /**
     * Las actividades con el nombre de quién las organiza, que es lo que la
     * tarjeta de la lista necesita. El cruce se hace aquí y no en la pantalla:
     * cuando entre el backend esto será un solo endpoint y nada de arriba cambia.
     */
    suspend fun getActividadesConAsociacion(): List<ActividadConAsociacion> {
        val porId = organizadoresPorId()
        return _actividades.value.mapNotNull { actividad ->
            porId[actividad.asociacionId]?.let { ActividadConAsociacion(actividad, it) }
        }
    }

    suspend fun getActividadConAsociacion(id: String): ActividadConAsociacion {
        val actividad = getActividad(id)
        return ActividadConAsociacion(actividad, organizadoresPorId().getValue(actividad.asociacionId))
    }

    // -----------------------------------------------------------------------
    // Administración (en memoria hasta que exista el servidor)
    // -----------------------------------------------------------------------

    /** Crea la actividad si su id es nuevo, o la reemplaza si ya existía. */
    suspend fun guardarActividad(actividad: Actividad) {
        _actividades.update { lista ->
            if (lista.any { it.id == actividad.id }) {
                lista.map { if (it.id == actividad.id) actividad else it }
            } else {
                // Las nuevas van hasta arriba, antes de las que ya pasaron.
                listOf(actividad) + lista
            }
        }
    }

    suspend fun eliminarActividad(id: String) {
        _actividades.update { lista -> lista.filterNot { it.id == id } }
    }

    fun nuevoIdDeActividad(): String = "act-${System.currentTimeMillis()}"

    // -----------------------------------------------------------------------
    // Proyectos y directorio de visiteo, como en el sitio
    // -----------------------------------------------------------------------

    suspend fun getProyectos(): List<Proyecto> = DatosDePrueba.proyectos

    suspend fun getCentros(): List<CentroVisiteo> = DatosDePrueba.centros

    /** Quien organiza puede ser una asociación del directorio o uno de los organizadores del sitio. */
    private fun organizadoresPorId(): Map<String, Asociacion> =
        (DatosDePrueba.asociaciones + DatosDePrueba.organizadores).associateBy { it.id }
}
