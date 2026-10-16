package mx.tec.familiasquesuman.ui.screens.perfil

import mx.tec.familiasquesuman.ui.components.BarraSuperior
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
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
import mx.tec.familiasquesuman.domain.Acompanante
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.cuantoFalta
import mx.tec.familiasquesuman.domain.fechaDeTexto
import mx.tec.familiasquesuman.ui.components.AvatarFamilia
import mx.tec.familiasquesuman.domain.Familia
import mx.tec.familiasquesuman.domain.Impacto
import mx.tec.familiasquesuman.domain.Usuario
import mx.tec.familiasquesuman.notificaciones.Notificaciones
import mx.tec.familiasquesuman.notificaciones.NotificacionesDemo
import mx.tec.familiasquesuman.notificaciones.RevisarAvisosWorker
import mx.tec.familiasquesuman.notificaciones.rememberPedirPermisoNotificaciones
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.GraficaBarras
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.SeccionFamilia
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
    usuarioActual: Usuario = Usuario.INVITADO,
    onMisActividadesClick: () -> Unit = {},
    onSwitchCuenta: (Boolean) -> Unit = {},
    accesosDisponibles: Boolean = true,
    etiquetasMensuales: List<String> = listOf("May", "Jun", "Jul", "Ago", "Sep"),
    valoresMensuales: List<Int> = emptyList(),
    favoritosDisponibles: Boolean = accesosDisponibles,
    onSugerirClick: () -> Unit = {},
    onSugerenciasClick: () -> Unit = {},
    onEnviarAviso: () -> Unit = {},
    onRegresar: (() -> Unit)? = null,
    onTestimoniosClick: () -> Unit = {},
    onRespuestasClick: () -> Unit = {},
    familiares: List<Acompanante> = emptyList(),
    onAgregarFamiliar: (Acompanante) -> Unit = {},
    onQuitarFamiliar: (Acompanante) -> Unit = {},
    foto: String? = null,
    onCambiarFoto: () -> Unit = {},
    onQuitarFoto: () -> Unit = {},
    proximas: List<Actividad>? = null,
    onProximaClick: (String) -> Unit = {},
    onVerActividades: () -> Unit = {}
) {
    Column(modifier.fillMaxSize().background(Fondo)) {
        BarraSuperior(if (usuarioActual.esAdmin) "Perfil Admin" else "Mi perfil", onRegresar = onRegresar)
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
                onSwitchCuenta = onSwitchCuenta,
                onSugerirClick = onSugerirClick,
                onSugerenciasClick = onSugerenciasClick,
                onEnviarAviso = onEnviarAviso,
                onTestimoniosClick = onTestimoniosClick,
                onRespuestasClick = onRespuestasClick,
                familiares = familiares,
                onAgregarFamiliar = onAgregarFamiliar,
                onQuitarFamiliar = onQuitarFamiliar,
                foto = foto,
                onCambiarFoto = onCambiarFoto,
                onQuitarFoto = onQuitarFoto,
                proximas = proximas ?: estado.datos.proximas,
                onProximaClick = onProximaClick,
                onVerActividades = onVerActividades
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
    onSwitchCuenta: (Boolean) -> Unit,
    onSugerirClick: () -> Unit,
    onSugerenciasClick: () -> Unit,
    onEnviarAviso: () -> Unit,
    onTestimoniosClick: () -> Unit,
    onRespuestasClick: () -> Unit,
    familiares: List<Acompanante>,
    onAgregarFamiliar: (Acompanante) -> Unit,
    onQuitarFamiliar: (Acompanante) -> Unit,
    foto: String?,
    onCambiarFoto: () -> Unit,
    onQuitarFoto: () -> Unit,
    proximas: List<Actividad>,
    onProximaClick: (String) -> Unit,
    onVerActividades: () -> Unit
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
            if (usuarioActual.esAdmin) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFFFD1D1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Lock, contentDescription = "Admin", tint = Color(0xFFB3261E))
                }
            } else {
                FotoDePerfil(foto, usuarioActual.nombreFamilia, onCambiarFoto, onQuitarFoto)
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
            AccesoPerfil(
                "Sugerencias recibidas", Icons.Outlined.Email, onSugerenciasClick,
                true, Modifier.fillMaxWidth()
            )
            AccesoPerfil(
                "Enviar aviso a las familias", Icons.Outlined.Notifications, onEnviarAviso,
                true, Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AccesoPerfil(
                    "Testimonios", Icons.Outlined.CheckCircle, onTestimoniosClick,
                    true, Modifier.weight(1f)
                )
                AccesoPerfil(
                    "Respuestas de encuestas", Icons.Outlined.DateRange, onRespuestasClick,
                    true, Modifier.weight(1f)
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
                if (valoresMensuales.isEmpty() && datos.porMes.isNotEmpty()) {
                    GraficaBarras(datos.porMes.map { it.first }, datos.porMes.map { it.second })
                    if (datos.porMes.all { it.second == 0 }) {
                        Spacer(Modifier.height(8.dp))
                        Text("Cuando participen en una actividad, aquí verán cuántas hicieron cada mes.",
                            style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                    }
                } else {
                    GraficaBarras(etiquetasMensuales, valoresMensuales)
                }
            }

            EtiquetaSeccion("MI FAMILIA")
            SeccionFamilia(familiares, onAgregarFamiliar, onQuitarFamiliar)

            EtiquetaSeccion("PRÓXIMAS")
            if (proximas.isEmpty()) {
                SinProximas(onVerActividades)
            } else {
                proximas.forEach { actividad -> TarjetaProxima(actividad, onClick = { onProximaClick(actividad.id) }) }
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
            AccesoPerfil(
                "Sugerir una actividad, campaña, proyecto o centro", Icons.Outlined.AddCircle,
                onSugerirClick, true, Modifier.fillMaxWidth()
            )
        }

        ProbarNotificaciones()
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

    EtiquetaSeccion("PROBAR NOTIFICACIONES")
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
    OutlinedButton(
        onClick = {
            RevisarAvisosWorker.probarEn(contexto)
            Toast.makeText(contexto, "Cierra la app: en 20 segundos llegan los avisos pendientes", Toast.LENGTH_LONG).show()
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Avisos con la app cerrada (en 20 s)") }
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

/** La foto de la familia, con un botón de cámara para cambiarla o quitarla. */
@Composable
private fun FotoDePerfil(foto: String?, nombre: String, onCambiar: () -> Unit, onQuitar: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Box {
        Box(Modifier.size(68.dp).clip(CircleShape).clickable { if (foto == null) onCambiar() else menu = true }) {
            AvatarFamilia(foto, nombre, 68.dp)
        }
        Box(
            Modifier.align(Alignment.BottomEnd).size(24.dp).clip(CircleShape).background(MarcaOro)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = null, tint = MarcaAzul, modifier = Modifier.size(13.dp))
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(text = { Text("Cambiar foto") }, onClick = { menu = false; onCambiar() })
            DropdownMenuItem(text = { Text("Quitar foto", color = ErrorRojo) }, onClick = { menu = false; onQuitar() })
        }
    }
}

@Composable
private fun TarjetaProxima(actividad: Actividad, onClick: () -> Unit) {
    val dia = fechaDeTexto(actividad.fecha)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Superficie,
        border = BorderStroke(1.dp, Borde),
        shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(
                Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(MarcaAzul),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(dia?.dayOfMonth?.toString() ?: "—", style = MaterialTheme.typography.titleLarge, color = MarcaOro)
                Text(
                    dia?.month?.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("es", "MX"))
                        ?.uppercase()?.trimEnd('.') ?: "",
                    style = MaterialTheme.typography.labelSmall, color = Color.White
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(actividad.titulo, style = MaterialTheme.typography.titleMedium, color = Tinta)
                Text(actividad.horario, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
            }
            if (dia != null) {
                Surface(shape = RoundedCornerShape(50), color = AcentoSuave) {
                    Text(cuantoFalta(dia), style = MaterialTheme.typography.labelMedium, color = AcentoTexto,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
                }
            }
        }
    }
}

/** Cuando la familia no tiene nada en puerta, se le dice claro y se le ofrece qué hacer. */
@Composable
private fun SinProximas(onVerActividades: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = MarcaAzul) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Aún no tienen actividades próximas", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text("Elijan una actividad de este mes y súmense en familia.",
                style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC9D6F5))
            Button(onClick = onVerActividades, shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MarcaOro, contentColor = MarcaAzul)) {
                Text("Ver actividades", fontWeight = FontWeight.Bold)
            }
        }
    }
}
