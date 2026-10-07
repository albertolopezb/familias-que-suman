package mx.tec.familiasquesuman.ui.screens.campanas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.data.CampanaRepository
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.ui.state.UiState

/** Categorías que ofrece el Figma en la hoja de filtros (P-10). */
val CategoriasFiltro = listOf("Alimentos", "Ropa", "Juguetes", "Salud", "Útiles escolares")

/** Chips de arriba de la lista: las mismas categorías de la hoja de filtros, en el mismo orden. */
val CategoriasRapidas = CategoriasFiltro

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
 * Hay tres cosas, y solo una de las dos primeras está activa a la vez ("una cosa o la otra"):
 *  - el [chipElegido] de la fila de arriba: un botón para navegar, sin ✕;
 *  - los [aplicados] desde la hoja de filtros: salen como píldoras con ✕;
 *  - el [borrador], lo que se está moviendo dentro de la hoja sin aplicar todavía.
 * Tocar un chip limpia los filtros de la hoja, y aplicar la hoja regresa los chips a "Todas".
 */
class CampanasViewModel(
    private val repo: CampanaRepository,
    private val actividadRepo: ActividadRepository
) : ViewModel() {

    private val carga = MutableStateFlow<UiState<List<Campana>>>(UiState.Cargando)
    private val _aplicados = MutableStateFlow(FiltrosCampanas())
    private val _chip = MutableStateFlow<String?>(null)
    private val _borrador = MutableStateFlow(FiltrosCampanas())

    val aplicados: StateFlow<FiltrosCampanas> = _aplicados.asStateFlow()

    /** Categoría del chip tocado; null = "Todas". */
    val chipElegido: StateFlow<String?> = _chip.asStateFlow()
    val borrador: StateFlow<FiltrosCampanas> = _borrador.asStateFlow()

    /** Lista ya filtrada, con el mismo UiState que pinta los cuatro estados. */
    val campanas: StateFlow<UiState<List<Campana>>> =
        combine(carga, _aplicados, _chip) { estado, filtros, chip ->
            when (estado) {
                is UiState.Exito -> UiState.Exito(
                    estado.datos.filter { (chip == null || it.categoria == chip) && filtros.acepta(it) }
                )
                else -> estado
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    /** Cuántas campañas hay abiertas sin ningún filtro ("Hay 3 campañas abiertas con otros filtros"). */
    val totalAbiertas: StateFlow<Int> =
        carga.map { estado ->
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

    // ---- Administración (solo en memoria) ----

    fun guardarCampana(original: Campana?, v: List<String>) {
        viewModelScope.launch {
            val base = original ?: Campana(
                id = "c${System.currentTimeMillis()}", titulo = "", asociacionId = "", categoria = "",
                cierra = "", urgente = false, descripcion = "", unidadMeta = "", metaTotal = 0,
                completados = 0, articulos = emptyList()
            )
            val nueva = base.copy(
                titulo = v[0].trim(),
                categoria = v[1].trim(),
                cierra = v[2].trim(),
                descripcion = v[3].trim(),
                unidadMeta = v[4].trim(),
                metaTotal = v[5].toIntOrNull() ?: base.metaTotal,
                completados = v[6].toIntOrNull() ?: base.completados,
                ciudad = v[7].trim().ifBlank { base.ciudad },
                urgente = v[8] == "true"
            )
            if (original == null) repo.agregarCampana(nueva) else repo.editarCampana(nueva)
            carga.value = UiState.Exito(repo.getCampanas())
        }
    }

    fun borrarCampana(id: String) {
        viewModelScope.launch {
            repo.borrarCampana(id)
            carga.value = UiState.Exito(repo.getCampanas())
        }
    }

    // ---- Chips rápidos y píldoras de la lista ----

    /** null = "Todas". Un chip es solo para navegar: limpia los filtros de la hoja. */
    fun elegirChipRapido(categoria: String?) {
        _chip.value = categoria
        _aplicados.value = FiltrosCampanas()
    }

    fun quitarCategoria(categoria: String) {
        _aplicados.update { it.copy(categorias = it.categorias - categoria) }
    }

    fun quitarUrgentes() {
        _aplicados.update { it.copy(soloUrgentes = false) }
    }

    /** "Quitar filtros" del estado vacío: deja todo como al entrar. */
    fun quitarFiltros() {
        _aplicados.value = FiltrosCampanas()
        _chip.value = null
    }

    // ---- Hoja de filtros ----

    fun abrirFiltros() {
        // Si había un chip elegido, la hoja lo trae marcado: al aplicar pasa a ser una píldora con ✕.
        val chip = _chip.value
        _borrador.value = if (chip != null) FiltrosCampanas(setOf(chip)) else _aplicados.value
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
        _chip.value = null
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
