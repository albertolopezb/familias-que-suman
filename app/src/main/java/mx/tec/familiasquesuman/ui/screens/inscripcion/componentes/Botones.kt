package mx.tec.familiasquesuman.ui.screens.inscripcion.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.ErrorRojo
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

private val Redondeo = RoundedCornerShape(12.dp)

/** Barra blanca con flecha de regreso y título, como "← Crear cuenta". */
@Composable
fun BarraConRegreso(titulo: String, onRegresar: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Superficie)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onRegresar) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Tinta)
        }
        Text(titulo, style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), color = Tinta)
    }
}

/** Botón oro de ancho completo. Con `cargando` muestra el texto de espera ("Reservando tus lugares…"). */
@Composable
fun BotonPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = Redondeo,
        colors = ButtonDefaults.buttonColors(
            containerColor = MarcaOro,
            contentColor = MarcaAzul,
            disabledContainerColor = if (cargando) MarcaOro.copy(alpha = 0.5f) else Borde,
            disabledContentColor = if (cargando) MarcaAzul.copy(alpha = 0.75f) else TintaSuave
        )
    ) {
        if (cargando) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MarcaAzul.copy(alpha = 0.75f))
            Spacer(Modifier.width(10.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp))
    }
}

/** Botón blanco con contorno azul marino, para la segunda opción. */
@Composable
fun BotonSecundario(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = Redondeo,
        border = BorderStroke(1.5.dp, if (habilitado) MarcaAzul else Borde),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Superficie, contentColor = MarcaAzul)
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp))
    }
}

/** Botón rojo para lo que no se deshace: "Sí, cancelar inscripción". */
@Composable
fun BotonPeligro(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, cargando: Boolean = false) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = Redondeo,
        colors = ButtonDefaults.buttonColors(
            containerColor = ErrorRojo,
            contentColor = Color.White,
            disabledContainerColor = ErrorRojo.copy(alpha = 0.5f),
            disabledContentColor = Color.White
        )
    ) {
        if (cargando) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            Spacer(Modifier.width(10.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp))
    }
}

/** Franja blanca pegada abajo donde viven los botones de la pantalla. */
@Composable
fun BarraAccionesInferior(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.fillMaxWidth().background(Superficie)) {
        HorizontalDivider(color = Borde)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = contenido
        )
    }
}
