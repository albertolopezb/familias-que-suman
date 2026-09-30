package mx.tec.familiasquesuman.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import mx.tec.familiasquesuman.ui.screens.actividades.RutasActividades
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.campanas.RutasCampanas
import mx.tec.familiasquesuman.ui.screens.inicio.RutasInicio

object Rutas {
    const val INICIO = RutasInicio.INICIO
    const val EXPLORAR = RutasInicio.EXPLORAR
    const val MIS_ACTIVIDADES = RutasActividades.MIS_ACTIVIDADES
    const val PERFIL = "perfil"
}

/**
 * Una pestaña de la barra inferior. `prefijos` marca la pestaña como activa
 * también en las pantallas que cuelgan de ella (el detalle de una actividad
 * deja encendida "Actividades", como en el sitio).
 */
data class Pestana(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector,
    val prefijos: List<String> = listOf(ruta)
)

/** Las pestañas de la barra inferior. Perfil y Mis Actividades se abren desde el botón de perfil de arriba. */
val pestanas = listOf(
    Pestana(Rutas.INICIO, "Inicio", IconosWeb.Casa),
    Pestana(RutasActividades.LISTA, "Actividades", IconosWeb.Personas, listOf("actividades")),
    Pestana(RutasInicio.PROYECTOS, "Proyectos", IconosWeb.Foco),
    Pestana(RutasCampanas.LISTA, "Donar", IconosWeb.ManoCorazon, listOf("campanas")),
    Pestana(RutasInicio.VISITEO, "Directorio", IconosWeb.Ubicacion)
)
