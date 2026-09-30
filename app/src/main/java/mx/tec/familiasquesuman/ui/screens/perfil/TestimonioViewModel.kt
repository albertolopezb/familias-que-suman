package mx.tec.familiasquesuman.ui.screens.perfil

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.tec.familiasquesuman.data.PerfilRepository
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.ui.state.UiState

data class FormularioTestimonio(
    val experiencia: String = "",
    val foto: Uri? = null,
    val revisandoFoto: Boolean = false,
    val mensaje: String? = null
)

class TestimonioViewModel(
    private val perfilRepository: PerfilRepository,
    private val contenido: ContentResolver
) : ViewModel() {
    private val _actividad = MutableStateFlow<UiState<Actividad>>(UiState.Cargando)
    val actividad = _actividad.asStateFlow()
    private val _formulario = MutableStateFlow(FormularioTestimonio())
    val formulario = _formulario.asStateFlow()
    private var carga: Job? = null
    private var revisionFoto: Job? = null

    fun cargarActividad(id: String) {
        carga?.cancel()
        carga = viewModelScope.launch {
            _actividad.value = UiState.Cargando
            try {
                // No existe historial de actividades completadas ni contrato de testimonios.
                val encontrada = perfilRepository.getProximas().firstOrNull { it.id == id }
                _actividad.value = if (encontrada != null) UiState.Exito(encontrada)
                    else UiState.Error("La actividad no está disponible para este formulario.")
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _actividad.value = UiState.Error("No se pudo cargar la actividad. Intenta de nuevo.")
            }
        }
    }

    fun cambiarExperiencia(texto: String) {
        _formulario.update { it.copy(experiencia = texto, mensaje = null) }
    }

    fun seleccionarFoto(uri: Uri?) {
        if (uri == null) return // Cancelar el selector conserva la foto anterior.
        revisionFoto?.cancel()
        revisionFoto = viewModelScope.launch {
            _formulario.update { it.copy(revisandoFoto = true, mensaje = null) }
            try {
                val valida = withContext(Dispatchers.IO) {
                    if (contenido.getType(uri)?.startsWith("image/") != true) return@withContext false
                    contenido.openInputStream(uri)?.use { entrada ->
                        val bloque = ByteArray(8192)
                        var total = 0L
                        while (true) {
                            val leidos = entrada.read(bloque)
                            if (leidos < 0) break
                            total += leidos
                            if (total > 5_000_000) return@use false
                        }
                        total > 0
                    } ?: false
                }
                _formulario.update { it.copy(
                    foto = if (valida) uri else it.foto,
                    revisandoFoto = false,
                    mensaje = if (valida) null else "Selecciona una foto de hasta 5 MB."
                ) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _formulario.update { it.copy(revisandoFoto = false, mensaje = "No se pudo leer la foto.") }
            }
        }
    }

    fun enviarARevision() {
        // Pendiente del equipo: modelo y operación para enviar texto, foto y actividad a revisión.
        // Se conserva el formulario y nunca se informa un envío exitoso.
        _formulario.update { it.copy(mensaje = "El envío a revisión todavía no está disponible. Tu testimonio no se ha enviado.") }
    }
}
