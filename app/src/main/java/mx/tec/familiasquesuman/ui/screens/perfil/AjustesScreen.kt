package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.FilaAjuste
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.*

private val EscudoAjustes = ImageVector.Builder("Privacidad", 24.dp, 24.dp, 24f, 24f).apply {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
        moveTo(12f, 2f); lineTo(20f, 5f); lineTo(20f, 12f)
        curveTo(20f, 16f, 16f, 20f, 12f, 22f)
        curveTo(8f, 20f, 4f, 16f, 4f, 12f); lineTo(4f, 5f); close()
    }
}.build()
private val SalidaAjustes = ImageVector.Builder("Cerrar sesión", 24.dp, 24.dp, 24f, 24f).apply {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
        moveTo(14f, 3f); lineTo(20f, 3f); lineTo(20f, 21f); lineTo(14f, 21f)
        moveTo(16f, 12f); lineTo(2f, 12f)
        moveTo(7f, 7f); lineTo(2f, 12f); lineTo(7f, 17f)
    }
}.build()

@Composable
fun AjustesScreen(
    ciudad: UiState<String>,
    preferencias: PreferenciasAjustes,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    onCambiarCiudad: () -> Unit,
    onRecordatorioChange: (Boolean) -> Unit,
    onAvisosFavoritosChange: (Boolean) -> Unit,
    onUrgenciasCiudadChange: (Boolean) -> Unit,
    onAvisoPrivacidad: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().background(Fondo)) {
        Surface(color = Superficie) {
            Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Tinta)
                }
                Text("Ajustes", style = MaterialTheme.typography.headlineMedium, color = Tinta)
            }
        }
        when (ciudad) {
            UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(ciudad.mensaje, onReintentar)
            is UiState.Exito -> Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("CIUDAD", style = MaterialTheme.typography.labelSmall, color = TintaSuave)
                TarjetaAjustes {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MarcaAzul)
                        // Familia solo contiene ciudad; no se agrega una entidad federativa al dato.
                        Text(ciudad.datos, style = MaterialTheme.typography.titleMedium,
                            color = Tinta, modifier = Modifier.weight(1f))
                        TextButton(onClick = onCambiarCiudad) { Text("Cambiar", color = MarcaAzul) }
                    }
                }
                Text("NOTIFICACIONES", style = MaterialTheme.typography.labelSmall, color = TintaSuave)
                TarjetaAjustes {
                    FilaAjuste("Recordatorio de actividades", "24 horas antes de cada inscripción (RF-16)",
                        preferencias.recordatorioActividades, onRecordatorioChange)
                    FilaAjuste("Avisos de tus favoritos", "Cuando publican algo nuevo (RF-17, RF-22)",
                        preferencias.avisosFavoritos, onAvisosFavoritosChange)
                    FilaAjuste("Urgencias de tu ciudad", "Cuando faltan voluntarios (RF-22)",
                        preferencias.urgenciasCiudad, onUrgenciasCiudadChange)
                }
                Text("CUENTA", style = MaterialTheme.typography.labelSmall, color = TintaSuave)
                TarjetaAjustes {
                    AccionCuenta("Aviso de privacidad", EscudoAjustes, Tinta, onAvisoPrivacidad)
                    AccionCuenta("Cerrar sesión", SalidaAjustes, Color.Red, onCerrarSesion)
                }
                preferencias.mensaje?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = TintaSuave) }
            }
        }
    }
}

@Composable
private fun TarjetaAjustes(contenido: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Superficie,
        border = BorderStroke(1.dp, Borde), shadowElevation = 2.dp) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp), content = contenido)
    }
}

@Composable
private fun AccionCuenta(texto: String, icono: ImageVector, color: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 40.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icono, contentDescription = null, tint = if (color == Tinta) MarcaAzul else color, modifier = Modifier.size(20.dp))
        Text(texto, style = MaterialTheme.typography.titleMedium, color = color)
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 680)
@Composable
private fun AjustesPreview() {
    FamiliasQueSumanTheme {
        AjustesScreen(UiState.Exito("Monterrey"), PreferenciasAjustes(), {}, {}, {}, {}, {}, {}, {}, {})
    }
}
