package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.Superficie

/**
 * Lo que se ve mientras la lista carga.
 *
 * Copia la forma de la tarjeta real a propósito: así el contenido no salta
 * cuando llegan los datos, y se entiende que está cargando y no que está vacío.
 */
@Composable
fun TarjetaSilueta(
    modifier: Modifier = Modifier,
    fraccionTitulo: Float = 0.88f,
    fraccionSubtitulo: Float = 0.64f
) {
    val transicion = rememberInfiniteTransition(label = "silueta")
    val opacidad by transicion.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "opacidad"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Superficie)
            .border(1.dp, Borde, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .alpha(opacidad),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Borde)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Hueso(fraccion = fraccionTitulo, alto = 16.dp)
                Hueso(fraccion = fraccionSubtitulo, alto = 12.dp)
                Hueso(fraccion = 0.45f, alto = 12.dp)
            }
        }
        Hueso(fraccion = 1f, alto = 6.dp)
    }
}

@Composable
private fun Hueso(fraccion: Float, alto: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth(fraccion)
            .height(alto)
            .clip(RoundedCornerShape(6.dp))
            .background(Borde)
    )
}
