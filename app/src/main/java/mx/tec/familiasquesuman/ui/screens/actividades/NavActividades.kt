package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import mx.tec.familiasquesuman.notificaciones.Notificaciones
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.compartirActividad
import mx.tec.familiasquesuman.ui.screens.inscripcion.alternarFavorita
import mx.tec.familiasquesuman.ui.screens.inscripcion.cuentaViewModel
import mx.tec.familiasquesuman.ui.state.AppViewModelProvider
import mx.tec.familiasquesuman.ui.state.UiState

object RutasActividades {
    const val LISTA = "actividades"
    const val DETALLE = "actividades/{actividadId}"
    const val SIN_CONEXION = "actividades_sin_conexion"

    /** La pestaña de la barra inferior. Rutas.MIS_ACTIVIDADES apunta aquí. */
    const val MIS_ACTIVIDADES = "mis_actividades"

    const val ARG_ACTIVIDAD_ID = "actividadId"

    fun detalle(id: String) = "actividades/$id"
}

/**
 * El único lugar donde se decide a dónde lleva cada botón de esta sección.
 *
 * Los saltos hacia otras partes llegan como parámetros: mientras esa parte no
 * exista, se quedan vacíos y nadie espera a nadie.
 *
 * - onInscribirme: parte 3, puerta de cuenta (RF-18) y acompañantes (RF-06).
 * - onCancelarInscripcion: parte 3, cancelar (RF-19).
 * - onResponderEncuesta / onCompartirTestimonio: parte 5 (RF-13, RF-12).
 * - onCambiarCiudad / onVerAsociaciones: parte 1.
 */
fun NavGraphBuilder.grafoActividades(
    nav: NavController,
    ciudad: String = "Monterrey, N.L.",
    onCambiarCiudad: () -> Unit = {},
    onIrAInicio: () -> Unit = { nav.popBackStack() },
    onInscribirme: (String) -> Unit = {},
    onCancelarInscripcion: (String) -> Unit = {},
    onResponderEncuesta: (String) -> Unit = {},
    onCompartirTestimonio: (String) -> Unit = {},
    onVerAsociaciones: () -> Unit = {}
) {

    composable(RutasActividades.LISTA) {
        val vm: ActividadesViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        val contexto = LocalContext.current

        ActividadesScreen(
            estado = estado,
            ciudad = ciudad,
            onActividadClick = { id -> nav.navigate(RutasActividades.detalle(id)) },
            onUnirme = onInscribirme,
            onCompartir = { item -> compartirActividad(contexto, item) },
            onIrAInicio = onIrAInicio,
            onCiudadClick = onCambiarCiudad,
            onReintentar = vm::cargar,
            onVerGuardadas = { nav.navigate(RutasActividades.SIN_CONEXION) },
            onVerAsociaciones = onVerAsociaciones,
            onForzarEstado = vm::siguienteModoDePrueba
        )
    }

    composable(
        RutasActividades.DETALLE,
        // La notificación de recordatorio abre el detalle de la actividad.
        deepLinks = listOf(navDeepLink { uriPattern = Notificaciones.URI_ACTIVIDAD + "{actividadId}" })
    ) { entrada ->
        val id = entrada.arguments?.getString(RutasActividades.ARG_ACTIVIDAD_ID).orEmpty()
        val vm: DetalleActividadViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val contexto = LocalContext.current

        // Dentro de un LaunchedEffect, no en el cuerpo del composable: si no,
        // la carga se dispara otra vez en cada recomposición.
        LaunchedEffect(id) { vm.cargar(id) }

        val estado by vm.estado.collectAsStateWithLifecycle()
        val cuenta = cuentaViewModel()
        val favoritas by cuenta.favoritas.collectAsStateWithLifecycle()

        when (val actual = estado) {
            is UiState.Cargando -> CargandoView()

            is UiState.Error -> ErrorView(
                mensaje = actual.mensaje,
                onReintentar = vm::reintentar
            )

            is UiState.Exito -> DetalleActividadScreen(
                item = actual.datos,
                esFavorito = id in favoritas,
                onRegresar = { nav.popBackStack() },
                onCompartir = { compartirActividad(contexto, actual.datos) },
                onAlternarFavorito = { nav.alternarFavorita(cuenta, id) },
                // Parte 3 conecta esto con la puerta de cuenta (RF-18).
                onInscribirme = { onInscribirme(id) },
                onVerOtrasActividades = {
                    // Si se llegó desde la lista, se regresa a ella; si no, se abre.
                    if (!nav.popBackStack(RutasActividades.LISTA, inclusive = false)) {
                        nav.navigate(RutasActividades.LISTA)
                    }
                }
            )
        }
    }

    composable(RutasActividades.MIS_ACTIVIDADES) {
        val vm: MisActividadesViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()

        when (val actual = estado) {
            is UiState.Cargando -> CargandoView()

            is UiState.Error -> ErrorView(
                mensaje = actual.mensaje,
                onReintentar = vm::cargar
            )

            is UiState.Exito -> MisActividadesScreen(
                datos = actual.datos,
                ciudad = ciudad,
                onCiudadClick = onCambiarCiudad,
                onProximaClick = { id -> nav.navigate(RutasActividades.detalle(id)) },
                onCancelar = onCancelarInscripcion,
                onResponderEncuesta = onResponderEncuesta,
                onCompartirTestimonio = onCompartirTestimonio,
                onVerActividades = { nav.navigate(RutasActividades.LISTA) }
            )
        }
    }

    composable(RutasActividades.SIN_CONEXION) { entrada ->
        // Usa el ViewModel de la lista, que es quien guardó lo último que cargó.
        // Si por alguna razón la lista no está abajo en la pila, usa uno propio.
        val dueno = remember(entrada) {
            runCatching { nav.getBackStackEntry(RutasActividades.LISTA) }.getOrDefault(entrada)
        }
        val vm: ActividadesViewModel =
            viewModel(viewModelStoreOwner = dueno, factory = AppViewModelProvider.Factory)
        val guardadas by vm.guardadas.collectAsStateWithLifecycle()

        SinConexionScreen(
            actividadesGuardadas = guardadas,
            onRegresar = { nav.popBackStack() },
            onReintentar = {
                vm.cargar()
                nav.popBackStack()
            }
        )
    }
}
