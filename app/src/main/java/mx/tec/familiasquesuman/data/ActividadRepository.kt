package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.CentroVisiteo
import mx.tec.familiasquesuman.domain.Proyecto

class ActividadRepository {

    suspend fun getActividades(): List<Actividad> = DatosDePrueba.actividades

    suspend fun getActividad(id: String): Actividad =
        DatosDePrueba.actividades.first { it.id == id }

    suspend fun getAsociaciones(): List<Asociacion> = DatosDePrueba.asociaciones

    suspend fun getAsociacion(id: String): Asociacion =
        DatosDePrueba.asociaciones.first { it.id == id }

    suspend fun agregarActividad(actividad: Actividad) {
        (DatosDePrueba.actividades as? MutableList)?.add(0, actividad)
    }

    suspend fun borrarActividad(id: String) {
        (DatosDePrueba.actividades as? MutableList)?.removeIf { it.id == id }
    }

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

    private fun organizadoresPorId(): Map<String, Asociacion> =
        (DatosDePrueba.asociaciones + DatosDePrueba.organizadores).associateBy { it.id }

    suspend fun getProyectos(): List<Proyecto> = DatosDePrueba.proyectos

    suspend fun getCentros(): List<CentroVisiteo> = DatosDePrueba.centros
}