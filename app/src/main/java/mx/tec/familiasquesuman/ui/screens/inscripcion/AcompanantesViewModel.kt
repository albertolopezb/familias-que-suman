package mx.tec.familiasquesuman.ui.screens.inscripcion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.domain.Acompanante
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.ui.state.UiState

/** Un renglón de P-06. `nueva` = recién agregada, todavía con campos para escribir. */
data class FilaUi(val id: Int, val nombre: String, val edad: String, val nueva: Boolean)

/** Lo que muestra la hoja de P-08. */
data class SinLugaresUi(val quedaban: Int, val ahora: Int, val cupoTotal: Int)

data class AcompanantesUi(
    val actividad: Actividad,
    val titular: Acompanante,
    val filas: List<FilaUi>,
    val lugaresDisponibles: Int,
    val consentimiento: Boolean = false,
    val enviando: Boolean = false,
    val sinLugares: SinLugaresUi? = null,
    val avisoRegistrado: Boolean = false,
    val confirmada: Boolean = false
) {
    private val edadMinima: Int? get() = actividad.edadMinima

    fun errorNombre(fila: FilaUi): String? =
        if (fila.nombre.isNotEmpty() && fila.nombre.trim().length < 3) "Escribe su nombre" else null

    /** Vacía todavía no es error; mal escrita o menor a la edad mínima, sí. */
    fun errorEdad(fila: FilaUi): String? {
        if (fila.edad.isEmpty()) return null
        val edad = fila.edad.toIntOrNull() ?: return "Edad en años"
        val minima = edadMinima
        return when {
            edad !in 1..99 -> "Edad en años"
            minima != null && edad < minima -> "La edad mínima para esta actividad es de $minima años"
            else -> null
        }
    }

    private fun filaValida(fila: FilaUi): Boolean =
        fila.nombre.trim().length >= 3 && fila.edad.isNotEmpty() && errorEdad(fila) == null

    val personas: Int get() = 1 + filas.size
    val hayMenores: Boolean get() = filas.any { (it.edad.toIntOrNull() ?: 99) < 18 }
    val excedeLugares: Boolean get() = personas > lugaresDisponibles

    val puedeConfirmar: Boolean
        get() = !enviando && lugaresDisponibles > 0 && !excedeLugares &&
            filas.all { filaValida(it) } && (!hayMenores || consentimiento)

    val acompanantes: List<Acompanante>
        get() = filas.map { Acompanante(it.nombre.trim(), it.edad.toIntOrNull() ?: 0) }
}

/**
 * P-06 · P-06b · P-08. Lista de acompañantes que crece y se achica, validación de
 * nombre y edad, cálculo de lugares, y `confirmar()` con la espera simulada.
 */
class AcompanantesViewModel(private val actividadRepository: ActividadRepository) : ViewModel() {

    private val _ui = MutableStateFlow<UiState<AcompanantesUi>>(UiState.Cargando)
    val ui: StateFlow<UiState<AcompanantesUi>> = _ui.asStateFlow()

    /** true = al confirmar, otra familia gana los lugares y sale P-08. Solo para probar. */
    var simularSinCupo: Boolean = false
        private set

    private var cargada: String? = null
    private var siguienteId = 0

    fun cargar(
        actividadId: String,
        titular: Acompanante,
        guardados: List<Acompanante>,
        lugaresDisponibles: Int,
        simularSinCupo: Boolean
    ) {
        if (cargada == actividadId) return
        cargada = actividadId
        this.simularSinCupo = simularSinCupo
        viewModelScope.launch {
            _ui.value = try {
                UiState.Exito(
                    AcompanantesUi(
                        actividad = actividadRepository.getActividad(actividadId),
                        titular = titular,
                        filas = guardados.map { FilaUi(siguienteId++, it.nombre, "${it.edad}", nueva = false) },
                        lugaresDisponibles = lugaresDisponibles
                    )
                )
            } catch (e: NoSuchElementException) {
                UiState.Error("No encontramos esa actividad. Puede que ya no esté publicada.")
            }
        }
    }

    private fun editar(cambio: (AcompanantesUi) -> AcompanantesUi) {
        val actual = _ui.value
        if (actual is UiState.Exito) _ui.value = UiState.Exito(cambio(actual.datos))
    }

    fun agregarFila() = editar { it.copy(filas = it.filas + FilaUi(siguienteId++, "", "", nueva = true)) }

    fun quitarFila(id: Int) = editar { ui -> ui.copy(filas = ui.filas.filterNot { it.id == id }) }

    fun onNombreChange(id: Int, nombre: String) =
        editar { ui -> ui.copy(filas = ui.filas.map { if (it.id == id) it.copy(nombre = nombre) else it }) }

    fun onEdadChange(id: Int, edad: String) =
        editar { ui -> ui.copy(filas = ui.filas.map { if (it.id == id) it.copy(edad = edad) else it }) }

    fun onConsentimientoChange(valor: Boolean) = editar { it.copy(consentimiento = valor) }

    fun confirmar() {
        val datos = (_ui.value as? UiState.Exito)?.datos ?: return
        if (!datos.puedeConfirmar) return
        viewModelScope.launch {
            editar { it.copy(enviando = true) }
            delay(1000)
            if (simularSinCupo) {
                editar {
                    it.copy(
                        enviando = false,
                        lugaresDisponibles = 0,
                        sinLugares = SinLugaresUi(it.lugaresDisponibles, 0, it.actividad.cupoTotal)
                    )
                }
            } else {
                editar { it.copy(enviando = false, confirmada = true) }
            }
        }
    }

    fun avisarmeSiSeLibera() = editar { it.copy(avisoRegistrado = true) }
    fun cerrarSinLugares() = editar { it.copy(sinLugares = null) }
    fun confirmacionAtendida() = editar { it.copy(confirmada = false) }
}
