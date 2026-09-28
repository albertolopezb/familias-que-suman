package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Asociacion

@Composable
fun AsociacionScreen(
    asociacion: Asociacion,
    esFavorito: Boolean,
    onToggleFavorito: () -> Unit,
    onVerActividades: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(asociacion.nombre, style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onToggleFavorito) {
                    Icon(
                        imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito"
                    )
                }
            }
            Text(asociacion.categoria, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(16.dp))
            Text(asociacion.descripcion, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Text("Dirección: ${asociacion.direccion}")
            Text("Teléfono: ${asociacion.telefono}")
        }

        Button(
            onClick = onVerActividades,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver actividades disponibles")
        }
    }
}