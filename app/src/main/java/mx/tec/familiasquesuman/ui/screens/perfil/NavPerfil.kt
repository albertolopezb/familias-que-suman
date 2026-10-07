package mx.tec.familiasquesuman.ui.screens.perfil

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import mx.tec.familiasquesuman.domain.MomentoEncuesta
import mx.tec.familiasquesuman.domain.Participacion
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.ui.state.familiasApplication
import mx.tec.familiasquesuman.domain.Usuario
import mx.tec.familiasquesuman.domain.UsuariosHardcodeados
import mx.tec.familiasquesuman.ui.navigation.Rutas
import mx.tec.familiasquesuman.ui.screens.actividades.RutasActividades
import mx.tec.familiasquesuman.ui.screens.campanas.RutasCampanas
import mx.tec.familiasquesuman.ui.screens.inicio.RutasInicio
import mx.tec.familiasquesuman.ui.screens.inscripcion.cuentaViewModel
import mx.tec.familiasquesuman.ui.state.AppViewModelProvider
import mx.tec.familiasquesuman.domain.TipoSugerencia
import mx.tec.familiasquesuman.ui.screens.sugerencias.RutasSugerencias
import mx.tec.familiasquesuman.ui.screens.sugerencias.sugerir
import mx.tec.familiasquesuman.ui.state.UiState

object RutasPerfil {
    const val FAVORITOS = "perfil/favoritos"
    const val INSIGNIAS = "perfil/insignias"
    /** El argumento es el id de la participación en el historial (RF-11). */
    const val TESTIMONIO = "perfil/testimonio/{actividadId}"
    /** Encuesta de después (RF-13): el argumento es el id de la participación. */
    const val ENCUESTA_FINAL = "perfil/encuesta_final/{actividadId}?titulo={titulo}"
    /** Encuesta de antes (RF-13): el argumento es el id de la actividad a la que se inscribió la familia. */
    const val ENCUESTA_PREVIA = "perfil/encuesta_previa/{actividadId}?titulo={titulo}"
    const val RESPUESTAS = "perfil/respuestas?actividadId={actividadId}"
    const val AVISO_PRIVACIDAD = "perfil/aviso_privacidad"
    const val AJUSTES = "perfil/ajustes"
    fun testimonio(participacionId: String) = "perfil/testimonio/${Uri.encode(participacionId)}"
    fun encuestaFinal(participacionId: String, titulo: String) =
        "perfil/encuesta_final/${Uri.encode(participacionId)}?titulo=${Uri.encode(titulo)}"
    fun encuestaPrevia(actividadId: String, titulo: String) =
        "perfil/encuesta_previa/${Uri.encode(actividadId)}?titulo=${Uri.encode(titulo)}"
    /** Sin [actividadId] muestra todas las respuestas de la cuenta (o de todas, para el admin). */
    fun respuestas(actividadId: String = "") = "perfil/respuestas?actividadId=${Uri.encode(actividadId)}"
}

/** Lleva a la encuesta de antes de una actividad a la que la familia ya se inscribió. */
fun NavController.responderEncuestaPrevia(actividadId: String, titulo: String) =
    navigate(RutasPerfil.encuestaPrevia(actividadId, titulo)) { launchSingleTop = true }

fun NavController.responderEncuestaPrevia(actividad: ActividadConAsociacion) =
    responderEncuestaPrevia(actividad.actividad.id, actividad.actividad.titulo)

/** Lleva a la encuesta de después de una actividad en la que la familia ya participó. */
fun NavController.responderEncuestaFinal(participacion: Participacion) =
    navigate(RutasPerfil.encuestaFinal(participacion.id, participacion.tituloActividad)) { launchSingleTop = true }

/** Lleva a compartir (o a consultar) el testimonio de una participación. */
fun NavController.compartirTestimonio(participacion: Participacion) =
    navigate(RutasPerfil.testimonio(participacion.id)) { launchSingleTop = true }

private val argumentosEncuesta = listOf(
    navArgument("actividadId") { type = NavType.StringType },
    navArgument("titulo") { type = NavType.StringType; defaultValue = "" }
)

/**
 * Una encuesta (RF-13) contestada de una pregunta a la vez. Al terminar se guarda ligada a la
 * actividad y se regresa; la flecha de la barra y el botón atrás vuelven a la pregunta anterior.
 */
@Composable
private fun EncuestaDestino(
    nav: NavController,
    entrada: NavBackStackEntry,
    definicion: DefinicionEncuesta,
    momento: MomentoEncuesta
) {
    val actividadId = entrada.arguments?.getString("actividadId").orEmpty()
    val titulo = entrada.arguments?.getString("titulo").orEmpty()
    val vm: EncuestaViewModel = viewModel(factory = viewModelFactory {
        initializer { EncuestaViewModel(definicion, momento, familiasApplication().container.encuestaRepository) }
    })
    val estado by vm.estado.collectAsStateWithLifecycle()
    val sesion by cuentaViewModel().sesion.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    val volver: () -> Unit = { if (!vm.retroceder()) nav.popBackStack() }
    BackHandler(enabled = estado.indiceActual > 0) { vm.retroceder() }
    val siguiente: () -> Unit = {
        if (vm.avanzar()) {
            sesion?.let { s ->
                vm.guardar(actividadId, titulo, s.correo, s.familia) {
                    Toast.makeText(contexto, "¡Gracias! Tus respuestas quedaron guardadas.", Toast.LENGTH_SHORT).show()
                    nav.popBackStack()
                }
            }
        }
    }
    if (momento == MomentoEncuesta.ANTES) {
        EncuestaPreviaScreen(
            estado = estado, onVolver = volver, onSeleccionarRespuesta = vm::seleccionarRespuesta,
            onSiguiente = siguiente, contextoActividad = titulo.ifBlank { null }
        )
    } else {
        EncuestaScreen(
            estado = estado, onVolver = volver, onSeleccionarRespuesta = vm::seleccionarRespuesta,
            onSiguiente = siguiente, onResponderDespues = { nav.popBackStack() },
            contextoActividad = titulo.ifBlank { null }
        )
    }
}

fun NavGraphBuilder.grafoPerfil(
    nav: NavController,
    usuarioActual: () -> Usuario = { UsuariosHardcodeados.USUARIO_NORMAL },
    onSwitchCuenta: (Boolean) -> Unit
) {
    composable(RutasPerfil.ENCUESTA_PREVIA, arguments = argumentosEncuesta) { entrada ->
        EncuestaDestino(nav, entrada, EncuestaPrevia, MomentoEncuesta.ANTES)
    }
    composable(RutasPerfil.AJUSTES) {
        val vm: AjustesViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val ciudad by vm.ciudad.collectAsStateWithLifecycle()
        val preferencias by vm.preferencias.collectAsStateWithLifecycle()
        AjustesScreen(
            ciudad = ciudad, preferencias = preferencias,
            onVolver = { nav.popBackStack() }, onReintentar = vm::reintentar,
            onCambiarCiudad = vm::solicitarCambioCiudad,
            onRecordatorioChange = vm::cambiarRecordatorio,
            onAvisosFavoritosChange = vm::cambiarAvisosFavoritos,
            onUrgenciasCiudadChange = vm::cambiarUrgenciasCiudad,
            onAvisoPrivacidad = { nav.navigate(RutasPerfil.AVISO_PRIVACIDAD) { launchSingleTop = true } },
            onCerrarSesion = cuentaViewModel()::cerrarSesion
        )
    }
    composable(RutasPerfil.AVISO_PRIVACIDAD) {
        val vm: AvisoPrivacidadViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        val sesion by cuentaViewModel().sesion.collectAsStateWithLifecycle()
        val correo = sesion?.correo
        LaunchedEffect(correo) { vm.cargar(correo) }
        AvisoPrivacidadScreen(
            estado = estado,
            conSesion = correo != null,
            onVolver = { nav.popBackStack() },
            onSolicitarEliminacion = vm::pedirConfirmacion,
            onConfirmarEliminacion = { if (correo != null) vm.confirmarEliminacion(correo) else vm.cancelarConfirmacion() },
            onCancelarConfirmacion = vm::cancelarConfirmacion
        )
    }
    composable(RutasPerfil.ENCUESTA_FINAL, arguments = argumentosEncuesta) { entrada ->
        EncuestaDestino(nav, entrada, EncuestaFinal, MomentoEncuesta.DESPUES)
    }
    composable(RutasPerfil.TESTIMONIO) { entrada ->
        val id = entrada.arguments?.getString("actividadId").orEmpty()
        val vm: TestimonioViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val participacion by vm.participacion.collectAsStateWithLifecycle()
        val formulario by vm.formulario.collectAsStateWithLifecycle()
        val existente by vm.existente.collectAsStateWithLifecycle()
        val sesion by cuentaViewModel().sesion.collectAsStateWithLifecycle()
        val correo = sesion?.correo
        LaunchedEffect(id, correo) { vm.cargar(id, correo) }
        val selector = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia(), vm::seleccionarFoto)
        TestimonioScreen(
            actividad = participacion, formulario = formulario, existente = existente,
            onVolver = { nav.popBackStack() }, onReintentar = { vm.cargar(id, correo) },
            onExperienciaChange = vm::cambiarExperiencia,
            onAgregarFoto = { selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onEnviar = { sesion?.let { vm.publicar(id, it.correo, it.familia) } },
            onEliminar = { sesion?.let { s -> vm.eliminar(s.correo) { nav.popBackStack() } } }
        )
    }
    composable(
        RutasPerfil.RESPUESTAS,
        arguments = listOf(navArgument("actividadId") { type = NavType.StringType; defaultValue = "" })
    ) { entrada ->
        val actividadId = entrada.arguments?.getString("actividadId").orEmpty()
        val vm: RespuestasEncuestasViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val respuestas by vm.respuestas.collectAsStateWithLifecycle()
        val sesion by cuentaViewModel().sesion.collectAsStateWithLifecycle()
        val esAdmin = usuarioActual().esAdmin
        val correo = sesion?.correo
        LaunchedEffect(esAdmin, correo, actividadId) { vm.cargar(if (esAdmin) null else correo, actividadId) }
        RespuestasEncuestasScreen(respuestas = respuestas, mostrarFamilia = esAdmin, onRegresar = { nav.popBackStack() })
    }
    composable(Rutas.PERFIL) {
        val vm: PerfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()

        // 1. Obtenemos la sesión activa de CuentaViewModel
        val cuentaVm = cuentaViewModel()
        val sesionActual by cuentaVm.sesion.collectAsStateWithLifecycle()

        // 2. Determinamos si es Admin o Usuario Normal según el correo en sesión
        val base = UsuariosHardcodeados.obtenerPorCorreo(sesionActual?.correo)
        // El nombre y el correo son los de la cuenta con la que se entró, no los de ejemplo.
        val usuarioCalculado = sesionActual?.let { base.copy(correo = it.correo, nombreFamilia = it.familia) } ?: base

        PerfilScreen(
            estado = estado,
            usuarioActual = usuarioCalculado, // ← Usamos el usuario calculado en tiempo real
            onReintentar = vm::reintentar,
            onFavoritosClick = { nav.navigate(RutasPerfil.FAVORITOS) { launchSingleTop = true } },
            onInsigniasClick = { nav.navigate(RutasPerfil.INSIGNIAS) { launchSingleTop = true } },
            onMisActividadesClick = { nav.navigate(Rutas.MIS_ACTIVIDADES) { launchSingleTop = true } },
            onSwitchCuenta = onSwitchCuenta,
            accesosDisponibles = true,
            favoritosDisponibles = true,
            onSugerirClick = { nav.sugerir(TipoSugerencia.ACTIVIDAD) },
            onSugerenciasClick = { nav.navigate(RutasSugerencias.BANDEJA) { launchSingleTop = true } },
            onTestimoniosClick = { nav.navigate(RutasInicio.TESTIMONIOS) { launchSingleTop = true } },
            onRespuestasClick = { nav.navigate(RutasPerfil.respuestas()) { launchSingleTop = true } },
            onRegresar = { nav.popBackStack() }
        )
    }
    composable(RutasPerfil.FAVORITOS) {
        val cuenta = cuentaViewModel()
        val asociaciones by cuenta.asociaciones.collectAsStateWithLifecycle()
        val actividades by cuenta.actividades.collectAsStateWithLifecycle()
        val campanas by cuenta.campanas.collectAsStateWithLifecycle()
        val favoritas by cuenta.favoritas.collectAsStateWithLifecycle()
        val items = asociaciones.filter { it.id in favoritas }
            .map { ItemFavorito(it.id, TipoFavorito.ASOCIACION, it.nombre, it.categoria) } +
            actividades.filter { it.id in favoritas }
                .map { ItemFavorito(it.id, TipoFavorito.ACTIVIDAD, it.titulo, "Actividad · ${it.fecha}") } +
            campanas.filter { it.id in favoritas }
                .map { ItemFavorito(it.id, TipoFavorito.CAMPANA, it.titulo, "Campaña · ${it.categoria.ifBlank { "Donación" }}") }
        FavoritosScreen(
            estado = UiState.Exito(items),
            onVolver = { nav.popBackStack() },
            onReintentar = {},
            onAbrir = { item ->
                nav.navigate(
                    when (item.tipo) {
                        TipoFavorito.ASOCIACION -> RutasInicio.asociacion(item.id)
                        TipoFavorito.ACTIVIDAD -> RutasActividades.detalle(item.id)
                        TipoFavorito.CAMPANA -> RutasCampanas.detalle(item.id)
                    }
                )
            },
            onQuitar = { item -> cuenta.alternarFavorita(item.id) }
        )
    }
    composable(RutasPerfil.INSIGNIAS) {
        val vm: InsigniasViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        InsigniasScreen(estado = estado, onVolver = { nav.popBackStack() }, onReintentar = vm::reintentar)
    }
}