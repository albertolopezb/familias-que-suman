package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoInstagram

// Piezas del detalle de un centro (familiasquesuman.com/directorio/{id}) y de un
// proyecto (familiasquesuman.com/proyectos/{id}). Las dos fichas se arman igual.

internal val EstiloSeccion = TextoWeb.Seccion.copy(fontSize = 16.sp, lineHeight = 22.sp)
internal val AzulMapa = Color(0xFF2563EB)

/** "Inicio › Directorio › Morada del Anciano": la última miga no se toca. */
@Composable
internal fun MigasDePan(migas: List<Pair<String, () -> Unit>>, actual: String) {
    Row(
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        migas.forEach { (texto, onClick) ->
            Text(texto, style = TextoWeb.Chico, modifier = Modifier.clickable(onClick = onClick))
            Icon(IconosWeb.FlechaDerecha, null, tint = Web.TextoApagado, modifier = Modifier.size(12.dp))
        }
        Text(
            actual,
            style = TextoWeb.Chico.copy(fontWeight = FontWeight.Medium),
            color = Web.Texto,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** La tarjeta blanca del sitio: borde fino, esquinas redondas y sombra suave. */
@Composable
internal fun TarjetaFicha(contenido: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Web.Tarjeta,
        border = BorderStroke(1.dp, Web.Borde),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = contenido)
    }
}

/** Una sección con título: blanca, o de color como "¿Cómo ayudar?" y "Recomendaciones". */
@Composable
internal fun SeccionFicha(
    titulo: String,
    texto: String,
    fondo: Color = Web.Tarjeta,
    borde: Color = Web.Borde
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = fondo,
        border = BorderStroke(1.dp, borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = EstiloSeccion)
            Text(texto, style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado))
        }
    }
}

/** "¿Cómo ayudar?" en azul muy claro, como el sitio. */
@Composable
internal fun SeccionComoAyudar(texto: String) =
    SeccionFicha("¿Cómo ayudar?", texto, Web.Primario.copy(alpha = 0.05f), Web.Primario.copy(alpha = 0.2f))

/** "Recomendaciones" en ámbar claro, como el sitio. */
@Composable
internal fun SeccionRecomendaciones(texto: String) =
    SeccionFicha("Recomendaciones", texto, Web.Amarillo.copy(alpha = 0.1f), Web.Amarillo.copy(alpha = 0.4f))

/** Un botón ancho de contacto: relleno (WhatsApp, Cómo llegar, Llamar) o con borde. */
@Composable
internal fun BotonFicha(
    texto: String,
    icono: ImageVector,
    color: Color,
    onClick: () -> Unit,
    relleno: Boolean = true
) {
    val forma = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(forma)
            .background(if (relleno) color else color.copy(alpha = 0.06f))
            .then(if (relleno) Modifier else Modifier.border(1.5.dp, color, forma))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tinta = if (relleno) Color.White else color
        Icon(icono, contentDescription = null, tint = tinta, modifier = Modifier.size(18.dp))
        Text(texto, style = TextoWeb.Boton.copy(fontSize = 15.sp), color = tinta)
    }
}

/** "Síguenos en:" con el círculo de colores de Instagram. */
@Composable
internal fun SiguenosEnInstagram(onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Síguenos en:", style = TextoWeb.Chip.copy(color = Web.TextoApagado))
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFFFDC7D), Color(0xFFF77737), Color(0xFF962FBF))))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(IconoInstagram, contentDescription = "Instagram", tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}
