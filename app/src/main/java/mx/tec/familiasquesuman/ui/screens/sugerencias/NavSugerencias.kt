package mx.tec.familiasquesuman.ui.screens.sugerencias

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import mx.tec.familiasquesuman.domain.TipoSugerencia
import mx.tec.familiasquesuman.ui.screens.inscripcion.cuentaViewModel
import mx.tec.familiasquesuman.ui.state.AppViewModelProvider

object RutasSugerencias {
    const val SUGERIR = "sugerir/{tipo}"
    const val BANDEJA = "sugerencias"

    fun sugerir(tipo: TipoSugerencia) = "sugerir/${tipo.name}"
}

/** Desde cualquier lista: abre "Sugerir" con el tipo de esa lista ya elegido. */
fun NavController.sugerir(tipo: TipoSugerencia) = navigate(RutasSugerencias.sugerir(tipo)) { launchSingleTop = true }

/** Grafo de sugerencias. En FamiliasApp.kt va UNA línea: grafoSugerencias(nav, esAdmin). */
fun NavGraphBuilder.grafoSugerencias(nav: NavController, esAdmin: () -> Boolean = { false }) {

    composable(
        route = RutasSugerencias.SUGERIR,
        arguments = listOf(navArgument("tipo") { type = NavType.StringType })
    ) { entrada ->
        val tipo = entrada.arguments?.getString("tipo")
            ?.let { nombre -> TipoSugerencia.entries.firstOrNull { it.name == nombre } }
            ?: TipoSugerencia.ACTIVIDAD
        val vm: SugerenciasViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val formulario by vm.formulario.collectAsStateWithLifecycle()
        // Con sesión, "Tus datos" ya vienen llenos.
        val sesion by cuentaViewModel().sesion.collectAsStateWithLifecycle()
        LaunchedEffect(tipo) { vm.preparar(tipo, sesion?.familia, sesion?.correo) }

        SugerirScreen(
            formulario = formulario,
            onCambio = vm::editar,
            onEnviar = vm::enviar,
            onSugerirOtra = vm::sugerirOtra,
            onRegresar = { nav.popBackStack() }
        )
    }

    composable(RutasSugerencias.BANDEJA) {
        val vm: SugerenciasViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val sugerencias by vm.sugerencias.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { vm.cargarBandeja() }
        BandejaSugerenciasScreen(
            sugerencias = sugerencias,
            esAdmin = esAdmin(),
            onCambiarEstado = vm::cambiarEstado,
            onRegresar = { nav.popBackStack() }
        )
    }
}
