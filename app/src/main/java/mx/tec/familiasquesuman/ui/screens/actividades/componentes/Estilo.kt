package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.R
import mx.tec.familiasquesuman.ui.theme.Inter

/**
 * Los colores y letras de familiasquesuman.com/actividades, medidos del sitio.
 *
 * Viven aquí y no en ui/theme/ porque ese archivo es compartido. Si al final las
 * demás partes también calcan el sitio, se suben al tema en un PR aparte.
 */
object Web {
    // Marca
    val Primario = Color(0xFF1D3972)     // títulos y enlaces
    val Amarillo = Color(0xFFF9B11F)     // botón "Unirme"
    val WhatsApp = Color(0xFF25D366)

    // Superficies
    val Fondo = Color(0xFFF8FAFC)
    val Tarjeta = Color(0xFFFFFFFF)
    val Secundario = Color(0xFFEBF0F4)   // pista de la barra y píldoras
    val Borde = Color(0xFFD7E0EA)

    // Texto
    val Texto = Color(0xFF141F38)
    val TextoApagado = Color(0xFF667799)

    // Estados del cupo
    val Verde = Color(0xFF22C55E)
    val VerdeTexto = Color(0xFF15803D)
    val VerdeFondo = Color(0xFFDCFCE7)
    val Ambar = Color(0xFFF59E0B)
    val AmbarTexto = Color(0xFFB45309)
    val AmbarFondo = Color(0xFFFEF3C7)
    val AmbarSuave = Color(0xFFFFFBEB)
    val Rojo = Color(0xFFEF4444)
    val RojoTexto = Color(0xFFDC2626)
    val RojoFondo = Color(0xFFFEF2F2)
    val RojoBorde = Color(0xFFFECACA)

    // Chips de aportación y temas
    val MoradoFondo = Color(0xFFF3E8FF)
    val MoradoTexto = Color(0xFF7E22CE)
    val RosaFondo = Color(0xFFFFF1F2)
    val Rosa = Color(0xFFF43F5E)
    val VerdeTema = Color(0xFF16A34A)
}

/** Lora: la letra de los títulos del sitio. */
val Lora = FontFamily(
    Font(R.font.lora_semibold, FontWeight.SemiBold),
    Font(R.font.lora_bold, FontWeight.Bold)
)

/** Los tamaños del sitio, con los nombres de Tailwind que usa (text-2xl, text-lg…). */
object TextoWeb {
    /** h1 de la página: font-lora text-2xl font-bold */
    val Titulo = TextStyle(
        fontFamily = Lora, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 30.sp, color = Web.Primario
    )

    /** Título de la tarjeta: font-lora text-lg font-semibold leading-tight */
    val TituloTarjeta = TextStyle(
        fontFamily = Lora, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 22.sp, color = Web.Texto
    )

    /** Encabezado de sección del detalle: font-lora text-sm font-semibold */
    val Seccion = TextStyle(
        fontFamily = Lora, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, color = Web.Texto
    )

    /** "ACTIVIDADES PASADAS": text-sm uppercase tracking-wide */
    val Rotulo = TextStyle(
        fontFamily = Lora, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.35.sp, color = Web.TextoApagado
    )

    /** text-sm */
    val Cuerpo = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 22.sp, color = Web.Texto
    )

    /** text-xs */
    val Chico = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, color = Web.TextoApagado
    )

    /** Chips: text-xs font-semibold */
    val Chip = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp
    )

    /** Botones: font-semibold, text-base */
    val Boton = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 20.sp
    )
}
