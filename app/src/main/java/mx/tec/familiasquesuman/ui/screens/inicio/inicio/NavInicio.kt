package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.CentroVisiteo
import mx.tec.familiasquesuman.domain.Proyecto
import mx.tec.familiasquesuman.ui.components.CampoAdmin
import mx.tec.familiasquesuman.ui.components.DialogoConfirmarBorrado
import mx.tec.familiasquesuman.ui.components.DialogoFormularioAdmin
import mx.tec.familiasquesuman.ui.components.TipoCampo
import mx.tec.familiasquesuman.ui.screens.inscripcion.RutasInscripcion
import mx.tec.familiasquesuman.ui.screens.inscripcion.alternarFavorita
import mx.tec.familiasquesuman.ui.screens.inscripcion.cuentaViewModel
import mx.tec.familiasquesuman.ui.state.AppViewModelProvider

object RutasInicio {
    const val SPLASH = "splash"
    const val PERMISO_NOTIFICACIONES = "permiso_notificaciones"
    const val CIUDAD = "ciudad"
    const val INICIO = "inicio_main"
    const val EXPLORAR = "explorar"
    const val ASOCIACION = "asociacion/{asociacionId}"
    const val VISITEO = "visiteo"
    const val PROYECTOS = "proyectos"
    const val TESTIMONIOS = "testimonios"

    fun asociacion(id: String) = "asociacion/$id"
}

fun NavGraphBuilder.grafoInicio(
    nav: NavController,
    onNavegarAActividades: () -> Unit = {},
    onNavegarACampanas: () -> Unit = {},
    onVerAgenda: () -> Unit = {},
    esAdmin: () -> Boolean = { false }
) {
    composable(RutasInicio.SPLASH) {
        SplashScreen(
            onSplashFinished = {
                nav.navigate(RutasInicio.CIUDAD) {
                    popUpTo(RutasInicio.SPLASH) { inclusive = true }
                }
            }
        )
    }

    composable(RutasInicio.PERMISO_NOTIFICACIONES) {
        PermisoNotificacionesScreen(
            onContinuar = { nav.navigate(RutasInicio.CIUDAD) }
        )
    }

    composable(RutasInicio.CIUDAD) {
        val vm: InicioViewModel = viewModel(factory = AppViewModelProvider.Factory)
        CiudadScreen(
            ciudadActual = vm.ciudadElegida,
            onCiudadSeleccionada = { ciudad ->
                vm.cambiarCiudad(ciudad)
                nav.navigate(RutasInicio.INICIO) {
                    popUpTo(RutasInicio.CIUDAD) { inclusive = true }
                }
            }
        )
    }

    composable(RutasInicio.INICIO) {
        val vm: InicioViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val asociacionDestacada by vm.asociacionDestacada.collectAsStateWithLifecycle()
        val cuenta = cuentaViewModel()
        val sesion by cuenta.sesion.collectAsStateWithLifecycle()
        val asociaciones by cuenta.asociaciones.collectAsStateWithLifecycle()
        val favoritas by cuenta.favoritas.collectAsStateWithLifecycle()

        InicioScreen(
            ciudad = vm.ciudadElegida,
            onCambiarCiudad = vm::cambiarCiudad,
            asociacion = asociacionDestacada,
            nombreFamilia = sesion?.familia,
            causas = asociaciones.take(2),
            favoritas = favoritas,
            onAlternarFavorita = { id -> nav.alternarFavorita(cuenta, id) },
            onCausaClick = { id -> nav.navigate(RutasInicio.asociacion(id)) },
            onVerAgenda = onVerAgenda,
            onCrearCuenta = { nav.navigate(RutasInscripcion.CREAR_CUENTA) { launchSingleTop = true } },
            onIniciarSesion = { nav.navigate(RutasInscripcion.INICIAR_SESION) { launchSingleTop = true } },
            // Explorar lleva a las actividades.
            onExplorarClick = onNavegarAActividades,
            onActividadesClick = onNavegarAActividades,
            onDonarClick = onNavegarACampanas,
            onProyectosClick = { nav.navigate(RutasInicio.PROYECTOS) },
            onVisiteoClick = { nav.navigate(RutasInicio.VISITEO) }
        )
    }

    composable(RutasInicio.EXPLORAR) {
        val vm: ExplorarViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val textoBusqueda by vm.busqueda.collectAsStateWithLifecycle()
        val categoriaSeleccionada by vm.categoriaSeleccionada.collectAsStateWithLifecycle()
        val asociaciones by vm.asociacionesFiltradas.collectAsStateWithLifecycle()
        val cuenta = cuentaViewModel()
        var creando by remember { mutableStateOf(false) }
        var editando by remember { mutableStateOf<Asociacion?>(null) }
        var borrando by remember { mutableStateOf<Asociacion?>(null) }

        ExplorarScreen(
            textoBusqueda = textoBusqueda,
            onBusquedaChange = vm::onBusquedaChange,
            categoriaSeleccionada = categoriaSeleccionada,
            onCategoriaSelect = vm::onCategoriaSelect,
            asociaciones = asociaciones,
            onAsociacionClick = { id -> nav.navigate(RutasInicio.asociacion(id)) },
            esAdmin = esAdmin(),
            onCrearAsociacion = { creando = true },
            onEditarAsociacion = { editando = it },
            onBorrarAsociacion = { borrando = it }
        )

        if (creando || editando != null) {
            val actual = editando
            DialogoFormularioAdmin(
                titulo = if (actual == null) "Crear asociación" else "Editar asociación",
                campos = listOf(
                    CampoAdmin("Nombre", actual?.nombre.orEmpty()),
                    CampoAdmin("Categoría", actual?.categoria.orEmpty()),
                    CampoAdmin("Descripción", actual?.descripcion.orEmpty(), TipoCampo.MULTILINEA),
                    CampoAdmin("Dirección", actual?.direccion.orEmpty()),
                    CampoAdmin("Teléfono", actual?.telefono.orEmpty()),
                    CampoAdmin("WhatsApp", actual?.whatsapp.orEmpty()),
                    CampoAdmin("Correo", actual?.correo.orEmpty())
                ),
                onGuardar = { valores ->
                    vm.guardarAsociacion(actual?.id, valores, onListo = cuenta::recargarCatalogo)
                    creando = false
                    editando = null
                },
                onDescartar = { creando = false; editando = null }
            )
        }
        borrando?.let { asociacion ->
            DialogoConfirmarBorrado(
                nombre = asociacion.nombre,
                onConfirmar = {
                    vm.borrarAsociacion(asociacion.id, onListo = cuenta::recargarCatalogo)
                    borrando = null
                },
                onDescartar = { borrando = null }
            )
        }
    }

    composable(RutasInicio.ASOCIACION) { backStackEntry ->
        val id = backStackEntry.arguments?.getString("asociacionId") ?: ""
        val vm: AsociacionViewModel = viewModel(factory = AppViewModelProvider.Factory)
        vm.cargarAsociacion(id)

        val asociacion by vm.asociacion.collectAsStateWithLifecycle()
        val cuenta = cuentaViewModel()
        val favoritas by cuenta.favoritas.collectAsStateWithLifecycle()

        asociacion?.let {
            AsociacionScreen(
                asociacion = it,
                esFavorito = id in favoritas,
                onToggleFavorito = { nav.alternarFavorita(cuenta, id) },
                onVerActividades = onNavegarAActividades
            )
        }
    }

    composable(RutasInicio.VISITEO) {
        val vm: DirectorioViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val centros by vm.centros.collectAsStateWithLifecycle()
        var creando by remember { mutableStateOf(false) }
        var editando by remember { mutableStateOf<CentroVisiteo?>(null) }
        var borrando by remember { mutableStateOf<CentroVisiteo?>(null) }
        VisiteoScreen(
            // "Ver detalles" del centro llega con la ficha de asociación (RF-03).
            onCentroClick = { },
            centros = centros,
            onIrAInicio = { nav.navigate(RutasInicio.INICIO) },
            onComoAyudar = onNavegarACampanas,
            esAdmin = esAdmin(),
            onCrearCentro = { creando = true },
            onEditarCentro = { editando = it },
            onBorrarCentro = { borrando = it }
        )

        if (creando || editando != null) {
            val actual = editando
            DialogoFormularioAdmin(
                titulo = if (actual == null) "Crear centro" else "Editar centro",
                campos = listOf(
                    CampoAdmin("Nombre", actual?.nombre.orEmpty()),
                    CampoAdmin("Tipo (Asilos, Casas hogar, Comedores)", actual?.tipo.orEmpty()),
                    CampoAdmin("Resumen", actual?.resumen.orEmpty(), TipoCampo.MULTILINEA),
                    CampoAdmin("Información general", actual?.informacion.orEmpty(), TipoCampo.MULTILINEA),
                    CampoAdmin("Necesidades (una por línea)", actual?.necesidades?.joinToString("\n").orEmpty(), TipoCampo.MULTILINEA),
                    CampoAdmin("Dirección", actual?.direccion.orEmpty()),
                    CampoAdmin("Verificado", (actual?.verificado ?: true).toString(), TipoCampo.INTERRUPTOR)
                ),
                onGuardar = { valores ->
                    vm.guardarCentro(actual, valores)
                    creando = false
                    editando = null
                },
                onDescartar = { creando = false; editando = null }
            )
        }
        borrando?.let { centro ->
            DialogoConfirmarBorrado(
                nombre = centro.nombre,
                onConfirmar = { vm.borrarCentro(centro.id); borrando = null },
                onDescartar = { borrando = null }
            )
        }
    }

    composable(RutasInicio.PROYECTOS) {
        val vm: DirectorioViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val proyectos by vm.proyectos.collectAsStateWithLifecycle()
        var creando by remember { mutableStateOf(false) }
        var editando by remember { mutableStateOf<Proyecto?>(null) }
        var borrando by remember { mutableStateOf<Proyecto?>(null) }
        ProyectosScreen(
            proyectos = proyectos,
            esAdmin = esAdmin(),
            onIrAInicio = { nav.navigate(RutasInicio.INICIO) },
            onCrearProyecto = { creando = true },
            onEditarProyecto = { id -> editando = proyectos.firstOrNull { it.id == id } },
            onBorrarProyecto = { id -> borrando = proyectos.firstOrNull { it.id == id } }
        )

        if (creando || editando != null) {
            val actual = editando
            DialogoFormularioAdmin(
                titulo = if (actual == null) "Crear proyecto" else "Editar proyecto",
                campos = listOf(
                    CampoAdmin("Nombre", actual?.nombre.orEmpty()),
                    CampoAdmin("Descripción", actual?.descripcion.orEmpty(), TipoCampo.MULTILINEA),
                    CampoAdmin("Beneficiarios (ej. 25 Mujeres)", actual?.beneficiarios.orEmpty()),
                    CampoAdmin("Ciudad", actual?.ciudad ?: "Monterrey, N.L."),
                    CampoAdmin("Vigencia (ej. Hasta 29 jun 2026)", actual?.vigencia.orEmpty()),
                    CampoAdmin("Activo", (actual?.activo ?: true).toString(), TipoCampo.INTERRUPTOR)
                ),
                onGuardar = { valores ->
                    vm.guardarProyecto(actual, valores)
                    creando = false
                    editando = null
                },
                onDescartar = { creando = false; editando = null }
            )
        }
        borrando?.let { proyecto ->
            DialogoConfirmarBorrado(
                nombre = proyecto.nombre,
                onConfirmar = { vm.borrarProyecto(proyecto.id); borrando = null },
                onDescartar = { borrando = null }
            )
        }
    }

    composable(RutasInicio.TESTIMONIOS) {
        TestimoniosScreen()
    }
}