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
import mx.tec.familiasquesuman.domain.Sexo
import mx.tec.familiasquesuman.ui.state.UiState
import java.time.LocalDate
import java.time.Period

/**
 * Un renglón de P-06. `nueva` = recién agregada, todavía con campos para llenar.
 * La fecha de nacimiento se elige en un calendario y de ella sale la edad. Los acompañantes
 * guardados antes de que existiera el calendario solo traen `edadGuardada`.
 */
data class FilaUi(
    val id: Int,
    val nombre: String,
    val fechaNacimiento: LocalDate?,
    val nueva: Boolean,
    val edadGuardada: Int? = null,
    val sexo: Sexo? = null
) {
    val edad: Int? get() = fechaNacimiento?.let { edadEnAnios(it) } ?: edadGuardada
}

/** Años cumplidos a hoy. */
fun edadEnAnios(fechaNacimiento: LocalDate, hoy: LocalDate = LocalDate.now()): Int =
    Period.between(fechaNacimiento, hoy).years

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

    /** Sin fecha todavía no es error; menor a la edad mínima, sí. */
    fun errorEdad(fila: FilaUi): String? {
        val edad = fila.edad ?: return null
        val minima = edadMinima
        return if (minima != null && edad < minima) "La edad mínima para esta actividad es de $minima años" else null
    }

    private fun filaValida(fila: FilaUi): Boolean =
        fila.nombre.trim().length >= 3 && fila.edad != null && errorEdad(fila) == null

    val personas: Int get() = 1 + filas.size
    val hayMenores: Boolean get() = filas.any { (it.edad ?: 99) < 18 }
    val excedeLugares: Boolean get() = personas > lugaresDisponibles

    val puedeConfirmar: Boolean
        get() = !enviando && lugaresDisponibles > 0 && !excedeLugares &&
            filas.all { filaValida(it) } && (!hayMenores || consentimiento)

    val acompanantes: List<Acompanante>
        get() = filas.map { Acompanante(it.nombre.trim(), it.edad ?: 0, it.fechaNacimiento, it.sexo) }
}

/**
 * P-06 · P-06b · P-08. Lista de acompañantes que crece y se achica, validación de
 * nombre y fecha de nacimiento, cálculo de lugares, y `confirmar()` con la espera simulada.
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
                        filas = guardados.map {
                            FilaUi(siguienteId++, it.nombre, it.fechaNacimiento, nueva = false, edadGuardada = it.edad, sexo = it.sexo)
                        },
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

    fun agregarFila() = editar { it.copy(filas = it.filas + FilaUi(siguienteId++, "", null, nueva = true)) }

    fun quitarFila(id: Int) = editar { ui -> ui.copy(filas = ui.filas.filterNot { it.id == id }) }

    fun onNombreChange(id: Int, nombre: String) =
        editar { ui -> ui.copy(filas = ui.filas.map { if (it.id == id) it.copy(nombre = nombre) else it }) }

    fun onFechaNacimientoChange(id: Int, fecha: LocalDate) =
        editar { ui -> ui.copy(filas = ui.filas.map { if (it.id == id) it.copy(fechaNacimiento = fecha) else it }) }

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
