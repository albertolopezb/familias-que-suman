package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.domain.Asociacion

class ExplorarViewModel(
    private val actividadRepository: ActividadRepository
) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    private val _categoriaSeleccionada = MutableStateFlow<String?>(null)
    val categoriaSeleccionada: StateFlow<String?> = _categoriaSeleccionada

    private val _todasAsociaciones = MutableStateFlow<List<Asociacion>>(emptyList())

    val asociacionesFiltradas: StateFlow<List<Asociacion>> = combine(
        _todasAsociaciones,
        _busqueda,
        _categoriaSeleccionada
    ) { lista, query, cat ->
        lista.filter {
            val coincideTexto = query.isEmpty() || it.nombre.contains(query, ignoreCase = true) || it.descripcion.contains(query, ignoreCase = true)
            val coincideCat = cat == null || it.categoria.equals(cat, ignoreCase = true)
            coincideTexto && coincideCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            _todasAsociaciones.value = actividadRepository.getAsociaciones()
        }
    }

    fun onBusquedaChange(nuevoTexto: String) {
        _busqueda.value = nuevoTexto
    }

    fun onCategoriaSelect(categoria: String?) {
        _categoriaSeleccionada.value = if (_categoriaSeleccionada.value == categoria) null else categoria
    }
}