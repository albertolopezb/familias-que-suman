package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.data.red.ApiExcepcion
import mx.tec.familiasquesuman.data.red.InscripcionCuerpo
import mx.tec.familiasquesuman.data.red.InvitacionDto
import mx.tec.familiasquesuman.data.red.PreferenciasDto
import mx.tec.familiasquesuman.data.red.YoCambiosDto
import mx.tec.familiasquesuman.data.red.YoDto
import mx.tec.familiasquesuman.data.red.aAcompanante
import mx.tec.familiasquesuman.data.red.aPersona
import mx.tec.familiasquesuman.data.red.llamar
import mx.tec.familiasquesuman.domain.Acompanante
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Lo que regresó el servidor al inscribirse. */
sealed interface ResultadoInscripcion {
    data class Lista(val lugaresDisponibles: Int) : ResultadoInscripcion
    /** Otras familias ganaron los lugares (P-08). */
    data class SinLugares(val lugaresDisponibles: Int) : ResultadoInscripcion
    data class Rechazada(val mensaje: String) : ResultadoInscripcion
}

data class Cancelacion(val liberados: Int, val antes: Int, val despues: Int)

/**
 * La cuenta con sesión, en la API: /v1/yo/… Nunca se manda de quién es: lo dice el token.
 */
class CuentaRepository {

    suspend fun yo(): YoDto = llamar { yo() }

    suspend fun completarPerfil(nombreFamilia: String?, titular: String?, aceptarAviso: Boolean?): YoDto =
        llamar { editarYo(YoCambiosDto(nombreFamilia = nombreFamilia, titularNombre = titular, aceptarAviso = aceptarAviso)) }

    suspend fun cambiarCiudad(ciudad: String) { llamar { editarYo(YoCambiosDto(ciudad = ciudad)) } }

    suspend fun acompanantes(): List<Acompanante> = llamar { acompanantes() }.map { it.aAcompanante() }

    suspend fun agregarAcompanante(persona: Acompanante) { llamar { agregarAcompanante(persona.aPersona()) } }

    suspend fun quitarAcompanante(persona: Acompanante) {
        val id = llamar { acompanantes() }.firstOrNull { it.nombre.equals(persona.nombre.trim(), ignoreCase = true) }?.id ?: return
        llamar { quitarAcompanante(id) }.exigirExito()
    }

    suspend fun favoritos(): Set<String> = llamar { favoritos() }.map { it.objetoId }.toSet()

    suspend fun marcarFavorito(id: String, marcar: Boolean) {
        if (marcar) llamar { marcarFavorito(id) } else llamar { quitarFavorito(id) }.exigirExito()
    }

    /** actividadId → quién va además de la persona titular. */
    suspend fun inscripciones(): Map<String, List<Acompanante>> =
        llamar { inscripciones() }.associate { i -> i.actividadId to i.acompanantes.map { it.aAcompanante() } }

    suspend fun inscribirse(actividadId: String, acompanantes: List<Acompanante>, consentimiento: Boolean): ResultadoInscripcion = try {
        val r = llamar { inscribirse(actividadId, InscripcionCuerpo(acompanantes.map { it.aPersona() }, consentimiento)) }
        ResultadoInscripcion.Lista(r.lugaresDisponibles)
    } catch (e: ApiExcepcion) {
        when (e.codigo) {
            "sin_lugares" -> ResultadoInscripcion.SinLugares(e.datoEntero("lugares_disponibles") ?: 0)
            else -> if (e.estado in 400..499) ResultadoInscripcion.Rechazada(e.message) else throw e
        }
    }

    suspend fun cancelar(actividadId: String): Cancelacion {
        val r = llamar { cancelar(actividadId) }
        return Cancelacion(r.liberados, r.lugaresAntes, r.lugaresDisponibles)
    }

    suspend fun subirFoto(bytes: ByteArray) {
        val parte = MultipartBody.Part.createFormData("foto", "perfil.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
        llamar { cambiarFoto(parte) }
    }

    suspend fun descargarFoto(): ByteArray = llamar { miFoto() }.bytes()

    suspend fun quitarFoto() { llamar { quitarFoto() }.exigirExito() }

    suspend fun registrarInvitacion(actividadId: String): Int = llamar { registrarInvitacion(InvitacionDto(actividadId)) }.total

    suspend fun guardarPreferencias(recordatorio: Boolean, favoritos: Boolean, urgencias: Boolean) {
        llamar { guardarPreferencias(PreferenciasDto(recordatorio, favoritos, urgencias)) }
    }

    /** El servidor deja de aceptar este token desde ya (no solo cuando venza). */
    suspend fun cerrarSesion() { runCatching { llamar { cerrarSesion() } } }
}
