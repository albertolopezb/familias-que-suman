package mx.tec.familiasquesuman.ui.screens.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mx.tec.familiasquesuman.ui.screens.actividades.RutasActividades
import mx.tec.familiasquesuman.ui.screens.admin.componentes.SinAcceso
import mx.tec.familiasquesuman.ui.state.AppViewModelProvider

object RutasAdmin {
    const val ACCESO = "admin/acceso"
    const val PANEL = "admin"
    const val ACTIVIDADES = "admin/actividades"
    const val NUEVA = "admin/actividades/nueva"
    const val EDITAR = "admin/actividades/{actividadId}/editar"
    const val INSCRITOS = "admin/inscritos"
    const val SECCION = "admin/seccion/{seccion}"

    fun editar(id: String) = "admin/actividades/$id/editar"
    fun seccion(s: SeccionAdmin) = "admin/seccion/${s.name}"

    /** Pantallas donde se esconde la barra inferior. */
    val sinBarra = setOf(ACCESO, NUEVA, EDITAR)
}

/**
 * El panel de administración. Igual que en el sitio, todo lo que cuelga de
 * "admin/" revisa el rol antes de mostrarse; sin él se ve "No tienes acceso".
 */
fun NavGraphBuilder.grafoAdmin(nav: NavController) {

    composable(RutasAdmin.ACCESO) {
        val vm: AccesoAdminViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val formulario by vm.formulario.collectAsStateWithLifecycle()
        AccesoAdminScreen(
            formulario = formulario,
            onCorreoChange = vm::cambiarCorreo,
            onContrasenaChange = vm::cambiarContrasena,
            onEntrar = {
                // Con cuenta de admin el menú ofrece cambiar a la vista de admin.
                vm.entrar { nav.popBackStack() }
            },
            onRegresar = { nav.popBackStack() }
        )
    }

    composable(RutasAdmin.PANEL) {
        SoloAdmin(nav) {
            val vm: AdminActividadesViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val resumen by vm.resumen.collectAsStateWithLifecycle()
            PanelAdminScreen(
                nombre = SesionAdmin.nombre,
                resumen = resumen,
                onSeccion = { seccion ->
                    when (seccion) {
                        SeccionAdmin.ACTIVIDADES -> nav.navigate(RutasAdmin.ACTIVIDADES)
                        SeccionAdmin.INSCRITOS -> nav.navigate(RutasAdmin.INSCRITOS)
                        else -> nav.navigate(RutasAdmin.seccion(seccion))
                    }
                },
                onSalir = {
                    SesionAdmin.cerrarSesion()
                    nav.popBackStack()
                },
                onRegresar = { nav.popBackStack() }
            )
        }
    }

    composable(RutasAdmin.ACTIVIDADES) {
        SoloAdmin(nav) {
            val vm: AdminActividadesViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val actividades by vm.actividades.collectAsStateWithLifecycle()
            AdminActividadesScreen(
                actividades = actividades,
                onNueva = { nav.navigate(RutasAdmin.NUEVA) },
                onEditar = { id -> nav.navigate(RutasAdmin.editar(id)) },
                onDuplicar = vm::duplicar,
                onEliminar = vm::eliminar,
                onRegresar = { nav.popBackStack() }
            )
        }
    }

    composable(RutasAdmin.NUEVA) {
        SoloAdmin(nav) { Formulario(nav, id = null) }
    }

    composable(RutasAdmin.EDITAR) { entrada ->
        val id = entrada.arguments?.getString("actividadId")
        SoloAdmin(nav) { Formulario(nav, id = id) }
    }

    composable(RutasAdmin.INSCRITOS) {
        SoloAdmin(nav) {
            val vm: AdminActividadesViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val actividades by vm.actividades.collectAsStateWithLifecycle()
            AdminInscritosScreen(
                actividades = actividades,
                onActividadClick = { id -> nav.navigate(RutasActividades.detalle(id)) },
                onRegresar = { nav.popBackStack() }
            )
        }
    }

    composable(RutasAdmin.SECCION) { entrada ->
        val seccion = entrada.arguments?.getString("seccion")
            ?.let { nombre -> SeccionAdmin.entries.firstOrNull { it.name == nombre } }
            ?: SeccionAdmin.METRICAS
        SoloAdmin(nav) {
            AdminPendienteScreen(seccion = seccion, onRegresar = { nav.popBackStack() })
        }
    }
}

@Composable
private fun Formulario(nav: NavController, id: String?) {
    val vm: FormularioActividadViewModel = viewModel(factory = AppViewModelProvider.Factory)
    LaunchedEffect(id) { vm.cargar(id) }
    val formulario by vm.formulario.collectAsStateWithLifecycle()
    val organizadores by vm.organizadores.collectAsStateWithLifecycle()
    FormularioActividadScreen(
        formulario = formulario,
        organizadores = organizadores,
        onCambio = vm::actualizar,
        onGuardar = { vm.guardar { nav.popBackStack() } },
        onCancelar = { nav.popBackStack() }
    )
}

/** Muestra el contenido solo si hay sesión de admin; si no, lo mismo que el sitio. */
@Composable
private fun SoloAdmin(nav: NavController, contenido: @Composable () -> Unit) {
    if (SesionAdmin.activa) {
        contenido()
    } else {
        SinAcceso(onIrAlAcceso = { nav.navigate(RutasAdmin.ACCESO) })
    }
}
