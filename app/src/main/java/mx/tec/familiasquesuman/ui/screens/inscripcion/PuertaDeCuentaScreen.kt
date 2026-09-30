package mx.tec.familiasquesuman.ui.screens.inscripcion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPrimario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonSecundario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.HojaInferior
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * P-04 · Puerta de cuenta. Hoja inferior sobre el detalle de la actividad.
 * En la navegación es un `dialog`: el detalle se sigue viendo detrás, oscurecido.
 *
 * @param actividad título de la actividad a la que iban; null mientras carga.
 * @param cuando "Sábado 12 de septiembre, 9:00"
 */
@Composable
fun PuertaDeCuentaScreen(
    actividad: String?,
    cuando: String,
    onCrearCuenta: () -> Unit,
    onYaTengoCuenta: () -> Unit,
    onCerrar: () -> Unit,
    conVelo: Boolean = false
) {
    HojaInferior(onCerrar = onCerrar, conVelo = conVelo) {
        Text(
            "Para inscribirte necesitas una cuenta",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, lineHeight = 26.sp),
            color = Tinta
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Solo te la pedimos una vez. Después regresas justo aquí.",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
            color = TintaSuave
        )
        if (actividad != null) {
            Spacer(Modifier.height(14.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Fondo)
                    .border(1.dp, Borde, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text("VAS A INSCRIBIRTE EN", style = MaterialTheme.typography.labelSmall, color = TintaSuave)
                Spacer(Modifier.height(4.dp))
                Text(actividad, style = MaterialTheme.typography.titleMedium, color = Tinta)
                Text(cuando, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
            }
        }
        Spacer(Modifier.height(16.dp))
        BotonPrimario("Crear cuenta", onCrearCuenta)
        Spacer(Modifier.height(10.dp))
        BotonSecundario("Ya tengo cuenta", onYaTengoCuenta)
    }
}

@Preview(showBackground = true, heightDp = 640, name = "P-04 · Puerta de cuenta")
@Composable
private fun PuertaDeCuentaPreview() {
    FamiliasQueSumanTheme {
        Box(Modifier.fillMaxSize().background(Color(0xFFE2E8F0))) {
            PuertaDeCuentaScreen(
                "Preparar despensas de fin de mes", "Sábado 12 de septiembre, 9:00",
                {}, {}, {}, conVelo = true
            )
        }
    }
}
