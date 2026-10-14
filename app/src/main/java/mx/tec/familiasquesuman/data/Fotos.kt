package mx.tec.familiasquesuman.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import mx.tec.familiasquesuman.data.red.ClienteApi
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

/**
 * Las fotos que salen del teléfono y las que llegan de la API.
 *
 * Al subir: la foto se reduce a 1600 px y se vuelve a guardar como JPEG antes de mandarla. Pesa
 * menos (menos datos móviles) y, como es una imagen nueva, ya no lleva los metadatos EXIF de la
 * cámara (la ubicación GPS, entre otros). El servidor lo vuelve a hacer de todos modos: no confía
 * en que el teléfono lo haya hecho.
 */
object Fotos {

    private const val LADO_MAXIMO = 1600

    fun prepararParaSubir(original: ByteArray): ByteArray {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(original, 0, original.size, limites)
        require(limites.outWidth > 0 && limites.outHeight > 0) { "No es una imagen" }
        var muestra = 1
        while (maxOf(limites.outWidth, limites.outHeight) / (muestra * 2) >= LADO_MAXIMO) muestra *= 2
        val bitmap = BitmapFactory.decodeByteArray(original, 0, original.size, BitmapFactory.Options().apply { inSampleSize = muestra })
            ?: error("No se pudo leer la imagen")
        val escala = minOf(1f, LADO_MAXIMO.toFloat() / maxOf(bitmap.width, bitmap.height))
        val final = if (escala < 1f) Bitmap.createScaledBitmap(bitmap, (bitmap.width * escala).toInt(), (bitmap.height * escala).toInt(), true) else bitmap
        return ByteArrayOutputStream().use { salida ->
            final.compress(Bitmap.CompressFormat.JPEG, 85, salida)
            salida.toByteArray()
        }
    }

    /**
     * Descarga una foto de la API a la caché de la app (una vez: si ya está, no se vuelve a pedir)
     * y devuelve la ruta del archivo. Así las pantallas siguen mostrando fotos desde un archivo.
     */
    fun descargar(ruta: String, carpeta: File): String? = runCatching {
        carpeta.mkdirs()
        val nombre = MessageDigest.getInstance("SHA-256").digest(ruta.toByteArray()).joinToString("") { "%02x".format(it) }.take(32)
        val archivo = File(carpeta, "$nombre.jpg")
        if (!archivo.exists() || archivo.length() == 0L) {
            ClienteApi.http.newCall(Request.Builder().url(ClienteApi.urlDe(ruta)).build()).execute().use { r ->
                if (!r.isSuccessful) return null
                val cuerpo = r.body ?: return null
                val temporal = File(carpeta, "$nombre.tmp")
                temporal.outputStream().use { cuerpo.byteStream().copyTo(it) }
                temporal.renameTo(archivo)
            }
        }
        archivo.absolutePath
    }.getOrNull()
}
