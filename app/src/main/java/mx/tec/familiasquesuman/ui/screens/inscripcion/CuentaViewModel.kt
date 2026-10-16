package mx.tec.familiasquesuman.ui.screens.inscripcion

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.data.AvisosStore
import mx.tec.familiasquesuman.data.CampanaRepository
import mx.tec.familiasquesuman.data.CuentaRepository
import mx.tec.familiasquesuman.data.CuentasStore
import mx.tec.familiasquesuman.data.FavoritosStore
import mx.tec.familiasquesuman.data.Fotos
import mx.tec.familiasquesuman.data.PerfilLocalStore
import mx.tec.familiasquesuman.data.PreferenciasStore
import mx.tec.familiasquesuman.data.red.ApiExcepcion
import mx.tec.familiasquesuman.data.red.YoDto
import mx.tec.familiasquesuman.data.red.aAcompanante
import mx.tec.familiasquesuman.data.sesion.ServicioSesion
import mx.tec.familiasquesuman.domain.Acompanante
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.Aviso
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.domain.QueAvisar
import mx.tec.familiasquesuman.domain.Usuario
import mx.tec.familiasquesuman.domain.avisoBienvenida
import mx.tec.familiasquesuman.domain.avisoCancelacion
import mx.tec.familiasquesuman.domain.avisoInscripcion
import mx.tec.familiasquesuman.domain.avisosDelDia
import mx.tec.familiasquesuman.domain.juntarAvisos
import java.time.LocalDateTime

/**
 * Quién tiene la sesión abierta, tal como lo dice el servidor (/v1/yo). [esAdmin] sale de los
 * permisos que la API firmó dentro del token: la app solo lo usa para enseñar botones.
 */
data class Sesion(
    val familia: String,
    val correo: String,
    val titular: Acompanante,
    val esAdmin: Boolean = false,
    val ciudad: String = "Monterrey",
    val id: String = ""
)

/** Lo que ven las pantallas como "usuario actual". Sin sesión, nadie es admin. */
fun Sesion?.comoUsuario(): Usuario =
    this?.let { Usuario(correo = it.correo, esAdmin = it.esAdmin, nombreFamilia = it.familia, ciudad = it.ciudad) } ?: Usuario.INVITADO

/** Lo que P-05b necesita para pintar en rojo. `null` = ese campo está bien o sigue vacío. */
data class EstadoValidacion(
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorServidor: Boolean = false
)

/**
 * P-05 · Crear cuenta. La contraseña viaja una sola vez a la API (por HTTPS), que guarda solo su
 * hash Argon2id; en el teléfono no se guarda. El aviso de privacidad aceptado se guarda en el
 * servidor con su fecha.
 */
data class CrearCuentaUi(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val aceptaAviso: Boolean = false,
    val enviando: Boolean = false,
    val errorServidor: Boolean = false,
    val mensaje: String? = null,
    /** "Ese correo ya tiene cuenta": lo dice el servidor, no la validación en vivo. */
    val correoOcupado: Boolean = false,
    /** Lo que el servidor dijo de la contraseña (por ejemplo, que es demasiado fácil). */
    val errorContrasenaServidor: String? = null,
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
            errorContrasena = errorContrasenaServidor
                ?: if (contrasena.isNotEmpty() && contrasena.length < 8) "Usa mínimo 8 caracteres" else null,
            errorServidor = errorServidor
        )

    /** El botón se prende solo con los tres campos válidos y la casilla marcada. */
    val puedeEnviar: Boolean
        get() = !enviando && !correoOcupado && nombre.trim().length >= 3 &&
            esCorreoValido(correo) && contrasena.length >= 8 && aceptaAviso
}

/** P-26 · Iniciar sesión con correo y contraseña. */
data class IniciarSesionUi(
    val correo: String = "",
    val contrasena: String = "",
    val enviando: Boolean = false,
    val error: String? = null,
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
    val antes: Int? = null,
    val error: String? = null
)

/**
 * La cuenta: sesión, formularios, a dónde regresar, inscripciones, favoritos y avisos.
 *
 * Vive a nivel Activity (ver `cuentaViewModel()` en NavInscripcion) para que todas las pantallas
 * vean la misma sesión. Lo que manda es el SERVIDOR: aquí se guarda una copia para pintar rápido
 * y para el RevisarAvisosWorker, y se vuelve a pedir al abrir la app.
 */
class CuentaViewModel(
    private val actividadRepository: ActividadRepository,
    private val campanaRepository: CampanaRepository,
    private val cuentaRepository: CuentaRepository,
    private val servicioSesion: ServicioSesion,
    private val favoritosStore: FavoritosStore,
    private val perfilLocal: PerfilLocalStore? = null,
    private val cuentasStore: CuentasStore? = null,
    private val avisosStore: AvisosStore? = null,
    private val preferenciasStore: PreferenciasStore? = null
) : ViewModel() {

    private val _sesion = MutableStateFlow<Sesion?>(null)
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    /** Los acompañantes que la cuenta ya registró alguna vez. */
    private val _acompanantes = MutableStateFlow<List<Acompanante>>(emptyList())
    val acompanantes: StateFlow<List<Acompanante>> = _acompanantes.asStateFlow()

    /** actividadId → acompañantes que van (sin contar al titular). */
    private val _inscripciones = MutableStateFlow<Map<String, List<Acompanante>>>(emptyMap())
    val inscripciones: StateFlow<Map<String, List<Acompanante>>> = _inscripciones.asStateFlow()

    /** Ids de las asociaciones y actividades que la familia marcó con el corazón. */
    private val _favoritas = MutableStateFlow<Set<String>>(emptySet())
    val favoritas: StateFlow<Set<String>> = _favoritas.asStateFlow()

    private val _actividades = MutableStateFlow<List<Actividad>>(emptyList())
    val actividades: StateFlow<List<Actividad>> = _actividades.asStateFlow()

    private val _campanas = MutableStateFlow<List<Campana>>(emptyList())
    val campanas: StateFlow<List<Campana>> = _campanas.asStateFlow()

    /** Ruta de la copia local de la foto de perfil; null = sin foto (se ven las iniciales). */
    private val _foto = MutableStateFlow<String?>(null)
    val foto: StateFlow<String?> = _foto.asStateFlow()

    /** Cuántas veces la cuenta ha invitado a otras familias a una actividad (lo cuenta el servidor). */
    private val _invitaciones = MutableStateFlow(0)
    val invitaciones: StateFlow<Int> = _invitaciones.asStateFlow()

    /** El corazón que se tocó sin sesión: se pone solo al entrar. */
    private var favoritoPendiente: String? = null

    /** Todas las asociaciones, también las que solo organizan: para poner el nombre de quién organiza. */
    private val _asociaciones = MutableStateFlow<List<Asociacion>>(emptyList())
    val asociaciones: StateFlow<List<Asociacion>> = _asociaciones.asStateFlow()

    /**
     * Antes se llevaba la cuenta de los lugares que movía cada inscripción en el teléfono. Ahora
     * los lugares los cuenta el servidor y la lista se vuelve a pedir después de cada cambio,
     * así que no hay nada que ajustar. Se queda vacío por compatibilidad con las pantallas.
     */
    val ajusteLugares: StateFlow<Map<String, Int>> = MutableStateFlow<Map<String, Int>>(emptyMap()).asStateFlow()

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

    /** La bandeja de notificaciones de la cuenta con sesión, de la más nueva a la más vieja. */
    private val _avisos = MutableStateFlow<List<Aviso>>(emptyList())
    val avisos: StateFlow<List<Aviso>> = _avisos.asStateFlow()

    /** El número del círculo de perfil. */
    val sinLeer: StateFlow<Int> = _avisos
        .map { lista -> lista.count { !it.leido } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        viewModelScope.launch {
            cargarCatalogo()
            // Si quedó una sesión guardada (el token, cifrado con el Keystore), se retoma sin pedir contraseña.
            if (servicioSesion.hayCredenciales()) runCatching { refrescarCuenta() } else olvidarSesionLocal()
            revisarAvisos()
        }
        // Prender o apagar un aviso en Ajustes (o cambiar de ciudad) se nota en la bandeja y se
        // guarda también en el servidor.
        viewModelScope.launch {
            preferenciasStore?.preferencias?.drop(1)?.collect { prefs ->
                revisarAvisos()
                if (_sesion.value != null) runCatching {
                    cuentaRepository.guardarPreferencias(prefs.recordatorioActividades, prefs.avisosFavoritos, prefs.urgenciasCiudad)
                }
            }
        }
    }

    private suspend fun cargarCatalogo() {
        runCatching { _actividades.value = actividadRepository.getActividades() }
        runCatching { _asociaciones.value = actividadRepository.getTodasLasAsociaciones() }
        runCatching { _campanas.value = campanaRepository.getCampanas() }
    }

    /** El admin cambió el directorio, o cambió algún lugar: se vuelve a leer. */
    fun recargarCatalogo() {
        viewModelScope.launch {
            cargarCatalogo()
            revisarAvisos()
        }
    }

    fun actividad(id: String): Actividad? = _actividades.value.firstOrNull { it.id == id }
    fun asociacion(id: String): Asociacion? = _asociaciones.value.firstOrNull { it.id == id }

    /** Los lugares que dijo el servidor la última vez que se pidió la lista. */
    fun lugaresDisponibles(actividad: Actividad): Int =
        (actividad(actividad.id) ?: actividad).lugaresDisponibles.coerceIn(0, actividad.cupoTotal.coerceAtLeast(0))

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

    // ── La sesión ──

    /** Lo que se ve si algo sale mal al entrar. El texto lo da el servidor (ya viene en español). */
    private fun mensajeDe(e: Throwable): String = when (e) {
        is ApiExcepcion -> e.message
        else -> "No pudimos entrar. Intenta de nuevo."
    }

    /** Lee la cuenta del servidor y deja todo listo. Si el token ya no sirve, cierra la sesión local. */
    private suspend fun refrescarCuenta(): YoDto? = try {
        val yo = cuentaRepository.yo()
        abrirSesion(yo)
        yo
    } catch (e: CancellationException) {
        throw e
    } catch (e: ApiExcepcion) {
        if (e.estado == 401) {
            // El token venció, se cerró o la cuenta cambió de contraseña: se pide entrar otra vez.
            servicioSesion.cerrar()
            olvidarSesionLocal()
        }
        null    // sin conexión: se queda lo que había y se reintenta la próxima vez
    }

    private suspend fun abrirSesion(yo: YoDto) {
        val titular = yo.titular.aAcompanante()
        _sesion.value = Sesion(yo.nombreFamilia, yo.correo, titular, yo.esAdmin, yo.ciudad, yo.id)
        _invitaciones.value = yo.invitaciones
        cuentasStore?.guardarSesion(yo.correo)
        _avisos.value = avisosStore?.cargar(yo.correo).orEmpty()

        runCatching { _acompanantes.value = cuentaRepository.acompanantes() }
        runCatching {
            _inscripciones.value = cuentaRepository.inscripciones()
            cuentasStore?.guardarInscritas(yo.correo, _inscripciones.value.keys)
        }
        runCatching {
            var marcadas = cuentaRepository.favoritos()
            favoritoPendiente?.let { pendiente ->
                cuentaRepository.marcarFavorito(pendiente, true)
                marcadas = marcadas + pendiente
            }
            favoritoPendiente = null
            _favoritas.value = marcadas
            favoritosStore.guardar(yo.correo, marcadas)
        }
        _foto.value = if (!yo.tieneFoto) {
            perfilLocal?.quitarFoto(yo.correo)
            null
        } else {
            perfilLocal?.foto(yo.correo) ?: runCatching {
                val bytes = cuentaRepository.descargarFoto()
                withContext(Dispatchers.IO) { perfilLocal?.guardarFoto(yo.correo, bytes) }
            }.getOrNull()
        }
        revisarAvisos()
    }

    private fun olvidarSesionLocal() {
        _sesion.value?.correo?.let { correo ->
            cuentasStore?.olvidar(correo)
            perfilLocal?.quitarFoto(correo)
        }
        cuentasStore?.guardarSesion(null)
        _sesion.value = null
        _acompanantes.value = emptyList()
        _inscripciones.value = emptyMap()
        _favoritas.value = emptySet()
        _foto.value = null
        _invitaciones.value = 0
        _avisos.value = emptyList()
    }

    // ── P-05 · Crear cuenta ──

    fun onNombreChange(v: String) = _crear.update { it.copy(nombre = v, errorServidor = false, mensaje = null) }
    fun onCorreoChange(v: String) = _crear.update { it.copy(correo = v, errorServidor = false, mensaje = null, correoOcupado = false) }
    fun onContrasenaChange(v: String) = _crear.update { it.copy(contrasena = v, errorContrasenaServidor = null) }
    fun onAceptaAvisoChange(v: Boolean) = _crear.update { it.copy(aceptaAviso = v) }

    /** Crea la cuenta en la API (que ya responde el token) y deja la sesión abierta. */
    fun crearCuenta() {
        val f = _crear.value
        if (!f.puedeEnviar) return
        viewModelScope.launch {
            _crear.update { it.copy(enviando = true, errorServidor = false, mensaje = null) }
            try {
                servicioSesion.registrarse(f.correo, f.contrasena, f.nombre)
                if (refrescarCuenta() == null) throw ApiExcepcion(0, "sin_conexion", "Tu cuenta se creó, pero no pudimos leerla. Revisa tu conexión.")
                recibirAvisos(listOf(avisoBienvenida()), reemplazar = true)
                _crear.value = CrearCuentaUi(lista = true)       // la contraseña no se queda en memoria
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiExcepcion) {
                when {
                    e.codigo == "correo_ocupado" -> _crear.update { it.copy(enviando = false, correoOcupado = true) }
                    e.codigo == "contrasena_debil" || (e.codigo == "dato_invalido" && "contrasena" in e.message) ->
                        _crear.update { it.copy(enviando = false, errorContrasenaServidor = "Esa contraseña es muy fácil de adivinar. Usa una frase o mezcla palabras y números.") }
                    else -> _crear.update { it.copy(enviando = false, errorServidor = true, mensaje = e.message) }
                }
            } catch (e: Exception) {
                _crear.update { it.copy(enviando = false, errorServidor = true, mensaje = mensajeDe(e)) }
            }
        }
    }

    fun crearAtendida() = _crear.update { it.copy(lista = false) }

    // ── P-26 · Iniciar sesión ──

    fun onCorreoEntrarChange(v: String) = _entrar.update { it.copy(correo = v, error = null) }
    fun onContrasenaEntrarChange(v: String) = _entrar.update { it.copy(contrasena = v, error = null) }

    /** Correo y contraseña → la API responde el JWT. Si no coinciden, no se dice cuál de los dos falló. */
    fun iniciarSesion() {
        val f = _entrar.value
        if (!f.puedeEnviar) return
        viewModelScope.launch {
            _entrar.update { it.copy(enviando = true, error = null) }
            try {
                servicioSesion.iniciar(f.correo, f.contrasena)
                if (refrescarCuenta() == null) throw ApiExcepcion(0, "sin_conexion", "Entraste, pero no pudimos leer tu cuenta. Revisa tu conexión.")
                _entrar.value = IniciarSesionUi(lista = true)       // la contraseña no se queda en memoria
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _entrar.update { it.copy(enviando = false, contrasena = "", error = mensajeDe(e)) }
            }
        }
    }

    fun entrarAtendida() = _entrar.update { it.copy(lista = false) }

    /**
     * Cierra la sesión en los dos lados: el servidor deja de aceptar el token (aunque no haya
     * vencido) y el teléfono borra el token y la copia de los datos de la familia.
     */
    fun cerrarSesion() {
        viewModelScope.launch {
            cuentaRepository.cerrarSesion(servicioSesion.refreshActual())
            servicioSesion.cerrar()
            olvidarSesionLocal()
        }
    }

    // ── Foto de perfil e invitaciones ──

    /** Sube la foto elegida (reducida y sin metadatos) y deja una copia en el teléfono. */
    fun cambiarFoto(uri: Uri?) {
        val correo = _sesion.value?.correo ?: return
        if (uri == null) return // Cancelar el selector deja la foto como estaba.
        viewModelScope.launch {
            runCatching {
                val jpeg = withContext(Dispatchers.IO) {
                    val bytes = perfilLocal?.leer(uri) ?: error("No se pudo leer la foto")
                    Fotos.prepararParaSubir(bytes)
                }
                cuentaRepository.subirFoto(jpeg)
                withContext(Dispatchers.IO) { perfilLocal?.guardarFoto(correo, jpeg) }
            }.onSuccess { ruta -> if (ruta != null) _foto.value = ruta }
        }
    }

    fun quitarFoto() {
        val correo = _sesion.value?.correo ?: return
        viewModelScope.launch {
            runCatching { cuentaRepository.quitarFoto() }.onSuccess {
                perfilLocal?.quitarFoto(correo)
                _foto.value = null
            }
        }
    }

    /** La familia compartió una invitación a una actividad. */
    fun registrarInvitacion(actividadId: String) {
        if (_sesion.value == null) return
        viewModelScope.launch {
            runCatching { cuentaRepository.registrarInvitacion(actividadId) }.onSuccess { _invitaciones.value = it }
        }
    }

    /** Pone o quita el corazón. Se ve de inmediato y, si el servidor no lo guarda, se regresa. */
    fun alternarFavorita(id: String) {
        val correo = _sesion.value?.correo ?: return
        val marcar = id !in _favoritas.value
        _favoritas.update { if (marcar) it + id else it - id }
        favoritosStore.guardar(correo, _favoritas.value)
        revisarAvisos()
        viewModelScope.launch {
            runCatching { cuentaRepository.marcarFavorito(id, marcar) }.onFailure {
                _favoritas.update { actuales -> if (marcar) actuales - id else actuales + id }
                favoritosStore.guardar(correo, _favoritas.value)
            }
        }
    }

    /** Sin sesión el corazón pide entrar; al hacerlo, queda marcado. */
    fun recordarFavoritoPendiente(id: String) {
        favoritoPendiente = id
    }

    // ── P-27 · Recuperar ──

    fun precargarCorreoRecuperar() {
        val correo = _entrar.value.correo
        if (_recuperar.value.correo.isEmpty() && correo.isNotEmpty()) _recuperar.update { it.copy(correo = correo) }
    }

    fun onCorreoRecuperarChange(v: String) = _recuperar.update { it.copy(correo = v, enviado = false) }

    /**
     * Le avisa al equipo de Familias que Suman que esta familia necesita una contraseña nueva. Se
     * responde lo mismo exista o no el correo (y aunque falle): así nadie averigua quién tiene cuenta.
     */
    fun enviarEnlace() {
        val correo = _recuperar.value.correo
        if (!esCorreoValido(correo)) return
        viewModelScope.launch {
            _recuperar.update { it.copy(enviando = true) }
            runCatching { servicioSesion.recuperar(correo) }
            _recuperar.update { it.copy(enviando = false, enviado = true) }
        }
    }

    // ── Mi Perfil · Mi familia ──

    /** Registra a una persona de la familia. Mismo nombre = misma persona: se reemplaza. */
    fun agregarFamiliar(persona: Acompanante) {
        val nombre = persona.nombre.trim()
        _acompanantes.update { actuales ->
            actuales.filterNot { it.nombre.trim().equals(nombre, ignoreCase = true) } + persona.copy(nombre = nombre)
        }
        viewModelScope.launch {
            runCatching { cuentaRepository.agregarAcompanante(persona.copy(nombre = nombre)) }
            runCatching { _acompanantes.value = cuentaRepository.acompanantes() }
        }
    }

    /** Lo quita de la familia registrada. Las inscripciones que ya hizo no cambian. */
    fun quitarFamiliar(persona: Acompanante) {
        _acompanantes.update { it - persona }
        viewModelScope.launch {
            runCatching { cuentaRepository.quitarAcompanante(persona) }
            runCatching { _acompanantes.value = cuentaRepository.acompanantes() }
        }
    }

    // ── P-07 · Inscripciones ──

    /**
     * El servidor YA aceptó la inscripción (ver AcompanantesViewModel.confirmar): aquí solo se
     * pone al día lo que ve la app y se avisa.
     */
    fun registrarInscripcion(actividadId: String, acompanantes: List<Acompanante>) {
        _inscripciones.update { it + (actividadId to acompanantes) }
        _acompanantes.update { guardados ->
            guardados + acompanantes.filter { nuevo -> guardados.none { it.nombre.trim().equals(nuevo.nombre.trim(), ignoreCase = true) } }
        }
        _sesion.value?.correo?.let { cuentasStore?.guardarInscritas(it, _inscripciones.value.keys) }
        actividad(actividadId)?.let { recibirAvisos(listOf(avisoInscripcion(it, 1 + acompanantes.size)), reemplazar = true) }
        recargarCatalogo()
    }

    // ── P-20 → P-32 · Cancelar ──

    fun cancelarInscripcion(actividadId: String) {
        if (actividadId !in _inscripciones.value) return
        val actividad = actividad(actividadId) ?: return
        viewModelScope.launch {
            _cancelacion.value = CancelacionUi(actividadId)
            try {
                val r = cuentaRepository.cancelar(actividadId)
                _inscripciones.update { it - actividadId }
                _sesion.value?.correo?.let { cuentasStore?.guardarInscritas(it, _inscripciones.value.keys) }
                recibirAvisos(listOf(avisoCancelacion(actividad, r.liberados)), reemplazar = true)
                _cancelacion.value = CancelacionUi(actividadId, cancelando = false, liberados = r.liberados, antes = r.antes)
                recargarCatalogo()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _cancelacion.value = CancelacionUi(actividadId, cancelando = false, error = (e as? ApiExcepcion)?.message ?: "No se pudo cancelar.")
            }
        }
    }

    fun cancelacionAtendida() {
        _cancelacion.value = null
    }

    // ── Notificaciones ──

    /** Abrir la bandeja las da por vistas: el número del círculo de perfil se quita. */
    fun marcarAvisosLeidos() {
        if (_avisos.value.all { it.leido }) return
        _avisos.update { lista -> lista.map { it.copy(leido = true) } }
        guardarAvisos()
    }

    /** Revisa si hoy toca algún aviso nuevo: recordatorios, favoritos o urgencias, según Ajustes. */
    private fun revisarAvisos() {
        if (_sesion.value == null || _actividades.value.isEmpty()) return
        val prefs = preferenciasStore?.preferencias?.value
        val nuevos = avisosDelDia(
            ahora = LocalDateTime.now(),
            actividades = _actividades.value,
            campanas = _campanas.value,
            asociaciones = _asociaciones.value.associate { it.id to it.nombre },
            inscritas = _inscripciones.value.keys,
            favoritas = _favoritas.value,
            lugares = ::lugaresDisponibles,
            ciudad = prefs?.ciudad ?: "Monterrey",
            queAvisar = prefs?.let { QueAvisar(it.recordatorioActividades, it.avisosFavoritos, it.urgenciasCiudad) }
                ?: QueAvisar()
        )
        recibirAvisos(nuevos, reemplazar = false)
    }

    /**
     * Pone los avisos en la bandeja sin leer. Los que ya estaban no se repiten, salvo con
     * [reemplazar]: volver a inscribirse a la misma actividad vuelve a avisar.
     */
    private fun recibirAvisos(nuevos: List<Aviso>, reemplazar: Boolean) {
        val (bandeja, llegaron) = juntarAvisos(_avisos.value, nuevos, reemplazar, System.currentTimeMillis())
        if (llegaron.isEmpty()) return
        _avisos.value = bandeja
        guardarAvisos()
    }

    private fun guardarAvisos() {
        val correo = _sesion.value?.correo ?: return
        avisosStore?.guardar(correo, _avisos.value)
    }
}
