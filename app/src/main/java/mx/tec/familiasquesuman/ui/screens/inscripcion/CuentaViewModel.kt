package mx.tec.familiasquesuman.ui.screens.inscripcion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.data.AvisosStore
import mx.tec.familiasquesuman.data.PreferenciasStore
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.data.FavoritosStore
import mx.tec.familiasquesuman.data.CampanaRepository
import mx.tec.familiasquesuman.data.CuentaLocal
import mx.tec.familiasquesuman.data.CuentasStore
import mx.tec.familiasquesuman.data.cifrarContrasena
import mx.tec.familiasquesuman.data.contrasenaCoincide
import mx.tec.familiasquesuman.data.PerfilLocalStore
import mx.tec.familiasquesuman.data.PerfilRepository
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.tec.familiasquesuman.domain.Acompanante
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.Aviso
import mx.tec.familiasquesuman.domain.QueAvisar
import mx.tec.familiasquesuman.domain.avisoBienvenida
import mx.tec.familiasquesuman.domain.avisoCancelacion
import mx.tec.familiasquesuman.domain.avisoInscripcion
import mx.tec.familiasquesuman.domain.avisosDelDia
import mx.tec.familiasquesuman.domain.juntarAvisos
import mx.tec.familiasquesuman.domain.lugaresConAjuste
import java.time.LocalDateTime

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
 * pantallas vean la misma sesión. Las cuentas, la sesión, la familia y las inscripciones se
 * guardan en el teléfono ([CuentasStore]): al volver a abrir la app la sesión sigue abierta, y
 * una cuenta creada aquí puede volver a entrar después de cerrar sesión.
 */
class CuentaViewModel(
    private val actividadRepository: ActividadRepository,
    private val perfilRepository: PerfilRepository,
    private val campanaRepository: CampanaRepository,
    private val favoritosStore: FavoritosStore,
    private val perfilLocal: PerfilLocalStore? = null,
    private val cuentasStore: CuentasStore? = null,
    private val avisosStore: AvisosStore? = null,
    private val preferenciasStore: PreferenciasStore? = null
) : ViewModel() {

    private data class CuentaGuardada(
        /** La contraseña cifrada con [cifrarContrasena]. */
        val clave: String,
        val sesion: Sesion,
        val acompanantes: List<Acompanante>,
        val inscripciones: Map<String, List<Acompanante>>,
        val favoritas: Set<String> = emptySet()
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

    /** Ids de las asociaciones y actividades que la familia marcó con el corazón. */
    private val _favoritas = MutableStateFlow<Set<String>>(emptySet())
    val favoritas: StateFlow<Set<String>> = _favoritas.asStateFlow()

    private val _actividades = MutableStateFlow<List<Actividad>>(emptyList())
    val actividades: StateFlow<List<Actividad>> = _actividades.asStateFlow()

    private val _campanas = MutableStateFlow<List<Campana>>(emptyList())
    val campanas: StateFlow<List<Campana>> = _campanas.asStateFlow()

    /** Ruta de la foto de perfil de la cuenta con sesión; null = sin foto (se ven las iniciales). */
    private val _foto = MutableStateFlow<String?>(null)
    val foto: StateFlow<String?> = _foto.asStateFlow()

    /** Cuántas veces la cuenta ha invitado a otras familias a una actividad. */
    private val _invitaciones = MutableStateFlow(0)
    val invitaciones: StateFlow<Int> = _invitaciones.asStateFlow()

    /** El corazón que se tocó sin sesión: se pone solo al entrar. */
    private var favoritoPendiente: String? = null

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

    /** La bandeja de notificaciones de la cuenta con sesión, de la más nueva a la más vieja. */
    private val _avisos = MutableStateFlow<List<Aviso>>(emptyList())
    val avisos: StateFlow<List<Aviso>> = _avisos.asStateFlow()

    /** El número del círculo de perfil. */
    val sinLeer: StateFlow<Int> = _avisos
        .map { lista -> lista.count { !it.leido } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        // Lo guardado en el teléfono va primero y sin esperar: si quedó una sesión abierta, la app
        // abre ya con ella en lugar de enseñar un momento "Iniciar sesión".
        cuentasStore?.todas()?.forEach { cuentas[it.correo] = it.aGuardada() }
        _ajusteLugares.value = cuentasStore?.ajusteLugares().orEmpty()
        cuentasStore?.correoSesion()?.let { cuentas[it] }?.let { abrirSesion(it) }

        viewModelScope.launch {
            _actividades.value = actividadRepository.getActividades()
            _asociaciones.value = actividadRepository.getAsociaciones()
            _campanas.value = campanaRepository.getCampanas()
            val familia = perfilRepository.getFamilia()

            // Las cuentas de prueba solo se agregan si el teléfono no tiene ya su versión guardada
            // (con la familia y las inscripciones que se le hayan cambiado).
            // 1. Cuenta Normal Hardcodeada (Ana Rodríguez)
            if (familia.correo !in cuentas) cuentas[familia.correo] = CuentaGuardada(
                withContext(Dispatchers.Default) { cifrarContrasena(CuentaDePrueba.CONTRASENA) }, // "familia123"
                Sesion(familia.nombre, familia.correo, CuentaDePrueba.titular),
                CuentaDePrueba.acompanantes,
                CuentaDePrueba.inscripciones,
                favoritosStore.cargar(familia.correo) ?: perfilRepository.getFavoritas().map { it.id }.toSet()
            )

            // 2. Cuenta de Administrador Hardcodeada
            val correoAdmin = "admin@correo.com"
            if (correoAdmin !in cuentas) cuentas[correoAdmin] = CuentaGuardada(
                clave = withContext(Dispatchers.Default) { cifrarContrasena("admin123") }, // Contraseña para la cuenta de admin
                sesion = Sesion(
                    familia = "Administrador General",
                    correo = correoAdmin,
                    titular = Acompanante("Administrador General", edad = 30)
                ),
                acompanantes = emptyList(),
                inscripciones = emptyMap(),
                favoritas = emptySet()
            )

            // Con el catálogo ya cargado se ve si hoy toca algún aviso.
            revisarAvisos()
        }
        // Prender o apagar un aviso en Ajustes (o cambiar de ciudad) se nota en la bandeja.
        viewModelScope.launch { preferenciasStore?.preferencias?.collect { revisarAvisos() } }
    }

    /** El admin cambió el directorio: se vuelve a leer para que Inicio y Favoritos no queden viejos. */
    fun recargarCatalogo() {
        viewModelScope.launch {
            _actividades.value = actividadRepository.getActividades()
            _asociaciones.value = actividadRepository.getAsociaciones()
            _campanas.value = campanaRepository.getCampanas()
            revisarAvisos()
        }
    }

    fun actividad(id: String): Actividad? = _actividades.value.firstOrNull { it.id == id }
    fun asociacion(id: String): Asociacion? = _asociaciones.value.firstOrNull { it.id == id }

    /** Lo que dice el repositorio más lo que cambió en esta sesión. */
    fun lugaresDisponibles(actividad: Actividad): Int = lugaresConAjuste(actividad, _ajusteLugares.value)

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
                    val clave = withContext(Dispatchers.Default) { cifrarContrasena(f.contrasena) }
                    cuentas[correo] = CuentaGuardada(clave, sesion, emptyList(), emptyMap())
                    abrirSesion(cuentas.getValue(correo))
                    recibirAvisos(listOf(avisoBienvenida()), reemplazar = true)
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
            val coincide = cuenta != null &&
                withContext(Dispatchers.Default) { contrasenaCoincide(f.contrasena, cuenta.clave) }
            if (cuenta == null || !coincide) {
                _entrar.update { it.copy(enviando = false, error = true) }
            } else {
                abrirSesion(cuenta)
                _entrar.value = IniciarSesionUi(lista = true)
            }
        }
    }

    fun entrarAtendida() = _entrar.update { it.copy(lista = false) }

    /** Cierra la sesión. La cuenta y lo que tiene se quedan guardados para la próxima vez que entre. */
    fun cerrarSesion() {
        guardarCuentaActual()
        cuentasStore?.guardarSesion(null)
        _sesion.value = null
        _acompanantes.value = emptyList()
        _inscripciones.value = emptyMap()
        _favoritas.value = emptySet()
        _foto.value = null
        _invitaciones.value = 0
        _avisos.value = emptyList()
    }

    private fun abrirSesion(cuenta: CuentaGuardada) {
        _sesion.value = cuenta.sesion
        _acompanantes.value = cuenta.acompanantes
        _inscripciones.value = cuenta.inscripciones
        val guardadas = favoritosStore.cargar(cuenta.sesion.correo) ?: cuenta.favoritas
        val pendiente = favoritoPendiente
        favoritoPendiente = null
        _favoritas.value = if (pendiente != null) guardadas + pendiente else guardadas
        // También los de ejemplo: así siguen ahí cuando la sesión se abre sola al volver a la app.
        favoritosStore.guardar(cuenta.sesion.correo, _favoritas.value)
        _foto.value = perfilLocal?.foto(cuenta.sesion.correo)
        _invitaciones.value = perfilLocal?.invitaciones(cuenta.sesion.correo) ?: 0
        cuentasStore?.guardarSesion(cuenta.sesion.correo)
        guardarCuentaActual()
        _avisos.value = avisosStore?.cargar(cuenta.sesion.correo).orEmpty()
        revisarAvisos()
    }

    /** Lo de la cuenta con sesión queda guardado en cuanto cambia, sin esperar a cerrar sesión. */
    private fun guardarCuentaActual() {
        val s = _sesion.value ?: return
        val cuenta = cuentas[s.correo]
            ?.copy(acompanantes = _acompanantes.value, inscripciones = _inscripciones.value, favoritas = _favoritas.value)
            ?: return
        cuentas[s.correo] = cuenta
        cuentasStore?.guardar(CuentaLocal(s.correo, s.familia, s.titular, cuenta.clave, cuenta.acompanantes, cuenta.inscripciones))
    }

    private fun guardarAjusteLugares() {
        cuentasStore?.guardarAjusteLugares(_ajusteLugares.value)
    }

    private fun CuentaLocal.aGuardada() = CuentaGuardada(clave, Sesion(familia, correo, titular), acompanantes, inscripciones)

    // ── Foto de perfil e invitaciones ──

    /** Guarda la foto elegida en el selector como foto de la cuenta. */
    fun cambiarFoto(uri: Uri?) {
        val correo = _sesion.value?.correo ?: return
        if (uri == null) return // Cancelar el selector deja la foto como estaba.
        viewModelScope.launch {
            val ruta = withContext(Dispatchers.IO) { perfilLocal?.guardarFoto(correo, uri) }
            if (ruta != null) _foto.value = ruta
        }
    }

    fun quitarFoto() {
        val correo = _sesion.value?.correo ?: return
        perfilLocal?.quitarFoto(correo)
        _foto.value = null
    }

    /** La familia compartió una invitación a una actividad. */
    fun registrarInvitacion() {
        val correo = _sesion.value?.correo ?: return
        _invitaciones.value = perfilLocal?.sumarInvitacion(correo) ?: (_invitaciones.value + 1)
    }

    /** Pone o quita el corazón. Solo con sesión: quien llama decide qué hacer si no la hay. */
    fun alternarFavorita(id: String) {
        val correo = _sesion.value?.correo ?: return
        _favoritas.update { if (id in it) it - id else it + id }
        favoritosStore.guardar(correo, _favoritas.value)
        guardarCuentaActual()
        revisarAvisos()
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

    /** Responde lo mismo exista o no el correo: así nadie averigua quién tiene cuenta. */
    fun enviarEnlace() {
        if (!esCorreoValido(_recuperar.value.correo)) return
        viewModelScope.launch {
            _recuperar.update { it.copy(enviando = true) }
            delay(800)
            _recuperar.update { it.copy(enviando = false, enviado = true) }
        }
    }

    // ── Mi Perfil · Mi familia ──

    /**
     * Registra a una persona de la familia. Desde ese momento aparece sola en P-06 al inscribirse
     * a cualquier actividad, y ahí se puede quitar a quien no va.
     */
    fun agregarFamiliar(persona: Acompanante) {
        _acompanantes.update { actuales ->
            val nombre = persona.nombre.trim()
            // Mismo nombre = misma persona: se reemplaza para no duplicarla.
            actuales.filterNot { it.nombre.trim().equals(nombre, ignoreCase = true) } + persona.copy(nombre = nombre)
        }
        guardarCuentaActual()
    }

    /** Lo quita de la familia registrada. Las inscripciones que ya hizo no cambian. */
    fun quitarFamiliar(persona: Acompanante) {
        _acompanantes.update { it - persona }
        guardarCuentaActual()
    }

    // ── P-07 · Inscripciones ──

    fun registrarInscripcion(actividadId: String, acompanantes: List<Acompanante>) {
        val anteriores = _inscripciones.value[actividadId]
        // Si ya estaban inscritos, sus lugares viejos se devuelven antes de tomar los nuevos.
        val cambio = -(1 + acompanantes.size) + (anteriores?.let { 1 + it.size } ?: 0)
        _inscripciones.update { it + (actividadId to acompanantes) }
        // Mismo nombre = misma persona, aunque su edad ya haya cambiado desde que se registró.
        _acompanantes.update { guardados ->
            guardados + acompanantes.filter { nuevo -> guardados.none { it.nombre.trim().equals(nuevo.nombre.trim(), ignoreCase = true) } }
        }
        _ajusteLugares.update { it + (actividadId to (it[actividadId] ?: 0) + cambio) }
        guardarAjusteLugares()
        guardarCuentaActual()
        actividad(actividadId)?.let { recibirAvisos(listOf(avisoInscripcion(it, 1 + acompanantes.size)), reemplazar = true) }
        revisarAvisos()
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
            guardarAjusteLugares()
            guardarCuentaActual()
            recibirAvisos(listOf(avisoCancelacion(actividad, liberados)), reemplazar = true)
            _cancelacion.value = CancelacionUi(actividadId, cancelando = false, liberados = liberados, antes = antes)
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
