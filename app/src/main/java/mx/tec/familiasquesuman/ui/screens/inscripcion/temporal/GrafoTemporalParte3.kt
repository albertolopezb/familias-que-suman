package mx.tec.familiasquesuman.ui.screens.inscripcion.temporal

import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import mx.tec.familiasquesuman.notificaciones.Notificaciones
import mx.tec.familiasquesuman.notificaciones.NotificacionesDemo
import mx.tec.familiasquesuman.notificaciones.rememberPedirPermisoNotificaciones
import mx.tec.familiasquesuman.ui.navigation.Rutas
import mx.tec.familiasquesuman.ui.screens.inscripcion.RutasInscripcion
import mx.tec.familiasquesuman.ui.screens.inscripcion.cuentaViewModel

/**
 * ⚠ TEMPORAL. Pantallas mínimas de otras partes para poder recorrer la parte 3:
 *
 *   Rutas.MIS_ACTIVIDADES     → lista de actividades + mis inscripciones   (parte 2)
 *   temporal_actividad/{id}   → detalle con "Inscribirme"                  (parte 2)
 *   Rutas.PERFIL              → sesión y el botón de demo de notificaciones (parte 5)
 *
 * Al integrar: se borra la línea `grafoTemporalParte3(nav)` de FamiliasApp.kt y esta
 * carpeta completa, y se conectan los saltos reales:
 *   - Detalle de actividad → "Inscribirme": RutasInscripcion.inscribirse(id)
 *   - Mis Actividades → actividad próxima:  RutasInscripcion.cancelar(id)
 *   - Ajustes → demo: NotificacionesDemo.dispararRecordatorio / dispararUrgencia
 *   - El navDeepLink de abajo se mueve al DetalleActividad real (notificación tocada).
 */
object RutasTemporales {
    const val DETALLE = "temporal_actividad/{actividadId}"
    fun detalle(id: String) = "temporal_actividad/$id"
}

fun NavGraphBuilder.grafoTemporalParte3(nav: NavController) {

    composable(Rutas.MIS_ACTIVIDADES) {
        val cuenta = cuentaViewModel()
        val actividades by cuenta.actividades.collectAsStateWithLifecycle()
        val inscripciones by cuenta.inscripciones.collectAsStateWithLifecycle()
        val sesion by cuenta.sesion.collectAsStateWithLifecycle()
        cuenta.ajusteLugares.collectAsStateWithLifecycle().value // recompone al cambiar los lugares

        ActividadesTemporalScreen(
            actividades = actividades.map { it to cuenta.lugaresDisponibles(it) },
            inscritas = if (sesion != null) actividades.filter { it.id in inscripciones } else emptyList(),
            onVerDetalle = { nav.navigate(RutasTemporales.detalle(it)) },
            onCasoDePrueba = { id, sinCupo -> nav.navigate(RutasInscripcion.inscribirse(id, sinCupo)) },
            onCancelar = { nav.navigate(RutasInscripcion.cancelar(it)) }
        )
    }

    composable(
        RutasTemporales.DETALLE,
        deepLinks = listOf(navDeepLink { uriPattern = Notificaciones.URI_ACTIVIDAD + "{actividadId}" })
    ) { entrada ->
        val actividadId = entrada.arguments?.getString("actividadId") ?: return@composable
        val cuenta = cuentaViewModel()
        val actividades by cuenta.actividades.collectAsStateWithLifecycle()
        val inscripciones by cuenta.inscripciones.collectAsStateWithLifecycle()
        val actividad = actividades.firstOrNull { it.id == actividadId } ?: return@composable
        var simularSinCupo by rememberSaveable { mutableStateOf(actividadId == "act2") }

        DetalleTemporalScreen(
            actividad = actividad,
            lugaresDisponibles = cuenta.lugaresDisponibles(actividad),
            yaInscritos = actividadId in inscripciones,
            simularSinCupo = simularSinCupo,
            onSimularSinCupoChange = { simularSinCupo = it },
            onInscribirme = { nav.navigate(RutasInscripcion.inscribirse(actividadId, simularSinCupo)) },
            onRegresar = { nav.popBackStack() }
        )
    }

    composable(Rutas.PERFIL) {
        val context = LocalContext.current
        val cuenta = cuentaViewModel()
        val sesion by cuenta.sesion.collectAsStateWithLifecycle()
        var activadas by rememberSaveable { mutableStateOf(Notificaciones.estanActivadas(context)) }
        val pedirPermiso = rememberPedirPermisoNotificaciones { activadas = it }
        val avisar: (Boolean) -> Unit = { enviada ->
            Toast.makeText(
                context,
                if (enviada) "Listo, revisa tus notificaciones" else "Primero activa las notificaciones",
                Toast.LENGTH_SHORT
            ).show()
        }

        PerfilTemporalScreen(
            sesion = sesion,
            notificacionesActivadas = activadas,
            onIniciarSesion = { nav.navigate(RutasInscripcion.INICIAR_SESION) },
            onCrearCuenta = { nav.navigate(RutasInscripcion.CREAR_CUENTA) },
            onCerrarSesion = cuenta::cerrarSesion,
            onPedirPermiso = pedirPermiso,
            onRecordatorio = { avisar(NotificacionesDemo.dispararRecordatorio(context)) },
            onUrgencia = { avisar(NotificacionesDemo.dispararUrgencia(context)) },
            onInscripcion = { avisar(NotificacionesDemo.dispararInscripcionConfirmada(context)) }
        )
    }
}
