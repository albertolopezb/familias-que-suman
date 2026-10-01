package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Campana

/** La única puerta a las campañas de donación. Ver la nota de `suspend` en ActividadRepository. */
class CampanaRepository {

    suspend fun getCampanas(): List<Campana> = DatosDePrueba.campanas.toList()

    suspend fun agregarCampana(campana: Campana) {
        DatosDePrueba.campanas.add(0, campana)
    }

    suspend fun editarCampana(campana: Campana) {
        val i = DatosDePrueba.campanas.indexOfFirst { it.id == campana.id }
        if (i >= 0) DatosDePrueba.campanas[i] = campana
    }

    suspend fun borrarCampana(id: String) {
        DatosDePrueba.campanas.removeAll { it.id == id }
    }

    suspend fun getCampana(id: String): Campana =
        DatosDePrueba.campanas.first { it.id == id }
}
