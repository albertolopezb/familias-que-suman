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
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.tec.familiasquesuman.ui.screens.inicio.RutasInicio
import mx.tec.familiasquesuman.ui.screens.inicio.grafoInicio
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.TintaSuave
import mx.tec.familiasquesuman.ui.screens.campanas.grafoCampanas

@Composable
fun FamiliasApp() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    val mostrarBarraInferior = rutaActual != RutasInicio.SPLASH && rutaActual != RutasInicio.CIUDAD

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (mostrarBarraInferior) {
                NavigationBar(containerColor = Superficie) {
                    pestanas.forEach { pestana ->
                        val seleccionada = rutaActual == pestana.ruta

                        NavigationBarItem(
                            selected = seleccionada,
                            onClick = {
                                if (rutaActual != pestana.ruta) {
                                    nav.navigate(pestana.ruta) {
                                        // Mantiene la raíz en la pantalla de inicio limpia
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
            grafoInicio(
                nav = nav,
                onNavegarAActividades = { nav.navigate(Rutas.MIS_ACTIVIDADES) },
                onNavegarACampanas = { nav.navigate(Rutas.EXPLORAR) }
            )

            grafoCampanas(nav)
        }
    }
}