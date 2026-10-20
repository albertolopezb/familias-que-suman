package mx.tec.familiasquesuman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.inicio.BuscadorEstiloMinimal
import java.text.Normalizer

/**
 * El buscador de Actividades y Donar: cerrado es una píldora chica ("Buscar…") que no le quita
 * espacio a la lista; al tocarla se abre el mismo buscador de Proyectos y Directorio con el
 * teclado listo, y la ✕ lo cierra y limpia la búsqueda.
 */
@Composable
fun BuscadorPlegable(
    valor: String,
    onValor: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var abierto by rememberSaveable { mutableStateOf(valor.isNotEmpty()) }
    if (!abierto) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.12f))
                .clickable(onClickLabel = placeholder) { abierto = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(IconosWeb.Buscar, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Text(placeholder, style = TextoWeb.Chip, color = Color.White)
        }
        return
    }
    val foco = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { foco.requestFocus() } }
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            BuscadorEstiloMinimal(valor = valor, onValor = onValor, placeholder = placeholder, modificadorCampo = Modifier.focusRequester(foco))
        }
        Icon(
            IconosWeb.Cerrar,
            contentDescription = "Cerrar la búsqueda",
            tint = Color.White,
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(CircleShape)
                .clickable { onValor(""); abierto = false }
                .padding(8.dp)
                .size(18.dp)
        )
    }
}

/** ¿Algún texto contiene la búsqueda? Sin distinguir mayúsculas ni acentos ("cafe" encuentra "Café"). */
fun coincideBusqueda(busqueda: String, vararg textos: String?): Boolean {
    val q = sinAcentos(busqueda.trim())
    if (q.isEmpty()) return true
    return textos.any { it != null && sinAcentos(it).contains(q) }
}

private fun sinAcentos(texto: String): String =
    Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
