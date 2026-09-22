package mx.tec.familiasquesuman.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

/** Destinos de la app. Cada pantalla nueva agrega su ruta aquí. */
object Rutas {
    const val INICIO = "inicio"
    const val EXPLORAR = "explorar"
    const val MIS_ACTIVIDADES = "mis_actividades"
    const val PERFIL = "perfil"
}

/** Las cuatro pestañas de la barra inferior, igual que en el Figma. */
data class Pestana(val ruta: String, val etiqueta: String, val icono: ImageVector)

val pestanas = listOf(
    Pestana(Rutas.INICIO, "Inicio", Icons.Outlined.Home),
    Pestana(Rutas.EXPLORAR, "Explorar", Icons.Outlined.Search),
    Pestana(Rutas.MIS_ACTIVIDADES, "Mis Actividades", Icons.Outlined.DateRange),
    Pestana(Rutas.PERFIL, "Perfil", Icons.Outlined.Person)
)
