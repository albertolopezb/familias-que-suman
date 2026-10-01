package mx.tec.familiasquesuman.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.tec.familiasquesuman.domain.UsuariosHardcodeados
import mx.tec.familiasquesuman.ui.components.DestinoPerfil
import mx.tec.familiasquesuman.ui.components.LocalIrAPerfil
import mx.tec.familiasquesuman.ui.screens.actividades.RutasActividades
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.grafoActividades
import mx.tec.familiasquesuman.ui.screens.inscripcion.cuentaViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.tec.familiasquesuman.domain.Usuario
import mx.tec.familiasquesuman.ui.screens.campanas.RutasCampanas
import mx.tec.familiasquesuman.ui.screens.campanas.grafoCampanas
import mx.tec.familiasquesuman.ui.screens.inicio.RutasInicio
import mx.tec.familiasquesuman.ui.screens.inicio.grafoInicio
import mx.tec.familiasquesuman.ui.screens.inscripcion.RutasInscripcion
import mx.tec.familiasquesuman.ui.screens.inscripcion.grafoInscripcion
import mx.tec.familiasquesuman.ui.screens.perfil.RutasPerfil
import mx.tec.familiasquesuman.ui.screens.perfil.grafoPerfil

@Composable
fun FamiliasApp() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    val cuenta = cuentaViewModel()
    val sesion by cuenta.sesion.collectAsStateWithLifecycle()

    // Manejo del usuario actual sincronizado con la sesión
    var usuarioActualOverride by remember<MutableState<Usuario?>> { mutableStateOf(null) }

    // Si la sesión cambia o se actualiza, obtenemos el usuario correspondiente al correo activo
    val usuarioActual = usuarioActualOverride
        ?: UsuariosHardcodeados.obtenerPorCorreo(sesion?.correo)

    val abrirConSesion: (String) -> Unit = { ruta ->
        if (cuenta.sesion.value == null) {
            cuenta.recordarDestino(ruta)
            nav.navigate(RutasInscripcion.INICIAR_SESION) { launchSingleTop = true }
        } else {
            nav.navigate(ruta) { launchSingleTop = true }
        }
    }

    // Al cerrar sesión, lo que era solo de la familia desaparece y se vuelve al Inicio.
    LaunchedEffect(sesion) {
        // Limpiamos la sobreescritura manual si el usuario cierra o cambia de sesión
        usuarioActualOverride = null
        if (sesion == null && rutaActual in rutasConSesion) {
            nav.navigate(RutasInicio.INICIO) {
                popUpTo(RutasInicio.INICIO) { inclusive = false }
                launchSingleTop = true
            }
        }
    }
 // MAYBE QUITAR ESTO ------!!!!!!!!
    val mostrarBarraInferior = rutaActual != RutasInicio.SPLASH && rutaActual != RutasInicio.CIUDAD &&
            rutaActual != RutasPerfil.TESTIMONIO && rutaActual != RutasPerfil.ENCUESTA_FINAL &&
            rutaActual != RutasPerfil.AVISO_PRIVACIDAD && rutaActual != RutasPerfil.ENCUESTA_PREVIA

    CompositionLocalProvider(
        LocalIrAPerfil provides { destino ->
            when (destino) {
                DestinoPerfil.PERFIL -> abrirConSesion(Rutas.PERFIL)
                DestinoPerfil.MIS_ACTIVIDADES -> abrirConSesion(Rutas.MIS_ACTIVIDADES)
                DestinoPerfil.NOTIFICACIONES, DestinoPerfil.AJUSTES -> abrirConSesion(RutasPerfil.AJUSTES)
                DestinoPerfil.INICIAR_SESION -> {
                    cuenta.tomarDestino()
                    nav.navigate(RutasInscripcion.INICIAR_SESION) { launchSingleTop = true }
                }

                DestinoPerfil.CREAR_CUENTA -> {
                    cuenta.tomarDestino()
                    nav.navigate(RutasInscripcion.CREAR_CUENTA) { launchSingleTop = true }
                }

                DestinoPerfil.CERRAR_SESION -> {
                    usuarioActualOverride = null
                    cuenta.cerrarSesion()
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (mostrarBarraInferior) {
                    BarraInferior(rutaActual = rutaActual, onPestana = { nav.irA(it.ruta) })
                }
            }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = RutasInicio.SPLASH,
                modifier = Modifier.padding(padding)
            ) {
                // Pasamos usuarioActual y el handler para cambiar la cuenta desde el Perfil
                // grafoPerfil en FamiliasApp.kt
                grafoPerfil(
                    nav = nav,
                    usuarioActual = { usuarioActual },
                    onSwitchCuenta = { esAdmin ->
                        // Cambiamos usuarioActualOverride (la variable var) en lugar de usuarioActual
                        usuarioActualOverride = if (esAdmin) {
                            UsuariosHardcodeados.USUARIO_ADMIN
                        } else {
                            UsuariosHardcodeados.USUARIO_NORMAL
                        }
                    }
                )

                grafoInicio(
                    nav = nav,
                    onNavegarAActividades = { nav.navigate(RutasActividades.LISTA) },
                    onNavegarACampanas = { nav.navigate(RutasCampanas.LISTA) },
                    // "Ver agenda" es lo de la familia: Mis actividades (pide sesión).
                    onVerAgenda = { abrirConSesion(Rutas.MIS_ACTIVIDADES) }
                )

                // Pasamos esAdmin dinámicamente según la cuenta activa
                grafoActividades(
                    nav = nav,
                    esAdmin = { usuarioActual.esAdmin }, // <--- Se actualiza automáticamente cuando usuarioActual cambia
                    onIrAInicio = {
                        if (!nav.popBackStack(RutasInicio.INICIO, inclusive = false)) {
                            nav.navigate(RutasInicio.INICIO)
                        }
                    },
                    onVerAsociaciones = { nav.navigate(RutasInicio.EXPLORAR) },
                    onInscribirme = { id -> nav.navigate(RutasInscripcion.inscribirse(id)) },
                    onCancelarInscripcion = { id -> nav.navigate(RutasInscripcion.cancelar(id)) },
                    onResponderEncuesta = { nav.navigate(RutasPerfil.ENCUESTA_FINAL) }
                )

                grafoCampanas(nav)
                grafoInscripcion(nav)
            }
        }
    }
}

/** Pantallas que solo existen con sesión abierta. */
private val rutasConSesion = setOf(
    Rutas.PERFIL, Rutas.MIS_ACTIVIDADES, RutasPerfil.AJUSTES, RutasPerfil.FAVORITOS, RutasPerfil.INSIGNIAS
)

/** Cambio de pestaña: una sola copia de cada pantalla y la pila limpia. */
private fun NavController.irA(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.findStartDestination().id) {
            saveState = false // Desactivar para actualizar los cambios de rol
        }
        launchSingleTop = true
        restoreState = false // Desactivar para que recomponga con el nuevo valor de esAdmin
    }
}

@Composable
private fun BarraInferior(rutaActual: String?, onPestana: (Pestana) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Web.Tarjeta).navigationBarsPadding()) {
        HorizontalDivider(color = Web.Borde, thickness = 1.dp)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            pestanas.forEach { pestana ->
                val activa = rutaActual != null &&
                        pestana.prefijos.any { rutaActual == it || rutaActual.startsWith("$it/") }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { if (!activa || rutaActual != pestana.ruta) onPestana(pestana) }
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activa) Web.Secundario else Web.Tarjeta)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            pestana.icono,
                            contentDescription = pestana.etiqueta,
                            tint = if (activa) Web.Primario else Web.TextoApagado,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        pestana.etiqueta,
                        style = TextoWeb.Chico.copy(
                            fontSize = 10.sp,
                            fontWeight = if (activa) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (activa) Web.Primario else Web.TextoApagado
                    )
                }
            }
        }
    }
}