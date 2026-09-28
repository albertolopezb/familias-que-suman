package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mx.tec.familiasquesuman.ui.state.AppViewModelProvider

object RutasInicio {
    const val SPLASH = "splash"
    const val PERMISO_NOTIFICACIONES = "permiso_notificaciones"
    const val CIUDAD = "ciudad"
    const val INICIO = "inicio_main"
    const val EXPLORAR = "explorar"
    const val ASOCIACION = "asociacion/{asociacionId}"
    const val VISITEO = "visiteo"
    const val PROYECTOS = "proyectos"
    const val TESTIMONIOS = "testimonios"

    fun asociacion(id: String) = "asociacion/$id"
}

fun NavGraphBuilder.grafoInicio(
    nav: NavController,
    onNavegarAActividades: () -> Unit = {},
    onNavegarACampanas: () -> Unit = {}
) {
    composable(RutasInicio.SPLASH) {
        SplashScreen(
            onSplashFinished = {
                nav.navigate(RutasInicio.CIUDAD) {
                    popUpTo(RutasInicio.SPLASH) { inclusive = true }
                }
            }
        )
    }

    composable(RutasInicio.PERMISO_NOTIFICACIONES) {
        PermisoNotificacionesScreen(
            onContinuar = { nav.navigate(RutasInicio.CIUDAD) }
        )
    }

    composable(RutasInicio.CIUDAD) {
        val vm: InicioViewModel = viewModel(factory = AppViewModelProvider.Factory)
        CiudadScreen(
            ciudadActual = vm.ciudadElegida,
            onCiudadSeleccionada = { ciudad ->
                vm.cambiarCiudad(ciudad)
                nav.navigate(RutasInicio.INICIO) {
                    popUpTo(RutasInicio.CIUDAD) { inclusive = true }
                }
            }
        )
    }

    composable(RutasInicio.INICIO) {
        val vm: InicioViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val asociacionDestacada by vm.asociacionDestacada.collectAsStateWithLifecycle()

        InicioScreen(
            ciudad = vm.ciudadElegida,
            onCambiarCiudad = vm::cambiarCiudad,
            asociacion = asociacionDestacada,
            onExplorarClick = { nav.navigate(RutasInicio.EXPLORAR) },
            onActividadesClick = onNavegarAActividades,
            onDonarClick = onNavegarACampanas,
            onProyectosClick = { nav.navigate(RutasInicio.PROYECTOS) },
            onVisiteoClick = { nav.navigate(RutasInicio.VISITEO) }
        )
    }

    composable(RutasInicio.EXPLORAR) {
        val vm: ExplorarViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val textoBusqueda by vm.busqueda.collectAsStateWithLifecycle()
        val categoriaSeleccionada by vm.categoriaSeleccionada.collectAsStateWithLifecycle()
        val asociaciones by vm.asociacionesFiltradas.collectAsStateWithLifecycle()

        ExplorarScreen(
            textoBusqueda = textoBusqueda,
            onBusquedaChange = vm::onBusquedaChange,
            categoriaSeleccionada = categoriaSeleccionada,
            onCategoriaSelect = vm::onCategoriaSelect,
            asociaciones = asociaciones,
            onAsociacionClick = { id -> nav.navigate(RutasInicio.asociacion(id)) }
        )
    }

    composable(RutasInicio.ASOCIACION) { backStackEntry ->
        val id = backStackEntry.arguments?.getString("asociacionId") ?: ""
        val vm: AsociacionViewModel = viewModel(factory = AppViewModelProvider.Factory)
        vm.cargarAsociacion(id)

        val asociacion by vm.asociacion.collectAsStateWithLifecycle()
        val esFavorito by vm.esFavorito.collectAsStateWithLifecycle()

        asociacion?.let {
            AsociacionScreen(
                asociacion = it,
                esFavorito = esFavorito,
                onToggleFavorito = vm::toggleFavorito,
                onVerActividades = onNavegarAActividades
            )
        }
    }

    composable(RutasInicio.VISITEO) {
        VisiteoScreen(onCentroClick = { id -> nav.navigate(RutasInicio.asociacion(id)) })
    }

    composable(RutasInicio.PROYECTOS) {
        ProyectosScreen()
    }

    composable(RutasInicio.TESTIMONIOS) {
        TestimoniosScreen()
    }
}