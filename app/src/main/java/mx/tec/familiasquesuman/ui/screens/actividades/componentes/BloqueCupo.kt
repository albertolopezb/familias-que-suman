package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * El bloque rojo del detalle cuando la actividad se llenó (P-03b, RF-05).
 *
 * Va arriba de la información para que la familia lo vea antes de leer todo lo
 * demás, y le dice qué puede hacer en lugar de solo decirle que no.
 */
@Composable
fun AvisoSinLugares(cupoTotal: Int, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Web.RojoFondo)
            .border(1.dp, Web.RojoBorde, forma)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                IconosWeb.Personas,
                contentDescription = null,
                tint = Web.RojoTexto,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "¡Lleno! Ya no hay lugares",
                style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.SemiBold),
                color = Web.RojoTexto
            )
        }
        Text(
            text = "Se ocuparon los $cupoTotal lugares de esta actividad. Revisa las " +
                "otras fechas: seguido se abren visitas nuevas.",
            style = TextoWeb.Chico,
            color = Web.RojoTexto
        )
    }
}
