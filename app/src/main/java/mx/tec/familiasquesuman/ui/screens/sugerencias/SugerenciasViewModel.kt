package mx.tec.familiasquesuman.ui.screens.sugerencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.SugerenciaRepository
import mx.tec.familiasquesuman.domain.EstadoSugerencia
import mx.tec.familiasquesuman.domain.Sugerencia
import mx.tec.familiasquesuman.domain.TipoSugerencia
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Lo que la persona va llenando en "Sugerir". */
data class FormularioSugerencia(
    val tipo: TipoSugerencia = TipoSugerencia.ACTIVIDAD,
    val nombre: String = "",
    val descripcion: String = "",
    val detalleExtra: String = "",
    val ciudad: String = "Monterrey",
    val organizacion: String = "",
    val contactoCausa: String = "",
    val tuNombre: String = "",
    val tuContacto: String = "",
    val intentoEnviar: Boolean = false,  // los errores solo salen después del primer intento
    val enviando: Boolean = false,
    val enviada: Boolean = false
) {
    val errorNombre: String? get() = if (intentoEnviar && nombre.trim().length < 3) "Escribe el nombre" else null
    val errorDescripcion: String?
        get() = if (intentoEnviar && descripcion.trim().length < 10) "Cuéntanos un poco más (al menos 10 letras)" else null
    val errorTuNombre: String? get() = if (intentoEnviar && tuNombre.trim().length < 3) "Escribe tu nombre" else null
    val errorTuContacto: String?
        get() = if (intentoEnviar && tuContacto.isBlank()) "Déjanos un correo o teléfono para contactarte" else null

    val valido: Boolean
        get() = nombre.trim().length >= 3 && descripcion.trim().length >= 10 &&
            tuNombre.trim().length >= 3 && tuContacto.isNotBlank()
}

/**
 * El formulario "Sugerir" y la bandeja del admin. La pantalla de sugerir y la bandeja usan
 * cada una su propia instancia: el formulario no ve la lista y la bandeja no ve el formulario.
 */
class SugerenciasViewModel(private val repositorio: SugerenciaRepository) : ViewModel() {

    private val _formulario = MutableStateFlow(FormularioSugerencia())
    val formulario: StateFlow<FormularioSugerencia> = _formulario.asStateFlow()

    private val _sugerencias = MutableStateFlow<List<Sugerencia>>(emptyList())
    val sugerencias: StateFlow<List<Sugerencia>> = _sugerencias.asStateFlow()

    private var preparado = false

    /** Elige el tipo según de dónde se llegó y llena los datos de la cuenta, una sola vez. */
    fun preparar(tipo: TipoSugerencia, tuNombre: String?, tuContacto: String?) {
        if (preparado) return
        preparado = true
        _formulario.update {
            it.copy(tipo = tipo, tuNombre = tuNombre.orEmpty(), tuContacto = tuContacto.orEmpty())
        }
    }

    fun editar(cambio: (FormularioSugerencia) -> FormularioSugerencia) = _formulario.update(cambio)

    fun enviar() {
        val f = _formulario.value
        if (f.enviando) return
        if (!f.valido) {
            _formulario.update { it.copy(intentoEnviar = true) }
            return
        }
        viewModelScope.launch {
            _formulario.update { it.copy(enviando = true, intentoEnviar = true) }
            delay(600) // como si fuera al servidor
            repositorio.enviar(
                Sugerencia(
                    id = "sg${System.currentTimeMillis()}",
                    tipo = f.tipo,
                    nombre = f.nombre.trim(),
                    descripcion = f.descripcion.trim(),
                    detalleExtra = f.detalleExtra.trim(),
                    ciudad = f.ciudad.trim().ifBlank { "Monterrey" },
                    organizacion = f.organizacion.trim(),
                    contactoCausa = f.contactoCausa.trim(),
                    sugeridaPor = f.tuNombre.trim(),
                    contactoDeQuienSugiere = f.tuContacto.trim(),
                    fecha = LocalDate.now().format(FormatoFecha)
                )
            )
            _formulario.update { it.copy(enviando = false, enviada = true) }
        }
    }

    /** "Sugerir otra": deja el tipo y los datos de quien sugiere; borra lo demás. */
    fun sugerirOtra() = _formulario.update {
        FormularioSugerencia(tipo = it.tipo, tuNombre = it.tuNombre, tuContacto = it.tuContacto)
    }

    // ── Bandeja del admin ──

    fun cargarBandeja() {
        viewModelScope.launch { _sugerencias.value = repositorio.getSugerencias() }
    }

    fun cambiarEstado(id: String, estado: EstadoSugerencia) {
        viewModelScope.launch {
            repositorio.cambiarEstado(id, estado)
            _sugerencias.value = repositorio.getSugerencias()
        }
    }

    private companion object {
        val FormatoFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "MX"))
    }
}
