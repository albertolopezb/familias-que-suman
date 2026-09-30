package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Aportacion
import mx.tec.familiasquesuman.domain.TemaActividad

/**
 * Piezas chicas que comparten las pantallas de actividades.
 * Si alguna acaba usándose en otra área, se saca a ui/components/ en un PR aparte.
 */

/** Chip redondo de una línea: fondo suave y texto del mismo tono, más oscuro. */
@Composable
fun Etiqueta(
    texto: String,
    fondo: Color,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = texto,
        style = TextoWeb.Chip,
        color = color,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

/** "Sin aportación" en verde, "Aportación en especie" en morado y el monto en ámbar. */
@Composable
fun ChipAportacion(aportacion: Aportacion, modifier: Modifier = Modifier) {
    when (aportacion) {
        Aportacion.Ninguna ->
            Etiqueta("Sin aportación", Web.VerdeFondo, Web.VerdeTexto, modifier)
        is Aportacion.EnEspecie ->
            Etiqueta("Aportación en especie", Web.MoradoFondo, Web.MoradoTexto, modifier)
        is Aportacion.Monetaria ->
            Etiqueta(aportacion.monto, Web.AmbarFondo, Web.AmbarTexto, modifier)
    }
}

/** El título de la sección de aportación en el detalle. */
fun tituloDeAportacion(aportacion: Aportacion): String = when (aportacion) {
    Aportacion.Ninguna -> "Sin aportación"
    is Aportacion.EnEspecie -> "Aportación en especie"
    is Aportacion.Monetaria -> "Información de pago"
}

/** El ícono de la actividad dentro de su cuadro de color, como en el sitio. */
@Composable
fun IconoTema(
    tema: TemaActividad,
    modifier: Modifier = Modifier,
    tamano: Dp = 32.dp
) {
    val (icono, color, fondo) = estiloDeTema(tema)
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(tamano / 4))
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(tamano / 2)
        )
    }
}

private fun estiloDeTema(tema: TemaActividad): Triple<ImageVector, Color, Color> = when (tema) {
    TemaActividad.SALUD -> Triple(IconosWeb.Corazon, Web.Rosa, Web.RosaFondo)
    TemaActividad.CELEBRACION -> Triple(IconosWeb.Destellos, Web.Ambar, Web.AmbarSuave)
    TemaActividad.MEDIO_AMBIENTE -> Triple(IconosWeb.Pino, Web.VerdeTema, Web.VerdeFondo)
    TemaActividad.GENERAL -> Triple(IconosWeb.ManoCorazon, Web.Primario, Web.Secundario)
}

/** Ícono chico y texto gris: fecha, lugar y capacidad de la tarjeta. */
@Composable
fun FilaDato(icono: ImageVector, texto: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Web.TextoApagado,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = texto,
            style = TextoWeb.Chico,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * "Lugares tomados" con su barra (RF-05). Verde normalmente, ámbar cuando
 * quedan pocos y rojo cuando se llenó. Quién es "pocos" lo decide el dominio.
 */
@Composable
fun IndicadorCupo(actividad: Actividad, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Lugares tomados", style = TextoWeb.Chico)
            Text(
                text = textoDeCupo(actividad),
                style = TextoWeb.Chip,
                color = colorDeTextoCupo(actividad)
            )
        }
        BarraCupo(progreso = progresoDeCupo(actividad), color = colorDeCupo(actividad))
    }
}

/** La barra de cupo: pista gris y avance redondeado, de 8 dp como en el sitio. */
@Composable
fun BarraCupo(
    progreso: Float,
    color: Color,
    modifier: Modifier = Modifier,
    alto: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .clip(RoundedCornerShape(50))
            .background(Web.Secundario)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progreso.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

fun textoDeCupo(actividad: Actividad): String =
    if (actividad.sinLugares) "¡Lleno!" else "${actividad.ocupados} / ${actividad.cupoTotal} personas"

fun colorDeCupo(actividad: Actividad): Color = when {
    actividad.sinLugares -> Web.Rojo
    actividad.quedanPocos -> Web.Ambar
    else -> Web.Verde
}

fun colorDeTextoCupo(actividad: Actividad): Color = when {
    actividad.sinLugares -> Web.RojoTexto
    actividad.quedanPocos -> Web.AmbarTexto
    else -> Web.VerdeTexto
}

/** Qué tan llena está la actividad, de 0 a 1. */
fun progresoDeCupo(actividad: Actividad): Float =
    if (actividad.cupoTotal == 0) 0f else actividad.ocupados.toFloat() / actividad.cupoTotal

/** "09:45 – 11:30" → "09:45". La tarjeta solo enseña la hora de inicio. */
fun horaDeInicio(horario: String): String = horario.substringBefore("–").trim()

/**
 * Busca la foto de la actividad en res/drawable por su nombre.
 *
 * Así, para ponerle foto a una actividad basta con copiar el archivo a
 * res/drawable con el nombre que dice `Actividad.foto`; no hay que tocar código.
 * Devuelve null si todavía no está.
 */
@SuppressLint("DiscouragedApi")
@Composable
fun fotoDeActividad(nombre: String?): Int? {
    val contexto = LocalContext.current
    return remember(nombre) {
        nombre?.let {
            contexto.resources.getIdentifier(it, "drawable", contexto.packageName)
                .takeIf { id -> id != 0 }
        }
    }
}

/**
 * La imagen de arriba de la tarjeta: blanca, centrada y sin recortar, como el
 * sitio. Si la actividad tiene foto pero el archivo aún no está, se ve el ícono
 * del tema para que se note dónde va.
 */
@Composable
fun FotoTarjeta(
    foto: String,
    tema: TemaActividad,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    val recurso = fotoDeActividad(foto)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .background(Web.Tarjeta)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (recurso != null) {
            Image(
                painter = painterResource(recurso),
                contentDescription = descripcion,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .heightIn(max = 140.dp)
            )
        } else {
            IconoTema(tema = tema, tamano = 72.dp)
        }
    }
}

/** El botón amarillo del sitio. Apagado se pinta gris. */
@Composable
fun BotonAmarillo(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    radio: Dp = 12.dp,
    alto: Dp = 44.dp,
    negritas: Boolean = false,
    conFlechas: Boolean = true
) {
    val fondo = if (habilitado) Web.Amarillo else Web.Secundario
    val color = if (habilitado) Color.White else Web.TextoApagado
    Row(
        modifier = modifier
            .height(alto)
            .clip(RoundedCornerShape(radio))
            .background(fondo)
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (conFlechas && habilitado) {
            Icon(IconosWeb.Mas, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Text(
            text = texto,
            style = TextoWeb.Boton.copy(
                fontWeight = if (negritas) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = color
        )
        if (conFlechas && habilitado) {
            Icon(
                IconosWeb.FlechaDerecha,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/** El botón cuadrado con borde, para compartir. */
@Composable
fun BotonContorno(
    icono: ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Web.Borde, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = descripcion,
            tint = Web.TextoApagado,
            modifier = Modifier.size(14.dp)
        )
    }
}

/** La píldora gris del sitio: ciudad, compartir. */
@Composable
fun Pildora(
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Web.TextoApagado
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Web.Secundario)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(text = texto, style = TextoWeb.Chip, color = color)
    }
}
