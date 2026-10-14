package mx.tec.familiasquesuman.ui.state

import kotlinx.coroutines.CancellationException
import mx.tec.familiasquesuman.data.red.ApiExcepcion

/**
 * Corre algo que habla con la API. Si falla (sin internet, servidor caído, 4xx), devuelve el
 * error en lugar de lanzarlo: una excepción sin atrapar dentro de viewModelScope cierra la app.
 * La cancelación sí se deja pasar (es como se detienen las corrutinas).
 */
suspend fun <T> intentar(bloque: suspend () -> T): Result<T> = try {
    Result.success(bloque())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}

/** El mensaje que dio el servidor, o [porOmision] si no hubo respuesta. */
fun Throwable.mensajeParaPersona(porOmision: String = "No se pudo completar. Revisa tu conexión e intenta de nuevo."): String =
    (this as? ApiExcepcion)?.takeIf { !it.sinConexion }?.message ?: porOmision
