package mx.tec.familiasquesuman.ui.screens.inscripcion

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import mx.tec.familiasquesuman.FamiliasApplication
import mx.tec.familiasquesuman.domain.MomentoEncuesta
import mx.tec.familiasquesuman.ui.screens.perfil.responderEncuestaPrevia
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import mx.tec.familiasquesuman.notificaciones.NotificacionesDemo
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.navigation.Rutas
import mx.tec.familiasquesuman.ui.screens.actividades.RutasActividades
import mx.tec.familiasquesuman.ui.state.AppViewModelProvider
import mx.tec.familiasquesuman.ui.state.UiState

/**
 * Rutas de la parte 3. Los dos saltos que llegan desde la parte 2:
 *
 *   Detalle de actividad → "Inscribirme":       nav.navigate(RutasInscripcion.inscribirse(actividad.id))
 *   Mis Actividades → actividad próxima:         nav.navigate(RutasInscripcion.cancelar(actividad.id))
 *
 * `inscribirse` decide solo: sin sesión abre la puerta de cuenta (P-04); con sesión, P-06.
 */
object RutasInscripcion {
    const val INSCRIBIRSE = "inscribirse/{actividadId}?sinCupo={sinCupo}"
    const val PUERTA = "puerta_cuenta/{actividadId}"
    const val CREAR_CUENTA = "crear_cuenta"
    const val INICIAR_SESION = "iniciar_sesion"
    const val RECUPERAR = "recuperar_contrasena"
    const val ACOMPANANTES = "acompanantes/{actividadId}?sinCupo={sinCupo}"
    const val CONFIRMACION = "inscripcion_confirmada/{actividadId}"
    const val CANCELAR = "cancelar_inscripcion/{actividadId}"
    const val CANCELACION_CONFIRMADA = "cancelacion_confirmada/{actividadId}/{liberados}/{antes}"

    /** @param simularSinCupo true para llegar a P-08: al confirmar, otra familia gana los lugares. */
    fun inscribirse(actividadId: String, simularSinCupo: Boolean = false) =
        "inscribirse/$actividadId?sinCupo=$simularSinCupo"
    fun puerta(actividadId: String) = "puerta_cuenta/$actividadId"
    fun acompanantes(actividadId: String, simularSinCupo: Boolean) =
        "acompanantes/$actividadId?sinCupo=$simularSinCupo"
    fun confirmacion(actividadId: String) = "inscripcion_confirmada/$actividadId"
    fun cancelar(actividadId: String) = "cancelar_inscripcion/$actividadId"
    fun cancelacionConfirmada(actividadId: String, liberados: Int, antes: Int) =
        "cancelacion_confirmada/$actividadId/$liberados/$antes"
}

private val argumentoSinCupo = navArgument("sinCupo") {
    type = NavType.BoolType
    defaultValue = false
}

/**
 * La cuenta vive a nivel Activity: todas las pantallas (y las de las demás partes que
 * quieran saber si hay sesión) ven la misma instancia.
 */
@Composable
fun cuentaViewModel(): CuentaViewModel {
    val actividad = LocalActivity.current as ComponentActivity
    return viewModel(viewModelStoreOwner = actividad, factory = AppViewModelProvider.Factory)
}

// Saltos a otras partes de la app.
private fun NavController.irAOtrasActividades() = navigate(RutasActividades.LISTA) {
    popUpTo(Rutas.INICIO)
    launchSingleTop = true
}
private fun NavController.irAMisActividades() = navigate(Rutas.MIS_ACTIVIDADES) {
    popUpTo(Rutas.INICIO)
    launchSingleTop = true
}
private fun NavController.irAMiPerfil() = navigate(Rutas.PERFIL) {
    popUpTo(Rutas.INICIO)
    launchSingleTop = true
}

/**
 * El corazón de favoritos. Con sesión lo pone o lo quita; sin sesión pide entrar
 * y, al hacerlo, se regresa a la pantalla donde se tocó.
 */
fun NavController.alternarFavorita(cuenta: CuentaViewModel, id: String) {
    if (cuenta.sesion.value == null) {
        cuenta.recordarFavoritoPendiente(id)
        navigate(RutasInscripcion.INICIAR_SESION) { launchSingleTop = true }
    } else {
        cuenta.alternarFavorita(id)
    }
}

/** Después de crear cuenta o entrar: a la inscripción pendiente, o de regreso. */
private fun NavController.continuarDespuesDeEntrar(cuenta: CuentaViewModel, rutaActual: String) {
    val regreso = cuenta.tomarRegreso()
    if (regreso != null) {
        navigate(RutasInscripcion.acompanantes(regreso.actividadId, regreso.simularSinCupo)) {
            popUpTo(rutaActual) { inclusive = true }
        }
        return
    }
    val destino = cuenta.tomarDestino()
    if (destino != null) {
        navigate(destino) { popUpTo(rutaActual) { inclusive = true } }
    } else {
        popBackStack()
    }
}

fun NavGraphBuilder.grafoInscripcion(nav: NavController) {

    // Entrada desde "Inscribirme": decide entre P-04 y P-06 y se quita de en medio.
    composable(RutasInscripcion.INSCRIBIRSE, arguments = listOf(argumentoSinCupo)) { entrada ->
        val actividadId = entrada.arguments?.getString("actividadId") ?: return@composable
        val sinCupo = entrada.arguments?.getBoolean("sinCupo") ?: false
        val cuenta = cuentaViewModel()
        LaunchedEffect(actividadId) {
            val destino = if (cuenta.sesion.value == null) {
                cuenta.recordarRegreso(actividadId, sinCupo)
                RutasInscripcion.puerta(actividadId)
            } else {
                RutasInscripcion.acompanantes(actividadId, sinCupo)
            }
            nav.navigate(destino) { popUpTo(RutasInscripcion.INSCRIBIRSE) { inclusive = true } }
        }
    }

    // P-04 · Es un dialog: el detalle se sigue viendo detrás, con el velo del sistema.
    dialog(RutasInscripcion.PUERTA, dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) { entrada ->
        val actividadId = entrada.arguments?.getString("actividadId") ?: return@dialog
        val cuenta = cuentaViewModel()
        val actividades by cuenta.actividades.collectAsStateWithLifecycle()
        val actividad = actividades.firstOrNull { it.id == actividadId }

        PuertaDeCuentaScreen(
            actividad = actividad?.titulo,
            cuando = actividad?.let { "${it.fecha}, ${horaDeInicio(it.horario)}" } ?: "",
            onCrearCuenta = {
                nav.navigate(RutasInscripcion.CREAR_CUENTA) { popUpTo(RutasInscripcion.PUERTA) { inclusive = true } }
            },
            onYaTengoCuenta = {
                nav.navigate(RutasInscripcion.INICIAR_SESION) { popUpTo(RutasInscripcion.PUERTA) { inclusive = true } }
            },
            onCerrar = {
                cuenta.tomarRegreso()
                nav.popBackStack()
            }
        )
    }

    // P-05 · P-05b
    composable(RutasInscripcion.CREAR_CUENTA) {
        val cuenta = cuentaViewModel()
        val ui by cuenta.crear.collectAsStateWithLifecycle()
        LaunchedEffect(ui.lista) {
            if (ui.lista) {
                cuenta.crearAtendida()
                nav.continuarDespuesDeEntrar(cuenta, RutasInscripcion.CREAR_CUENTA)
            }
        }
        CrearCuentaScreen(
            ui = ui,
            estadoValidacion = ui.validacion,
            onNombreChange = cuenta::onNombreChange,
            onCorreoChange = cuenta::onCorreoChange,
            onContrasenaChange = cuenta::onContrasenaChange,
            onAceptaAvisoChange = cuenta::onAceptaAvisoChange,
            onCrearCuenta = cuenta::crearCuenta,
            onIniciarSesion = {
                nav.navigate(RutasInscripcion.INICIAR_SESION) { popUpTo(RutasInscripcion.CREAR_CUENTA) { inclusive = true } }
            },
            onRegresar = { nav.popBackStack() }
        )
    }

    // P-26
    composable(RutasInscripcion.INICIAR_SESION) {
        val cuenta = cuentaViewModel()
        val ui by cuenta.entrar.collectAsStateWithLifecycle()
        val regreso by cuenta.regreso.collectAsStateWithLifecycle()
        val actividades by cuenta.actividades.collectAsStateWithLifecycle()
        val regresoA = regreso?.let { r ->
            actividades.firstOrNull { it.id == r.actividadId }?.let { "${it.titulo} · ${fechaCorta(it.fecha)}" }
        }
        LaunchedEffect(ui.lista) {
            if (ui.lista) {
                cuenta.entrarAtendida()
                nav.continuarDespuesDeEntrar(cuenta, RutasInscripcion.INICIAR_SESION)
            }
        }
        IniciarSesionScreen(
            ui = ui,
            regresoA = regresoA,
            onCorreoChange = cuenta::onCorreoEntrarChange,
            onContrasenaChange = cuenta::onContrasenaEntrarChange,
            onEntrar = cuenta::iniciarSesion,
            onOlvideContrasena = {
                cuenta.precargarCorreoRecuperar()
                nav.navigate(RutasInscripcion.RECUPERAR)
            },
            onCrearCuenta = {
                nav.navigate(RutasInscripcion.CREAR_CUENTA) { popUpTo(RutasInscripcion.INICIAR_SESION) { inclusive = true } }
            },
            onRegresar = { nav.popBackStack() }
        )
    }

    // P-27
    composable(RutasInscripcion.RECUPERAR) {
        val cuenta = cuentaViewModel()
        val ui by cuenta.recuperar.collectAsStateWithLifecycle()
        RecuperarScreen(
            ui = ui,
            onCorreoChange = cuenta::onCorreoRecuperarChange,
            onEnviarEnlace = cuenta::enviarEnlace,
            onVolverAIniciarSesion = {
                if (!nav.popBackStack(RutasInscripcion.INICIAR_SESION, inclusive = false)) {
                    nav.navigate(RutasInscripcion.INICIAR_SESION) { popUpTo(RutasInscripcion.RECUPERAR) { inclusive = true } }
                }
            }
        )
    }

    // P-06 · P-06b · P-08
    composable(RutasInscripcion.ACOMPANANTES, arguments = listOf(argumentoSinCupo)) { entrada ->
        val actividadId = entrada.arguments?.getString("actividadId") ?: return@composable
        val sinCupo = entrada.arguments?.getBoolean("sinCupo") ?: false
        val cuenta = cuentaViewModel()
        val sesion by cuenta.sesion.collectAsStateWithLifecycle()
        val actividades by cuenta.actividades.collectAsStateWithLifecycle()
        val vm: AcompanantesViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val context = LocalContext.current

        val titular = sesion?.titular
        val actividad = actividades.firstOrNull { it.id == actividadId }
        LaunchedEffect(titular, actividad) {
            if (titular != null && actividad != null) {
                vm.cargar(
                    actividadId = actividadId,
                    titular = titular,
                    guardados = cuenta.inscripciones.value[actividadId] ?: cuenta.acompanantes.value,
                    lugaresDisponibles = cuenta.lugaresDisponibles(actividad),
                    simularSinCupo = sinCupo
                )
            }
        }

        when (val estado = vm.ui.collectAsStateWithLifecycle().value) {
            is UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(estado.mensaje, onReintentar = { nav.popBackStack() })
            is UiState.Exito -> {
                val ui = estado.datos
                LaunchedEffect(ui.confirmada) {
                    if (ui.confirmada) {
                        cuenta.registrarInscripcion(actividadId, ui.acompanantes)
                        // Notificación real del sistema. Si no hay permiso, no pasa nada.
                        NotificacionesDemo.dispararInscripcionConfirmada(
                            context,
                            tituloActividad = ui.actividad.titulo,
                            cuando = "el ${ui.actividad.fecha.lowercase()} a las ${horaDeInicio(ui.actividad.horario)}",
                            personas = ui.personas,
                            actividadId = actividadId
                        )
                        vm.confirmacionAtendida()
                        nav.navigate(RutasInscripcion.confirmacion(actividadId)) {
                            popUpTo(RutasInscripcion.ACOMPANANTES) { inclusive = true }
                        }
                    }
                }
                Box(Modifier.fillMaxSize()) {
                    AcompanantesScreen(
                        ui = ui,
                        onAgregarFila = vm::agregarFila,
                        onQuitarFila = vm::quitarFila,
                        onNombreChange = vm::onNombreChange,
                        onFechaNacimientoChange = vm::onFechaNacimientoChange,
                        onConsentimientoChange = vm::onConsentimientoChange,
                        onConfirmar = vm::confirmar,
                        onRegresar = { nav.popBackStack() }
                    )
                    ui.sinLugares?.let { sinLugares ->
                        SinLugaresScreen(
                            tituloActividad = ui.actividad.titulo,
                            sinLugares = sinLugares,
                            avisoRegistrado = ui.avisoRegistrado,
                            onVerOtrasActividades = { nav.irAOtrasActividades() },
                            onAvisarme = vm::avisarmeSiSeLibera,
                            onCerrar = vm::cerrarSinLugares
                        )
                    }
                }
            }
        }
    }

    // P-07
    composable(RutasInscripcion.CONFIRMACION) { entrada ->
        val actividadId = entrada.arguments?.getString("actividadId") ?: return@composable
        val cuenta = cuentaViewModel()
        val sesion by cuenta.sesion.collectAsStateWithLifecycle()
        val inscripciones by cuenta.inscripciones.collectAsStateWithLifecycle()
        val actividades by cuenta.actividades.collectAsStateWithLifecycle()
        val asociaciones by cuenta.asociaciones.collectAsStateWithLifecycle()
        val actividad = actividades.firstOrNull { it.id == actividadId }
        val titular = sesion?.titular
        if (actividad == null || titular == null) {
            CargandoView()
            return@composable
        }
        val contexto = LocalContext.current
        val nombreAsociacion = asociaciones.firstOrNull { it.id == actividad.asociacionId }?.nombre ?: "La asociación"
        // Se vuelve a leer al regresar de la encuesta para que la tarjeta cambie a "ya respondieron".
        val encuestas = (contexto.applicationContext as FamiliasApplication).container.encuestaRepository
        var encuestaContestada by remember { mutableStateOf(false) }
        var refrescar by remember { mutableIntStateOf(0) }
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refrescar++ }
        LaunchedEffect(refrescar, sesion?.correo) {
            encuestaContestada = sesion?.correo?.let { correo ->
                encuestas.getDeFamilia(correo).any { it.actividadId == actividadId && it.momento == MomentoEncuesta.ANTES }
            } ?: false
        }
        ConfirmacionScreen(
            actividad = actividad,
            asociacion = nombreAsociacion,
            asistentes = listOf(titular) + (inscripciones[actividadId] ?: emptyList()),
            onAgregarAlCalendario = { agregarActividadAlCalendario(contexto, actividad, nombreAsociacion) },
            onVerMisActividades = { nav.irAMisActividades() },
            encuestaContestada = encuestaContestada,
            onResponderEncuesta = { nav.responderEncuestaPrevia(actividad.id, actividad.titulo) }
        )
    }

    // P-20 · Diálogo sobre Mis Actividades
    dialog(RutasInscripcion.CANCELAR, dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) { entrada ->
        val actividadId = entrada.arguments?.getString("actividadId") ?: return@dialog
        val cuenta = cuentaViewModel()
        val inscripciones by cuenta.inscripciones.collectAsStateWithLifecycle()
        val cancelacion by cuenta.cancelacion.collectAsStateWithLifecycle()

        LaunchedEffect(cancelacion) {
            val c = cancelacion
            if (c != null && c.actividadId == actividadId && c.liberados != null && c.antes != null) {
                cuenta.cancelacionAtendida()
                nav.navigate(RutasInscripcion.cancelacionConfirmada(actividadId, c.liberados, c.antes)) {
                    popUpTo(RutasInscripcion.CANCELAR) { inclusive = true }
                }
            }
        }
        CancelarScreen(
            personas = 1 + (inscripciones[actividadId]?.size ?: 0),
            faltaParaActividad = CuentaDePrueba.FALTA_PARA_ACTIVIDAD,
            cancelando = cancelacion?.cancelando == true,
            onConfirmar = { cuenta.cancelarInscripcion(actividadId) },
            onMejorNo = { nav.popBackStack() }
        )
    }

    // P-32
    composable(
        RutasInscripcion.CANCELACION_CONFIRMADA,
        arguments = listOf(
            navArgument("liberados") { type = NavType.IntType },
            navArgument("antes") { type = NavType.IntType }
        )
    ) { entrada ->
        val actividadId = entrada.arguments?.getString("actividadId") ?: return@composable
        val cuenta = cuentaViewModel()
        val actividades by cuenta.actividades.collectAsStateWithLifecycle()
        val asociaciones by cuenta.asociaciones.collectAsStateWithLifecycle()
        val actividad = actividades.firstOrNull { it.id == actividadId }
        CancelacionConfirmadaScreen(
            asociacion = actividad?.let { a -> asociaciones.firstOrNull { it.id == a.asociacionId }?.nombre } ?: "la asociación",
            liberados = entrada.arguments?.getInt("liberados") ?: 0,
            antes = entrada.arguments?.getInt("antes") ?: 0,
            cupoTotal = actividad?.cupoTotal ?: 0,
            onVerOtrasActividades = { nav.irAOtrasActividades() },
            onVolverAMiPerfil = { nav.irAMiPerfil() }
        )
    }
}
