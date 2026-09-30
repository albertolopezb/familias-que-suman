package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BarraDeRegreso
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonAmarillo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.CirculoDeIcono
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TarjetaActividad
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.actividadDeMuestra
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * Lo que se ve sin internet (P-38).
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
            .background(Web.Fondo)
    ) {
        BarraDeRegreso(texto = "Actividades", onRegresar = onRegresar)

        // La franja de aviso, siempre arriba mientras no haya red.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Web.RojoFondo)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                IconosWeb.SinWifi,
                contentDescription = null,
                tint = Web.RojoTexto,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Sin conexión · estás viendo actividades guardadas",
                style = TextoWeb.Chip,
                color = Web.RojoTexto
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "aviso") {
                AvisoSinConexion(onReintentar = onReintentar)
            }

            if (actividadesGuardadas.isNotEmpty()) {
                item(key = "rotulo") {
                    Text(
                        text = "GUARDADAS EN TU TELÉFONO",
                        style = TextoWeb.Rotulo,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(actividadesGuardadas, key = { it.actividad.id }) { item ->
                    TarjetaActividad(
                        item = item,
                        onClick = {},
                        cupoEnVivo = false,
                        modifier = Modifier.alpha(0.75f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AvisoSinConexion(onReintentar: () -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Web.Tarjeta)
            .border(1.dp, Web.Borde, forma)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CirculoDeIcono(icono = IconosWeb.SinWifi, color = Web.Primario, fondo = Web.Secundario)
        Text(
            text = "No tienes conexión",
            style = TextoWeb.Titulo.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Puedes ver las actividades que ya tenías, pero para inscribirte " +
                "necesitas internet: los lugares se apartan en el momento.",
            style = TextoWeb.Cuerpo,
            color = Web.TextoApagado,
            textAlign = TextAlign.Center
        )
        BotonAmarillo(
            texto = "Reintentar",
            onClick = onReintentar,
            conFlechas = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun SinConexionPreview() {
    FamiliasQueSumanTheme {
        SinConexionScreen(
            actividadesGuardadas = listOf(actividadDeMuestra(8)),
            onRegresar = {},
            onReintentar = {}
        )
    }
}
