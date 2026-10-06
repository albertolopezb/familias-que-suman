package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.TestimonioRepository
import mx.tec.familiasquesuman.domain.EstadoTestimonio
import mx.tec.familiasquesuman.domain.Testimonio

/** La pantalla de revisión de testimonios del admin (RF-12). */
class RevisionTestimoniosViewModel(private val repositorio: TestimonioRepository) : ViewModel() {

    private val _testimonios = MutableStateFlow<List<Testimonio>>(emptyList())
    val testimonios: StateFlow<List<Testimonio>> = _testimonios.asStateFlow()

    fun cargar() {
        viewModelScope.launch { _testimonios.value = repositorio.getTodos() }
    }

    fun resolver(id: String, estado: EstadoTestimonio, nota: String) {
        viewModelScope.launch {
            repositorio.cambiarEstado(id, estado, nota)
            _testimonios.value = repositorio.getTodos()
        }
    }
}
