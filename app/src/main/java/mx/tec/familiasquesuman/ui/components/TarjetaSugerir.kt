package mx.tec.familiasquesuman.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.TipoSugerencia
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * "¿Conoces un proyecto que debería estar aquí? Sugiérelo": va al final de las listas de
 * actividades, campañas, proyectos y directorio, y abre "Sugerir" con ese tipo ya elegido.
 */
@Composable
fun TarjetaSugerir(tipo: TipoSugerencia, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Web.Amarillo.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Web.Amarillo.copy(alpha = 0.45f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(Web.Amarillo.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(IconosWeb.Mas, contentDescription = null, tint = Web.AmbarTexto, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "¿Conoces ${tipo.articulo} que debería estar aquí?",
                    style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.SemiBold),
                    color = Web.Texto
                )
                Text("Sugiérelo y el equipo lo revisa para publicarlo.", style = TextoWeb.Chico)
            }
            Icon(IconosWeb.FlechaDerecha, contentDescription = "Sugerir", tint = Web.AmbarTexto, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaSugerirPreview() {
    FamiliasQueSumanTheme { TarjetaSugerir(TipoSugerencia.PROYECTO, {}, Modifier.padding(16.dp)) }
}
