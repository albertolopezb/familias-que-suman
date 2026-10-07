package mx.tec.familiasquesuman.ui.screens.inscripcion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPeligro
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonSecundario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.NotaAmbar
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.Velo
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * P-20 · Cancelar inscripción. Diálogo centrado con velo, botón rojo y "Mejor no".
 * En la navegación es un `dialog` sobre Mis Actividades, que pone el velo; aquí `conVelo`
 * solo sirve para el @Preview.
 */
@Composable
fun CancelarScreen(
    personas: Int,
    faltaParaActividad: String,
    cancelando: Boolean,
    onConfirmar: () -> Unit,
    onMejorNo: () -> Unit,
    conVelo: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (conVelo) Velo else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !cancelando,
                onClick = onMejorNo
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Superficie)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("¿Cancelar tu inscripción?", style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), color = Tinta)
            Text(
                (if (personas == 1) "Tu lugar vuelve" else "Los ${lugares(personas)} vuelven") +
                    " a quedar disponibles de inmediato para otra familia. Puedes volver a inscribirte si todavía hay cupo.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                color = TintaSuave
            )
            NotaAmbar(faltaParaActividad)
            Spacer(Modifier.height(2.dp))
            BotonPeligro(if (cancelando) "Cancelando…" else "Sí, cancelar inscripción", onConfirmar, cargando = cancelando)
            BotonSecundario("Mejor no", onMejorNo, habilitado = !cancelando)
        }
    }
}

@Preview(showBackground = true, heightDp = 780, name = "P-20 · Cancelar inscripción")
@Composable
private fun CancelarPreview() {
    FamiliasQueSumanTheme { CancelarScreen(3, CuentaDePrueba.FALTA_PARA_ACTIVIDAD, false, {}, {}, conVelo = true) }
}
