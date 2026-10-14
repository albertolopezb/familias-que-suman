package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.data.red.EstadoSugerenciaCuerpo
import mx.tec.familiasquesuman.data.red.SugerenciaCuerpo
import mx.tec.familiasquesuman.data.red.aDominio
import mx.tec.familiasquesuman.data.red.llamar
import mx.tec.familiasquesuman.domain.EstadoSugerencia
import mx.tec.familiasquesuman.domain.Sugerencia

/**
 * Las sugerencias: cualquiera las manda (con o sin cuenta) y la moderación las revisa en su
 * bandeja. Mandar es público pero con límite (5 por hora por IP, en el servidor).
 */
class SugerenciaRepository {

    /** La bandeja: solo responde si la cuenta tiene el permiso "moderar:contenido". */
    suspend fun getSugerencias(): List<Sugerencia> = llamar { bandejaSugerencias() }.map { it.aDominio() }

    suspend fun enviar(sugerencia: Sugerencia) {
        llamar {
            sugerir(
                SugerenciaCuerpo(
                    tipo = sugerencia.tipo.name,
                    nombre = sugerencia.nombre,
                    descripcion = sugerencia.descripcion,
                    detalleExtra = sugerencia.detalleExtra,
                    ciudad = sugerencia.ciudad,
                    organizacion = sugerencia.organizacion,
                    contactoCausa = sugerencia.contactoCausa,
                    sugeridaPor = sugerencia.sugeridaPor,
                    contacto = sugerencia.contactoDeQuienSugiere
                )
            )
        }
    }

    suspend fun cambiarEstado(id: String, estado: EstadoSugerencia) {
        llamar { revisarSugerencia(id, EstadoSugerenciaCuerpo(estado.name)) }
    }
}
