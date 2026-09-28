package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Asociacion

@Composable
fun ExplorarScreen(
    textoBusqueda: String,
    onBusquedaChange: (String) -> Unit,
    categoriaSeleccionada: String?,
    onCategoriaSelect: (String?) -> Unit,
    asociaciones: List<Asociacion>,
    onAsociacionClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = onBusquedaChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar asociaciones...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )

        Spacer(Modifier.height(12.dp))
        Text("${asociaciones.size} asociaciones encontradas", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(asociaciones) { asociacion ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onAsociacionClick(asociacion.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(asociacion.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(asociacion.categoria, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(asociacion.descripcion, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                    }
                }
            }
        }
    }
}