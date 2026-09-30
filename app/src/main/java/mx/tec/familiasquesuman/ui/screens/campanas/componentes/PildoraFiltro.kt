package mx.tec.familiasquesuman.ui.screens.campanas.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * Chip redondo. Sirve para los chips de categoría (seleccionado = azul relleno)
 * y, con [onQuitar], para las píldoras de filtro activo con su ✕.
 */
@Composable
fun PildoraFiltro(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onQuitar: (() -> Unit)? = null
) {
    val fondo = if (seleccionada) MarcaAzul else Superficie
    val contenido = if (seleccionada) Color.White else TintaSuave
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = fondo,
        border = if (seleccionada) null else BorderStroke(1.dp, Borde)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = texto, style = MaterialTheme.typography.labelMedium, color = contenido)
            if (onQuitar != null) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quitar filtro $texto",
                    tint = contenido,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable(onClick = onQuitar)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PildoraFiltroPreview() {
    FamiliasQueSumanTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            PildoraFiltro("Todas", seleccionada = true, onClick = {})
            PildoraFiltro("Alimentos", seleccionada = false, onClick = {})
            PildoraFiltro("Juguetes", seleccionada = true, onClick = {}, onQuitar = {})
        }
    }
}
