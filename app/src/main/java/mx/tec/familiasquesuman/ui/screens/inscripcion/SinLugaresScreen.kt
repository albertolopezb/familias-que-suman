package mx.tec.familiasquesuman.ui.screens.inscripcion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.AvisoExito
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPrimario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonSecundario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.HojaInferior
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * P-08 · Se acabaron los lugares. El único error diseñado del proyecto:
 * contador en cero, "Quedaban 3" tachado y tres salidas.
 */
@Composable
fun SinLugaresScreen(
    tituloActividad: String,
    sinLugares: SinLugaresUi,
    avisoRegistrado: Boolean,
    onVerOtrasActividades: () -> Unit,
    onAvisarme: () -> Unit,
    onCerrar: () -> Unit
) {
    HojaInferior(onCerrar = onCerrar) {
        Text("Se ocuparon los últimos lugares", style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), color = Tinta)
        Spacer(Modifier.height(6.dp))
        Text(
            "Mientras confirmabas, otra familia tomó " +
                (if (sinLugares.quedaban == 1) "el lugar que quedaba" else "los ${sinLugares.quedaban} lugares que quedaban") +
                ". Tu inscripción no se registró y no se guardó ningún dato.",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
            color = TintaSuave
        )
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ErrorFondo)
                .padding(12.dp)
        ) {
            Text(
                "${tituloActividad.split(" ").take(2).joinToString(" ")} — ${sinLugares.ahora} de ${lugares(sinLugares.cupoTotal)}",
                style = MaterialTheme.typography.titleMedium,
                color = ErrorTexto
            )
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append("Quedaban ${sinLugares.quedaban}")
                    }
                    append(" · actualizado hace un momento")
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                color = ErrorTexto
            )
        }
        Spacer(Modifier.height(14.dp))
        BotonPrimario("Ver otras actividades", onVerOtrasActividades)
        Spacer(Modifier.height(10.dp))
        if (avisoRegistrado) {
            AvisoExito("Listo. Te mandamos una notificación si se libera un lugar.")
        } else {
            BotonSecundario("Avisarme si se libera un lugar", onAvisarme)
        }
        TextButton(onClick = onCerrar, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar", style = MaterialTheme.typography.labelLarge, color = TintaSuave)
        }
    }
}

@Preview(showBackground = true, heightDp = 640, name = "P-08 · Se acabaron los lugares")
@Composable
private fun SinLugaresPreview() {
    FamiliasQueSumanTheme {
        SinLugaresScreen("Preparar despensas de fin de mes", SinLugaresUi(3, 0, 20), false, {}, {}, {})
    }
}
