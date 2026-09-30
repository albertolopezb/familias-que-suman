package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.domain.CentroVisiteo
import mx.tec.familiasquesuman.domain.Proyecto

/** Los datos de las páginas Proyectos y Directorio de Visiteo del sitio. */
class DirectorioViewModel(
    private val actividadRepository: ActividadRepository
) : ViewModel() {

    private val _proyectos = MutableStateFlow<List<Proyecto>>(emptyList())
    val proyectos: StateFlow<List<Proyecto>> = _proyectos.asStateFlow()

    private val _centros = MutableStateFlow<List<CentroVisiteo>>(emptyList())
    val centros: StateFlow<List<CentroVisiteo>> = _centros.asStateFlow()

    init {
        viewModelScope.launch {
            _proyectos.value = actividadRepository.getProyectos()
            _centros.value = actividadRepository.getCentros()
        }
    }
}
