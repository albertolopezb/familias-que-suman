package mx.tec.familiasquesuman.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.data.PerfilRepository
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Aportacion
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.EstadoParticipacion
import mx.tec.familiasquesuman.domain.TemaActividad

// ---------------------------------------------------------------------------
// Panel y lista de actividades
// ---------------------------------------------------------------------------

/** Los números del panel. Los de las bandejas son de muestra hasta que haya servidor. */
data class ResumenAdmin(
    val centrosPendientes: Int,
    val pendientesPorRevisar: Int,
    val actividadesProximas: Int,
    val personasInscritas: Int
)

class AdminActividadesViewModel(
    private val actividadRepository: ActividadRepository,
    private val perfilRepository: PerfilRepository
) : ViewModel() {

    /** Todas las actividades, próximas primero. Se actualiza sola al guardar o borrar. */
    val actividades: StateFlow<List<Actividad>> = actividadRepository.actividades
        .map { lista -> lista.sortedBy { it.yaPaso } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _resumen = MutableStateFlow(ResumenAdmin(0, 0, 0, 0))
    val resumen: StateFlow<ResumenAdmin> = _resumen.asStateFlow()

    init {
        viewModelScope.launch {
            actividadRepository.actividades.collect { lista ->
                val enRevision = perfilRepository.getHistorial()
                    .count { it.estado == EstadoParticipacion.TESTIMONIO_EN_REVISION }
                _resumen.value = ResumenAdmin(
                    // De muestra: el sitio los cuenta en sus bandejas de entrada.
                    centrosPendientes = CENTROS_PENDIENTES_DEMO,
                    pendientesPorRevisar = enRevision + MENSAJES_PENDIENTES_DEMO,
                    actividadesProximas = lista.count { !it.yaPaso },
                    personasInscritas = lista.filter { !it.yaPaso && it.tieneCupo }.sumOf { it.ocupados }
                )
            }
        }
    }

    fun eliminar(id: String) {
        viewModelScope.launch { actividadRepository.eliminarActividad(id) }
    }

    /** Como el botón "Duplicar" del sitio: misma actividad, nuevo id y sin inscritos. */
    fun duplicar(actividad: Actividad) {
        viewModelScope.launch {
            actividadRepository.guardarActividad(
                actividad.copy(
                    id = actividadRepository.nuevoIdDeActividad(),
                    titulo = "${actividad.titulo} (copia)",
                    lugaresDisponibles = actividad.cupoTotal,
                    yaPaso = false
                )
            )
        }
    }

    companion object {
        const val CENTROS_PENDIENTES_DEMO = 2
        const val MENSAJES_PENDIENTES_DEMO = 3
    }
}

// ---------------------------------------------------------------------------
// Formulario de actividad (crear y editar)
// ---------------------------------------------------------------------------

enum class TipoAportacion { NINGUNA, DINERO, ESPECIE }

/** Los campos del formulario del sitio, como texto mientras se escriben. */
data class FormularioActividad(
    val id: String? = null,          // null = actividad nueva
    val nombre: String = "",
    val fecha: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val direccion: String = "",
    val puntoDeEncuentro: String = "",
    val cupo: String = "",
    val ocupados: Int = 0,
    val ciudad: String = "",
    val tema: TemaActividad = TemaActividad.GENERAL,
    val tipoAportacion: TipoAportacion = TipoAportacion.NINGUNA,
    val monto: String = "",
    val detalleAportacion: String = "",
    val descripcionCorta: String = "",
    val descripcionLarga: String = "",
    val queHaremos: String = "",
    val queIncluye: String = "",
    val queLlevar: String = "",
    val recomendaciones: String = "",
    val organizadorId: String = "o1",
    val foto: String? = null,
    val yaPaso: Boolean = false,
    val intentoGuardar: Boolean = false
) {
    val errorNombre: String? get() = if (intentoGuardar && nombre.isBlank()) "Escribe el nombre." else null
    val errorCiudad: String? get() = if (intentoGuardar && ciudad.isBlank()) "Escribe la ciudad." else null
    val errorCupo: String?
        get() = when {
            !intentoGuardar || cupo.isBlank() -> null
            cupo.toIntOrNull() == null || cupo.toInt() < 0 -> "El cupo es un número de personas."
            cupo.toInt() in 1 until ocupados -> "Ya hay $ocupados personas inscritas."
            else -> null
        }
    val esValido: Boolean
        get() = nombre.isNotBlank() && ciudad.isNotBlank() &&
            (cupo.isBlank() || (cupo.toIntOrNull()?.let { it >= 0 && (it == 0 || it >= ocupados) } ?: false))
}

class FormularioActividadViewModel(
    private val actividadRepository: ActividadRepository
) : ViewModel() {

    private val _formulario = MutableStateFlow(FormularioActividad())
    val formulario: StateFlow<FormularioActividad> = _formulario.asStateFlow()

    private val _organizadores = MutableStateFlow<List<Asociacion>>(emptyList())
    val organizadores: StateFlow<List<Asociacion>> = _organizadores.asStateFlow()

    private var cargado: String? = null

    init {
        viewModelScope.launch { _organizadores.value = actividadRepository.getOrganizadores() }
    }

    /** `id` null abre el formulario vacío; con id, lo llena para editar. */
    fun cargar(id: String?) {
        val llave = id ?: "nueva"
        if (cargado == llave) return
        cargado = llave
        if (id == null) {
            _formulario.value = FormularioActividad()
            return
        }
        viewModelScope.launch {
            val a = runCatching { actividadRepository.getActividad(id) }.getOrNull() ?: return@launch
            _formulario.value = FormularioActividad(
                id = a.id,
                nombre = a.titulo,
                fecha = a.fecha,
                horaInicio = a.horario.substringBefore("–").trim(),
                horaFin = a.horario.substringAfter("–", "").trim(),
                direccion = a.direccion,
                puntoDeEncuentro = a.puntoDeEncuentro,
                cupo = if (a.tieneCupo) a.cupoTotal.toString() else "",
                ocupados = if (a.tieneCupo) a.ocupados else 0,
                ciudad = a.municipio,
                tema = a.tema,
                tipoAportacion = when (a.aportacion) {
                    Aportacion.Ninguna -> TipoAportacion.NINGUNA
                    is Aportacion.Monetaria -> TipoAportacion.DINERO
                    is Aportacion.EnEspecie -> TipoAportacion.ESPECIE
                },
                monto = (a.aportacion as? Aportacion.Monetaria)?.monto.orEmpty(),
                detalleAportacion = when (val ap = a.aportacion) {
                    is Aportacion.Monetaria -> ap.detalle
                    is Aportacion.EnEspecie -> ap.detalle
                    Aportacion.Ninguna -> ""
                },
                descripcionCorta = a.descripcion,
                descripcionLarga = a.acercaDelProyecto,
                queHaremos = a.queHaremos,
                queIncluye = a.queIncluye,
                queLlevar = a.queLlevar,
                recomendaciones = a.recomendaciones,
                organizadorId = a.asociacionId,
                foto = a.foto,
                yaPaso = a.yaPaso
            )
        }
    }

    fun actualizar(cambio: (FormularioActividad) -> FormularioActividad) {
        _formulario.update(cambio)
    }

    /** Devuelve true si guardó; si falta algo, marca los errores y no guarda. */
    fun guardar(onGuardado: () -> Unit) {
        val f = _formulario.value.copy(intentoGuardar = true)
        _formulario.value = f
        if (!f.esValido) return
        val cupo = f.cupo.toIntOrNull() ?: 0
        val horario = listOf(f.horaInicio.trim(), f.horaFin.trim()).filter { it.isNotEmpty() }
            .joinToString(" – ")
        val actividad = Actividad(
            id = f.id ?: actividadRepository.nuevoIdDeActividad(),
            titulo = f.nombre.trim(),
            asociacionId = f.organizadorId,
            fecha = f.fecha.trim(),
            horario = horario,
            direccion = f.direccion.trim(),
            edadMinima = null,
            descripcion = f.descripcionCorta.trim(),
            cupoTotal = cupo,
            lugaresDisponibles = (cupo - f.ocupados).coerceAtLeast(0),
            municipio = f.ciudad.trim(),
            tema = f.tema,
            aportacion = when (f.tipoAportacion) {
                TipoAportacion.NINGUNA -> Aportacion.Ninguna
                TipoAportacion.DINERO -> Aportacion.Monetaria(
                    monto = f.monto.trim().let { if (it.startsWith("$") || it.isEmpty()) it else "\$$it" },
                    detalle = f.detalleAportacion.trim()
                )
                TipoAportacion.ESPECIE -> Aportacion.EnEspecie(f.detalleAportacion.trim())
            },
            puntoDeEncuentro = f.puntoDeEncuentro.trim(),
            acercaDelProyecto = f.descripcionLarga.trim(),
            queHaremos = f.queHaremos.trim(),
            queIncluye = f.queIncluye.trim(),
            queLlevar = f.queLlevar.trim(),
            recomendaciones = f.recomendaciones.trim(),
            foto = f.foto,
            yaPaso = f.yaPaso
        )
        viewModelScope.launch {
            actividadRepository.guardarActividad(actividad)
            onGuardado()
        }
    }
}

// ---------------------------------------------------------------------------
// Acceso de administrador
// ---------------------------------------------------------------------------

data class FormularioAcceso(
    val correo: String = "",
    val contrasena: String = "",
    val error: String? = null
)

class AccesoAdminViewModel : ViewModel() {
    private val _formulario = MutableStateFlow(FormularioAcceso())
    val formulario: StateFlow<FormularioAcceso> = _formulario.asStateFlow()

    fun cambiarCorreo(valor: String) = _formulario.update { it.copy(correo = valor, error = null) }
    fun cambiarContrasena(valor: String) = _formulario.update { it.copy(contrasena = valor, error = null) }

    fun entrar(onEntrar: () -> Unit) {
        val f = _formulario.value
        if (SesionAdmin.entrar(f.correo, f.contrasena)) {
            _formulario.value = FormularioAcceso()
            onEntrar()
        } else {
            _formulario.update { it.copy(error = "Correo o contraseña incorrectos.") }
        }
    }
}
