package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.net.Uri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mx.tec.familiasquesuman.ui.navigation.Rutas
import mx.tec.familiasquesuman.ui.screens.inscripcion.cuentaViewModel
import mx.tec.familiasquesuman.ui.state.AppViewModelProvider

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

fun NavGraphBuilder.grafoPerfil(nav: NavController, onEncuestaFinalizada: (() -> Unit)? = null) {
    composable(RutasPerfil.ENCUESTA_PREVIA) {
        // Instancia por destino y configuración propia: no comparte respuestas ni definición con P-22.
        val vm: EncuestaViewModel = viewModel(factory = viewModelFactory {
            initializer { EncuestaViewModel(EncuestaPreviaP33) }
        })
        val estado by vm.estado.collectAsStateWithLifecycle()
        EncuestaPreviaScreen(
            estado = estado,
            onVolver = { nav.popBackStack() },
            onSeleccionarRespuesta = vm::seleccionarRespuesta,
            // Preguntas 2/3 y destino posterior pendientes; no se inventa una transición.
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
            // Sin persistencia: salir no registra un recordatorio ni una encuesta enviada.
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
        PerfilScreen(
            estado = estado,
            onReintentar = vm::reintentar,
            onFavoritosClick = { nav.navigate(RutasPerfil.FAVORITOS) { launchSingleTop = true } },
            onInsigniasClick = { nav.navigate(RutasPerfil.INSIGNIAS) { launchSingleTop = true } },
            onMisActividadesClick = { nav.navigate(Rutas.MIS_ACTIVIDADES) { launchSingleTop = true } },
            accesosDisponibles = true,
            favoritosDisponibles = true
        )
    }
    composable(RutasPerfil.FAVORITOS) {
        val vm: FavoritosViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        FavoritosScreen(estado = estado, onVolver = { nav.popBackStack() }, onReintentar = vm::reintentar)
    }
    composable(RutasPerfil.INSIGNIAS) {
        val vm: InsigniasViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        InsigniasScreen(estado = estado, onVolver = { nav.popBackStack() }, onReintentar = vm::reintentar)
    }
}
