package mx.tec.familiasquesuman.ui.screens.perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
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
    const val TESTIMONIO = "perfil/testimonio/{actividadId}"
    const val ENCUESTA_FINAL = "perfil/encuesta_final"
    const val ENCUESTA_PREVIA = "perfil/encuesta_previa"
    const val AVISO_PRIVACIDAD = "perfil/aviso_privacidad"
    const val AJUSTES = "perfil/ajustes"
    fun testimonio(actividadId: String) = "perfil/testimonio/${Uri.encode(actividadId)}"
}

fun NavGraphBuilder.grafoPerfil(
    nav: NavController,
    usuarioActual: () -> Usuario = { UsuariosHardcodeados.USUARIO_NORMAL },
    onSwitchCuenta: (Boolean) -> Unit,
    onEncuestaFinalizada: (() -> Unit)? = null
) {
    composable(RutasPerfil.ENCUESTA_PREVIA) {
        val vm: EncuestaViewModel = viewModel(factory = viewModelFactory {
            initializer { EncuestaViewModel(EncuestaPreviaP33) }
        })
        val estado by vm.estado.collectAsStateWithLifecycle()
        EncuestaPreviaScreen(
            estado = estado,
            onVolver = { nav.popBackStack() },
            onSeleccionarRespuesta = vm::seleccionarRespuesta,
            onSiguiente = { vm.avanzar() }
        )
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
        AvisoPrivacidadScreen(
            estado = estado,
            onVolver = { nav.popBackStack() },
            onSolicitarEliminacion = vm::solicitarEliminacion
        )
    }
    composable(RutasPerfil.ENCUESTA_FINAL) {
        val vm: EncuestaViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        EncuestaScreen(
            estado = estado,
            onVolver = { nav.popBackStack() },
            onSeleccionarRespuesta = vm::seleccionarRespuesta,
            onSiguiente = {
                if (vm.avanzar()) {
                    if (onEncuestaFinalizada != null) onEncuestaFinalizada()
                    else nav.navigate(RutasPerfil.AVISO_PRIVACIDAD) { launchSingleTop = true }
                }
            },
            onResponderDespues = { nav.popBackStack() }
        )
    }
    composable(RutasPerfil.TESTIMONIO) { entrada ->
        val id = entrada.arguments?.getString("actividadId").orEmpty()
        val vm: TestimonioViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val actividad by vm.actividad.collectAsStateWithLifecycle()
        val formulario by vm.formulario.collectAsStateWithLifecycle()
        LaunchedEffect(id) { vm.cargarActividad(id) }
        val selector = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia(), vm::seleccionarFoto)
        TestimonioScreen(
            actividad = actividad, formulario = formulario,
            onVolver = { nav.popBackStack() }, onReintentar = { vm.cargarActividad(id) },
            onExperienciaChange = vm::cambiarExperiencia,
            onAgregarFoto = { selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onEnviar = vm::enviarARevision
        )
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
            onSugerenciasClick = { nav.navigate(RutasSugerencias.BANDEJA) { launchSingleTop = true } }
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