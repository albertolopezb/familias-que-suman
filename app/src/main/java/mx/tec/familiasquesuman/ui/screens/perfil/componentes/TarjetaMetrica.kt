package mx.tec.familiasquesuman.ui.screens.perfil.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.theme.*



@Composable
fun TarjetaMetrica(numero: Int, etiqueta: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 100.dp),
        shape = RoundedCornerShape(14.dp),
        color = Superficie,
        border = BorderStroke(1.dp, Borde),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            Text(numero.toString(), style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp
            ), color = Tinta)
            Text(etiqueta, style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave, textAlign = TextAlign.Center)
        }
    }
}
