package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.EstadoTestimonio
import mx.tec.familiasquesuman.domain.Testimonio

/**
 * Los testimonios de las familias y su revisión (RF-12). Viven en memoria como el resto de los
 * datos de prueba; con el backend serán un POST de la familia y un PATCH del admin.
 *
 * Lo público sale solo de [getPublicados]: nada se muestra sin que Familias que Suman lo apruebe.
 */
class TestimonioRepository {

    private val testimonios = mutableListOf(
        Testimonio(
            id = "t1", participacionId = "p3", actividadTitulo = "Regalando Estrellas Visita al Materno Infantil",
            familia = "Familia Rodríguez", correo = "ana.rodriguez@correo.com",
            experiencia = "Mis hijos prepararon dibujos para los niños del hospital y volvieron con ganas de ayudar más. " +
                "Fue la mejor tarde en familia que hemos tenido.",
            foto = null, fecha = "3 ago 2026", estado = EstadoTestimonio.APROBADO
        ),
        Testimonio(
            id = "t2", participacionId = "p2", actividadTitulo = "Regalando Estrellas Visita al Materno Infantil",
            familia = "Familia Rodríguez", correo = "ana.rodriguez@correo.com",
            experiencia = "Entregamos kits de higiene y los niños se dieron cuenta de lo mucho que pueden aportar.",
            foto = null, fecha = "7 sep 2026", estado = EstadoTestimonio.EN_REVISION
        )
    )

    suspend fun getTodos(): List<Testimonio> = testimonios.toList()

    /** Lo único que ve el público. */
    suspend fun getPublicados(): List<Testimonio> = testimonios.filter { it.publicado }

    suspend fun getDeFamilia(correo: String): List<Testimonio> =
        testimonios.filter { it.correo.equals(correo, ignoreCase = true) }

    /**
     * Guarda el testimonio de una participación. Si la familia ya tenía uno (porque le pidieron
     * ajustes), lo reemplaza y vuelve a revisión; nunca queda publicado por sí solo.
     */
    suspend fun enviar(nuevo: Testimonio) {
        val enRevision = nuevo.copy(estado = EstadoTestimonio.EN_REVISION, nota = "")
        val i = testimonios.indexOfFirst {
            it.participacionId == nuevo.participacionId && it.correo.equals(nuevo.correo, ignoreCase = true)
        }
        if (i >= 0) testimonios[i] = enRevision.copy(id = testimonios[i].id) else testimonios.add(0, enRevision)
    }

    suspend fun cambiarEstado(id: String, estado: EstadoTestimonio, nota: String = "") {
        val i = testimonios.indexOfFirst { it.id == id }
        if (i >= 0) {
            testimonios[i] = testimonios[i].copy(
                estado = estado,
                nota = if (estado == EstadoTestimonio.AJUSTAR) nota else ""
            )
        }
    }
}
