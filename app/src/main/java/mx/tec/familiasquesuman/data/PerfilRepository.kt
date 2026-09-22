package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.Familia
import mx.tec.familiasquesuman.domain.Impacto

/** La única puerta a los datos de la familia: perfil, impacto, próximas y favoritas. */
class PerfilRepository {

    suspend fun getFamilia(): Familia = DatosDePrueba.familia

    suspend fun getImpacto(): Impacto = DatosDePrueba.impacto

    suspend fun getProximas(): List<Actividad> =
        DatosDePrueba.actividades.filter { it.id in DatosDePrueba.proximasDeLaFamilia }

    suspend fun getFavoritas(): List<Asociacion> =
        DatosDePrueba.asociaciones.filter { it.id in DatosDePrueba.favoritas }
}
