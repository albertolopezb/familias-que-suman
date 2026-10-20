package mx.tec.familiasquesuman.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import java.util.UUID

/**
 * Elegir una foto de la galería o tomarla con la cámara, como en la Práctica 10:
 *
 *  · Sin pedir permisos. La galería es el Photo Picker del sistema, y la cámara se abre con
 *    `TakePicture`: la app de cámara del teléfono escribe la foto en un archivo que esta app le
 *    presta por FileProvider. La app no declara `CAMERA`, así que nunca ve la cámara en vivo.
 *  · La foto no se pierde si Android cierra la app mientras la cámara está abierta: la dirección
 *    del archivo vive en `rememberSaveable`, que sobrevive a que el proceso muera.
 *  · Después, la foto pasa por `Fotos.prepararParaSubir` (se reduce y se le quitan los EXIF, el
 *    GPS entre ellos) igual que una de la galería.
 *
 * Regresa la función que abre la pregunta "¿galería o cámara?". [alElegir] recibe null si la
 * persona canceló.
 */
@Composable
fun rememberElegirFoto(alElegir: (Uri?) -> Unit): () -> Unit {
    val contexto = LocalContext.current
    var preguntando by rememberSaveable { mutableStateOf(false) }
    var pendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia(), alElegir)
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        val uri = pendiente?.toUri()
        pendiente = null
        alElegir(if (tomada) uri else null)
    }

    if (preguntando) {
        AlertDialog(
            onDismissRequest = { preguntando = false },
            title = { Text("Agregar una foto") },
            text = { Text("La foto se reduce y se le quita la ubicación antes de enviarla.") },
            confirmButton = {
                TextButton(onClick = {
                    preguntando = false
                    val uri = archivoParaLaCamara(contexto)
                    pendiente = uri.toString()
                    camara.launch(uri)
                }) { Text("Tomar foto") }
            },
            dismissButton = {
                TextButton(onClick = {
                    preguntando = false
                    galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("Elegir de la galería") }
            }
        )
    }
    return { preguntando = true }
}

/** Un archivo nuevo en la caché para la foto de la cámara. Se borran las anteriores: ya se subieron. */
private fun archivoParaLaCamara(contexto: Context): Uri {
    val carpeta = File(contexto.cacheDir, "camara").apply { mkdirs() }
    carpeta.listFiles()?.forEach { it.delete() }
    val archivo = File(carpeta, "foto-${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(contexto, "${contexto.packageName}.archivos", archivo)
}
