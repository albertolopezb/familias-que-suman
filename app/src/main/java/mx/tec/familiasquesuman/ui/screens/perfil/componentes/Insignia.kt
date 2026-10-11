package mx.tec.familiasquesuman.ui.screens.perfil.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.*

/**
 * Una insignia: dorada sobre azul cuando ya se ganó; gris con su avance ("2 de 5") cuando falta.
 */
@Composable
fun Insignia(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    actual: Int,
    meta: Int,
    modifier: Modifier = Modifier
) {
    val obtenida = actual >= meta
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = if (obtenida) AcentoSuave else Superficie,
        border = BorderStroke(if (obtenida) 1.5.dp else 1.dp, if (obtenida) MarcaOro else Borde)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(50.dp).clip(CircleShape).background(if (obtenida) MarcaAzul else Borde),
                contentAlignment = Alignment.Center) {
                Icon(icono, contentDescription = null, tint = if (obtenida) MarcaOro else TintaSuave,
                    modifier = Modifier.size(24.dp))
            }
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = Tinta, textAlign = TextAlign.Center)
            Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = TintaSuave,
                textAlign = TextAlign.Center, modifier = Modifier.heightIn(min = 32.dp))
            if (obtenida) {
                Text("¡Obtenida!", style = MaterialTheme.typography.labelMedium, color = AcentoTexto)
            } else {
                LinearProgressIndicator(
                    progress = { actual.toFloat() / meta },
                    modifier = Modifier.fillMaxWidth(0.8f).height(5.dp).clip(RoundedCornerShape(3.dp)),
                    color = MarcaOro, trackColor = Borde, gapSize = 0.dp, drawStopIndicator = {}
                )
                Text("$actual de $meta", style = MaterialTheme.typography.labelMedium, color = TintaSuave)
            }
        }
    }
}
