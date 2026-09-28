package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProyectosScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Proyectos Vigentes", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Construcción de Aula Digital", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { 0.75f }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(4.dp))
                Text("75% Avance", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}