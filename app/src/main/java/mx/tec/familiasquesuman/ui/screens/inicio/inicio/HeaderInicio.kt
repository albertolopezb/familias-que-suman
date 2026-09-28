package mx.tec.familiasquesuman.ui.screens.inicio.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.AmbarAcento
import mx.tec.familiasquesuman.ui.theme.AzulMarinoPrimario

@Composable
fun HeaderInicio(
    nombreFamilia: String,
    ciudadActual: String,
    onCiudadSeleccionada: (String) -> Unit,
    onNotificacionesClick: () -> Unit
) {
    var desplegado by remember { mutableStateOf(false) }
    val ciudades = listOf("Monterrey", "Hermosillo")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "¡Buenos días!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = nombreFamilia,
                    style = MaterialTheme.typography.headlineSmall,
                    color = AzulMarinoPrimario
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "👋", style = MaterialTheme.typography.titleMedium)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                // Píldora de Ciudad
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
                            contentDescription = null,
                            tint = AzulMarinoPrimario,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Menú Desplegable Flotante
                DropdownMenu(
                    expanded = desplegado,
                    onDismissRequest = { desplegado = false },
                    offset = DpOffset(0.dp, 8.dp),
                    modifier = Modifier
                        .background(Color.White)
                        .width(160.dp)
                ) {
                    ciudades.forEach { ciudad ->
                        val esSeleccionada = ciudad == ciudadActual
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (esSeleccionada) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = AmbarAcento,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        Spacer(modifier = Modifier.width(26.dp))
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

            Spacer(modifier = Modifier.width(8.dp))

            // Botón Notificaciones
            Box {
                IconButton(onClick = onNotificacionesClick) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notificaciones",
                        tint = AzulMarinoPrimario
                    )
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AmbarAcento)
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)
                )
            }
        }
    }
}