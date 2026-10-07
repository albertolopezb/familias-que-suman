package mx.tec.familiasquesuman.ui.screens.campanas

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.domain.PuntoEntrega
import mx.tec.familiasquesuman.domain.TipoSugerencia
import mx.tec.familiasquesuman.ui.components.CampoAdmin
import mx.tec.familiasquesuman.ui.components.WhatsAppGeneral
import mx.tec.familiasquesuman.ui.components.abrir
import mx.tec.familiasquesuman.ui.components.abrirMapa
import mx.tec.familiasquesuman.ui.components.abrirWhatsApp
import mx.tec.familiasquesuman.ui.components.DialogoConfirmarBorrado
import mx.tec.familiasquesuman.ui.components.DialogoFormularioAdmin
import mx.tec.familiasquesuman.ui.components.TipoCampo
import mx.tec.familiasquesuman.ui.screens.inscripcion.alternarFavorita
import mx.tec.familiasquesuman.ui.screens.inscripcion.cuentaViewModel
import mx.tec.familiasquesuman.ui.screens.sugerencias.sugerir
import mx.tec.familiasquesuman.ui.state.AppViewModelProvider
import mx.tec.familiasquesuman.ui.state.UiState

object RutasCampanas {
    const val LISTA = "campanas"
    const val DETALLE = "campanas/{campanaId}"
    const val CONFIRMADO = "campanas/{campanaId}/apartado"
    const val COMO_DONAR = "campanas-como-donar"

    fun detalle(id: String) = "campanas/$id"
    fun confirmado(id: String) = "campanas/$id/apartado"
}

/**
 * "Quiero ayudar" de una campaña: abre su WhatsApp con el mensaje ya escrito y, si no tiene,
 * marca su teléfono (ACTION_DIAL solo abre el marcador: no pide permiso ni llama sola).
 */
private fun contactarCampana(contexto: Context, campana: Campana) {
    val whatsapp = campana.whatsapp
    val telefono = campana.telefono
    when {
        whatsapp != null ->
            abrirWhatsApp(contexto, whatsapp, "Hola, me interesa apoyar la campaña ${campana.titulo}")
        telefono != null ->
            abrir(contexto, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono")))
    }
}

/** "Cómo llegar" de un punto de entrega. */
private fun abrirMapa(contexto: Context, punto: PuntoEntrega): Unit =
    abrirMapa(contexto, "${punto.direccion}, ${punto.colonia}")

/**
 * Grafo de la parte 4. En FamiliasApp.kt se agrega UNA línea, al final del NavHost:
 *     grafoCampanas(nav)
 * Y para llegar desde Inicio ("Quiero Donar"): nav.navigate(RutasCampanas.LISTA)
 */
fun NavGraphBuilder.grafoCampanas(nav: NavController, esAdmin: () -> Boolean = { false }) {

    // P-09 / 09b / 09c / 09d + hoja de filtros P-10
    composable(RutasCampanas.LISTA) {
        val vm: CampanasViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.campanas.collectAsStateWithLifecycle()
        val aplicados by vm.aplicados.collectAsStateWithLifecycle()
        val chip by vm.chipElegido.collectAsStateWithLifecycle()
        val borrador by vm.borrador.collectAsStateWithLifecycle()
        val conteo by vm.conteoBorrador.collectAsStateWithLifecycle()
        val total by vm.totalAbiertas.collectAsStateWithLifecycle()
        val nombres by vm.nombresAsociacion.collectAsStateWithLifecycle()
        var verFiltros by rememberSaveable { mutableStateOf(false) }
        val contexto = LocalContext.current
        var creando by remember { mutableStateOf(false) }
        var editando by remember { mutableStateOf<Campana?>(null) }
        var borrando by remember { mutableStateOf<Campana?>(null) }
        val lista = (estado as? UiState.Exito)?.datos.orEmpty()

        CampanasScreen(
            estado = estado,
            filtros = aplicados,
            chipElegido = chip,
            totalAbiertas = total,
            nombresAsociacion = nombres,
            onBack = { nav.popBackStack() },
            onAbrirFiltros = { vm.abrirFiltros(); verFiltros = true },
            onChipRapido = vm::elegirChipRapido,
            onQuitarCategoria = vm::quitarCategoria,
            onQuitarUrgentes = vm::quitarUrgentes,
            onQuitarFiltros = vm::quitarFiltros,
            onCampanaClick = { id -> nav.navigate(RutasCampanas.detalle(id)) },
            onReintentar = vm::cargar,
            onAyudar = { campana -> contactarCampana(contexto, campana) },
            onNoEncontre = {
                abrirWhatsApp(contexto, WhatsAppGeneral, "Hola, no encontré dónde aportar lo que tengo. ¿Me ayudan?")
            },
            onLlamar = { telefono -> abrir(contexto, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono"))) },
            onWhatsAppCentro = { numero ->
                abrirWhatsApp(contexto, numero, "Hola, tengo algo para aportar y me gustaría coordinar la entrega.")
            },
            onComoLlegar = { direccion -> abrirMapa(contexto, direccion) },
            onAbrirEnlace = { enlace -> abrir(contexto, Intent(Intent.ACTION_VIEW, Uri.parse(enlace))) },
            esAdmin = esAdmin(),
            onCrearCampana = { creando = true },
            onEditarCampana = { id -> editando = lista.firstOrNull { it.id == id } },
            onBorrarCampana = { id -> borrando = lista.firstOrNull { it.id == id } },
            onSugerir = { nav.sugerir(TipoSugerencia.CAMPANA) }
        )

        if (creando || editando != null) {
            val actual = editando
            DialogoFormularioAdmin(
                titulo = if (actual == null) "Crear campaña" else "Editar campaña",
                campos = listOf(
                    CampoAdmin("Título", actual?.titulo.orEmpty()),
                    CampoAdmin("Categoría", actual?.categoria.orEmpty()),
                    CampoAdmin("Cierra (ej. 30 de diciembre)", actual?.cierra.orEmpty()),
                    CampoAdmin("Descripción", actual?.descripcion.orEmpty(), TipoCampo.MULTILINEA),
                    CampoAdmin("Unidad de la meta (kits, despensas…)", actual?.unidadMeta.orEmpty()),
                    CampoAdmin("Meta total", (actual?.metaTotal ?: 0).toString(), TipoCampo.NUMERO),
                    CampoAdmin("Completados", (actual?.completados ?: 0).toString(), TipoCampo.NUMERO),
                    CampoAdmin("Ciudad", actual?.ciudad ?: "Monterrey"),
                    CampoAdmin("Urgente", (actual?.urgente ?: false).toString(), TipoCampo.INTERRUPTOR)
                ),
                onGuardar = { valores ->
                    vm.guardarCampana(actual, valores)
                    creando = false
                    editando = null
                },
                onDescartar = { creando = false; editando = null }
            )
        }

        borrando?.let { campana ->
            DialogoConfirmarBorrado(
                nombre = campana.titulo,
                onConfirmar = { vm.borrarCampana(campana.id); borrando = null },
                onDescartar = { borrando = null }
            )
        }

        if (verFiltros) {
            FiltrosSheet(
                borrador = borrador,
                conteo = conteo,
                onCategoria = vm::alternarCategoriaBorrador,
                onUrgentes = vm::cambiarUrgentesBorrador,
                onLimpiar = vm::limpiarBorrador,
                onAplicar = { vm.aplicarFiltros(); verFiltros = false },
                onCerrar = { verFiltros = false }
            )
        }
    }

    // P-11 / P-11c + hoja de apartar P-12 / P-12b / P-14
    composable(
        route = RutasCampanas.DETALLE,
        arguments = listOf(navArgument("campanaId") { type = NavType.StringType })
    ) { entrada ->
        val id = entrada.arguments?.getString("campanaId") ?: return@composable
        val vm: DetalleCampanaViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        val hoja by vm.hoja.collectAsStateWithLifecycle()
        val irAConfirmado by vm.irAConfirmado.collectAsStateWithLifecycle()
        val nombreAsociacion by vm.nombreAsociacion.collectAsStateWithLifecycle()
        val contexto = LocalContext.current
        // El corazón usa los favoritos compartidos de la cuenta, igual que Inicio y Actividades.
        val cuenta = cuentaViewModel()
        val favoritas by cuenta.favoritas.collectAsStateWithLifecycle()

        LaunchedEffect(id) { vm.cargar(id) }
        LaunchedEffect(irAConfirmado) {
            if (irAConfirmado) {
                vm.confirmadoNavegado()
                nav.navigate(RutasCampanas.confirmado(id))
            }
        }

        when (val e = estado) {
            is UiState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(e.mensaje, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { nav.popBackStack() }) { Text("Regresar") }
            }
            is UiState.Exito -> {
                DetalleCampanaScreen(
                    campana = e.datos,
                    campanaCompleta = vm.campanaCompleta(e.datos),
                    onBack = { nav.popBackStack() },
                    onApartar = vm::abrirApartar,
                    onVerOtrasCampanas = {
                        if (!nav.popBackStack(RutasCampanas.LISTA, inclusive = false)) nav.popBackStack()
                    },
                    nombreAsociacion = nombreAsociacion,
                    onAyudar = { contactarCampana(contexto, e.datos) },
                    onLlamar = {
                        e.datos.telefono?.let { abrir(contexto, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it"))) }
                    },
                    onOpcion = { opcion ->
                        val numero = e.datos.whatsapp
                        if (numero != null) {
                            abrirWhatsApp(
                                contexto, numero,
                                "Hola, quiero ayudar con: ${opcion.nombre} (${opcion.precio})"
                            )
                        } else {
                            contactarCampana(contexto, e.datos)
                        }
                    },
                    onComoLlegar = { punto -> abrirMapa(contexto, punto) },
                    onAbrirEnlace = { enlace -> abrir(contexto, Intent(Intent.ACTION_VIEW, Uri.parse(enlace))) },
                    esFavorita = id in favoritas,
                    onAlternarFavorita = { nav.alternarFavorita(cuenta, id) }
                )
            }
        }

        val hojaActual = hoja
        if (hojaActual != null) {
            ApartarSheet(
                hoja = hojaActual,
                onCantidad = vm::cambiarCantidad,
                onConfirmar = vm::confirmarApartado,
                onCerrar = vm::cerrarHoja
            )
        }
    }

    // P-13: comparte el ViewModel del detalle para ver la barra ya actualizada
    composable(
        route = RutasCampanas.CONFIRMADO,
        arguments = listOf(navArgument("campanaId") { type = NavType.StringType })
    ) { entrada ->
        val padre = remember(entrada) { nav.getBackStackEntry(RutasCampanas.DETALLE) }
        val vm: DetalleCampanaViewModel = viewModel(viewModelStoreOwner = padre, factory = AppViewModelProvider.Factory)
        val estado by vm.estado.collectAsStateWithLifecycle()
        val hecho by vm.ultimoApartado.collectAsStateWithLifecycle()
        val contexto = LocalContext.current

        val campana = (estado as? UiState.Exito)?.datos
        val apartado = hecho
        if (campana != null && apartado != null) {
            ApartadoConfirmadoScreen(
                campana = campana,
                hecho = apartado,
                onAvisar = {
                    val numero = campana.whatsapp ?: campana.telefono ?: WhatsAppGeneral
                    abrirWhatsApp(
                        contexto, numero,
                        "Hola, me comprometí a aportar ${apartado.cantidad} de «${apartado.articuloNombre}» " +
                            "para la campaña ${campana.titulo}. ¿Cómo y cuándo te los entrego?"
                    )
                },
                onComoLlegar = { punto -> abrirMapa(contexto, punto) },
                onRegresar = { nav.popBackStack() }
            )
        }
    }

    // P-24 (ya no se llega desde ninguna pantalla; se deja sin usar)
    composable(RutasCampanas.COMO_DONAR) {
        ComoDonarScreen(onBack = { nav.popBackStack() })
    }
}
