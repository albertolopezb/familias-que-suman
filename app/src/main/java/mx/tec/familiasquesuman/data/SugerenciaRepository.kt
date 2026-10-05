package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.EstadoSugerencia
import mx.tec.familiasquesuman.domain.Sugerencia
import mx.tec.familiasquesuman.domain.TipoSugerencia

/**
 * Las sugerencias de actividades, campañas, proyectos y centros que mandan las familias.
 * Viven en memoria como el resto de los datos de prueba; con el backend serán un POST
 * y la bandeja del admin un GET.
 */
class SugerenciaRepository {

    suspend fun getSugerencias(): List<Sugerencia> = sugerencias.toList()

    suspend fun enviar(sugerencia: Sugerencia) {
        sugerencias.add(0, sugerencia)
    }

    suspend fun cambiarEstado(id: String, estado: EstadoSugerencia) {
        val i = sugerencias.indexOfFirst { it.id == id }
        if (i >= 0) sugerencias[i] = sugerencias[i].copy(estado = estado)
    }

    private companion object {
        // Dos de ejemplo para que la bandeja del admin no arranque vacía.
        val sugerencias = mutableListOf(
            Sugerencia(
                id = "sg1",
                tipo = TipoSugerencia.CENTRO,
                nombre = "Casa Hogar Nuevo Amanecer",
                descripcion = "Casa hogar para niñas de 6 a 17 años. Reciben visitas de familias los " +
                    "fines de semana y siempre necesitan apoyo con tareas escolares.",
                detalleExtra = "Av. Ruiz Cortines 2500, Guadalupe",
                ciudad = "Monterrey",
                organizacion = "Nuevo Amanecer A.B.P.",
                contactoCausa = "8112345678",
                sugeridaPor = "Familia Garza",
                contactoDeQuienSugiere = "garza.familia@correo.com",
                fecha = "2 oct 2026"
            ),
            Sugerencia(
                id = "sg2",
                tipo = TipoSugerencia.ACTIVIDAD,
                nombre = "Limpieza del río Santa Catarina",
                descripcion = "Jornada de limpieza en familia con la asociación del barrio. Ponen " +
                    "guantes y bolsas; los niños pueden ayudar a separar reciclables.",
                detalleExtra = "Último domingo del mes, 8:00 am, puente Gonzalitos",
                ciudad = "Monterrey",
                organizacion = "",
                contactoCausa = "",
                sugeridaPor = "Ana Rodríguez",
                contactoDeQuienSugiere = "ana.rodriguez@correo.com",
                fecha = "28 sep 2026"
            )
        )
    }
}
