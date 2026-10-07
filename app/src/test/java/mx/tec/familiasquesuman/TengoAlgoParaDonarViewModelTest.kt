package mx.tec.familiasquesuman

import mx.tec.familiasquesuman.ui.screens.campanas.*
import mx.tec.familiasquesuman.ui.state.UiState
import org.junit.Assert.*
import org.junit.Test

class TengoAlgoParaDonarViewModelTest {
    @Test fun todosYTodasLasCategoriasConservanElFiltroExactoYOrden() {
        val vm = TengoAlgoParaDonarViewModel()
        assertEquals(CentrosDeRecepcion, vm.estado.value.centrosVisibles)
        val esperados = listOf(
            listOf("gran-familia", "apadrina"),
            listOf("vifac", "hogar-misericordia", "gran-familia", "apadrina"),
            listOf("vifac", "amad", "hogar-misericordia", "gran-familia", "apadrina"),
            listOf("vifac", "amad", "hogar-misericordia", "gran-familia", "apadrina"),
            listOf("amad", "hogar-misericordia", "apadrina"),
            listOf("amad"), listOf("amad", "gran-familia"),
            listOf("amad", "hogar-misericordia", "apadrina"),
            listOf("amad", "gran-familia", "apadrina"),
            listOf("gran-familia", "apadrina"), listOf("amad", "apadrina"),
            listOf("gran-familia", "apadrina")
        )
        TiposDeDonacion.forEachIndexed { indice, tipo ->
            vm.seleccionarTipo(tipo)
            assertEquals(tipo, vm.estado.value.tipoSeleccionado)
            assertEquals(esperados[indice], vm.estado.value.centrosVisibles.map { it.id })
        }
        vm.seleccionarTipo("ropa") // No debe confundirse con Ropa.
        assertEquals("Material didáctico", vm.estado.value.tipoSeleccionado)
        vm.seleccionarTipo("Todos")
        assertEquals(CentrosDeRecepcion, vm.estado.value.centrosVisibles)
    }

    @Test fun abrirCerrarYReemplazarUnSoloCentroSinPerderSeleccion() {
        val vm = TengoAlgoParaDonarViewModel()
        vm.seleccionarCentro("vifac")
        assertEquals("vifac", vm.estado.value.centroExpandido)
        vm.seleccionarCentro("amad")
        assertEquals("amad", vm.estado.value.centroExpandido)
        vm.seleccionarCentro("amad")
        assertNull(vm.estado.value.centroExpandido)
        vm.seleccionarCentro("vifac")
        vm.seleccionarTipo("Juguetes")
        assertEquals("vifac", vm.estado.value.centroExpandido)
        vm.seleccionarTipo("Todos")
        assertEquals("vifac", vm.estado.value.centroExpandido)
        vm.seleccionarCentro("inexistente")
        assertEquals("vifac", vm.estado.value.centroExpandido)
    }

    @Test fun vacioEsExitoYLaCargaYErrorSonIndependientesDeCampanas() {
        val vm = TengoAlgoParaDonarViewModel { listOf(CentrosDeRecepcion.first()) }
        vm.seleccionarTipo("Juguetes")
        assertTrue(vm.estado.value.datos is UiState.Exito)
        assertTrue(vm.estado.value.centrosVisibles.isEmpty())
        vm.simularCarga()
        assertEquals(UiState.Cargando, vm.estado.value.datos)
        vm.simularError()
        assertTrue(vm.estado.value.datos is UiState.Error)
        vm.cargar()
        assertTrue(vm.estado.value.datos is UiState.Exito)
        assertEquals("Juguetes", vm.estado.value.tipoSeleccionado)
        val fallido = TengoAlgoParaDonarViewModel { error("Fuente no disponible") }
        assertTrue(fallido.estado.value.datos is UiState.Error)
    }
}
