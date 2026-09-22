package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Actividad
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
}
