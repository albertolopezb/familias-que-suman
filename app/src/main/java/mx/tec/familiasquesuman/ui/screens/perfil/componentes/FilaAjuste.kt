package mx.tec.familiasquesuman.ui.screens.perfil.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.*

@Composable
fun FilaAjuste(
    titulo: String,
    descripcion: String,
    activo: Boolean,
    onCambiar: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = Tinta)
            Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
        }
        Switch(checked = activo, onCheckedChange = onCambiar,
            modifier = Modifier.semantics { contentDescription = titulo },
            colors = SwitchDefaults.colors(
                checkedTrackColor = MarcaAzul, checkedThumbColor = Superficie,
                uncheckedTrackColor = Borde, uncheckedThumbColor = Superficie,
                uncheckedBorderColor = Borde
            ))
    }
}
