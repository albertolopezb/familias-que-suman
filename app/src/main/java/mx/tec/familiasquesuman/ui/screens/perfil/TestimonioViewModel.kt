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
import mx.tec.familiasquesuman.data.TestimonioRepository
import mx.tec.familiasquesuman.domain.Participacion
import mx.tec.familiasquesuman.domain.Testimonio
import mx.tec.familiasquesuman.ui.state.UiState
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Un testimonio es breve: lo bastante para contar algo, lo bastante corto para leerlo en una tarjeta. */
const val MIN_LETRAS_TESTIMONIO = 20
const val MAX_LETRAS_TESTIMONIO = 500
private const val MAX_BYTES_FOTO = 5_000_000L

data class FormularioTestimonio(
    val experiencia: String = "",
    val foto: Uri? = null,
    val revisandoFoto: Boolean = false,
    val enviando: Boolean = false,
    val enviado: Boolean = false,
    val mensaje: String? = null
) {
    val experienciaValida: Boolean get() = experiencia.trim().length in MIN_LETRAS_TESTIMONIO..MAX_LETRAS_TESTIMONIO
    val puedeEnviar: Boolean get() = experienciaValida && foto != null && !revisandoFoto && !enviando
}

/**
 * El formulario "Compartir testimonio" (RF-12): una foto y una experiencia breve de una participación.
 * Se publica en cuanto se envía; quien lo escribió puede eliminarlo después.
 */
class TestimonioViewModel(
    private val perfilRepository: PerfilRepository,
    private val testimonioRepository: TestimonioRepository,
    private val contenido: ContentResolver,
    private val carpetaFotos: File
) : ViewModel() {
    private val _participacion = MutableStateFlow<UiState<Participacion>>(UiState.Cargando)
    val participacion = _participacion.asStateFlow()
    private val _formulario = MutableStateFlow(FormularioTestimonio())
    val formulario = _formulario.asStateFlow()

    /** El testimonio que esta cuenta ya publicó de esta participación, si lo hay. */
    private val _existente = MutableStateFlow<Testimonio?>(null)
    val existente = _existente.asStateFlow()

    private var carga: Job? = null
    private var revisionFoto: Job? = null

    fun cargar(participacionId: String, correo: String?) {
        carga?.cancel()
        carga = viewModelScope.launch {
            _participacion.value = UiState.Cargando
            try {
                val encontrada = perfilRepository.getHistorial().firstOrNull { it.id == participacionId }
                _existente.value = correo?.let { c ->
                    testimonioRepository.getDeFamilia(c).firstOrNull { it.participacionId == participacionId }
                }
                _participacion.value = if (encontrada != null) UiState.Exito(encontrada)
                    else UiState.Error("Esta actividad no aparece en tu historial.")
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _participacion.value = UiState.Error("No se pudo cargar la actividad. Intenta de nuevo.")
            }
        }
    }

    fun cambiarExperiencia(texto: String) {
        _formulario.update { it.copy(experiencia = texto.take(MAX_LETRAS_TESTIMONIO), mensaje = null) }
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
                            if (total > MAX_BYTES_FOTO) return@use false
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

    /**
     * Publica el testimonio. La foto se copia al almacenamiento de la app porque el permiso
     * del selector es temporal.
     */
    fun publicar(participacionId: String, correo: String, familia: String) {
        val actual = _formulario.value
        val actividad = (_participacion.value as? UiState.Exito)?.datos ?: return
        if (actual.enviando) return
        val foto = actual.foto
        if (!actual.puedeEnviar || foto == null) {
            _formulario.update { it.copy(mensaje = when {
                actual.foto == null -> "Agrega una foto de la actividad."
                actual.experiencia.trim().length < MIN_LETRAS_TESTIMONIO ->
                    "Cuéntanos un poco más (mínimo $MIN_LETRAS_TESTIMONIO letras)."
                else -> null
            }) }
            return
        }
        viewModelScope.launch {
            _formulario.update { it.copy(enviando = true, mensaje = null) }
            try {
                val ruta = withContext(Dispatchers.IO) { copiarFoto(foto) }
                testimonioRepository.publicar(
                    Testimonio(
                        id = "t${System.currentTimeMillis()}",
                        participacionId = participacionId,
                        actividadTitulo = actividad.tituloActividad,
                        familia = familia,
                        correo = correo,
                        experiencia = actual.experiencia.trim(),
                        foto = ruta,
                        fecha = LocalDate.now().format(FormatoFecha)
                    )
                )
                _formulario.update { it.copy(enviando = false, enviado = true) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _formulario.update { it.copy(enviando = false, mensaje = "No se pudo publicar. Tu testimonio se conserva; intenta de nuevo.") }
            }
        }
    }

    /** Elimina el testimonio de esta cuenta (el repositorio solo deja a su autora o autor). */
    fun eliminar(correo: String, alTerminar: () -> Unit) {
        val testimonio = _existente.value ?: return
        viewModelScope.launch {
            if (testimonioRepository.eliminar(testimonio.id, correo, esAdmin = false)) {
                _existente.value = null
                alTerminar()
            } else {
                _formulario.update { it.copy(mensaje = "No se pudo eliminar. Intenta de nuevo.") }
            }
        }
    }

    private fun copiarFoto(origen: Uri): String {
        carpetaFotos.mkdirs()
        val destino = File(carpetaFotos, "testimonio_${System.currentTimeMillis()}.img")
        val entrada = contenido.openInputStream(origen) ?: error("No se pudo abrir la foto")
        entrada.use { de -> destino.outputStream().use { a -> de.copyTo(a) } }
        return destino.absolutePath
    }

    private companion object {
        val FormatoFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "MX"))
    }
}
