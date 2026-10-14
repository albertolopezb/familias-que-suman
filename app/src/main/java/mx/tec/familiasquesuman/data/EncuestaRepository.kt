package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.data.red.EncuestaCuerpo
import mx.tec.familiasquesuman.data.red.RespuestaDto
import mx.tec.familiasquesuman.data.red.aDominio
import mx.tec.familiasquesuman.data.red.llamar
import mx.tec.familiasquesuman.domain.MomentoEncuesta
import mx.tec.familiasquesuman.domain.RespuestaEncuesta

/**
 * Las respuestas de las encuestas de antes y después (RF-13). La de antes se liga a la actividad;
 * la de después, a la participación (en la app, su `actividadId` es el id de la participación).
 */
interface EncuestaRepository {
    /** Las de todas las familias: solo para quien tiene permiso de moderar (el servidor lo revisa). */
    suspend fun getTodas(): List<RespuestaEncuesta>
    suspend fun getDeFamilia(correo: String): List<RespuestaEncuesta>
    suspend fun getPorActividad(actividadId: String): List<RespuestaEncuesta>
    /** Si la misma familia ya había contestado esa encuesta, se reemplazan sus respuestas. */
    suspend fun guardar(nueva: RespuestaEncuesta)
}

class EncuestaRepositoryApi : EncuestaRepository {

    override suspend fun getTodas(): List<RespuestaEncuesta> = llamar { todasLasEncuestas() }.map { it.aDominio("") }

    override suspend fun getDeFamilia(correo: String): List<RespuestaEncuesta> = llamar { misEncuestas() }.map { it.aDominio(correo) }

    override suspend fun getPorActividad(actividadId: String): List<RespuestaEncuesta> =
        llamar { todasLasEncuestas(actividadId) }.map { it.aDominio("") }

    override suspend fun guardar(nueva: RespuestaEncuesta) {
        val cuerpo = EncuestaCuerpo(nueva.respuestas.map { RespuestaDto(it.numero, it.pregunta, it.respuesta) })
        when (nueva.momento) {
            MomentoEncuesta.ANTES -> llamar { encuestaPrevia(nueva.actividadId, cuerpo) }
            MomentoEncuesta.DESPUES -> llamar { encuestaFinal(nueva.actividadId, cuerpo) }
        }
    }
}

/** En memoria, para las pruebas unitarias. */
class EncuestaRepositoryEnMemoria(iniciales: List<RespuestaEncuesta> = emptyList()) : EncuestaRepository {
    private val respuestas = iniciales.toMutableList()

    override suspend fun getTodas(): List<RespuestaEncuesta> = respuestas.toList()

    override suspend fun getDeFamilia(correo: String): List<RespuestaEncuesta> =
        respuestas.filter { it.correo.equals(correo, ignoreCase = true) }

    override suspend fun getPorActividad(actividadId: String): List<RespuestaEncuesta> =
        respuestas.filter { it.actividadId == actividadId }

    override suspend fun guardar(nueva: RespuestaEncuesta) {
        respuestas.removeAll {
            it.correo.equals(nueva.correo, ignoreCase = true) && it.actividadId == nueva.actividadId && it.momento == nueva.momento
        }
        respuestas.add(0, nueva)
    }
}
