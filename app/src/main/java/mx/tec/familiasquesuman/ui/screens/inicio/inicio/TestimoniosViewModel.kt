package mx.tec.familiasquesuman.ui.screens.inicio

import mx.tec.familiasquesuman.ui.state.intentar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.TestimonioRepository
import mx.tec.familiasquesuman.domain.Testimonio

/**
 * Los testimonios publicados, y su moderación (HU-11): quien modera aprueba o rechaza lo que está
 * en revisión y elimina cualquiera; cada cuenta elimina los suyos.
 */
class TestimoniosViewModel(private val repositorio: TestimonioRepository) : ViewModel() {

    private val _testimonios = MutableStateFlow<List<Testimonio>>(emptyList())
    val testimonios: StateFlow<List<Testimonio>> = _testimonios.asStateFlow()

    /** Solo se llena para el admin; si su cuenta no tiene `moderar:contenido`, el servidor dice 403 y queda vacía. */
    private val _porRevisar = MutableStateFlow<List<Testimonio>>(emptyList())
    val porRevisar: StateFlow<List<Testimonio>> = _porRevisar.asStateFlow()

    fun cargar(esAdmin: Boolean = false) {
        viewModelScope.launch { intentar { repositorio.getTodos() }.onSuccess { _testimonios.value = it } }
        if (esAdmin) viewModelScope.launch { intentar { repositorio.porRevisar() }.onSuccess { _porRevisar.value = it } }
    }

    fun revisar(id: String, aprobar: Boolean) {
        viewModelScope.launch {
            if (intentar { repositorio.revisar(id, aprobar) }.getOrDefault(false)) {
                _porRevisar.value = _porRevisar.value.filterNot { it.id == id }
                if (aprobar) intentar { repositorio.getTodos() }.onSuccess { _testimonios.value = it }
            }
        }
    }

    fun eliminar(id: String, correo: String?, esAdmin: Boolean) {
        viewModelScope.launch {
            intentar { repositorio.eliminar(id, correo, esAdmin) }
            intentar { repositorio.getTodos() }.onSuccess { _testimonios.value = it }
        }
    }
}
