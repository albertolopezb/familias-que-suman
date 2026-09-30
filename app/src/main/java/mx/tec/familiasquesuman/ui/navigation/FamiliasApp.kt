package mx.tec.familiasquesuman.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.tec.familiasquesuman.ui.screens.actividades.RutasActividades
import mx.tec.familiasquesuman.ui.screens.actividades.grafoActividades
import mx.tec.familiasquesuman.ui.screens.inicio.RutasInicio
import mx.tec.familiasquesuman.ui.screens.inscripcion.RutasInscripcion
import mx.tec.familiasquesuman.ui.screens.inscripcion.grafoInscripcion
import mx.tec.familiasquesuman.ui.screens.inicio.grafoInicio
import mx.tec.familiasquesuman.ui.screens.perfil.RutasPerfil
import mx.tec.familiasquesuman.ui.screens.perfil.grafoPerfil
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.TintaSuave
import mx.tec.familiasquesuman.ui.screens.campanas.RutasCampanas
import mx.tec.familiasquesuman.ui.screens.campanas.grafoCampanas

@Composable
fun FamiliasApp() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    val mostrarBarraInferior = rutaActual != RutasInicio.SPLASH && rutaActual != RutasInicio.CIUDAD &&
        rutaActual != RutasPerfil.TESTIMONIO && rutaActual != RutasPerfil.ENCUESTA_FINAL &&
        rutaActual != RutasPerfil.AVISO_PRIVACIDAD && rutaActual != RutasPerfil.ENCUESTA_PREVIA

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (mostrarBarraInferior) {
                NavigationBar(containerColor = Superficie) {
                    pestanas.forEach { pestana ->
                        val seleccionada = rutaActual == pestana.ruta ||
                            (pestana.ruta == Rutas.PERFIL &&
                                (rutaActual == RutasPerfil.FAVORITOS || rutaActual == RutasPerfil.INSIGNIAS ||
                                    rutaActual == RutasPerfil.AJUSTES))

                        NavigationBarItem(
                            selected = seleccionada,
                            onClick = {
                                if (rutaActual != pestana.ruta) {
                                    nav.navigate(pestana.ruta) {
                                        popUpTo(nav.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(pestana.icono, contentDescription = pestana.etiqueta) },
                            label = { Text(pestana.etiqueta, style = MaterialTheme.typography.labelMedium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MarcaOro,
                                selectedTextColor = AcentoTexto,
                                indicatorColor = AcentoSuave,
                                unselectedIconColor = TintaSuave,
                                unselectedTextColor = TintaSuave
                            )
                        )
                    }
                }
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
                onNavegarAActividades = { nav.navigate(RutasActividades.LISTA) },
                // "Quiero Donar" lleva a la lista de campañas (parte 4).
                onNavegarACampanas = { nav.navigate(RutasCampanas.LISTA) }
            )

            grafoActividades(
                nav = nav,
                onIrAInicio = {
                    if (!nav.popBackStack(RutasInicio.INICIO, inclusive = false)) {
                        nav.navigate(RutasInicio.INICIO)
                    }
                },
                onVerAsociaciones = { nav.navigate(RutasInicio.EXPLORAR) },
                // Parte 3: inscribirse (puerta de cuenta o acompañantes) y cancelar.
                onInscribirme = { id -> nav.navigate(RutasInscripcion.inscribirse(id)) },
                onCancelarInscripcion = { id -> nav.navigate(RutasInscripcion.cancelar(id)) },
                // Mis Actividades → encuesta final (parte 5, RF-13).
                onResponderEncuesta = { nav.navigate(RutasPerfil.ENCUESTA_FINAL) }
                // Pendientes:
                // onCompartirTestimonio → parte 5 busca la actividad solo en las próximas;
                //   hace falta que también la busque en el historial.
                // ciudad y onCambiarCiudad → cuando la ciudad de la parte 1 sea compartida.
            )


            grafoCampanas(nav)
            grafoInscripcion(nav)
        }
    }
}
