package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.CategoriaMoradoFondo
import mx.tec.familiasquesuman.ui.theme.CategoriaMoradoTexto
import mx.tec.familiasquesuman.ui.theme.Confirmado
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorRojo
import mx.tec.familiasquesuman.ui.theme.ErrorTexto
import mx.tec.familiasquesuman.ui.theme.MarcaOro

/**
 * Piezas chicas que comparten las pantallas de actividades.
 * Si alguna acaba usándose en otra área, se saca a ui/components/ en un PR aparte.
 */

/** Chip de una línea: fondo suave y texto del mismo tono, más oscuro. */
@Composable
fun Etiqueta(
    texto: String,
    fondo: Color,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(fondo)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(text = texto, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/** La barra de cupo. El color lo decide quien la usa, con colorDeCupo(). */
@Composable
fun BarraCupo(
    progreso: Float,
    color: Color,
    modifier: Modifier = Modifier,
    alto: Dp = 6.dp,
    colorPista: Color = Borde
) {
    val radio = RoundedCornerShape(alto / 2f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .clip(radio)
            .background(colorPista)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progreso.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(radio)
                .background(color)
        )
    }
}

/**
 * El lugar de la foto de la actividad. Mientras no haya fotos reales lleva un
 * degradado del tono de la categoría: para ponerle imagen basta cambiar este
 * Box por un Image con contentScale = ContentScale.Crop.
 */
@Composable
fun Miniatura(
    tinte: Pair<Color, Color>,
    modifier: Modifier = Modifier,
    tamano: Dp = 56.dp,
    radio: Dp = 12.dp
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(radio))
            .background(Brush.linearGradient(listOf(tinte.first, tinte.second)))
    )
}

/** La edad mínima, en las palabras del Figma. */
@Composable
fun EtiquetaEdad(edadMinima: Int?, modifier: Modifier = Modifier) {
    Etiqueta(
        texto = if (edadMinima == null) "Todas las edades" else "Desde $edadMinima años",
        fondo = CategoriaMoradoFondo,
        color = CategoriaMoradoTexto,
        modifier = modifier
    )
}

/**
 * Los lugares que quedan (RF-05). Verde normalmente, ámbar cuando quedan pocos
 * y rojo cuando se acabaron. Quién es "pocos" lo decide el dominio, no esta capa.
 */
@Composable
fun ChipCupo(actividad: Actividad, modifier: Modifier = Modifier) {
    val texto = if (actividad.sinLugares) {
        "Sin lugares"
    } else {
        "Quedan ${actividad.lugaresDisponibles} de ${actividad.cupoTotal} lugares"
    }
    val fondo = when {
        actividad.sinLugares -> ErrorFondo
        actividad.quedanPocos -> AcentoSuave
        else -> ConfirmadoFondo
    }
    val color = when {
        actividad.sinLugares -> ErrorTexto
        actividad.quedanPocos -> AcentoTexto
        else -> ConfirmadoTexto
    }
    Etiqueta(texto = texto, fondo = fondo, color = color, modifier = modifier)
}

/** El color de la barra, que acompaña al chip. */
fun colorDeCupo(actividad: Actividad): Color = when {
    actividad.sinLugares -> ErrorRojo
    actividad.quedanPocos -> MarcaOro
    else -> Confirmado
}

/** Qué tan llena está la actividad, de 0 a 1. */
fun progresoDeCupo(actividad: Actividad): Float =
    if (actividad.cupoTotal == 0) 0f else actividad.ocupados.toFloat() / actividad.cupoTotal

/** El tono de la miniatura, según la categoría de la asociación. */
fun tinteDeCategoria(categoria: String): Pair<Color, Color> = when (categoria) {
    "Alimentación" -> Color(0xFFFDF3DC) to Color(0xFFF3DFAE)
    "Adultos mayores" -> Color(0xFFDBEAFE) to Color(0xFFBFDBFE)
    "Ropa y abrigo" -> Color(0xFFD1FAE5) to Color(0xFFA7F3D0)
    "Niñez" -> Color(0xFFEDE9FE) to Color(0xFFDDD6FE)
    else -> Color(0xFFE8EDF3) to Color(0xFFD5DEE8)
}
