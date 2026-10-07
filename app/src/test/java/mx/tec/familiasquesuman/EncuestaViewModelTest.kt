package mx.tec.familiasquesuman

import mx.tec.familiasquesuman.data.EncuestaRepository
import mx.tec.familiasquesuman.domain.MomentoEncuesta
import mx.tec.familiasquesuman.ui.screens.perfil.*
import org.junit.Assert.*
import org.junit.Test

class EncuestaViewModelTest {
    private fun vm(definicion: DefinicionEncuesta = EncuestaFinal, momento: MomentoEncuesta = MomentoEncuesta.DESPUES) =
        EncuestaViewModel(definicion, momento, EncuestaRepository())

    @Test fun lasEncuestasDeAntesYDespuesEstanCompletas() {
        assertEquals((1..3).toList(), EncuestaPrevia.preguntas.map { it.numero })
        assertEquals((1..4).toList(), EncuestaFinal.preguntas.map { it.numero })
    }

    @Test fun noPermiteAvanzarSinRespuesta() {
        val vm = vm()
        assertFalse(vm.estado.value.puedeAvanzar)
        assertFalse(vm.avanzar())
        assertEquals(1, vm.estado.value.preguntaActual.numero)
    }

    @Test fun reemplazaSeleccionEIgnoraOpcionesInvalidas() {
        val vm = vm()
        vm.seleccionarRespuesta(0)
        vm.seleccionarRespuesta(2)
        vm.seleccionarRespuesta(9)
        assertEquals(mapOf(1 to 2), vm.estado.value.respuestas)
        assertEquals(0.25f, vm.estado.value.progreso, 0.001f)
    }

    @Test fun soloFinalizaConTodasLasPreguntasContestadas() {
        val vm = vm()
        // Las tres primeras solo avanzan; la cuarta es la que termina.
        repeat(3) {
            vm.seleccionarRespuesta(0)
            assertFalse(vm.avanzar())
        }
        assertTrue(vm.estado.value.esUltima)
        assertFalse(vm.avanzar())
        vm.seleccionarRespuesta(1)
        assertTrue(vm.avanzar())
        assertEquals(mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 1), vm.estado.value.respuestas)
    }

    @Test fun retrocederConservaLasRespuestas() {
        val vm = vm()
        vm.seleccionarRespuesta(1)
        vm.avanzar()
        assertTrue(vm.retroceder())
        assertEquals(1, vm.estado.value.preguntaActual.numero)
        assertEquals(1, vm.estado.value.respuestaSeleccionada)
        assertFalse(vm.retroceder())
    }

    @Test fun estructuraCompletaAvanzaYFinalizaConTodasLasRespuestas() {
        // Contenido sintético exclusivo de pruebas; no se muestra en la app.
        val vm = vm(DefinicionEncuesta(2, listOf(
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
