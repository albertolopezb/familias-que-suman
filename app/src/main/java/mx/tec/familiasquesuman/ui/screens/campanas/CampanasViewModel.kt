package mx.tec.familiasquesuman.ui.screens.campanas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.data.CampanaRepository
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.ui.state.UiState

/** Categorías que ofrece el Figma en la hoja de filtros (P-10). */
val CategoriasFiltro = listOf("Alimentos", "Ropa", "Juguetes", "Salud", "Útiles escolares")

/** Chips rápidos de arriba de la lista (P-09). */
val CategoriasRapidas = listOf("Alimentos", "Juguetes")

data class FiltrosCampanas(
    val categorias: Set<String> = emptySet(),
    val soloUrgentes: Boolean = false
) {
    val hayActivos: Boolean get() = categorias.isNotEmpty() || soloUrgentes

    fun acepta(c: Campana): Boolean =
        (categorias.isEmpty() || c.categoria in categorias) && (!soloUrgentes || c.urgente)
}

/**
 * Filtros de campañas (RF-20). El filtrado es en Kotlin sobre la lista del repositorio.
 * Hay dos juegos de filtros: los [aplicados] (los que ve la lista) y el [borrador]
 * (lo que se está moviendo dentro de la hoja, sin aplicar todavía).
 */
class CampanasViewModel(
    private val repo: CampanaRepository,
    private val actividadRepo: ActividadRepository
) : ViewModel() {

    private val carga = MutableStateFlow<UiState<List<Campana>>>(UiState.Cargando)
    private val _aplicados = MutableStateFlow(FiltrosCampanas())
    private val _borrador = MutableStateFlow(FiltrosCampanas())

    val aplicados: StateFlow<FiltrosCampanas> = _aplicados.asStateFlow()
    val borrador: StateFlow<FiltrosCampanas> = _borrador.asStateFlow()

    /** Lista ya filtrada, con el mismo UiState que pinta los cuatro estados. */
    val campanas: StateFlow<UiState<List<Campana>>> =
        combine(carga, _aplicados) { estado, filtros ->
            when (estado) {
                is UiState.Exito -> UiState.Exito(estado.datos.filter { filtros.acepta(it) })
                else -> estado
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    /** Cuántas campañas hay abiertas sin ningún filtro ("Hay 3 campañas abiertas con otros filtros"). */
    val totalAbiertas: StateFlow<Int> =
        carga.combine(_aplicados) { estado, _ ->
            (estado as? UiState.Exito)?.datos?.size ?: 0
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Resultados en vivo para el botón "Ver 2 campañas" de la hoja. */
    val conteoBorrador: StateFlow<Int> =
        combine(carga, _borrador) { estado, filtros ->
            (estado as? UiState.Exito)?.datos?.count { filtros.acepta(it) } ?: 0
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _nombresAsociacion = MutableStateFlow<Map<String, String>>(emptyMap())

    /** id de asociación → nombre, para el "Parroquia San Bernabé · Cierra el…" de cada tarjeta. */
    val nombresAsociacion: StateFlow<Map<String, String>> = _nombresAsociacion.asStateFlow()

    init {
        cargar()
        cargarNombres()
    }

    private fun cargarNombres() {
        viewModelScope.launch {
            _nombresAsociacion.value = try {
                actividadRepo.getAsociaciones().associate { it.id to it.nombre }
            } catch (e: Exception) {
                emptyMap() // Sin nombre, la tarjeta solo dice "Cierra el…".
            }
        }
    }

    fun cargar() {
        viewModelScope.launch {
            carga.value = UiState.Cargando
            delay(700) // Se nota el estado de carga; con el backend se quita.
            carga.value = try {
                UiState.Exito(repo.getCampanas())
            } catch (e: Exception) {
                UiState.Error("No se pudieron cargar las campañas")
            }
        }
    }

    // ---- Chips rápidos y píldoras de la lista ----

    /** null = "Todas". */
    fun elegirChipRapido(categoria: String?) {
        _aplicados.update { it.copy(categorias = if (categoria == null) emptySet() else setOf(categoria)) }
    }

    fun quitarCategoria(categoria: String) {
        _aplicados.update { it.copy(categorias = it.categorias - categoria) }
    }

    fun quitarUrgentes() {
        _aplicados.update { it.copy(soloUrgentes = false) }
    }

    fun quitarFiltros() {
        _aplicados.value = FiltrosCampanas()
    }

    // ---- Hoja de filtros ----

    fun abrirFiltros() {
        _borrador.value = _aplicados.value
    }

    fun alternarCategoriaBorrador(categoria: String) {
        _borrador.update {
            it.copy(categorias = if (categoria in it.categorias) it.categorias - categoria else it.categorias + categoria)
        }
    }

    fun cambiarUrgentesBorrador(valor: Boolean) {
        _borrador.update { it.copy(soloUrgentes = valor) }
    }

    fun limpiarBorrador() {
        _borrador.value = FiltrosCampanas()
    }

    fun aplicarFiltros() {
        _aplicados.value = _borrador.value
    }

    // ---- Solo para pruebas: borrar cuando llegue el backend ----

    /** P-09c sin filtros: lista vacía. */
    fun forzarVacio() {
        carga.value = UiState.Exito(emptyList())
    }

    /** P-09d: error de carga. */
    fun forzarError() {
        carga.value = UiState.Error("No se pudieron cargar las campañas")
    }
}
