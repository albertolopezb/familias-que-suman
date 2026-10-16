package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * RF-22 · El admin manda una urgencia a las familias (de su ciudad o de todas). Les llega como
 * notificación en menos de 15 minutos, si en Ajustes tienen prendidas las urgencias. Los mismos
 * límites que valida la API: título de 3 a 80 letras y texto de 3 a 300.
 */
@Composable
fun DialogoEnviarAviso(
    ciudad: String,
    enviando: Boolean,
    onEnviar: (titulo: String, cuerpo: String, ciudad: String?) -> Unit,
    onCerrar: () -> Unit
) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var cuerpo by rememberSaveable { mutableStateOf("") }
    var soloMiCiudad by rememberSaveable { mutableStateOf(true) }
    val valido = titulo.trim().length in 3..80 && cuerpo.trim().length in 3..300

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Enviar aviso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titulo, onValueChange = { titulo = it.take(80) }, singleLine = true,
                    label = { Text("Título") }, supportingText = { Text("${titulo.length}/80") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cuerpo, onValueChange = { cuerpo = it.take(300) }, minLines = 3,
                    label = { Text("Mensaje") }, supportingText = { Text("${cuerpo.length}/300") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = soloMiCiudad, onCheckedChange = { soloMiCiudad = it })
                    Text(if (soloMiCiudad) "Solo a las familias de $ciudad" else "A las familias de todas las ciudades")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onEnviar(titulo.trim(), cuerpo.trim(), if (soloMiCiudad) ciudad else null) },
                enabled = valido && !enviando
            ) { Text(if (enviando) "Enviando…" else "Enviar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}
