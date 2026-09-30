package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.Familia
import mx.tec.familiasquesuman.domain.Impacto
import mx.tec.familiasquesuman.domain.Participacion

/** La única puerta a los datos de la familia: perfil, impacto, próximas y favoritas. */
class PerfilRepository {

    suspend fun getFamilia(): Familia = DatosDePrueba.familia

    suspend fun getImpacto(): Impacto = DatosDePrueba.impacto

    suspend fun getProximas(): List<Actividad> =
        DatosDePrueba.actividades.filter { it.id in DatosDePrueba.proximasDeLaFamilia }

    suspend fun getFavoritas(): List<Asociacion> =
        DatosDePrueba.asociaciones.filter { it.id in DatosDePrueba.favoritas }

    /** Las próximas, con el nombre de quién las organiza. */
    suspend fun getProximasConAsociacion(): List<ActividadConAsociacion> {
        val porId = DatosDePrueba.asociaciones.associateBy { it.id }
        return getProximas().mapNotNull { actividad ->
            porId[actividad.asociacionId]?.let { ActividadConAsociacion(actividad, it) }
        }
    }

    /** Lo que la familia ya hizo, de lo más reciente a lo más viejo (RF-11). */
    suspend fun getHistorial(): List<Participacion> = DatosDePrueba.historial
}
