package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PermisoNotificacionesScreen(onContinuar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("¡No te pierdas de nada!", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Activa las notificaciones para enterarte de eventos urgentes y recordar tus voluntariados.", style = MaterialTheme.typography.bodyMedium)
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onContinuar, modifier = Modifier.fillMaxWidth()) {
                Text("Activar notificaciones")
            }
            TextButton(onClick = onContinuar, modifier = Modifier.fillMaxWidth()) {
                Text("Ahora no")
            }
        }
    }
}