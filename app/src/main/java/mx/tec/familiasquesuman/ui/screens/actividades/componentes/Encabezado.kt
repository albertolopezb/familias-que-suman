package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import mx.tec.familiasquesuman.ui.components.BarraSuperior
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.R
import mx.tec.familiasquesuman.ui.components.BotonPerfil
import mx.tec.familiasquesuman.ui.components.SelectorCiudad

/**
 * La barra de arriba: logo a la izquierda y el perfil a la derecha, sobre blanco
 * y con una línea abajo. El Inicio además enseña el selector de ciudad.
 */
@Composable
fun EncabezadoApp(
    modifier: Modifier = Modifier,
    // Solo el Inicio pasa la ciudad; en las demás pantallas no hay selector.
    ciudad: String? = null,
    onCiudadSeleccionada: (String) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth().background(Web.Tarjeta)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // El logo trae mucho margen blanco: el recorte deja solo las letras.
            Image(
                painter = painterResource(R.drawable.logo_familias),
                contentDescription = "Familias que Suman",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(120.dp)
                    .height(40.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ciudad != null) {
                    SelectorCiudad(ciudad, onCiudadSeleccionada)
                    Spacer(Modifier.width(8.dp))
                }
                BotonPerfil()
            }
        }
        HorizontalDivider(color = Web.Borde, thickness = 1.dp)
    }
}

/**
 * Migas de pan, título y subtítulo: el arranque de cada página del sitio.
 * El título acepta un toque largo para que la lista pueda esconder ahí su botón
 * de pruebas.
 */
@Composable
fun TituloDePagina(
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    modificadorTitulo: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 24.dp, bottom = 16.dp)) {
        Text(text = titulo, style = TextoWeb.Titulo, modifier = modificadorTitulo)
        Text(
            text = subtitulo,
            style = TextoWeb.Cuerpo.copy(lineHeight = 20.sp),
            color = Web.TextoApagado,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * La barra del detalle: "‹ Actividades" para regresar y, a la derecha, las
 * píldoras que la pantalla necesite (compartir, favorito).
 */
@Composable
fun BarraDeRegreso(
    texto: String,
    onRegresar: () -> Unit,
    modifier: Modifier = Modifier,
    acciones: @Composable () -> Unit = {}
) {
    mx.tec.familiasquesuman.ui.components.BarraSuperior(texto, onRegresar, modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { acciones() }
    }
}

/** El círculo con ícono grande de los estados vacío, error y sin conexión. */
@Composable
fun CirculoDeIcono(
    icono: ImageVector,
    color: Color,
    fondo: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
    }
}
