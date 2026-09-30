package mx.tec.familiasquesuman.ui.screens.perfil.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/** Una lista vacía significa datos no disponibles, no cinco meses con valor cero. */
@Composable
fun GraficaBarras(etiquetas: List<String>, valores: List<Int>, modifier: Modifier = Modifier) {
    require(valores.isEmpty() || valores.size == etiquetas.size)
    require(valores.all { it >= 0 })
    Column(modifier = modifier) {
        if (valores.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(76.dp), contentAlignment = Alignment.Center) {
                Text("Datos mensuales no disponibles", style = MaterialTheme.typography.bodyMedium,
                    color = TintaSuave)
            }
        } else {
            // Normalización únicamente visual; no asigna valores a meses sin datos.
            val maximo = (valores.maxOrNull() ?: 0).coerceAtLeast(1)
            Row(Modifier.fillMaxWidth().height(76.dp), verticalAlignment = Alignment.Bottom) {
                valores.forEachIndexed { indice, valor ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                        Box(Modifier.width(33.dp).height((66f * valor / maximo).dp)
                            .clip(RoundedCornerShape(7.dp)).background(MarcaOro)
                            .semantics { contentDescription = "${etiquetas[indice]}: $valor actividades" })
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            etiquetas.forEach { etiqueta ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                }
            }
        }
    }
}
