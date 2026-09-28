package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.*

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
            .background(GrisFondo)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "¡Bienvenido!",
                style = MaterialTheme.typography.labelLarge,
                color = AmbarAcento
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Selecciona tu ciudad",
                style = MaterialTheme.typography.headlineMedium,
                color = AzulMarinoPrimario
            )
            Spacer(modifier = Modifier.height(24.dp))

            ciudades.forEach { ciudad ->
                val esElegida = seleccionada == ciudad
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { seleccionada = ciudad }
                        .then(
                            if (esElegida) Modifier.border(2.dp, AmbarAcento, RoundedCornerShape(16.dp))
                            else Modifier
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (esElegida) Color.White else Color(0xFFF1F5F9)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = esElegida,
                            onClick = { seleccionada = ciudad },
                            colors = RadioButtonDefaults.colors(selectedColor = AmbarAcento)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = ciudad,
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulMarinoPrimario
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onCiudadSeleccionada(seleccionada) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulMarinoPrimario),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continuar", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}