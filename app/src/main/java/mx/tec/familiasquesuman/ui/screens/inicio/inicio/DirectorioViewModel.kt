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
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            _proyectos.value = actividadRepository.getProyectos()
            _centros.value = actividadRepository.getCentros()
        }
    }

    // ── Administración ──

    fun guardarProyecto(original: Proyecto?, v: List<String>) {
        viewModelScope.launch {
            val nuevo = (original ?: Proyecto(
                id = "pr${System.currentTimeMillis()}", nombre = "", descripcion = "",
                beneficiarios = null, ciudad = "", vigencia = null, logo = null
            )).copy(
                nombre = v[0].trim(),
                descripcion = v[1].trim(),
                beneficiarios = v[2].trim().ifBlank { null },
                ciudad = v[3].trim(),
                vigencia = v[4].trim().ifBlank { null },
                activo = v[5] == "true"
            )
            if (original == null) actividadRepository.agregarProyecto(nuevo) else actividadRepository.editarProyecto(nuevo)
            cargar()
        }
    }

    fun borrarProyecto(id: String) {
        viewModelScope.launch {
            actividadRepository.borrarProyecto(id)
            cargar()
        }
    }

    fun guardarCentro(original: CentroVisiteo?, v: List<String>) {
        viewModelScope.launch {
            val nuevo = (original ?: CentroVisiteo(
                id = "ce${System.currentTimeMillis()}", nombre = "", tipo = "", resumen = "",
                informacion = "", necesidades = emptyList(), direccion = "", logo = null
            )).copy(
                nombre = v[0].trim(),
                tipo = v[1].trim(),
                resumen = v[2].trim(),
                informacion = v[3].trim(),
                necesidades = v[4].lines().map { it.trim() }.filter { it.isNotEmpty() },
                direccion = v[5].trim(),
                verificado = v[6] == "true"
            )
            if (original == null) actividadRepository.agregarCentro(nuevo) else actividadRepository.editarCentro(nuevo)
            cargar()
        }
    }

    fun borrarCentro(id: String) {
        viewModelScope.launch {
            actividadRepository.borrarCentro(id)
            cargar()
        }
    }
}
