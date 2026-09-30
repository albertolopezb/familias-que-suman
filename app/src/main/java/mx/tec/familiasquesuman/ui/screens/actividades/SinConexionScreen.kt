package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoActividades
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TarjetaActividad
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.actividadDeMuestra
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * Lo que se ve sin internet.
 *
 * El alcance del reto excluye un modo offline completo: la app muestra lo último
 * que alcanzó a guardar, pero no finge saber cuántos lugares quedan, porque ese
 * número se reserva en el servidor y sin conexión estaría viejo.
 */
@Composable
fun SinConexionScreen(
    actividadesGuardadas: List<ActividadConAsociacion>,
    onRegresar: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        EncabezadoActividades(
            titulo = "Actividades en Familia",
            onRegresar = onRegresar
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ErrorFondo)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Sin conexión · estás viendo datos guardados",
                style = MaterialTheme.typography.labelMedium,
                color = ErrorTexto
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            actividadesGuardadas.forEach { item ->
                TarjetaActividad(
                    item = item,
                    onClick = {},
                    cupoEnVivo = false
                )
            }

            Text(
                text = "Para inscribirte necesitas internet: los lugares se reservan en el momento.",
                style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave,
                textAlign = TextAlign.Center
            )

            OutlinedButton(onClick = onReintentar) {
                Text("Reintentar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SinConexionPreview() {
    FamiliasQueSumanTheme {
        SinConexionScreen(
            actividadesGuardadas = listOf(actividadDeMuestra(8), actividadDeMuestra(2)),
            onRegresar = {},
            onReintentar = {}
        )
    }
}
