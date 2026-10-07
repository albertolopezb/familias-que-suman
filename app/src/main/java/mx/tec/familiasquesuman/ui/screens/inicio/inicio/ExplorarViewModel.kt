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

    fun cargar() {
        viewModelScope.launch {
            _todasAsociaciones.value = actividadRepository.getAsociaciones()
        }
    }

    fun guardarAsociacion(id: String?, v: List<String>, onListo: () -> Unit = {}) {
        viewModelScope.launch {
            val asociacion = Asociacion(
                id = id ?: "a${System.currentTimeMillis()}",
                nombre = v[0].trim(),
                categoria = v[1].trim(),
                descripcion = v[2].trim(),
                direccion = v[3].trim(),
                telefono = v[4].trim(),
                whatsapp = v[5].trim(),
                correo = v[6].trim()
            )
            if (id == null) actividadRepository.agregarAsociacion(asociacion) else actividadRepository.editarAsociacion(asociacion)
            cargar()
            onListo()
        }
    }

    fun borrarAsociacion(id: String, onListo: () -> Unit = {}) {
        viewModelScope.launch {
            actividadRepository.borrarAsociacion(id)
            cargar()
            onListo()
        }
    }

    fun onBusquedaChange(nuevoTexto: String) {
        _busqueda.value = nuevoTexto
    }

    fun onCategoriaSelect(categoria: String?) {
        _categoriaSeleccionada.value = if (_categoriaSeleccionada.value == categoria) null else categoria
    }
}