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

    suspend fun getAsociaciones(): List<Asociacion> = DatosDePrueba.asociaciones.toList()

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

    suspend fun getProyectos(): List<Proyecto> = DatosDePrueba.proyectos.toList()

    suspend fun getCentros(): List<CentroVisiteo> = DatosDePrueba.centros.toList()

    // ── Administración (solo en memoria, como el resto de los datos de prueba) ──

    suspend fun agregarAsociacion(asociacion: Asociacion) {
        DatosDePrueba.asociaciones.add(asociacion)
    }

    suspend fun editarAsociacion(asociacion: Asociacion) {
        val i = DatosDePrueba.asociaciones.indexOfFirst { it.id == asociacion.id }
        if (i >= 0) DatosDePrueba.asociaciones[i] = asociacion
    }

    suspend fun borrarAsociacion(id: String) {
        DatosDePrueba.asociaciones.removeAll { it.id == id }
    }

    suspend fun agregarProyecto(proyecto: Proyecto) {
        DatosDePrueba.proyectos.add(0, proyecto)
    }

    suspend fun editarProyecto(proyecto: Proyecto) {
        val i = DatosDePrueba.proyectos.indexOfFirst { it.id == proyecto.id }
        if (i >= 0) DatosDePrueba.proyectos[i] = proyecto
    }

    suspend fun borrarProyecto(id: String) {
        DatosDePrueba.proyectos.removeAll { it.id == id }
    }

    suspend fun agregarCentro(centro: CentroVisiteo) {
        DatosDePrueba.centros.add(0, centro)
    }

    suspend fun editarCentro(centro: CentroVisiteo) {
        val i = DatosDePrueba.centros.indexOfFirst { it.id == centro.id }
        if (i >= 0) DatosDePrueba.centros[i] = centro
    }

    suspend fun borrarCentro(id: String) {
        DatosDePrueba.centros.removeAll { it.id == id }
    }
}