package mx.tec.familiasquesuman

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.espresso.Espresso
import mx.tec.familiasquesuman.ui.screens.campanas.*
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TengoAlgoParaDonarUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun cambiarModoConservaEstadoDeCentrosYEstadoDeCampanas() {
        val vm = TengoAlgoParaDonarViewModel()
        compose.setContent {
            val centros by vm.estado.collectAsState()
            FamiliasQueSumanTheme {
                CampanasScreen(
                    estado = UiState.Error("Error de campañas de prueba"),
                    filtros = FiltrosCampanas(), totalAbiertas = 0,
                    onBack = {}, onAbrirFiltros = {}, onChipRapido = {},
                    onQuitarCategoria = {}, onQuitarUrgentes = {}, onQuitarFiltros = {},
                    onCampanaClick = {}, onReintentar = {},
                    estadoCentros = centros, onTipoCentro = vm::seleccionarTipo,
                    onSeleccionarCentro = vm::seleccionarCentro, onReintentarCentros = vm::cargar
                )
            }
        }
        compose.onNodeWithText("Error de campañas de prueba").assertExists()
        compose.onNodeWithText("Tengo algo para donar").performClick()
        compose.onNodeWithText("Error de campañas de prueba").assertDoesNotExist()
        compose.onNodeWithTag("tiposDonacion").performScrollToNode(hasText("Ropa"))
        compose.onNode(hasText("Ropa") and hasAnyAncestor(hasTestTag("tiposDonacion"))).performClick()
        compose.onNodeWithText("VIFAC").performClick()
        compose.onNodeWithText("Donar a campaña").performClick()
        compose.onNodeWithText("Error de campañas de prueba").assertExists()
        compose.onNodeWithText("Tengo algo para donar").performClick()
        compose.runOnIdle {
            assertEquals("Ropa", vm.estado.value.tipoSeleccionado)
            assertEquals("vifac", vm.estado.value.centroExpandido)
        }
        compose.onNodeWithText(CentrosDeRecepcion.first().descripcion).assertExists()
    }

    @Test fun filtrosExpansionYAccionesEntreganLosDatosOriginales() {
        val vm = TengoAlgoParaDonarViewModel()
        var telefono = ""
        var direccion = ""
        var whatsapp = ""
        var enlace = ""
        var ayuda = false
        compose.setContent {
            val estado by vm.estado.collectAsState()
            FamiliasQueSumanTheme {
                TengoAlgoParaDonar(
                    onLlamar = { telefono = it }, onWhatsApp = { whatsapp = it },
                    onComoLlegar = { direccion = it }, onAbrirEnlace = { enlace = it },
                    onNoEncontre = { ayuda = true }, estado = estado,
                    onTipoSeleccionado = vm::seleccionarTipo,
                    onCentroSeleccionado = vm::seleccionarCentro, onReintentar = vm::cargar
                )
            }
        }
        TiposDeDonacion.forEach { tipo ->
            compose.onNodeWithTag("tiposDonacion").performScrollToNode(hasText(tipo))
            compose.onNode(hasText(tipo) and hasAnyAncestor(hasTestTag("tiposDonacion"))).performClick()
            compose.runOnIdle { assertEquals(tipo, vm.estado.value.tipoSeleccionado) }
        }
        compose.onNodeWithTag("tiposDonacion").performScrollToNode(hasText("Todos"))
        compose.onNodeWithText("Todos").performClick()
        compose.onNodeWithText(CentrosDeRecepcion.first().descripcion).assertDoesNotExist()
        compose.onNodeWithText("VIFAC").performClick()
        compose.onNodeWithText(CentrosDeRecepcion.first().descripcion).assertExists()
        compose.onNodeWithText("Llamar").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(CentrosDeRecepcion.first().telefono, telefono) }
        compose.onNodeWithText("Cómo llegar").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(CentrosDeRecepcion.first().direccion, direccion) }
        compose.onNodeWithTag("centrosLista").performScrollToNode(hasText("VIFAC"))
        compose.onNodeWithText("VIFAC").performClick()
        compose.onNodeWithText(CentrosDeRecepcion.first().descripcion).assertDoesNotExist()
        val apadrina = CentrosDeRecepcion.last()
        compose.onNodeWithTag("centrosLista").performScrollToNode(hasText(apadrina.nombre))
        compose.onNodeWithText(apadrina.nombre).performClick()
        compose.onNodeWithText("WhatsApp").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(apadrina.whatsapp, whatsapp) }
        compose.onNodeWithContentDescription("Instagram").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(apadrina.instagram, enlace) }
        compose.onNodeWithText("Cuéntanos y te ayudamos").performScrollTo()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.runOnIdle { assertEquals(true, ayuda) }
    }

    @Test fun loadingErrorReintentoYEmptyTienenRepresentacionSeparada() {
        val vm = TengoAlgoParaDonarViewModel { emptyList() }
        vm.simularCarga()
        compose.setContent {
            val estado by vm.estado.collectAsState()
            FamiliasQueSumanTheme {
                TengoAlgoParaDonar({}, {}, {}, {}, {}, estado = estado,
                    onTipoSeleccionado = vm::seleccionarTipo,
                    onCentroSeleccionado = vm::seleccionarCentro, onReintentar = vm::cargar)
            }
        }
        compose.onNodeWithText("Cargando centros…").assertExists()
        compose.runOnIdle { vm.simularError() }
        compose.onNodeWithText("No se pudieron cargar los centros de donación.").assertExists()
        compose.onNodeWithText("Reintentar").performClick()
        compose.onNodeWithText("Todavía no hay asociaciones para este tipo de aportación.").assertExists()
        compose.onNodeWithText("0 centros aceptan lo que seleccionaste").assertExists()
        compose.onNodeWithText("Cargando centros…").assertDoesNotExist()
    }
}

class DonarNavegacionUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun donarDesdeBarraInferiorYBackRegresaAInicioSinDestinosDeFiltros() {
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodesWithText("Continuar").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Continuar").performClick()
        compose.onNodeWithText("Donar").performClick()
        compose.onNodeWithText("Quiero Donar").assertExists()
        compose.onNodeWithText("Tengo algo para donar").performClick()
        compose.onNodeWithTag("tiposDonacion").performScrollToNode(hasText("Ropa"))
        compose.onNode(hasText("Ropa") and hasAnyAncestor(hasTestTag("tiposDonacion"))).performClick()
        compose.onNodeWithText("VIFAC").performClick()
        compose.onNodeWithText("Donar a campaña").performClick()
        compose.onNodeWithText("Tengo algo para donar").performClick()
        compose.onNodeWithText(CentrosDeRecepcion.first().descripcion).assertExists()
        Espresso.pressBack()
        compose.onNodeWithText("¿CÓMO QUIERES AYUDAR HOY?").assertExists()
        compose.onNodeWithText("Tengo algo para donar").assertDoesNotExist()
        compose.onNodeWithText("Donar").performClick()
        compose.onNodeWithText("Donar a campaña").assertExists()
        Espresso.pressBack()
        compose.onNodeWithText("¿CÓMO QUIERES AYUDAR HOY?").assertExists()
    }
}
