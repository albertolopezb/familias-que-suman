package mx.tec.familiasquesuman.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            pestanas.forEach { pestana ->
                val activa = rutaActual != null &&
                    pestana.prefijos.any { rutaActual == it || rutaActual.startsWith("$it/") }
                Column(
                    // Seis pestañas: cada una ocupa su sexto del ancho para que quepan las etiquetas.
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { if (!activa || rutaActual != pestana.ruta) onPestana(pestana) }
                        .padding(horizontal = 2.dp, vertical = 4.dp),
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
