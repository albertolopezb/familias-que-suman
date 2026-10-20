package mx.tec.familiasquesuman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formato = DateTimeFormatter.ofPattern("d 'de' MMMM, HH:mm", Locale.forLanguageTag("es-MX"))

/**
 * La franja de arriba cuando no hay conexión y lo que se ve es lo guardado en el teléfono
 * (data/local/CatalogoLocal). Dice de cuándo es, para que nadie confunda el cupo guardado con el
 * de ahorita. TalkBack la anuncia sola cuando aparece (liveRegion).
 */
@Composable
fun AvisoSinConexion(guardadoEn: Long, modifier: Modifier = Modifier) {
    val cuando = Instant.ofEpochMilli(guardadoEn).atZone(ZoneId.systemDefault()).format(formato)
    Text(
        text = "Sin conexión · estás viendo lo guardado el $cuando",
        style = MaterialTheme.typography.labelLarge,
        color = Color(0xFF5C3B00),
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFFE7B3))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
    )
}
