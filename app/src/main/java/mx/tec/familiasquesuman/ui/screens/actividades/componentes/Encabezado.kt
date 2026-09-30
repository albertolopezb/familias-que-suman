package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta

/**
 * El encabezado de las pantallas de actividades: flecha de regreso opcional,
 * título y un hueco a la derecha para lo que cada pantalla necesite.
 */
@Composable
fun EncabezadoActividades(
    titulo: String,
    modifier: Modifier = Modifier,
    onRegresar: (() -> Unit)? = null,
    accionDerecha: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp)
            .background(Superficie)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onRegresar != null) {
            BotonCircular(onClick = onRegresar)
        }
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            color = Tinta,
            modifier = Modifier.weight(1f)
        )
        accionDerecha()
    }
}

/** El botón redondo de regresar, con su área táctil de 40 dp. */
@Composable
fun BotonCircular(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Fondo)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = "Regresar",
            tint = Tinta
        )
    }
}
