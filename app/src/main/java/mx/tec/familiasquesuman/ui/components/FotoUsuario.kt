package mx.tec.familiasquesuman.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.tec.familiasquesuman.ui.theme.Borde
import java.io.File
import java.io.InputStream

/** El lado más largo al que se baja una foto de la familia para pintarla: lo justo para una tarjeta. */
private const val LADO_MAXIMO = 1280

/**
 * Una foto que subió la familia: la ruta de un archivo de la app o un `content://` del selector.
 * Se decodifica fuera del hilo principal y a un tamaño razonable; mientras carga (o si no se
 * puede leer) queda un recuadro gris.
 */
@Composable
fun FotoUsuario(
    ruta: String,
    descripcion: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val contexto = LocalContext.current
    val imagen by produceState<ImageBitmap?>(null, ruta) {
        value = withContext(Dispatchers.IO) { decodificar(contexto, ruta) }
    }
    val lista = imagen
    if (lista == null) {
        Box(modifier.background(Borde))
    } else {
        Image(lista, descripcion, modifier, contentScale = contentScale)
    }
}

private fun decodificar(contexto: Context, ruta: String): ImageBitmap? = runCatching {
    fun abrir(): InputStream? =
        if (ruta.startsWith("content://")) contexto.contentResolver.openInputStream(Uri.parse(ruta))
        else File(ruta).inputStream()

    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    abrir()?.use { BitmapFactory.decodeStream(it, null, medidas) }
    var reduccion = 1
    while (maxOf(medidas.outWidth, medidas.outHeight) / (reduccion * 2) >= LADO_MAXIMO) reduccion *= 2
    val opciones = BitmapFactory.Options().apply { inSampleSize = reduccion }
    abrir()?.use { BitmapFactory.decodeStream(it, null, opciones) }?.asImageBitmap()
}.getOrNull()
