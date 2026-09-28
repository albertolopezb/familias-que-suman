package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VisiteoScreen(onCentroClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Directorio de Visiteo", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Centro de Apoyo Comunitario", style = MaterialTheme.typography.titleMedium)
                Text("Horario: 9:00 AM - 5:00 PM")
                Text("Edad mínima: Sin restricción")
            }
        }
    }
}