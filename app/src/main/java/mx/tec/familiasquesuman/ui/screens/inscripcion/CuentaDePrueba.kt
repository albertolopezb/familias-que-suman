package mx.tec.familiasquesuman.ui.screens.inscripcion

import mx.tec.familiasquesuman.domain.Acompanante
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Asociacion

/**
 * Datos de prueba que el Figma usa y que todavía no están en DatosDePrueba.
 * domain/ y data/ están congelados: ya se pidieron en el chat del equipo. Cuando
 * quien integra los agregue, esto se borra y CuentaViewModel los lee del repositorio.
 * La familia y su correo SÍ salen de PerfilRepository.
 */
internal object CuentaDePrueba {
    const val CONTRASENA = "familia123"

    /** La persona de la cuenta: primera fila fija de P-06. */
    val titular = Acompanante("Ana Rodríguez", 38)

    /** Los que Ana ya registró antes; aparecen solos al inscribirse. */
    val acompanantes = listOf(Acompanante("Mateo Rodríguez", 9), Acompanante("Renata Rodríguez", 7))

    /**
     * Ana ya viene inscrita a la Posada Sendero para poder enseñar Mis Actividades y cancelar
     * (P-20). Las demás actividades empiezan sin inscripción (P-07).
     */
    val inscripciones = mapOf("act2" to emptyList<Acompanante>())

    /** Sin fechas reales no se puede calcular; es el texto del Figma (P-20). */
    const val FALTA_PARA_ACTIVIDAD = "Faltan 3 días para la actividad, todavía estás a tiempo."
}

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
    val titular = CuentaDePrueba.titular
    val acompanantes = CuentaDePrueba.acompanantes
}
