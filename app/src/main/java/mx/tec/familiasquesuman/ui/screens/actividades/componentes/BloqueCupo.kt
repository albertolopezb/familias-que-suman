package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorTexto

/**
 * El bloque de lugares del detalle (RF-05).
 *
 * Es lo primero que la familia busca al abrir una actividad, por eso tiene
 * fondo propio en vez de ser una línea más del texto.
 */
@Composable
fun BloqueCupo(actividad: Actividad, modifier: Modifier = Modifier) {
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(fondo)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (actividad.sinLugares) {
                "Ya no hay lugares para esta actividad"
            } else {
                "Quedan ${actividad.lugaresDisponibles} de ${actividad.cupoTotal} lugares"
            },
            style = MaterialTheme.typography.labelLarge,
            color = color
        )

        if (actividad.sinLugares) {
            Text(
                text = "Se llenó hace un momento.",
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        } else {
            BarraCupo(
                progreso = progresoDeCupo(actividad),
                color = colorDeCupo(actividad),
                alto = 7.dp,
                colorPista = color.copy(alpha = 0.22f)
            )
        }
    }
}
