package mx.tec.familiasquesuman.ui.screens.campanas.componentes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.Confirmado
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * Barra de avance de una campaña. Si se pasa [animarDesde], la barra arranca ahí y
 * sube a [progreso] a la vista (el momento de P-13: de 40 % a 44 %).
 */
@Composable
fun BarraMeta(
    progreso: Float,
    modifier: Modifier = Modifier,
    color: Color = Confirmado,
    animarDesde: Float? = null,
    alto: Dp = 8.dp
) {
    var objetivo by remember { mutableFloatStateOf(animarDesde ?: progreso) }
    LaunchedEffect(progreso) { objetivo = progreso }
    val valor by animateFloatAsState(
        targetValue = objetivo.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "barraMeta"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.22f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(valor)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BarraMetaPreview() {
    FamiliasQueSumanTheme { BarraMeta(progreso = 0.4f, modifier = Modifier.background(Color.White)) }
}
