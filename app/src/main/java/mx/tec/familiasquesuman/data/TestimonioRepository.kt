package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Testimonio
import mx.tec.familiasquesuman.domain.puedeEliminarlo
import java.io.File

/**
 * Los testimonios de las familias (RF-12). Se publican en cuanto se envían: no hay aprobación
 * previa, y a cambio cada testimonio se puede eliminar después (por su autora o autor, o por
 * el admin). Viven en memoria como el resto de los datos de prueba; con el backend serán un
 * POST, un GET y un DELETE que valida lo mismo en el servidor.
 */
class TestimonioRepository {

    private val testimonios = mutableListOf(
        Testimonio(
            id = "t1", participacionId = "p3", actividadTitulo = "Regalando Estrellas Visita al Materno Infantil",
            familia = "Familia Rodríguez", correo = "ana.rodriguez@correo.com",
            experiencia = "Mis hijos prepararon dibujos para los niños del hospital y volvieron con ganas de ayudar más. " +
                "Fue la mejor tarde en familia que hemos tenido.",
            foto = null, fecha = "3 ago 2026"
        ),
        Testimonio(
            id = "t2", participacionId = "p2", actividadTitulo = "Regalando Estrellas Visita al Materno Infantil",
            familia = "Familia Rodríguez", correo = "ana.rodriguez@correo.com",
            experiencia = "Entregamos kits de higiene y los niños se dieron cuenta de lo mucho que pueden aportar.",
            foto = null, fecha = "7 sep 2026"
        )
    )

    /** Todos los testimonios publicados, del más nuevo al más viejo. */
    suspend fun getTodos(): List<Testimonio> = testimonios.toList()

    suspend fun getDeFamilia(correo: String): List<Testimonio> =
        testimonios.filter { it.correo.equals(correo, ignoreCase = true) }

    /** Publica el testimonio de una participación; si la familia ya tenía uno de esa, lo reemplaza. */
    suspend fun publicar(nuevo: Testimonio) {
        val i = testimonios.indexOfFirst {
            it.participacionId == nuevo.participacionId && it.correo.equals(nuevo.correo, ignoreCase = true)
        }
        if (i >= 0) testimonios[i] = nuevo.copy(id = testimonios[i].id) else testimonios.add(0, nuevo)
    }

    /**
     * Elimina un testimonio y su foto. Solo lo logra quien lo escribió o el admin; con
     * cualquier otra cuenta (o sin sesión) no pasa nada.
     *
     * @return true si se eliminó.
     */
    suspend fun eliminar(id: String, correoDeQuienPide: String?, esAdmin: Boolean): Boolean {
        val i = testimonios.indexOfFirst { it.id == id }
        if (i < 0 || !testimonios[i].puedeEliminarlo(correoDeQuienPide, esAdmin)) return false
        val quitado = testimonios.removeAt(i)
        quitado.foto?.let { runCatching { File(it).delete() } }
        return true
    }
}
