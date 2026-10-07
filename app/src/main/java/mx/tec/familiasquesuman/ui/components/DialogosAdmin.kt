package mx.tec.familiasquesuman.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

enum class TipoCampo { TEXTO, NUMERO, MULTILINEA, INTERRUPTOR }

/** Un renglón del formulario de administración. El primero es obligatorio. */
data class CampoAdmin(
    val etiqueta: String,
    val valor: String = "",
    val tipo: TipoCampo = TipoCampo.TEXTO
)

/** Lo que devuelve un interruptor dentro de la lista de valores. */
fun String.comoBooleano(): Boolean = this == "true"

/**
 * El formulario de crear o editar que usan Directorio, Proyectos, Visiteo y Campañas.
 * Devuelve los valores en el mismo orden de [campos].
 */
@Composable
fun DialogoFormularioAdmin(
    titulo: String,
    campos: List<CampoAdmin>,
    onGuardar: (List<String>) -> Unit,
    onDescartar: () -> Unit
) {
    val valores = remember { mutableStateListOf<String>().also { l -> campos.forEach { l.add(it.valor) } } }

    AlertDialog(
        onDismissRequest = onDescartar,
        title = { Text(titulo) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                campos.forEachIndexed { i, campo ->
                    when (campo.tipo) {
                        TipoCampo.INTERRUPTOR -> Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(campo.etiqueta)
                            Switch(
                                checked = valores[i].comoBooleano(),
                                onCheckedChange = { valores[i] = it.toString() }
                            )
                        }

                        else -> OutlinedTextField(
                            value = valores[i],
                            onValueChange = { valores[i] = it },
                            label = { Text(campo.etiqueta) },
                            singleLine = campo.tipo != TipoCampo.MULTILINEA,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (campo.tipo == TipoCampo.NUMERO) KeyboardType.Number else KeyboardType.Text
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (valores.first().isNotBlank()) onGuardar(valores.toList()) }) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDescartar) { Text("Cancelar") } }
    )
}

@Composable
fun DialogoConfirmarBorrado(
    nombre: String,
    onConfirmar: () -> Unit,
    onDescartar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDescartar,
        title = { Text("¿Borrar?") },
        text = { Text("Se va a borrar \"$nombre\". No se puede deshacer.") },
        confirmButton = { Button(onClick = onConfirmar) { Text("Borrar") } },
        dismissButton = { TextButton(onClick = onDescartar) { Text("Cancelar") } }
    )
}

/** Los dos íconos de editar y borrar que van debajo de cada tarjeta cuando la cuenta es admin. */
@Composable
fun FilaAccionesAdmin(onEditar: () -> Unit, onBorrar: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onEditar) {
            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = onBorrar) {
            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
        }
    }
}

/** El botón redondo de "+" de abajo a la derecha. */
@Composable
fun BotonCrearAdmin(descripcion: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        Icon(Icons.Default.Add, contentDescription = descripcion)
    }
}
