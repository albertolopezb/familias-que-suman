package mx.tec.familiasquesuman.ui.screens.inscripcion.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.Superficie

/** El velo oscuro que va detrás de hojas y diálogos. */
val Velo = Color.Black.copy(alpha = 0.45f)

/**
 * La hoja blanca que sube desde abajo sobre la pantalla oscurecida (P-04, P-08).
 * Hecha a mano para no depender de ModalBottomSheet, que sigue siendo experimental.
 *
 * @param conVelo false cuando ya hay velo, por ejemplo dentro de un `dialog` de navegación.
 */
@Composable
fun HojaInferior(
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier,
    conVelo: Boolean = true,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(if (conVelo) Modifier.background(Velo) else Modifier)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCerrar),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Superficie)
                // Que tocar dentro de la hoja no la cierre.
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp, bottom = 16.dp)
        ) {
            Box(
                Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Borde)
            )
            Spacer(Modifier.height(14.dp))
            contenido()
        }
    }
}
