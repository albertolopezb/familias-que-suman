package mx.tec.familiasquesuman.ui.screens.campanas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.PildoraFiltro
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * P-10: hoja de filtros. El botón cuenta los resultados en vivo y se deshabilita
 * con "Ninguna campaña coincide" cuando el resultado es cero.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosSheet(
    borrador: FiltrosCampanas,
    conteo: Int,
    onCategoria: (String) -> Unit,
    onUrgentes: (Boolean) -> Unit,
    onLimpiar: () -> Unit,
    onAplicar: () -> Unit,
    onCerrar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        containerColor = Color.White
    ) {
        FiltrosContenido(borrador, conteo, onCategoria, onUrgentes, onLimpiar, onAplicar)
    }
}

/** Lo de adentro de la hoja, separado para poder verlo en @Preview. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltrosContenido(
    borrador: FiltrosCampanas,
    conteo: Int,
    onCategoria: (String) -> Unit,
    onUrgentes: (Boolean) -> Unit,
    onLimpiar: () -> Unit,
    onAplicar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Filtrar campañas", style = MaterialTheme.typography.titleLarge, color = Tinta)

        Text("CATEGORÍA", style = MaterialTheme.typography.labelSmall, color = TintaSuave)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoriasFiltro.forEach { cat ->
                PildoraFiltro(cat, seleccionada = cat in borrador.categorias, onClick = { onCategoria(cat) })
            }
        }

        Text("URGENCIA", style = MaterialTheme.typography.labelSmall, color = TintaSuave)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Solo las que cierran esta semana",
                style = MaterialTheme.typography.bodyLarge,
                color = Tinta,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = borrador.soloUrgentes,
                onCheckedChange = onUrgentes,
                colors = SwitchDefaults.colors(checkedTrackColor = MarcaAzul, checkedThumbColor = Color.White)
            )
        }

        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onAplicar,
            enabled = conteo > 0,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MarcaOro,
                contentColor = MarcaAzul,
                disabledContainerColor = Color(0xFFE2E8F0),
                disabledContentColor = TintaSuave
            )
        ) {
            Text(
                text = when {
                    conteo == 0 -> "Ninguna campaña coincide"
                    conteo == 1 -> "Ver 1 campaña"
                    else -> "Ver $conteo campañas"
                },
                style = MaterialTheme.typography.labelLarge
            )
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextButton(onClick = onLimpiar) {
                Text("Limpiar filtros", style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FiltrosPreview() {
    FamiliasQueSumanTheme {
        FiltrosContenido(
            borrador = FiltrosCampanas(setOf("Alimentos"), soloUrgentes = true),
            conteo = 2, onCategoria = {}, onUrgentes = {}, onLimpiar = {}, onAplicar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FiltrosSinResultadosPreview() {
    FamiliasQueSumanTheme {
        FiltrosContenido(
            borrador = FiltrosCampanas(setOf("Juguetes")),
            conteo = 0, onCategoria = {}, onUrgentes = {}, onLimpiar = {}, onAplicar = {}
        )
    }
}
