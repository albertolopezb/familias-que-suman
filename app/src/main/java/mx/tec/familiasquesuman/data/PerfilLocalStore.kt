package mx.tec.familiasquesuman.data

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Una copia de la foto de perfil en el teléfono, para no descargarla cada vez. La original vive
 * en el servidor (la API la limpia y la guarda en el almacén S3).
 */
class PerfilLocalStore(contexto: Context) {

    private val app = contexto.applicationContext
    private val carpeta = File(app.filesDir, "perfil")

    private fun archivo(correo: String) = File(carpeta, "foto_${correo.trim().lowercase().hashCode().toUInt()}.jpg")

    /** Ruta del archivo de la foto, o null si no hay copia. */
    fun foto(correo: String): String? = archivo(correo).takeIf { it.exists() && it.length() > 0 }?.absolutePath

    /** Guarda la copia. Se llama fuera del hilo principal. */
    fun guardarFoto(correo: String, bytes: ByteArray): String? = runCatching {
        carpeta.mkdirs()
        val destino = archivo(correo)
        destino.writeBytes(bytes)
        destino.absolutePath
    }.getOrNull()

    fun quitarFoto(correo: String) {
        archivo(correo).delete()
    }

    /** Los bytes de la foto elegida en el selector (el permiso del selector es temporal). */
    fun leer(uri: Uri): ByteArray? = runCatching {
        app.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    }.getOrNull()
}
