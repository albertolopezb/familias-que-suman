package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.TestimonioRepository
import mx.tec.familiasquesuman.domain.Testimonio

/** Los testimonios publicados, y su moderación: el admin elimina cualquiera y cada cuenta los suyos. */
class TestimoniosViewModel(private val repositorio: TestimonioRepository) : ViewModel() {

    private val _testimonios = MutableStateFlow<List<Testimonio>>(emptyList())
    val testimonios: StateFlow<List<Testimonio>> = _testimonios.asStateFlow()

    fun cargar() {
        viewModelScope.launch { _testimonios.value = repositorio.getTodos() }
    }

    fun eliminar(id: String, correo: String?, esAdmin: Boolean) {
        viewModelScope.launch {
            repositorio.eliminar(id, correo, esAdmin)
            _testimonios.value = repositorio.getTodos()
        }
    }
}
