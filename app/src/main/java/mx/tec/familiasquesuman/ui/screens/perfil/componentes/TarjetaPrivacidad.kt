package mx.tec.familiasquesuman.ui.screens.perfil.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.*

@Composable
fun TarjetaPrivacidad(titulo: String, texto: String, icono: ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Superficie,
        border = BorderStroke(1.dp, Borde), shadowElevation = 2.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(icono, contentDescription = null, tint = MarcaAzul, modifier = Modifier.size(20.dp))
                Text(titulo, style = MaterialTheme.typography.titleLarge, color = Tinta)
            }
            Text(texto, style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
        }
    }
}
