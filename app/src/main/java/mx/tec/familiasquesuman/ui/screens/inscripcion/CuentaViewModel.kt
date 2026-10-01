package mx.tec.familiasquesuman.ui.screens.inscripcion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.data.PerfilRepository
import mx.tec.familiasquesuman.domain.Acompanante
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Asociacion

/** Quién tiene la sesión abierta. En la etapa 2 sale del servidor. */
data class Sesion(val familia: String, val correo: String, val titular: Acompanante)

/** Lo que P-05b necesita para pintar en rojo. `null` = ese campo está bien o sigue vacío. */
data class EstadoValidacion(
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorServidor: Boolean = false
)

data class CrearCuentaUi(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val aceptaAviso: Boolean = false,
    val enviando: Boolean = false,
    val errorServidor: Boolean = false,
    /** "Ese correo ya tiene cuenta": lo dice el servidor, no la validación en vivo. */
    val correoOcupado: Boolean = false,
    val lista: Boolean = false
) {
    /** Validación en vivo: un campo vacío todavía no es error, uno mal escrito sí. */
    val validacion: EstadoValidacion
        get() = EstadoValidacion(
            errorNombre = if (nombre.isNotEmpty() && nombre.trim().length < 3) "Escribe el nombre de tu familia" else null,
            errorCorreo = when {
                correoOcupado -> "Ese correo ya tiene cuenta. Inicia sesión con él."
                correo.isNotEmpty() && !esCorreoValido(correo) -> "Escribe un correo válido, por ejemplo familia@correo.com"
                else -> null
            },
            errorContrasena = if (contrasena.isNotEmpty() && contrasena.length < 8) "Usa mínimo 8 caracteres" else null,
            errorServidor = errorServidor
        )

    /** El botón se prende solo con los tres campos válidos y la casilla marcada. */
    val puedeEnviar: Boolean
        get() = !enviando && !correoOcupado && nombre.trim().length >= 3 &&
            esCorreoValido(correo) && contrasena.length >= 8 && aceptaAviso
}

data class IniciarSesionUi(
    val correo: String = "",
    val contrasena: String = "",
    val enviando: Boolean = false,
    val error: Boolean = false,
    val lista: Boolean = false
) {
    val errorCorreo: String?
        get() = if (correo.isNotEmpty() && !esCorreoValido(correo)) "Escribe un correo válido, por ejemplo familia@correo.com" else null
    val puedeEnviar: Boolean get() = !enviando && esCorreoValido(correo) && contrasena.isNotEmpty()
}

data class RecuperarUi(val correo: String = "", val enviando: Boolean = false, val enviado: Boolean = false) {
    val errorCorreo: String?
        get() = if (correo.isNotEmpty() && !esCorreoValido(correo)) "Escribe un correo válido, por ejemplo familia@correo.com" else null
}

/** A qué inscripción regresar después de crear cuenta o entrar (P-04 → P-05/P-26 → P-06). */
data class Regreso(val actividadId: String, val simularSinCupo: Boolean)

data class CancelacionUi(
    val actividadId: String,
    val cancelando: Boolean = true,
    val liberados: Int? = null,
    val antes: Int? = null
)

/**
 * La cuenta mientras no hay servidor: sesión, formularios, a dónde regresar e inscripciones.
 *
 * Vive a nivel Activity (ver `cuentaViewModel()` en NavInscripcion) para que todas las
 * pantallas vean la misma sesión. Todo está en memoria: al cerrar la app se pierde, y eso
 * es lo esperado en esta etapa.
 */
class CuentaViewModel(
    private val actividadRepository: ActividadRepository,
    private val perfilRepository: PerfilRepository
) : ViewModel() {

    private data class CuentaGuardada(
        val contrasena: String,
        val sesion: Sesion,
        val acompanantes: List<Acompanante>,
        val inscripciones: Map<String, List<Acompanante>>
    )

    private val cuentas = mutableMapOf<String, CuentaGuardada>()

    private val _sesion = MutableStateFlow<Sesion?>(null)
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    /** Los acompañantes que la cuenta ya registró alguna vez. */
    private val _acompanantes = MutableStateFlow<List<Acompanante>>(emptyList())
    val acompanantes: StateFlow<List<Acompanante>> = _acompanantes.asStateFlow()

    /** actividadId → acompañantes que van (sin contar al titular). */
    private val _inscripciones = MutableStateFlow<Map<String, List<Acompanante>>>(emptyMap())
    val inscripciones: StateFlow<Map<String, List<Acompanante>>> = _inscripciones.asStateFlow()

    private val _actividades = MutableStateFlow<List<Actividad>>(emptyList())
    val actividades: StateFlow<List<Actividad>> = _actividades.asStateFlow()

    private val _asociaciones = MutableStateFlow<List<Asociacion>>(emptyList())
    val asociaciones: StateFlow<List<Asociacion>> = _asociaciones.asStateFlow()

    /** Cuántos lugares movieron las inscripciones y cancelaciones de esta sesión. */
    private val _ajusteLugares = MutableStateFlow<Map<String, Int>>(emptyMap())
    val ajusteLugares: StateFlow<Map<String, Int>> = _ajusteLugares.asStateFlow()

    private val _crear = MutableStateFlow(CrearCuentaUi())
    val crear: StateFlow<CrearCuentaUi> = _crear.asStateFlow()

    private val _entrar = MutableStateFlow(IniciarSesionUi())
    val entrar: StateFlow<IniciarSesionUi> = _entrar.asStateFlow()

    private val _recuperar = MutableStateFlow(RecuperarUi())
    val recuperar: StateFlow<RecuperarUi> = _recuperar.asStateFlow()

    private val _regreso = MutableStateFlow<Regreso?>(null)
    val regreso: StateFlow<Regreso?> = _regreso.asStateFlow()

    private val _cancelacion = MutableStateFlow<CancelacionUi?>(null)
    val cancelacion: StateFlow<CancelacionUi?> = _cancelacion.asStateFlow()

    init {
        viewModelScope.launch {
            _actividades.value = actividadRepository.getActividades()
            _asociaciones.value = actividadRepository.getAsociaciones()
            val familia = perfilRepository.getFamilia()
            cuentas[familia.correo] = CuentaGuardada(
                CuentaDePrueba.CONTRASENA,
                Sesion(familia.nombre, familia.correo, CuentaDePrueba.titular),
                CuentaDePrueba.acompanantes,
                CuentaDePrueba.inscripciones
            )
        }
    }

    fun actividad(id: String): Actividad? = _actividades.value.firstOrNull { it.id == id }
    fun asociacion(id: String): Asociacion? = _asociaciones.value.firstOrNull { it.id == id }

    /** Lo que dice el repositorio más lo que cambió en esta sesión. */
    fun lugaresDisponibles(actividad: Actividad): Int =
        (actividad.lugaresDisponibles + (_ajusteLugares.value[actividad.id] ?: 0)).coerceIn(0, actividad.cupoTotal)

    // ── A dónde regresar ──

    fun recordarRegreso(actividadId: String, simularSinCupo: Boolean) {
        _destinoPendiente = null
        _regreso.value = Regreso(actividadId, simularSinCupo)
    }

    /** Pantalla que pidió sesión (perfil, mis actividades...): se abre al entrar. */
    private var _destinoPendiente: String? = null

    fun recordarDestino(ruta: String) {
        _regreso.value = null
        _destinoPendiente = ruta
    }

    fun tomarDestino(): String? = _destinoPendiente.also { _destinoPendiente = null }

    /** Devuelve el regreso pendiente y lo olvida, para que no se use dos veces. */
    fun tomarRegreso(): Regreso? = _regreso.value.also { _regreso.value = null }

    // ── P-05 · Crear cuenta ──

    fun onNombreChange(v: String) = _crear.update { it.copy(nombre = v, errorServidor = false) }
    fun onCorreoChange(v: String) = _crear.update { it.copy(correo = v, correoOcupado = false, errorServidor = false) }
    fun onContrasenaChange(v: String) = _crear.update { it.copy(contrasena = v, errorServidor = false) }
    fun onAceptaAvisoChange(v: Boolean) = _crear.update { it.copy(aceptaAviso = v) }

    /** Para ver la franja roja de P-05b, usa un correo que empiece con "error" (error@correo.com). */
    fun crearCuenta() {
        val f = _crear.value
        if (!f.puedeEnviar) return
        viewModelScope.launch {
            _crear.update { it.copy(enviando = true, errorServidor = false) }
            delay(1000)
            val correo = f.correo.trim().lowercase()
            when {
                correo.startsWith("error") -> _crear.update { it.copy(enviando = false, errorServidor = true) }
                correo in cuentas -> _crear.update { it.copy(enviando = false, correoOcupado = true) }
                else -> {
                    val sesion = Sesion(f.nombre.trim(), correo, Acompanante(f.nombre.trim(), edad = 0))
                    cuentas[correo] = CuentaGuardada(f.contrasena, sesion, emptyList(), emptyMap())
                    abrirSesion(cuentas.getValue(correo))
                    _crear.value = CrearCuentaUi(lista = true)
                }
            }
        }
    }

    fun crearAtendida() = _crear.update { it.copy(lista = false) }

    // ── P-26 · Iniciar sesión ──

    fun onCorreoEntrarChange(v: String) = _entrar.update { it.copy(correo = v, error = false) }
    fun onContrasenaEntrarChange(v: String) = _entrar.update { it.copy(contrasena = v, error = false) }

    fun iniciarSesion() {
        val f = _entrar.value
        if (!f.puedeEnviar) return
        viewModelScope.launch {
            _entrar.update { it.copy(enviando = true, error = false) }
            delay(800)
            val cuenta = cuentas[f.correo.trim().lowercase()]
            if (cuenta == null || cuenta.contrasena != f.contrasena) {
                _entrar.update { it.copy(enviando = false, error = true) }
            } else {
                abrirSesion(cuenta)
                _entrar.value = IniciarSesionUi(lista = true)
            }
        }
    }

    fun entrarAtendida() = _entrar.update { it.copy(lista = false) }

    fun cerrarSesion() {
        _sesion.value?.let { s ->
            cuentas[s.correo]?.let { cuentas[s.correo] = it.copy(acompanantes = _acompanantes.value, inscripciones = _inscripciones.value) }
        }
        _sesion.value = null
        _acompanantes.value = emptyList()
        _inscripciones.value = emptyMap()
    }

    private fun abrirSesion(cuenta: CuentaGuardada) {
        _sesion.value = cuenta.sesion
        _acompanantes.value = cuenta.acompanantes
        _inscripciones.value = cuenta.inscripciones
    }

    // ── P-27 · Recuperar ──

    fun precargarCorreoRecuperar() {
        val correo = _entrar.value.correo
        if (_recuperar.value.correo.isEmpty() && correo.isNotEmpty()) _recuperar.update { it.copy(correo = correo) }
    }

    fun onCorreoRecuperarChange(v: String) = _recuperar.update { it.copy(correo = v, enviado = false) }

    /** Responde lo mismo exista o no el correo: así nadie averigua quién tiene cuenta. */
    fun enviarEnlace() {
        if (!esCorreoValido(_recuperar.value.correo)) return
        viewModelScope.launch {
            _recuperar.update { it.copy(enviando = true) }
            delay(800)
            _recuperar.update { it.copy(enviando = false, enviado = true) }
        }
    }

    // ── P-07 · Inscripciones ──

    fun registrarInscripcion(actividadId: String, acompanantes: List<Acompanante>) {
        val anteriores = _inscripciones.value[actividadId]
        // Si ya estaban inscritos, sus lugares viejos se devuelven antes de tomar los nuevos.
        val cambio = -(1 + acompanantes.size) + (anteriores?.let { 1 + it.size } ?: 0)
        _inscripciones.update { it + (actividadId to acompanantes) }
        _acompanantes.update { guardados -> (guardados + acompanantes).distinct() }
        _ajusteLugares.update { it + (actividadId to (it[actividadId] ?: 0) + cambio) }
    }

    // ── P-20 → P-32 · Cancelar ──

    fun cancelarInscripcion(actividadId: String) {
        val acompanantes = _inscripciones.value[actividadId] ?: return
        val actividad = actividad(actividadId) ?: return
        viewModelScope.launch {
            _cancelacion.value = CancelacionUi(actividadId)
            delay(800)
            val antes = lugaresDisponibles(actividad)
            val liberados = 1 + acompanantes.size
            _inscripciones.update { it - actividadId }
            _ajusteLugares.update { it + (actividadId to (it[actividadId] ?: 0) + liberados) }
            _cancelacion.value = CancelacionUi(actividadId, cancelando = false, liberados = liberados, antes = antes)
        }
    }

    fun cancelacionAtendida() {
        _cancelacion.value = null
    }
}
