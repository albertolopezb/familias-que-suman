package mx.tec.familiasquesuman.data

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Lo que la familia personaliza en su teléfono: la foto de perfil y cuántas veces ha invitado
 * a otras familias a una actividad. Se guarda por correo, igual que los favoritos; con el
 * backend la foto se sube al almacenamiento de fotos y esto queda como caché.
 */
class PerfilLocalStore(contexto: Context) {

    private val app = contexto.applicationContext
    private val prefs = app.getSharedPreferences("perfil_local", Context.MODE_PRIVATE)
    private val carpeta = File(app.filesDir, "perfil")

    private fun clave(correo: String) = correo.trim().lowercase()

    /** Ruta del archivo de la foto, o null si la cuenta no tiene. */
    fun foto(correo: String): String? =
        prefs.getString("foto:${clave(correo)}", null)?.takeIf { File(it).exists() }

    /**
     * Copia la foto elegida al almacenamiento de la app (el permiso del selector es temporal) y
     * la deja como foto de la cuenta. Se llama fuera del hilo principal.
     */
    fun guardarFoto(correo: String, origen: Uri): String? = runCatching {
        carpeta.mkdirs()
        foto(correo)?.let { File(it).delete() }
        val destino = File(carpeta, "foto_${clave(correo).hashCode()}_${System.currentTimeMillis()}.img")
        val entrada = app.contentResolver.openInputStream(origen) ?: return null
        entrada.use { de -> destino.outputStream().use { a -> de.copyTo(a) } }
        prefs.edit().putString("foto:${clave(correo)}", destino.absolutePath).apply()
        destino.absolutePath
    }.getOrNull()

    fun quitarFoto(correo: String) {
        foto(correo)?.let { File(it).delete() }
        prefs.edit().remove("foto:${clave(correo)}").apply()
    }

    fun invitaciones(correo: String): Int = prefs.getInt("inv:${clave(correo)}", 0)

    fun sumarInvitacion(correo: String): Int {
        val total = invitaciones(correo) + 1
        prefs.edit().putInt("inv:${clave(correo)}", total).apply()
        return total
    }
}
