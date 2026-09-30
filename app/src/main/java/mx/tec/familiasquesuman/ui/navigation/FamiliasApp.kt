package mx.tec.familiasquesuman.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.ui.screens.actividades.RutasActividades
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.grafoActividades
import mx.tec.familiasquesuman.ui.screens.admin.RutasAdmin
import mx.tec.familiasquesuman.ui.screens.admin.SesionAdmin
import mx.tec.familiasquesuman.ui.screens.admin.grafoAdmin
import mx.tec.familiasquesuman.ui.screens.campanas.RutasCampanas
import mx.tec.familiasquesuman.ui.screens.campanas.grafoCampanas
import mx.tec.familiasquesuman.ui.screens.inicio.RutasInicio
import mx.tec.familiasquesuman.ui.screens.inicio.grafoInicio
import mx.tec.familiasquesuman.ui.screens.perfil.RutasPerfil
import mx.tec.familiasquesuman.ui.screens.perfil.grafoPerfil

@Composable
fun FamiliasApp() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    val menu = rememberDrawerState(DrawerValue.Closed)
    val alcance = rememberCoroutineScope()

    val mostrarBarraInferior = rutaActual != RutasInicio.SPLASH && rutaActual != RutasInicio.CIUDAD &&
        rutaActual != RutasPerfil.TESTIMONIO && rutaActual != RutasPerfil.ENCUESTA_FINAL &&
        rutaActual != RutasPerfil.AVISO_PRIVACIDAD && rutaActual != RutasPerfil.ENCUESTA_PREVIA &&
        rutaActual !in RutasAdmin.sinBarra

    ModalNavigationDrawer(
        drawerState = menu,
        // Sin gesto en el splash ni en la selección de ciudad.
        gesturesEnabled = menu.isOpen,
        drawerContent = {
            MenuPrincipal(
                esAdmin = SesionAdmin.activa,
                onOpcion = { opcion ->
                    alcance.launch { menu.close() }
                    when (opcion) {
                        OpcionMenu.MIS_ACTIVIDADES -> nav.irA(RutasActividades.MIS_ACTIVIDADES)
                        OpcionMenu.PERFIL -> nav.irA(Rutas.PERFIL)
                        OpcionMenu.DONAR -> nav.irA(RutasCampanas.LISTA)
                        OpcionMenu.EXPLORAR -> nav.irA(RutasInicio.EXPLORAR)
                        OpcionMenu.AJUSTES -> nav.navigate(RutasPerfil.AJUSTES)
                        OpcionMenu.PANEL_ADMIN -> nav.navigate(RutasAdmin.PANEL)
                        OpcionMenu.ACCESO_ADMIN -> nav.navigate(RutasAdmin.ACCESO)
                        OpcionMenu.SALIR_ADMIN -> {
                            SesionAdmin.salir()
                            nav.irA(Rutas.INICIO)
                        }
                    }
                }
            )
        }
    ) {
        CompositionLocalProvider(LocalAbrirMenu provides { alcance.launch { menu.open() } }) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (mostrarBarraInferior) {
                        BarraInferior(rutaActual = rutaActual, onPestana = { nav.irA(it.ruta) })
                    }
                }
            ) { padding ->
                NavHost(
                    navController = nav,
                    startDestination = RutasInicio.SPLASH,
                    modifier = Modifier.padding(padding)
                ) {
                    grafoPerfil(nav)
                    grafoInicio(
                        nav = nav,
                        // La tarjeta "Actividades en Familia" lleva a la lista de
                        // actividades, no a las que la familia ya tiene inscritas.
                        onNavegarAActividades = { nav.irA(RutasActividades.LISTA) },
                        // "Quiero Donar" lleva a la lista de campañas (parte 4).
                        onNavegarACampanas = { nav.irA(RutasCampanas.LISTA) }
                    )

                    grafoActividades(
                        nav = nav,
                        onIrAInicio = { nav.irA(RutasInicio.INICIO) },
                        onVerAsociaciones = { nav.navigate(RutasInicio.EXPLORAR) },
                        // Mis Actividades → encuesta final (parte 5, RF-13).
                        onResponderEncuesta = { nav.navigate(RutasPerfil.ENCUESTA_FINAL) }
                        // Pendientes:
                        // onInscribirme y onCancelarInscripcion → parte 3, cuando exista su rama.
                        // onCompartirTestimonio → parte 5 busca la actividad solo en las próximas;
                        //   hace falta que también la busque en el historial.
                        // ciudad y onCambiarCiudad → cuando la ciudad de la parte 1 sea compartida.
                    )

                    grafoCampanas(nav)

                    grafoAdmin(nav)
                }
            }
        }
    }
}

/** Cambio de pestaña: una sola copia de cada pantalla y la pila limpia. */
private fun NavController.irA(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * La barra inferior del sitio: cinco pestañas, la activa con su ícono sobre un
 * cuadro gris y el texto en azul.
 */
@Composable
private fun BarraInferior(rutaActual: String?, onPestana: (Pestana) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Web.Tarjeta).navigationBarsPadding()) {
        HorizontalDivider(color = Web.Borde, thickness = 1.dp)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            pestanas.forEach { pestana ->
                val activa = rutaActual != null &&
                    pestana.prefijos.any { rutaActual == it || rutaActual.startsWith("$it/") } &&
                    rutaActual != RutasActividades.MIS_ACTIVIDADES
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { if (!activa || rutaActual != pestana.ruta) onPestana(pestana) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activa) Web.Secundario else Web.Tarjeta)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            pestana.icono,
                            contentDescription = pestana.etiqueta,
                            tint = if (activa) Web.Primario else Web.TextoApagado,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        pestana.etiqueta,
                        style = TextoWeb.Chico.copy(
                            fontSize = 10.sp,
                            fontWeight = if (activa) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (activa) Web.Primario else Web.TextoApagado
                    )
                }
            }
        }
    }
}
