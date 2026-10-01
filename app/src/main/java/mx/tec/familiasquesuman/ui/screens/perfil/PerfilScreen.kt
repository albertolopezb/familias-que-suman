package mx.tec.familiasquesuman.ui.screens.perfil

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.domain.Familia
import mx.tec.familiasquesuman.domain.Impacto
import mx.tec.familiasquesuman.domain.Usuario
import mx.tec.familiasquesuman.notificaciones.Notificaciones
import mx.tec.familiasquesuman.notificaciones.NotificacionesDemo
import mx.tec.familiasquesuman.notificaciones.rememberPedirPermisoNotificaciones
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.GraficaBarras
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.TarjetaMetrica
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.*

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
    usuarioActual: Usuario = mx.tec.familiasquesuman.domain.UsuariosHardcodeados.USUARIO_NORMAL,
    onMisActividadesClick: () -> Unit = {},
    onSwitchCuenta: (Boolean) -> Unit = {},
    accesosDisponibles: Boolean = true,
    etiquetasMensuales: List<String> = listOf("May", "Jun", "Jul", "Ago", "Sep"),
    valoresMensuales: List<Int> = emptyList(),
    favoritosDisponibles: Boolean = accesosDisponibles
) {
    Column(modifier.fillMaxSize().background(Fondo)) {
        EncabezadoApp()
        Surface(color = Superficie) {
            Text(
                text = if (usuarioActual.esAdmin) "Perfil Admin" else "Mi Perfil",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = Tinta
            )
        }
        when (estado) {
            UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(estado.mensaje, onReintentar)
            is UiState.Exito -> ContenidoPerfil(
                datos = estado.datos,
                usuarioActual = usuarioActual,
                onFavoritosClick = onFavoritosClick,
                onInsigniasClick = onInsigniasClick,
                accesosDisponibles = accesosDisponibles,
                etiquetasMensuales = etiquetasMensuales,
                valoresMensuales = valoresMensuales,
                favoritosDisponibles = favoritosDisponibles,
                onMisActividadesClick = onMisActividadesClick,
                onSwitchCuenta = onSwitchCuenta
            )
        }
    }
}

@Composable
private fun ContenidoPerfil(
    datos: DatosPerfil,
    usuarioActual: Usuario,
    onFavoritosClick: () -> Unit,
    onInsigniasClick: () -> Unit,
    accesosDisponibles: Boolean,
    etiquetasMensuales: List<String>,
    valoresMensuales: List<Int>,
    favoritosDisponibles: Boolean,
    onMisActividadesClick: () -> Unit,
    onSwitchCuenta: (Boolean) -> Unit
) {
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Encabezado de Usuario
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(if (usuarioActual.esAdmin) Color(0xFFFFD1D1) else Color(0xFFC5DEFF)),
                contentAlignment = Alignment.Center
            ) {
                if (usuarioActual.esAdmin) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Admin",
                        tint = Color(0xFFB3261E)
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (usuarioActual.esAdmin) "Administrador General" else usuarioActual.nombreFamilia,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Tinta
                )
                Text(
                    text = "${usuarioActual.ciudad} · ${if (usuarioActual.esAdmin) "Modo Gestión" else "Cuenta Familiar"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TintaSuave
                )
                Text(
                    text = usuarioActual.correo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TintaSuave
                )
            }
        }

        // Switch de Cuentas (Demo)
        SeccionSwitchCuentas(
            esAdmin = usuarioActual.esAdmin,
            correoActual = usuarioActual.correo,
            onSwitchCuenta = onSwitchCuenta
        )

        // VISTA CONDICIONAL SEGÚN EL ROL
        if (usuarioActual.esAdmin) {
            // --- VISTA SIMPLIFICADA PARA ADMIN ---
            EtiquetaSeccion("PANEL DE CONTROL ADMIN")
            TarjetaPerfil {
                Text(
                    text = "Permisos de Edición Activos",
                    style = MaterialTheme.typography.titleMedium,
                    color = Tinta
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Desde este rol tienes habilitadas las funciones de creación, modificación y eliminación en:\n\n" +
                            "• Actividades\n" +
                            "• Proyectos\n" +
                            "• Donaciones (Campañas)\n" +
                            "• Directorio de Visiteo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TintaSuave
                )
            }
        } else {
            // --- VISTA DETALLADA DE LA FAMILIA ---
            EtiquetaSeccion("TU IMPACTO")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TarjetaMetrica(datos.impacto.actividadesRealizadas, "Actividades\nrealizadas", Modifier.weight(1f))
                TarjetaMetrica(datos.impacto.horasDeServicio, "Horas\nde servicio", Modifier.weight(1f))
                TarjetaMetrica(datos.impacto.campanasApoyadas, "Campañas\napoyadas", Modifier.weight(1f))
            }

            TarjetaPerfil {
                Text("Actividades por mes", style = MaterialTheme.typography.titleMedium, color = Tinta)
                Spacer(Modifier.height(12.dp))
                GraficaBarras(etiquetasMensuales, valoresMensuales)
            }

            EtiquetaSeccion("PRÓXIMAS")
            datos.proximas.forEach { actividad ->
                TarjetaPerfil {
                    Text(actividad.titulo, style = MaterialTheme.typography.titleMedium, color = Tinta)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${actividad.fecha} · ${actividad.horario}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TintaSuave
                    )
                }
            }

            AccesoPerfil(
                "Mis actividades", Icons.Outlined.DateRange, onMisActividadesClick,
                true, Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AccesoPerfil(
                    "Mis favoritos", Icons.Outlined.FavoriteBorder, onFavoritosClick,
                    favoritosDisponibles, Modifier.weight(1f)
                )
                AccesoPerfil(
                    "Insignias", IconoInsignia, onInsigniasClick,
                    accesosDisponibles, Modifier.weight(1f)
                )
            }
        }

        ProbarNotificaciones()
    }
}

@Composable
private fun SeccionSwitchCuentas(
    esAdmin: Boolean,
    correoActual: String,
    onSwitchCuenta: (Boolean) -> Unit
) {
    EtiquetaSeccion("CAMBIAR CUENTA (MODO PRUEBAS)")
    TarjetaPerfil {
        Text(
            text = "Sesión activa: $correoActual\nRol actual: ${if (esAdmin) "Administrador" else "Usuario Normal"}",
            style = MaterialTheme.typography.bodyMedium,
            color = Tinta
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onSwitchCuenta(false) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (!esAdmin) MarcaAzul.copy(alpha = 0.15f) else Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (!esAdmin) "✓ Normal" else "Normal")
            }

            Button(
                onClick = { onSwitchCuenta(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esAdmin) MarcaAzul else Color.Gray
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (esAdmin) "✓ Admin" else "Admin")
            }
        }
    }
}

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
    Text(
        texto, style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 12.sp, letterSpacing = 0.8.sp, fontWeight = FontWeight.Bold
        ), color = TintaSuave
    )
}

@Composable
private fun TarjetaPerfil(contenido: @Composable ColumnScope.() -> Unit) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Superficie,
        border = BorderStroke(1.dp, Borde),
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp), content = contenido)
    }
}

@Composable
private fun AccesoPerfil(
    texto: String, icono: ImageVector, onClick: () -> Unit, disponible: Boolean, modifier: Modifier
) {
    Surface(
        onClick = onClick, enabled = disponible, modifier = modifier,
        shape = RoundedCornerShape(16.dp), color = AcentoSuave, contentColor = AcentoTexto
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(texto, style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp))
        }
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun PerfilPreview() {
    FamiliasQueSumanTheme {
        PerfilScreen(
            estado = UiState.Exito(
                DatosPerfil(
                    Familia("preview", "Familia Rodríguez", "Monterrey", ""),
                    Impacto(12, 36, 4), emptyList()
                )
            ),
            usuarioActual = Usuario("ana.rodriguez@correo.com", false, "Familia Rodríguez", "Monterrey"),
            onReintentar = {},
            onFavoritosClick = {},
            onInsigniasClick = {}
        )
    }
}