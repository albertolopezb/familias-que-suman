package mx.tec.familiasquesuman

import mx.tec.familiasquesuman.ui.screens.perfil.*
import org.junit.Assert.*
import org.junit.Test

class EncuestaViewModelTest {
    @Test fun noPermiteAvanzarSinRespuesta() {
        val vm = EncuestaViewModel()
        assertFalse(vm.estado.value.puedeAvanzar)
        assertFalse(vm.avanzar())
        assertEquals(2, vm.estado.value.preguntaActual.numero)
    }

    @Test fun reemplazaSeleccionEIgnoraOpcionesInvalidas() {
        val vm = EncuestaViewModel()
        vm.seleccionarRespuesta(0)
        vm.seleccionarRespuesta(2)
        vm.seleccionarRespuesta(9)
        assertEquals(mapOf(2 to 2), vm.estado.value.respuestas)
        assertEquals(0.5f, vm.estado.value.progreso, 0.001f)
    }

    @Test fun preguntaParcialNoFinalizaNiPierdeRespuesta() {
        val vm = EncuestaViewModel()
        vm.seleccionarRespuesta(1)
        assertFalse(vm.avanzar())
        assertEquals(1, vm.estado.value.respuestaSeleccionada)
        assertNotNull(vm.estado.value.mensaje)
    }

    @Test fun estructuraCompletaAvanzaYFinalizaConTodasLasRespuestas() {
        // Contenido sintético exclusivo de pruebas; no se muestra en la app.
        val vm = EncuestaViewModel(DefinicionEncuesta(2, listOf(
            PreguntaEncuesta(1, "Prueba 1", listOf("A", "B")),
            PreguntaEncuesta(2, "Prueba 2", listOf("A", "B"))
        )))
        vm.seleccionarRespuesta(0)
        assertFalse(vm.avanzar())
        assertEquals(2, vm.estado.value.preguntaActual.numero)
        assertFalse(vm.avanzar())
        vm.seleccionarRespuesta(1)
        assertTrue(vm.avanzar())
        assertEquals(mapOf(1 to 0, 2 to 1), vm.estado.value.respuestas)
    }
}
