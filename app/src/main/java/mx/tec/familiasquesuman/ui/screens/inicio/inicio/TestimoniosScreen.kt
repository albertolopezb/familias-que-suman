package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TestimoniosScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Testimonios", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("\"Fue una experiencia transformadora para mis hijos.\"", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text("- Familia Perez, hace 2 días", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}