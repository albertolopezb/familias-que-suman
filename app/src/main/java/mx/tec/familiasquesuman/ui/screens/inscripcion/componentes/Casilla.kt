package mx.tec.familiasquesuman.ui.screens.inscripcion.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/** Casilla azul marino con texto que también se puede tocar (aviso de privacidad, consentimiento). */
@Composable
fun Casilla(
    marcada: Boolean,
    onCambio: (Boolean) -> Unit,
    texto: String,
    modifier: Modifier = Modifier,
    habilitada: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = habilitada) { onCambio(!marcada) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = marcada,
            onCheckedChange = onCambio,
            enabled = habilitada,
            colors = CheckboxDefaults.colors(checkedColor = MarcaAzul, uncheckedColor = TintaSuave)
        )
        Text(texto, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp), color = Tinta)
    }
}

@Preview(showBackground = true)
@Composable
private fun CasillaPreview() {
    FamiliasQueSumanTheme {
        Casilla(true, {}, "He leído el aviso de privacidad y acepto cómo se usan los datos de mi familia.")
    }
}
