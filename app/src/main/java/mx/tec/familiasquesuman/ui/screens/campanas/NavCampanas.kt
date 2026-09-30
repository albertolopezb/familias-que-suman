package mx.tec.familiasquesuman.ui.screens.campanas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import mx.tec.familiasquesuman.ui.state.AppViewModelProvider
import mx.tec.familiasquesuman.ui.state.UiState

object RutasCampanas {
    const val LISTA = "campanas"
    const val DETALLE = "campanas/{campanaId}"
    const val CONFIRMADO = "campanas/{campanaId}/apartado"
    const val COMO_DONAR = "campanas-como-donar"

    fun detalle(id: String) = "campanas/$id"
    fun confirmado(id: String) = "campanas/$id/apartado"
}

/**
 * Grafo de la parte 4. En FamiliasApp.kt se agrega UNA línea, al final del NavHost:
 *     grafoCampanas(nav)
 * Y para llegar desde Inicio ("Quiero Donar"): nav.navigate(RutasCampanas.LISTA)
 */
fun NavGraphBuilder.grafoCampanas(nav: NavController) {

    // P-09 / 09b / 09c / 09d + hoja de filtros P-10
    composable(RutasCampanas.LISTA) {
        val vm: CampanasViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.campanas.collectAsStateWithLifecycle()
        val aplicados by vm.aplicados.collectAsStateWithLifecycle()
        val borrador by vm.borrador.collectAsStateWithLifecycle()
        val conteo by vm.conteoBorrador.collectAsStateWithLifecycle()
        val total by vm.totalAbiertas.collectAsStateWithLifecycle()
        var verFiltros by rememberSaveable { mutableStateOf(false) }

        CampanasScreen(
            estado = estado,
            filtros = aplicados,
            totalAbiertas = total,
            onBack = { nav.popBackStack() },
            onAbrirFiltros = { vm.abrirFiltros(); verFiltros = true },
            onChipRapido = vm::elegirChipRapido,
            onQuitarCategoria = vm::quitarCategoria,
            onQuitarUrgentes = vm::quitarUrgentes,
            onQuitarFiltros = vm::quitarFiltros,
            onCampanaClick = { id -> nav.navigate(RutasCampanas.detalle(id)) },
            onReintentar = vm::cargar
        )

        if (verFiltros) {
            FiltrosSheet(
                borrador = borrador,
                conteo = conteo,
                onCategoria = vm::alternarCategoriaBorrador,
                onUrgentes = vm::cambiarUrgentesBorrador,
                onLimpiar = vm::limpiarBorrador,
                onAplicar = { vm.aplicarFiltros(); verFiltros = false },
                onCerrar = { verFiltros = false }
            )
        }
    }

    // P-11 / P-11c + hoja de apartar P-12 / P-12b / P-14
    composable(
        route = RutasCampanas.DETALLE,
        arguments = listOf(navArgument("campanaId") { type = NavType.StringType })
    ) { entrada ->
        val id = entrada.arguments?.getString("campanaId") ?: return@composable
        val vm: DetalleCampanaViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        val hoja by vm.hoja.collectAsStateWithLifecycle()
        val irAConfirmado by vm.irAConfirmado.collectAsStateWithLifecycle()

        LaunchedEffect(id) { vm.cargar(id) }
        LaunchedEffect(irAConfirmado) {
            if (irAConfirmado) {
                vm.confirmadoNavegado()
                nav.navigate(RutasCampanas.confirmado(id))
            }
        }

        when (val e = estado) {
            is UiState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(e.mensaje, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { nav.popBackStack() }) { Text("Regresar") }
            }
            is UiState.Exito -> {
                DetalleCampanaScreen(
                    campana = e.datos,
                    campanaCompleta = vm.campanaCompleta(e.datos),
                    onBack = { nav.popBackStack() },
                    onApartar = vm::abrirApartar,
                    onVerComoDonar = { nav.navigate(RutasCampanas.COMO_DONAR) },
                    onVerOtrasCampanas = {
                        if (!nav.popBackStack(RutasCampanas.LISTA, inclusive = false)) nav.popBackStack()
                    }
                )
            }
        }

        val hojaActual = hoja
        if (hojaActual != null) {
            ApartarSheet(
                hoja = hojaActual,
                onCantidad = vm::cambiarCantidad,
                onConfirmar = vm::confirmarApartado,
                onCerrar = vm::cerrarHoja
            )
        }
    }

    // P-13: comparte el ViewModel del detalle para ver la barra ya actualizada
    composable(
        route = RutasCampanas.CONFIRMADO,
        arguments = listOf(navArgument("campanaId") { type = NavType.StringType })
    ) { entrada ->
        val padre = remember(entrada) { nav.getBackStackEntry(RutasCampanas.DETALLE) }
        val vm: DetalleCampanaViewModel = viewModel(viewModelStoreOwner = padre, factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        val hecho by vm.ultimoApartado.collectAsStateWithLifecycle()

        val campana = (estado as? UiState.Exito)?.datos
        val apartado = hecho
        if (campana != null && apartado != null) {
            ApartadoConfirmadoScreen(
                campana = campana,
                hecho = apartado,
                onVerComoEntregar = { nav.navigate(RutasCampanas.COMO_DONAR) },
                onApartarAlgoMas = { nav.popBackStack() }
            )
        }
    }

    // P-24
    composable(RutasCampanas.COMO_DONAR) {
        ComoDonarScreen(onBack = { nav.popBackStack() })
    }
}
