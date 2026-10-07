package mx.tec.familiasquesuman.ui.screens.inscripcion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraAccionesInferior
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPrimario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonSecundario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.IconoEnCirculo
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.Confirmado
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * P-32 · Cancelación confirmada. Palomita ámbar y bloque verde con los lugares liberados
 * y la barra ya actualizada.
 */
@Composable
fun CancelacionConfirmadaScreen(
    asociacion: String,
    liberados: Int,
    antes: Int,
    cupoTotal: Int,
    onVerOtrasActividades: () -> Unit,
    onVolverAMiPerfil: () -> Unit
) {
    val ahora = antes + liberados
    Column(Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconoEnCirculo(Icons.Filled.Check, AcentoSuave, AcentoTexto)
            Text("Cancelamos tu inscripción", style = MaterialTheme.typography.headlineMedium, color = Tinta)
            Text(
                "Ya avisamos al $asociacion.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                color = TintaSuave,
                textAlign = TextAlign.Center
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ConfirmadoFondo)
                    .padding(14.dp)
            ) {
                Text(
                    if (liberados == 1) "Se liberó 1 lugar" else "Se liberaron $liberados lugares",
                    style = MaterialTheme.typography.titleMedium,
                    color = ConfirmadoTexto
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (cupoTotal == 0) 0f else ahora.toFloat() / cupoTotal },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Confirmado,
                    trackColor = Color.White.copy(alpha = 0.7f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "La actividad pasó de $antes a ${lugares(ahora)} disponibles para otras familias.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ConfirmadoTexto
                )
            }
            Text(
                "Si cambian de opinión, pueden volver a inscribirse mientras quede cupo.",
                style = MaterialTheme.typography.labelMedium,
                color = TintaSuave,
                textAlign = TextAlign.Center
            )
        }
        BarraAccionesInferior {
            BotonPrimario("Ver otras actividades", onVerOtrasActividades)
            BotonSecundario("Volver a mi perfil", onVolverAMiPerfil)
        }
    }
}

@Preview(showBackground = true, heightDp = 780, name = "P-32 · Cancelación confirmada")
@Composable
private fun CancelacionConfirmadaPreview() {
    FamiliasQueSumanTheme {
        CancelacionConfirmadaScreen("Comedor Comunitario San Bernabé", 3, 0, 20, {}, {})
    }
}
