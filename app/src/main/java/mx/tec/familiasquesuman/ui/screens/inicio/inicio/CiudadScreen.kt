package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CiudadScreen(
    ciudadActual: String,
    onCiudadSeleccionada: (String) -> Unit
) {
    var seleccionada by remember { mutableStateOf(ciudadActual) }
    val ciudades = listOf("Monterrey", "Hermosillo")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Selecciona tu ciudad", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            ciudades.forEach { ciudad ->
                val esElegida = seleccionada == ciudad
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { seleccionada = ciudad }
                        .then(
                            if (esElegida) Modifier.border(2.dp, Color(0xFF1E88E5), RoundedCornerShape(12.dp))
                            else Modifier
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = esElegida, onClick = { seleccionada = ciudad })
                        Spacer(Modifier.width(8.dp))
                        Text(ciudad, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Button(
            onClick = { onCiudadSeleccionada(seleccionada) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar")
        }
    }
}