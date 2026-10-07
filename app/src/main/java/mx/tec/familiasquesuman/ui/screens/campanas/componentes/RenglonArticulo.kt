package mx.tec.familiasquesuman.ui.screens.campanas.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * Un artículo de la campaña. Si está completo, el botón se pinta gris con "✓ Completo"
 * y no se puede tocar: es la prueba visual del RF-21, no lo quites.
 */
@Composable
fun RenglonArticulo(
    articulo: ArticuloMeta,
    onApartar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(articulo.nombre, style = MaterialTheme.typography.labelLarge, color = Tinta)
            Text(
                "${articulo.apartados} de ${articulo.meta} comprometidos",
                style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave
            )
        }
        Button(
            onClick = onApartar,
            enabled = !articulo.completo,
            shape = RoundedCornerShape(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MarcaOro,
                contentColor = MarcaAzul,
                disabledContainerColor = Color(0xFFE2E8F0),
                disabledContentColor = TintaSuave
            )
        ) {
            Text(
                text = if (articulo.completo) "✓ Completo" else "Aportar",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RenglonArticuloPreview() {
    FamiliasQueSumanTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            RenglonArticulo(ArticuloMeta("1", "Rosario blanco", 45, 12), onApartar = {})
            RenglonArticulo(ArticuloMeta("3", "Vela decorada", 45, 45), onApartar = {})
        }
    }
}
