package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
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
 */
fun NavGraphBuilder.grafoActividades(
    nav: NavController,
    ciudad: String = "Monterrey",
    onInscribirme: (String) -> Unit = {},
    onVerAsociaciones: () -> Unit = {},
    onAbrirParticipacion: (String) -> Unit = {}
) {

    composable(RutasActividades.LISTA) {
        val vm: ActividadesViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()

        ActividadesScreen(
            estado = estado,
            ciudad = ciudad,
            onActividadClick = { id -> nav.navigate(RutasActividades.detalle(id)) },
            onRegresar = { nav.popBackStack() },
            onReintentar = vm::cargar,
            onVerAsociaciones = onVerAsociaciones
        )
    }

    composable(RutasActividades.DETALLE) { entrada ->
        val id = entrada.arguments?.getString(RutasActividades.ARG_ACTIVIDAD_ID).orEmpty()
        val vm: DetalleActividadViewModel = viewModel(factory = AppViewModelProvider.Factory)

        // Dentro de un LaunchedEffect, no en el cuerpo del composable: si no,
        // la carga se dispara otra vez en cada recomposición.
        LaunchedEffect(id) { vm.cargar(id) }

        val estado by vm.estado.collectAsStateWithLifecycle()
        val esFavorito by vm.esFavorito.collectAsStateWithLifecycle()

        when (val actual = estado) {
            is UiState.Cargando -> CargandoView()

            is UiState.Error -> ErrorView(
                mensaje = actual.mensaje,
                onReintentar = vm::reintentar
            )

            is UiState.Exito -> DetalleActividadScreen(
                item = actual.datos,
                esFavorito = esFavorito,
                onRegresar = { nav.popBackStack() },
                onAlternarFavorito = vm::alternarFavorito,
                // Parte 3 conecta esto con la puerta de cuenta (RF-18).
                onInscribirme = { onInscribirme(id) },
                onVerOtrasActividades = { nav.popBackStack() }
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
                onProximaClick = { id -> nav.navigate(RutasActividades.detalle(id)) },
                // Parte 5 conecta esto con testimonio (RF-12) y encuestas (RF-13).
                onParticipacionClick = onAbrirParticipacion,
                onVerActividades = { nav.navigate(RutasActividades.LISTA) }
            )
        }
    }

    composable(RutasActividades.SIN_CONEXION) {
        val vm: ActividadesViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        val guardadas = when (val actual = estado) {
            is UiState.Exito -> actual.datos
            else -> emptyList()
        }

        SinConexionScreen(
            actividadesGuardadas = guardadas,
            onRegresar = { nav.popBackStack() },
            onReintentar = vm::cargar
        )
    }
}
