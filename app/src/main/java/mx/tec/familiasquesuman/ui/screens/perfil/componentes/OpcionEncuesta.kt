package mx.tec.familiasquesuman.ui.screens.perfil.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.*

@Composable
fun OpcionEncuesta(texto: String, seleccionada: Boolean, onSeleccionar: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = Superficie,
        border = BorderStroke(1.dp, if (seleccionada) MarcaAzul else Borde), shadowElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().selectable(selected = seleccionada, onClick = onSeleccionar,
            role = Role.RadioButton).padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RadioButton(selected = seleccionada, onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = MarcaAzul, unselectedColor = TintaSuave))
            Text(texto, style = MaterialTheme.typography.bodyLarge, color = Tinta, modifier = Modifier.weight(1f))
        }
    }
}
