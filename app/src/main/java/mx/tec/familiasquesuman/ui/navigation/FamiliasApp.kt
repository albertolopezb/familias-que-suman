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
import mx.tec.familiasquesuman.ui.components.PantallaPendiente
import mx.tec.familiasquesuman.ui.screens.inicio.inicio.RutasInicio
import mx.tec.familiasquesuman.ui.screens.inicio.inicio.grafoInicio
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * La raíz de la app y el único lugar donde se decide a dónde lleva cada botón.
 * Las pantallas no navegan solas: reciben funciones como parámetro y aquí se conectan.
 */
@Composable
fun FamiliasApp() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = Superficie) {
                pestanas.forEach { pestana ->
                    NavigationBarItem(
                        selected = rutaActual == pestana.ruta,
                        onClick = {
                            nav.navigate(pestana.ruta) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
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
    ) { padding ->
        // Agrega el llamado a tu grafo dentro de NavHost en FamiliasApp.kt:
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
            // Las llamadas de los demás compañeros van aquí abajo
        }
    }
}
