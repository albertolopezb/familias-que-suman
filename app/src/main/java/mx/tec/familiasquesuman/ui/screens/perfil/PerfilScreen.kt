package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import mx.tec.familiasquesuman.notificaciones.Notificaciones
import mx.tec.familiasquesuman.notificaciones.NotificacionesDemo
import mx.tec.familiasquesuman.notificaciones.rememberPedirPermisoNotificaciones
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.familiasquesuman.domain.Familia
import mx.tec.familiasquesuman.domain.Impacto
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.GraficaBarras
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.TarjetaMetrica
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.*

// Icono de medalla dibujado localmente: no requiere material-icons-extended.
private val IconoInsignia = ImageVector.Builder("Insignia", 24.dp, 24.dp, 24f, 24f).apply {
    path(stroke = androidx.compose.ui.graphics.SolidColor(Color.Black), strokeLineWidth = 1.8f) {
        moveTo(17f, 8f)
        curveTo(17f, 14.7f, 7f, 14.7f, 7f, 8f)
        curveTo(7f, 1.3f, 17f, 1.3f, 17f, 8f)
        close()
        moveTo(8.5f, 12f); lineTo(7.5f, 21f); lineTo(12f, 18.5f)
        lineTo(16.5f, 21f); lineTo(15.5f, 12f)
    }
}.build()

@Composable
fun PerfilScreen(
    estado: UiState<DatosPerfil>,
    onReintentar: () -> Unit,
    onFavoritosClick: () -> Unit,
    onInsigniasClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMisActividadesClick: () -> Unit = {},
    accesosDisponibles: Boolean = true,
    etiquetasMensuales: List<String> = listOf("May", "Jun", "Jul", "Ago", "Sep"),
    valoresMensuales: List<Int> = emptyList(),
    favoritosDisponibles: Boolean = accesosDisponibles
) {
    Column(modifier.fillMaxSize().background(Fondo)) {
        Surface(color = Superficie) {
            Text("Mi Perfil", modifier = Modifier.fillMaxWidth().padding(16.dp),
                style = MaterialTheme.typography.headlineMedium, color = Tinta)
        }
        when (estado) {
            UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(estado.mensaje, onReintentar)
            is UiState.Exito -> ContenidoPerfil(
                estado.datos, onFavoritosClick, onInsigniasClick, accesosDisponibles,
                etiquetasMensuales, valoresMensuales, favoritosDisponibles, onMisActividadesClick
            )
        }
    }
}

@Composable
private fun ContenidoPerfil(
    datos: DatosPerfil,
    onFavoritosClick: () -> Unit,
    onInsigniasClick: () -> Unit,
    accesosDisponibles: Boolean,
    etiquetasMensuales: List<String>,
    valoresMensuales: List<Int>,
    favoritosDisponibles: Boolean,
    onMisActividadesClick: () -> Unit
) {
    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(60.dp).clip(CircleShape).background(Color(0xFFC5DEFF)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(datos.familia.nombre, style = MaterialTheme.typography.headlineMedium, color = Tinta)
                // Familia no proporciona entidad federativa ni estado de voluntariado.
                Text(datos.familia.ciudad, style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
            }
        }
        EtiquetaSeccion("TU IMPACTO")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaMetrica(datos.impacto.actividadesRealizadas, "Actividades\nrealizadas", Modifier.weight(1f))
            TarjetaMetrica(datos.impacto.horasDeServicio, "Horas\nde servicio", Modifier.weight(1f))
            TarjetaMetrica(datos.impacto.campanasApoyadas, "Campañas\napoyadas", Modifier.weight(1f))
        }
        TarjetaPerfil {
            Text("Actividades por mes", style = MaterialTheme.typography.titleMedium, color = Tinta)
            Spacer(Modifier.height(12.dp))
            // PerfilRepository todavía no ofrece una serie mensual. No se fabrican barras.
            GraficaBarras(etiquetasMensuales, valoresMensuales)
        }
        EtiquetaSeccion("PRÓXIMAS")
        datos.proximas.forEach { actividad ->
            TarjetaPerfil {
                Text(actividad.titulo, style = MaterialTheme.typography.titleMedium, color = Tinta)
                Spacer(Modifier.height(4.dp))
                // Se muestran fecha y horario originales. El nombre de asociación no viene en getProximas().
                Text("${actividad.fecha} · ${actividad.horario}",
                    style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
            }
        }
        AccesoPerfil("Mis actividades", Icons.Outlined.DateRange, onMisActividadesClick,
            true, Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AccesoPerfil("Mis favoritos", Icons.Outlined.FavoriteBorder, onFavoritosClick,
                favoritosDisponibles, Modifier.weight(1f))
            AccesoPerfil("Insignias", IconoInsignia, onInsigniasClick,
                accesosDisponibles, Modifier.weight(1f))
        }
        ProbarNotificaciones()
    }
}

/** Los botones de la parte 3 para ver cómo llegan las notificaciones. */
@Composable
private fun ProbarNotificaciones() {
    val contexto = LocalContext.current
    var activadas by remember { mutableStateOf(Notificaciones.estanActivadas(contexto)) }
    val pedirPermiso = rememberPedirPermisoNotificaciones { activadas = it }
    val avisar: (Boolean) -> Unit = { enviada ->
        Toast.makeText(
            contexto,
            if (enviada) "Listo, revisa tus notificaciones" else "Primero activa las notificaciones",
            Toast.LENGTH_SHORT
        ).show()
    }

    EtiquetaSeccion("NOTIFICACIONES")
    if (!activadas) {
        OutlinedButton(onClick = pedirPermiso, modifier = Modifier.fillMaxWidth()) {
            Text("Activar notificaciones")
        }
    }
    OutlinedButton(
        onClick = { avisar(NotificacionesDemo.dispararRecordatorio(contexto)) },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Mañana tienen actividad") }
    OutlinedButton(
        onClick = { avisar(NotificacionesDemo.dispararUrgencia(contexto)) },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Se necesitan 5 voluntarios urgentes") }
    OutlinedButton(
        onClick = { avisar(NotificacionesDemo.dispararInscripcionConfirmada(contexto)) },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Ya están inscritos") }
}

@Composable
private fun EtiquetaSeccion(texto: String) {
    Text(texto, style = MaterialTheme.typography.labelMedium.copy(
        fontSize = 12.sp, letterSpacing = 0.8.sp, fontWeight = FontWeight.Bold), color = TintaSuave)
}

@Composable
private fun TarjetaPerfil(contenido: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Superficie,
        border = BorderStroke(1.dp, Borde), shadowElevation = 2.dp) {
        Column(Modifier.padding(16.dp), content = contenido)
    }
}

@Composable
private fun AccesoPerfil(
    texto: String, icono: ImageVector, onClick: () -> Unit, disponible: Boolean, modifier: Modifier
) {
    Surface(onClick = onClick, enabled = disponible, modifier = modifier,
        shape = RoundedCornerShape(16.dp), color = AcentoSuave, contentColor = AcentoTexto) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(texto, style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp))
        }
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun PerfilPreview() {
    FamiliasQueSumanTheme {
        PerfilScreen(UiState.Exito(DatosPerfil(
            Familia("preview", "Familia Rodríguez", "Monterrey", ""),
            Impacto(12, 36, 4), emptyList()
        )), {}, {}, {})
    }
}
