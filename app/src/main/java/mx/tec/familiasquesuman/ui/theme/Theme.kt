package mx.tec.familiasquesuman.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Sin dynamic color a propósito: en Android 12+ reemplazaría la marca por los
// colores del fondo de pantalla del usuario. Solo tema claro: el oscuro no está en el alcance.
private val EsquemaDeColor = lightColorScheme(
    primary = MarcaAzul,
    onPrimary = Color.White,
    secondary = MarcaOro,
    onSecondary = MarcaAzul,
    secondaryContainer = AcentoSuave,
    onSecondaryContainer = AcentoTexto,
    background = Fondo,
    onBackground = Tinta,
    surface = Superficie,
    onSurface = Tinta,
    onSurfaceVariant = TintaSuave,
    outline = Borde,
    error = ErrorRojo,
    errorContainer = ErrorFondo,
    onErrorContainer = ErrorTexto
)

@Composable
fun FamiliasQueSumanTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EsquemaDeColor, typography = Tipografia, content = content)
}
