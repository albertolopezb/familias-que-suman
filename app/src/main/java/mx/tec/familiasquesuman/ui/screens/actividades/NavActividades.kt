package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.Participacion
import mx.tec.familiasquesuman.notificaciones.Notificaciones
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.compartirActividad
import mx.tec.familiasquesuman.ui.screens.inscripcion.alternarFavorita
import mx.tec.familiasquesuman.ui.screens.inscripcion.cuentaViewModel
import mx.tec.familiasquesuman.domain.TipoSugerencia
import mx.tec.familiasquesuman.ui.screens.sugerencias.sugerir
import mx.tec.familiasquesuman.ui.state.AppViewModelProvider
import mx.tec.familiasquesuman.ui.state.UiState
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

object RutasActividades {
    const val LISTA = "actividades"
    const val DETALLE = "actividades/{actividadId}"
    const val SIN_CONEXION = "actividades_sin_conexion"

    /** La pestaña de la barra inferior. Rutas.MIS_ACTIVIDADES apunta aquí. */
    const val MIS_ACTIVIDADES = "mis_actividades"

    const val ARG_ACTIVIDAD_ID = "actividadId"

    fun detalle(id: String) = "actividades/$id"
}

/**
 * El único lugar donde se decide a dónde lleva cada botón de esta sección.
 *
 * Los saltos hacia otras partes llegan como parámetros: mientras esa parte no
 * exista, se quedan vacíos y nadie espera a nadie.
 *
 * - onInscribirme: parte 3, puerta de cuenta (RF-18) y acompañantes (RF-06).
 * - onCancelarInscripcion: parte 3, cancelar (RF-19).
 * - onResponderEncuesta / onEncuestaPrevia / onVerRespuestas: encuestas de antes y después (RF-13).
 * - onCompartirTestimonio: testimonio con fotografía (RF-12).
 * - onCambiarCiudad / onVerAsociaciones: parte 1.
 */
fun NavGraphBuilder.grafoActividades(
    nav: NavController,
    ciudad: String = "Monterrey, N.L.",
    esAdmin: () -> Boolean = { false },
    onCambiarCiudad: () -> Unit = {},
    onIrAInicio: () -> Unit = { nav.popBackStack() },
    onInscribirme: (String) -> Unit = {},
    onInscribirmeConPrueba: (String, Boolean) -> Unit = { id, _ -> onInscribirme(id) },
    onCancelarInscripcion: (String) -> Unit = {},
    onResponderEncuesta: (Participacion) -> Unit = {},
    onCompartirTestimonio: (Participacion) -> Unit = {},
    onEncuestaPrevia: (ActividadConAsociacion) -> Unit = {},
    onVerRespuestas: (String) -> Unit = {},
    onVerAsociaciones: () -> Unit = {}
) {

    composable(RutasActividades.LISTA) {
        val vm: ActividadesViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        val contexto = LocalContext.current

        // Estados locales para controlar los diálogos de Admin
        var mostrandoCrear by remember { mutableStateOf<Boolean>(false) }
        var actividadAEditar by remember { mutableStateOf<ActividadConAsociacion?>(null) }
        // NUEVO: Estado para la actividad que se quiere confirmar borrar
        var actividadABorrar by remember { mutableStateOf<ActividadConAsociacion?>(null) }

        ActividadesScreen(
            estado = estado,
            ciudad = ciudad,
            esAdmin = esAdmin(),
            onActividadClick = { id -> nav.navigate(RutasActividades.detalle(id)) },
            onUnirme = onInscribirme,
            onCompartir = { item -> compartirActividad(contexto, item) },
            onIrAInicio = onIrAInicio,
            onCiudadClick = onCambiarCiudad,
            onReintentar = vm::cargar,
            onVerGuardadas = { nav.navigate(RutasActividades.SIN_CONEXION) },
            onVerAsociaciones = onVerAsociaciones,
            onForzarEstado = vm::siguienteModoDePrueba,
            onSugerir = { nav.sugerir(TipoSugerencia.ACTIVIDAD) },
            onBorrarActividad = { id: String ->
                // En lugar de borrar directo, guardamos el elemento a borrar para abrir la alerta
                if (estado is UiState.Exito) {
                    actividadABorrar = (estado as UiState.Exito).datos.find { it.actividad.id == id }
                }
            },
            onCrearActividad = {
                mostrandoCrear = true
            },
            onEditarActividad = { id: String ->
                if (estado is UiState.Exito) {
                    actividadAEditar = (estado as UiState.Exito).datos.find { it.actividad.id == id }
                }
            }
        )

        // Diálogo para Crear
        if (mostrandoCrear) {
            DialogoFormularioActividad(
                tituloDialogo = "Crear Nueva Actividad",
                onGuardar = { titulo, desc, fecha, horario, direccion, cupoTotal, libres, edadMin ->
                    vm.crearActividad(
                        titulo = titulo,
                        descripcion = desc,
                        fecha = fecha,
                        horario = horario,
                        direccion = direccion,
                        cupoTotal = cupoTotal,
                        lugaresDisponibles = libres,
                        edadMinima = edadMin
                    )
                    mostrandoCrear = false
                },
                onDescartar = { mostrandoCrear = false }
            )
        }

        // Diálogo para Editar
        // Diálogo para Editar
        actividadAEditar?.let { item ->
            DialogoFormularioActividad(
                tituloDialogo = "Editar Actividad",
                actividadInicial = item.actividad,
                onGuardar = { titulo, desc, fecha, horario, direccion, cupoTotal, libres, edadMin ->
                    vm.editarActividad(
                        id = item.actividad.id,
                        nuevoTitulo = titulo,
                        nuevaDescripcion = desc,
                        nuevaFecha = fecha,
                        nuevoHorario = horario,
                        nuevaDireccion = direccion,
                        nuevoCupoTotal = cupoTotal,
                        nuevosLugaresDisponibles = libres,
                        nuevaEdadMinima = edadMin
                    )
                    actividadAEditar = null
                },
                onDescartar = { actividadAEditar = null }
            )
        }

        actividadABorrar?.let { item ->
            AlertDialog(
                onDismissRequest = { actividadABorrar = null },
                title = { Text("Eliminar Actividad") },
                text = {
                    Text("¿Estás seguro de que deseas eliminar \"${item.actividad.titulo}\"? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.borrarActividad(item.actividad.id)
                            actividadABorrar = null
                        }
                    ) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { actividadABorrar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

    composable(
        RutasActividades.DETALLE,
        // La notificación de recordatorio abre el detalle de la actividad.
        deepLinks = listOf(navDeepLink { uriPattern = Notificaciones.URI_ACTIVIDAD + "{actividadId}" })
    ) { entrada ->
        val id = entrada.arguments?.getString(RutasActividades.ARG_ACTIVIDAD_ID).orEmpty()
        val vm: DetalleActividadViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val contexto = LocalContext.current

        // Dentro de un LaunchedEffect, no en el cuerpo del composable: si no,
        // la carga se dispara otra vez en cada recomposición.
        LaunchedEffect(id) { vm.cargar(id) }

        val estado by vm.estado.collectAsStateWithLifecycle()
        val cuenta = cuentaViewModel()
        val favoritas by cuenta.favoritas.collectAsStateWithLifecycle()
        var simularSinCupo by rememberSaveable { mutableStateOf(false) }

        when (val actual = estado) {
            is UiState.Cargando -> CargandoView()

            is UiState.Error -> ErrorView(
                mensaje = actual.mensaje,
                onReintentar = vm::reintentar
            )

            is UiState.Exito -> DetalleActividadScreen(
                item = actual.datos,
                esFavorito = id in favoritas,
                onRegresar = { nav.popBackStack() },
                onCompartir = { compartirActividad(contexto, actual.datos) },
                onAlternarFavorito = { nav.alternarFavorita(cuenta, id) },
                // Parte 3 conecta esto con la puerta de cuenta (RF-18).
                onInscribirme = { onInscribirmeConPrueba(id, simularSinCupo) },
                simularSinCupo = simularSinCupo,
                onSimularSinCupoChange = { simularSinCupo = it },
                onVerOtrasActividades = {
                    // Si se llegó desde la lista, se regresa a ella; si no, se abre.
                    if (!nav.popBackStack(RutasActividades.LISTA, inclusive = false)) {
                        nav.navigate(RutasActividades.LISTA)
                    }
                }
            )
        }
    }

    composable(RutasActividades.MIS_ACTIVIDADES) {
        val vm: MisActividadesViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        // Solo las actividades a las que esta cuenta se inscribió.
        val cuenta = cuentaViewModel()
        val inscripciones by cuenta.inscripciones.collectAsStateWithLifecycle()
        val sesion by cuenta.sesion.collectAsStateWithLifecycle()
        val correo = sesion?.correo
        LaunchedEffect(inscripciones.keys, correo) { vm.cargar(inscripciones.keys, correo) }
        // Al volver de una encuesta o de un testimonio, los pendientes se leen otra vez.
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar(inscripciones.keys, correo) }

        when (val actual = estado) {
            is UiState.Cargando -> CargandoView()

            is UiState.Error -> ErrorView(
                mensaje = actual.mensaje,
                onReintentar = { vm.cargar() }
            )

            is UiState.Exito -> MisActividadesScreen(
                datos = actual.datos,
                ciudad = ciudad,
                onCiudadClick = onCambiarCiudad,
                onProximaClick = { id -> nav.navigate(RutasActividades.detalle(id)) },
                onCancelar = onCancelarInscripcion,
                onResponderEncuesta = onResponderEncuesta,
                onCompartirTestimonio = onCompartirTestimonio,
                onEncuestaPrevia = onEncuestaPrevia,
                onVerRespuestas = onVerRespuestas,
                onVerActividades = { nav.navigate(RutasActividades.LISTA) },
                onRegresar = { nav.popBackStack() }
            )
        }
    }

    composable(RutasActividades.SIN_CONEXION) { entrada ->
        // Usa el ViewModel de la lista, que es quien guardó lo último que cargó.
        // Si por alguna razón la lista no está abajo en la pila, usa uno propio.
        val dueno = remember(entrada) {
            runCatching { nav.getBackStackEntry(RutasActividades.LISTA) }.getOrDefault(entrada)
        }
        val vm: ActividadesViewModel =
            viewModel(viewModelStoreOwner = dueno, factory = AppViewModelProvider.Factory)
        val guardadas by vm.guardadas.collectAsStateWithLifecycle()

        SinConexionScreen(
            actividadesGuardadas = guardadas,
            onRegresar = { nav.popBackStack() },
            onReintentar = {
                vm.cargar()
                nav.popBackStack()
            }
        )
    }
}
