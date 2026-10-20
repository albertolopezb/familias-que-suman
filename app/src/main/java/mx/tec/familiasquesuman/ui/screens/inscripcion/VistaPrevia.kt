package mx.tec.familiasquesuman.ui.screens.inscripcion

import mx.tec.familiasquesuman.domain.Acompanante
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Asociacion

/** Solo para los @Preview de esta carpeta. La app real lee los repositorios. */
internal object VistaPrevia {
    val asociacion = Asociacion(
        "a1", "Comedor Comunitario San Bernabé", "Alimentación", "",
        "Av. Rómulo Garza 240, Col. San Bernabé, Monterrey", "", "", ""
    )
    val actividad = Actividad(
        "act1", "Preparar despensas de fin de mes", "a1",
        "Sábado 12 de septiembre", "9:00 a 12:00", "Av. Rómulo Garza 240, Col. San Bernabé",
        edadMinima = 6, descripcion = "", cupoTotal = 20, lugaresDisponibles = 8
    )
    val titular = Acompanante("Ana Rodríguez", 38)
    val acompanantes = listOf(Acompanante("Mateo Rodríguez", 9), Acompanante("Renata Rodríguez", 7))
}
