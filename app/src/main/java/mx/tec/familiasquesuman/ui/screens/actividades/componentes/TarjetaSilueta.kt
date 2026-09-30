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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lo que se ve mientras la lista carga.
 *
 * Copia la forma de la tarjeta real a propósito: así el contenido no salta
 * cuando llegan los datos, y se entiende que está cargando y no que está vacío.
 */
@Composable
fun TarjetaSilueta(
    modifier: Modifier = Modifier,
    fraccionTitulo: Float = 0.7f
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
    val forma = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Web.Tarjeta)
            .border(1.dp, Web.Borde, forma)
            .alpha(opacidad)
    ) {
        // El hueco de la foto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 64.dp, vertical = 32.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Web.Secundario)
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Web.Secundario)
                )
                Hueso(fraccion = fraccionTitulo, alto = 18.dp)
            }
            Hueso(fraccion = 0.55f, alto = 12.dp)
            Hueso(fraccion = 0.75f, alto = 12.dp)
            Hueso(fraccion = 1f, alto = 8.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Web.Secundario)
                )
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Web.Secundario)
                )
            }
        }
    }
}

@Composable
private fun Hueso(fraccion: Float, alto: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth(fraccion)
            .height(alto)
            .clip(RoundedCornerShape(6.dp))
            .background(Web.Secundario)
    )
}
