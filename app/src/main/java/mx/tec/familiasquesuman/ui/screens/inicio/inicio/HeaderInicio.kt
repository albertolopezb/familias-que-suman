package mx.tec.familiasquesuman.ui.screens.inicio.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.AzulMarinoPrimario

/** El saludo del Inicio. La ciudad y el perfil están en el encabezado de arriba. */
@Composable
fun HeaderInicio(nombreFamilia: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (nombreFamilia == null) "¡Qué gusto verte por aquí!" else saludoDelDia(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = nombreFamilia ?: "Bienvenida, familia",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AzulMarinoPrimario
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "👋", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun saludoDelDia(): String = when (java.time.LocalTime.now().hour) {
    in 5..11 -> "¡Buenos días!"
    in 12..18 -> "¡Buenas tardes!"
    else -> "¡Buenas noches!"
}
