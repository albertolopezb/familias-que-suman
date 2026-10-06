package mx.tec.familiasquesuman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta

/**
 * La barra de arriba de toda pantalla secundaria: flecha de regreso, título y, a la derecha,
 * las acciones que la pantalla necesite. Es la misma en toda la app para que moverse entre
 * pantallas se sienta igual, como en una app nativa.
 */
@Composable
fun BarraSuperior(
    titulo: String,
    onRegresar: (() -> Unit)?,
    modifier: Modifier = Modifier,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier.fillMaxWidth().background(Superficie)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(start = if (onRegresar != null) 4.dp else 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onRegresar != null) {
                IconButton(onClick = onRegresar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Tinta)
                }
            }
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                color = Tinta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = if (onRegresar != null) 4.dp else 0.dp)
            )
            acciones()
        }
        HorizontalDivider(color = Borde, thickness = 1.dp)
    }
}
