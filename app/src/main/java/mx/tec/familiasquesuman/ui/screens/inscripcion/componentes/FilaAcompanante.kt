package mx.tec.familiasquesuman.ui.screens.inscripcion.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorRojo
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

private val Redondeo = RoundedCornerShape(12.dp)

/**
 * Un renglón de P-06. La primera fila (la persona de la cuenta) va sin ×.
 * Las demás llevan × para quitarlas. Si la edad no alcanza, el renglón se pinta en rojo.
 */
@Composable
fun FilaAcompanante(
    nombre: String,
    edad: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    onQuitar: (() -> Unit)? = null
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Redondeo)
                .background(if (error != null) ErrorFondo.copy(alpha = 0.45f) else Superficie)
                .border(1.dp, if (error != null) ErrorRojo else Borde, Redondeo)
                .padding(start = 14.dp, end = if (onQuitar != null) 2.dp else 14.dp)
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                nombre,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = Tinta,
                modifier = Modifier.weight(1f)
            )
            Text(edad, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
            if (onQuitar != null) {
                IconButton(onClick = onQuitar) {
                    Icon(Icons.Filled.Close, contentDescription = "Quitar a $nombre", tint = TintaSuave, modifier = Modifier.size(18.dp))
                }
            }
        }
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error, style = MaterialTheme.typography.bodyMedium, color = ErrorRojo)
        }
    }
}

/** La fila recién agregada: nombre y edad se escriben ahí mismo, con validación en vivo. */
@Composable
fun FilaAcompananteNueva(
    nombre: String,
    edad: String,
    onNombreChange: (String) -> Unit,
    onEdadChange: (String) -> Unit,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier,
    errorNombre: String? = null,
    errorEdad: String? = null,
    habilitada: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Redondeo)
            .background(Superficie)
            .border(1.dp, Borde, Redondeo)
            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        CampoTexto(
            etiqueta = "",
            valor = nombre,
            onValorChange = onNombreChange,
            placeholder = "Nombre completo",
            error = errorNombre,
            habilitado = habilitada,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        CampoTexto(
            etiqueta = "",
            valor = edad,
            onValorChange = { valor -> onEdadChange(valor.filter { it.isDigit() }.take(2)) },
            placeholder = "Edad",
            error = errorEdad,
            tipoTeclado = KeyboardType.Number,
            habilitado = habilitada,
            modifier = Modifier.width(84.dp)
        )
        IconButton(onClick = onQuitar, enabled = habilitada) {
            Icon(Icons.Filled.Close, contentDescription = "Quitar esta fila", tint = TintaSuave, modifier = Modifier.size(18.dp))
        }
    }
}

/** El botón con borde punteado de "+ Agregar acompañante". */
@Composable
fun BotonAgregarPunteado(texto: String, onClick: () -> Unit, habilitado: Boolean = true) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(Redondeo)
            .drawBehind {
                drawRoundRect(
                    color = TintaSuave.copy(alpha = 0.45f),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 5.dp.toPx()))
                    )
                )
            }
            .clickable(enabled = habilitado, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), color = MarcaAzul)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun FilasPreview() {
    FamiliasQueSumanTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FilaAcompanante("Ana Rodríguez (tú)", "38")
            FilaAcompanante("Mateo Rodríguez", "9 años", onQuitar = {})
            FilaAcompanante("Renata Rodríguez", "4 años", error = "La edad mínima es de 6 años", onQuitar = {})
            FilaAcompananteNueva("", "", {}, {}, {})
            BotonAgregarPunteado("+ Agregar acompañante", {})
        }
    }
}
