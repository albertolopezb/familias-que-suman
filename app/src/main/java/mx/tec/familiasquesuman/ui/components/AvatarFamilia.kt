package mx.tec.familiasquesuman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.theme.Inter
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.MarcaOro

/**
 * La cara de la familia en toda la app: su foto de perfil o, si no tiene, sus iniciales en
 * dorado sobre azul marino ("Familia Rodríguez" → "FR").
 */
@Composable
fun AvatarFamilia(
    foto: String?,
    nombre: String,
    tamano: Dp,
    modifier: Modifier = Modifier,
    conBorde: Boolean = false,
    /** Sobre el encabezado azul: círculo claro con las iniciales en azul marino, para que no se pierda. */
    claro: Boolean = false
) {
    val base = modifier
        .size(tamano)
        .clip(CircleShape)
        .then(if (conBorde) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
    if (foto != null) {
        FotoUsuario(foto, "Foto de perfil de $nombre", base)
    } else {
        Box(base.background(if (claro) Color(0xFFF1F5F9) else MarcaAzul), contentAlignment = Alignment.Center) {
            Text(
                iniciales(nombre),
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = (tamano.value * 0.36f).sp,
                    color = if (claro) MarcaAzul else MarcaOro
                )
            )
        }
    }
}

/** "Familia Rodríguez" → "FR"; "Ana" → "A". Ignora palabras de relleno como "de". */
fun iniciales(nombre: String): String =
    nombre.split(" ")
        .filter { it.isNotBlank() && it.lowercase() !in setOf("de", "la", "los", "las", "y") }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
