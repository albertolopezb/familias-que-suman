package mx.tec.familiasquesuman.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.navigation.NavBackStackEntry
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
import mx.tec.familiasquesuman.ui.screens.sugerencias.grafoSugerencias
import mx.tec.familiasquesuman.ui.screens.inscripcion.RutasInscripcion
import mx.tec.familiasquesuman.ui.screens.inscripcion.grafoInscripcion
import mx.tec.familiasquesuman.ui.screens.perfil.RutasPerfil
import mx.tec.familiasquesuman.ui.screens.perfil.compartirTestimonio
import mx.tec.familiasquesuman.ui.screens.perfil.grafoPerfil
import mx.tec.familiasquesuman.ui.screens.perfil.responderEncuestaFinal
import mx.tec.familiasquesuman.ui.screens.perfil.responderEncuestaPrevia

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
    // La barra de pestañas solo está en lo que se explora; los flujos (cuenta, inscripción,
    // encuestas, sugerir) usan toda la pantalla, como en una app nativa.
    val mostrarBarraInferior = rutaActual != null && rutasSinBarra.none { rutaActual == it || rutaActual.startsWith("$it/") }

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
                modifier = Modifier.padding(padding),
                enterTransition = { entrada(initialState, targetState) },
                exitTransition = { salida(initialState, targetState) },
                popEnterTransition = { entradaAlRegresar(initialState, targetState) },
                popExitTransition = { salidaAlRegresar(initialState, targetState) }
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
                    onVerAgenda = { abrirConSesion(Rutas.MIS_ACTIVIDADES) },
                    esAdmin = { usuarioActual.esAdmin }
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
                    onInscribirmeConPrueba = { id, sinCupo -> nav.navigate(RutasInscripcion.inscribirse(id, sinCupo)) },
                    onCancelarInscripcion = { id -> nav.navigate(RutasInscripcion.cancelar(id)) },
                    onResponderEncuesta = nav::responderEncuestaFinal,
                    onCompartirTestimonio = nav::compartirTestimonio,
                    onEncuestaPrevia = nav::responderEncuestaPrevia,
                    onVerRespuestas = { id -> nav.navigate(RutasPerfil.respuestas(id)) { launchSingleTop = true } }
                )

                grafoCampanas(nav, esAdmin = { usuarioActual.esAdmin })
                grafoInscripcion(nav)
                grafoSugerencias(nav, esAdmin = { usuarioActual.esAdmin })
            }
        }
    }
}

/** Pantallas completas, sin la barra de pestañas. */
private val rutasSinBarra = listOf(
    RutasInicio.SPLASH, RutasInicio.CIUDAD, RutasInicio.PERMISO_NOTIFICACIONES,
    "inscribirse", "puerta_cuenta", RutasInscripcion.CREAR_CUENTA, RutasInscripcion.INICIAR_SESION,
    RutasInscripcion.RECUPERAR, "acompanantes", "inscripcion_confirmada", "cancelar_inscripcion",
    "cancelacion_confirmada", "sugerir", "perfil/testimonio", "perfil/encuesta_final",
    "perfil/encuesta_previa", RutasPerfil.AVISO_PRIVACIDAD
)

private val rutasDePestana = pestanas.map { it.ruta }.toSet()

private const val DuracionPantalla = 300
private const val DuracionPestana = 180

/** Entre pestañas la pantalla se desvanece; al entrar a un detalle o flujo se desliza desde la derecha. */
private fun entreRaices(de: NavBackStackEntry, a: NavBackStackEntry) =
    de.destination.route in rutasDePestana && a.destination.route in rutasDePestana

private fun entrada(de: NavBackStackEntry, a: NavBackStackEntry): EnterTransition =
    if (entreRaices(de, a)) fadeIn(tween(DuracionPestana))
    else slideInHorizontally(tween(DuracionPantalla)) { it / 3 } + fadeIn(tween(DuracionPantalla))

private fun salida(de: NavBackStackEntry, a: NavBackStackEntry): ExitTransition =
    if (entreRaices(de, a)) fadeOut(tween(DuracionPestana))
    else slideOutHorizontally(tween(DuracionPantalla)) { -it / 5 } + fadeOut(tween(DuracionPantalla))

private fun entradaAlRegresar(de: NavBackStackEntry, a: NavBackStackEntry): EnterTransition =
    if (entreRaices(de, a)) fadeIn(tween(DuracionPestana))
    else slideInHorizontally(tween(DuracionPantalla)) { -it / 5 } + fadeIn(tween(DuracionPantalla))

private fun salidaAlRegresar(de: NavBackStackEntry, a: NavBackStackEntry): ExitTransition =
    if (entreRaices(de, a)) fadeOut(tween(DuracionPestana))
    else slideOutHorizontally(tween(DuracionPantalla)) { it / 3 } + fadeOut(tween(DuracionPantalla))

/** Pantallas que solo existen con sesión abierta. */
private val rutasConSesion = setOf(
    Rutas.PERFIL, Rutas.MIS_ACTIVIDADES, RutasPerfil.AJUSTES, RutasPerfil.FAVORITOS, RutasPerfil.INSIGNIAS
)

/** Cambio de pestaña: una sola copia de cada pantalla y la pila limpia. */
private fun NavController.irA(ruta: String) {
    navigate(ruta) {
        // La pantalla de abajo de la pila es siempre el Inicio: "atrás" desde una pestaña regresa a él.
        popUpTo(RutasInicio.INICIO) {
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