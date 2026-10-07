package mx.tec.familiasquesuman.ui.screens.perfil.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.*

@Composable
fun TarjetaFavorita(
    nombre: String,
    categoria: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onQuitar: (() -> Unit)? = null
) {
    // Colores de presentación, no reglas de negocio ni datos de asociaciones nuevos.
    val (fondoCategoria, textoCategoria) = when (categoria) {
        "Alimentación" -> AcentoSuave to AcentoTexto
        "Adultos mayores" -> CategoriaAzulFondo to CategoriaAzulTexto
        "Ropa y abrigo" -> ConfirmadoFondo to ConfirmadoTexto
        else -> Borde to TintaSuave
    }
    val tarjeta = if (onClick != null) modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick) else modifier.fillMaxWidth()
    Surface(tarjeta, shape = RoundedCornerShape(18.dp), color = Superficie,
        border = BorderStroke(1.dp, Borde), shadowElevation = 2.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(50.dp).clip(RoundedCornerShape(10.dp)).background(fondoCategoria))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(nombre, style = MaterialTheme.typography.titleMedium, color = Tinta)
                Surface(shape = RoundedCornerShape(8.dp), color = fondoCategoria) {
                    Text(categoria, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.bodyMedium, color = textoCategoria)
                }
            }
            // El corazón lo quita de favoritos.
            IconButton(onClick = { onQuitar?.invoke() }, enabled = onQuitar != null) {
                Icon(Icons.Filled.Favorite, contentDescription = "Quitar de favoritos",
                    tint = MarcaOro, modifier = Modifier.size(22.dp))
            }
        }
        // getFavoritas() no proporciona avisos de actividades nuevas ni campañas urgentes.
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TarjetaFavoritaPreview() {
    FamiliasQueSumanTheme { TarjetaFavorita("Comedor Comunitario San Bernabé", "Alimentación") }
}
