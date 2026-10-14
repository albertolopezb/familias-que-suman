package mx.tec.familiasquesuman.data

import android.content.Context

/**
 * Lo mínimo de la cuenta con sesión que se guarda en el teléfono para cuando la app está cerrada:
 * el correo (para saber de quién es la bandeja de avisos) y a qué actividades va (para los
 * recordatorios del RevisarAvisosWorker). La verdad la tiene el servidor; esto es solo una copia.
 *
 * Ya no hay cuentas ni contraseñas aquí: la identidad la lleva Auth0 y los tokens los guarda
 * cifrados el SDK (ver data/sesion/ServicioSesion.kt).
 */
class CuentasStore(contexto: Context) {

    private val prefs = contexto.applicationContext.getSharedPreferences("cuenta_local", Context.MODE_PRIVATE)

    private fun clave(correo: String) = correo.trim().lowercase()

    /** El correo de la cuenta con sesión, o null si nadie la tiene. */
    fun correoSesion(): String? = prefs.getString("sesion", null)

    fun guardarSesion(correo: String?) {
        val cambio = prefs.edit()
        if (correo == null) cambio.remove("sesion") else cambio.putString("sesion", clave(correo))
        cambio.apply()
    }

    fun inscritas(correo: String): Set<String> = prefs.getStringSet("inscritas:${clave(correo)}", null)?.toSet().orEmpty()

    fun guardarInscritas(correo: String, actividades: Set<String>) {
        prefs.edit().putStringSet("inscritas:${clave(correo)}", actividades).apply()
    }

    /** Al cerrar sesión no se deja nada de la familia en el teléfono. */
    fun olvidar(correo: String) {
        prefs.edit().remove("inscritas:${clave(correo)}").remove("sesion").apply()
    }
}
