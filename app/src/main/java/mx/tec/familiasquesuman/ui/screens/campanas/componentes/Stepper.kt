package mx.tec.familiasquesuman.ui.screens.campanas.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    var texto by remember { mutableStateOf(valor.toString()) }
    // Cualquier cambio (teclado, − o +) queda entre mínimo y máximo y se refleja en el número.
    fun poner(nuevo: Int) {
        val n = nuevo.coerceIn(minimo, maximo)
        texto = n.toString()
        onCambio(n)
    }

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
            BotonRedondo(activo = habilitado && valor > minimo, onClick = { poner(valor - 1) }) {
                // Sin ícono "menos" en material-icons-core: se dibuja con un texto.
                Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colorDe(habilitado && valor > minimo))
            }
            // El número se puede tocar y teclear; se corrige solo si se pasa del máximo.
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .border(1.dp, Borde, RoundedCornerShape(12.dp))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = texto,
                    onValueChange = { nuevo ->
                        val digitos = nuevo.filter { it.isDigit() }.take(4)
                        if (digitos.isEmpty()) {
                            texto = ""
                        } else {
                            poner(digitos.toInt())
                        }
                    },
                    enabled = habilitado,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = if (habilitado) Tinta else TintaSuave,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(MarcaAzul),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused && texto.isEmpty()) texto = valor.toString() }
                )
            }
            BotonRedondo(activo = habilitado && valor < maximo, onClick = { poner(valor + 1) }) {
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
