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

/** Estado sin datos: no representa una insignia obtenida ni bloqueada. */
@Composable
fun Insignia(titulo: String, descripcion: String, icono: ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = Superficie,
        border = BorderStroke(1.dp, Borde)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(50.dp).clip(CircleShape).background(Borde), contentAlignment = Alignment.Center) {
                Icon(icono, contentDescription = null, tint = TintaSuave, modifier = Modifier.size(24.dp))
            }
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = Tinta, textAlign = TextAlign.Center)
            Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = TintaSuave,
                textAlign = TextAlign.Center, modifier = Modifier.heightIn(min = 32.dp))
            Text("Sin datos", style = MaterialTheme.typography.labelMedium, color = TintaSuave)
        }
    }
}
