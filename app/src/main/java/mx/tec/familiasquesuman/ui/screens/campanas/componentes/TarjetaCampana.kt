package mx.tec.familiasquesuman.ui.screens.campanas.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * Tarjeta de la lista de campañas: chip "Cierra pronto", título, cierre, barra y avance.
 * [nombreAsociacion] es opcional: Campana solo trae asociacionId. Cuando el repositorio
 * exponga el nombre, se pasa desde el grafo y aparece antes del "Cierra el…".
 */
@Composable
fun TarjetaCampana(
    campana: Campana,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    nombreAsociacion: String? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Superficie,
        border = BorderStroke(1.dp, Borde),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (campana.urgente) {
                Surface(shape = RoundedCornerShape(50), color = AcentoSuave) {
                    Text(
                        "Cierra pronto",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = AcentoTexto
                    )
                }
            }
            Text(campana.titulo, style = MaterialTheme.typography.titleMedium, color = Tinta)
            val cierre = "Cierra el ${campana.cierra}"
            Text(
                text = if (nombreAsociacion != null) "$nombreAsociacion · $cierre" else cierre,
                style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave
            )
            Spacer(Modifier.height(4.dp))
            BarraMeta(progreso = campana.progreso)
            Text(
                "${campana.completados} de ${campana.metaTotal} ${campana.unidadMeta} completos",
                style = MaterialTheme.typography.labelLarge,
                color = ConfirmadoTexto
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaCampanaPreview() {
    FamiliasQueSumanTheme {
        TarjetaCampana(
            campana = Campana(
                "c1", "Kits de primera comunión para San Bernabé", "a4", "Útiles escolares",
                "20 de septiembre", true, "", "kits", 45, 18,
                listOf(ArticuloMeta("c1-1", "Rosario blanco", 45, 12))
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp),
            nombreAsociacion = "Parroquia San Bernabé"
        )
    }
}
