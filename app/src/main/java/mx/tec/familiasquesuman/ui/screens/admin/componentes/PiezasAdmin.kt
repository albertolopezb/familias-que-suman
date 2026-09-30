package mx.tec.familiasquesuman.ui.screens.admin.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web

/**
 * Piezas del panel de administración, copiadas de familiasquesuman.com/admin:
 * tarjetas blancas con borde, íconos en cuadro de color y rótulos en mayúsculas.
 */

/** Un renglón del panel: ícono en su cuadro, texto, contador rojo opcional y flecha. */
@Composable
fun RenglonAdmin(
    texto: String,
    icono: ImageVector,
    colorIcono: Color,
    fondoIcono: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contador: Int = 0
) {
    val forma = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Web.Tarjeta)
            .border(1.dp, Web.Borde, forma)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(fondoIcono),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(20.dp))
        }
        Text(
            text = texto,
            style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.Medium, fontSize = 15.sp),
            modifier = Modifier.weight(1f)
        )
        if (contador > 0) {
            Text(
                text = "$contador",
                style = TextoWeb.Chip.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Web.RojoTexto)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Icon(
            IconosWeb.FlechaDerecha,
            contentDescription = null,
            tint = Web.TextoApagado,
            modifier = Modifier.size(16.dp)
        )
    }
}

/** "OPERACIÓN", "CONTENIDO PÚBLICO"…: el rótulo de cada bloque del panel. */
@Composable
fun RotuloAdmin(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto.uppercase(),
        style = TextoWeb.Chico.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
        modifier = modifier.padding(bottom = 12.dp)
    )
}

/** Tarjeta de número grande: "Centros pendientes", "Personas"… */
@Composable
fun TarjetaNumero(
    numero: String,
    etiqueta: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(forma)
            .background(Web.Tarjeta)
            .border(1.dp, Web.Borde, forma)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = numero,
            style = TextoWeb.Titulo.copy(fontSize = 30.sp, lineHeight = 36.sp),
            color = color
        )
        Text(
            text = etiqueta,
            style = TextoWeb.Chico,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Campo de texto con el estilo del formulario del sitio: etiqueta arriba, borde gris. */
@Composable
fun CampoAdmin(
    etiqueta: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    lineas: Int = 1,
    teclado: KeyboardType = KeyboardType.Text,
    error: String? = null,
    ocultar: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = etiqueta,
            style = TextoWeb.Chip.copy(fontWeight = FontWeight.Medium),
            color = Web.Texto
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder, style = TextoWeb.Cuerpo, color = Web.TextoApagado) }
            } else null,
            textStyle = TextoWeb.Cuerpo,
            singleLine = lineas == 1,
            minLines = lineas,
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            visualTransformation = if (ocultar) {
                androidx.compose.ui.text.input.PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Web.Borde,
                focusedBorderColor = Web.Primario,
                unfocusedContainerColor = Web.Tarjeta,
                focusedContainerColor = Web.Tarjeta,
                errorBorderColor = Web.RojoTexto
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Text(text = error, style = TextoWeb.Chico, color = Web.RojoTexto)
        }
    }
}

/** Chip seleccionable para opciones cortas (tema, aportación). */
@Composable
fun OpcionAdmin(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(50)
    Text(
        text = texto,
        style = TextoWeb.Chip,
        color = if (seleccionada) Color.White else Web.Texto,
        modifier = modifier
            .clip(forma)
            .background(if (seleccionada) Web.Primario else Web.Tarjeta)
            .border(1.dp, if (seleccionada) Web.Primario else Web.Borde, forma)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

/** Lo que ve quien no es admin, con el mismo texto del sitio. */
@Composable
fun SinAcceso(onIrAlAcceso: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Web.Fondo)
            .padding(horizontal = 16.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            IconosWeb.Escudo,
            contentDescription = null,
            tint = Web.TextoApagado,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No tienes acceso a esta sección.",
            style = TextoWeb.Cuerpo,
            color = Web.TextoApagado
        )
        Text(
            text = "Iniciar sesión como administrador",
            style = TextoWeb.Chip,
            color = Web.Primario,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onIrAlAcceso)
                .padding(8.dp)
        )
    }
}

/** Separador suave entre bloques de un formulario. */
@Composable
fun EspacioAdmin(alto: Int = 8) {
    Box(Modifier.height(alto.dp))
}
