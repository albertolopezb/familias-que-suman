package mx.tec.familiasquesuman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.AmbarAcento
import mx.tec.familiasquesuman.ui.theme.AzulMarinoPrimario

private val CIUDADES = listOf("Monterrey", "Hermosillo")

/** La píldora de ciudad con su menú. Solo se enseña en el encabezado del Inicio. */
@Composable
fun SelectorCiudad(
    ciudadActual: String,
    onCiudadSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var desplegado by remember { mutableStateOf(false) }

    Box(modifier) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { desplegado = !desplegado },
            color = Color(0xFFF1F5F9)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AmbarAcento,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = ciudadActual,
                    style = MaterialTheme.typography.labelLarge,
                    color = AzulMarinoPrimario
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Cambiar ciudad",
                    tint = AzulMarinoPrimario,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = desplegado,
            onDismissRequest = { desplegado = false },
            offset = DpOffset(0.dp, 8.dp),
            modifier = Modifier
                .background(Color.White)
                .width(160.dp)
        ) {
            CIUDADES.forEach { ciudad ->
                val esSeleccionada = ciudad == ciudadActual
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (esSeleccionada) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AmbarAcento,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(18.dp))
                            }
                            Text(
                                text = ciudad,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (esSeleccionada) AmbarAcento else AzulMarinoPrimario
                            )
                        }
                    },
                    onClick = {
                        onCiudadSeleccionada(ciudad)
                        desplegado = false
                    }
                )
            }
        }
    }
}
