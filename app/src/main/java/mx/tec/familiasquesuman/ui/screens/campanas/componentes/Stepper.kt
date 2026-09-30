package mx.tec.familiasquesuman.ui.screens.campanas.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * Selector − / número / +. El − se apaga en [minimo] y el + en [maximo].
 * Con [habilitado] = false (mientras se aparta) los dos botones se apagan.
 */
@Composable
fun Stepper(
    valor: Int,
    minimo: Int,
    maximo: Int,
    onCambio: (Int) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Fondo,
        border = BorderStroke(1.dp, Borde)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonRedondo(activo = habilitado && valor > minimo, onClick = { onCambio(valor - 1) }) {
                // Sin ícono "menos" en material-icons-core: se dibuja con un texto.
                Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colorDe(habilitado && valor > minimo))
            }
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = if (habilitado) Tinta else TintaSuave
            )
            BotonRedondo(activo = habilitado && valor < maximo, onClick = { onCambio(valor + 1) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Más",
                    tint = colorDe(habilitado && valor < maximo),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun colorDe(activo: Boolean) = if (activo) MarcaAzul else TintaSuave.copy(alpha = 0.5f)

@Composable
private fun BotonRedondo(activo: Boolean, onClick: () -> Unit, contenido: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = activo,
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.5.dp, colorDe(activo)),
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) { contenido() }
    }
}

@Preview(showBackground = true)
@Composable
private fun StepperPreview() {
    FamiliasQueSumanTheme {
        Stepper(valor = 2, minimo = 1, maximo = 33, onCambio = {}, modifier = Modifier.padding(16.dp))
    }
}
