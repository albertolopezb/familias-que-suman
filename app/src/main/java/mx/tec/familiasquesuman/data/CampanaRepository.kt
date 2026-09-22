package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Campana

/** La única puerta a las campañas de donación. Ver la nota de `suspend` en ActividadRepository. */
class CampanaRepository {

    suspend fun getCampanas(): List<Campana> = DatosDePrueba.campanas

    suspend fun getCampana(id: String): Campana =
        DatosDePrueba.campanas.first { it.id == id }
}
