package mx.tec.familiasquesuman.ui.screens.inscripcion.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorRojo
import mx.tec.familiasquesuman.ui.theme.ErrorTexto

private val Redondeo = RoundedCornerShape(12.dp)

/** Franja roja con triángulo: "No se pudo crear la cuenta" (P-05b). */
@Composable
fun AvisoError(titulo: String, mensaje: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Redondeo)
            .background(ErrorFondo)
            .border(1.dp, ErrorRojo.copy(alpha = 0.3f), Redondeo)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = ErrorRojo, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(titulo, style = MaterialTheme.typography.labelLarge, color = ErrorTexto)
            Spacer(Modifier.height(2.dp))
            Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = ErrorTexto)
        }
    }
}

/** Caja verde con palomita: "Si ese correo tiene cuenta, el enlace llega…" (P-27). */
@Composable
fun AvisoExito(mensaje: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Redondeo)
            .background(ConfirmadoFondo)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = ConfirmadoTexto, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = ConfirmadoTexto)
    }
}

/** Caja crema: "AL ENTRAR REGRESAS A…" (P-26) o "Faltan 3 días…" (P-20). */
@Composable
fun NotaAmbar(texto: String, modifier: Modifier = Modifier, etiqueta: String? = null) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Redondeo)
            .background(AcentoSuave)
            .padding(12.dp)
    ) {
        if (etiqueta != null) {
            Text(etiqueta.uppercase(), style = MaterialTheme.typography.labelSmall, color = AcentoTexto)
            Spacer(Modifier.height(4.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), color = AcentoTexto)
    }
}

/** El círculo de color con un icono al centro (palomita verde en P-07, ámbar en P-32). */
@Composable
fun IconoEnCirculo(
    icono: ImageVector,
    fondo: Color,
    tinte: Color,
    modifier: Modifier = Modifier,
    tamano: Dp = 64.dp
) {
    Box(
        modifier = modifier.size(tamano).clip(CircleShape).background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(tamano * 0.42f))
    }
}
