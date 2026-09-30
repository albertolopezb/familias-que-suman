package mx.tec.familiasquesuman.notificaciones

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import mx.tec.familiasquesuman.R

/**
 * Notificaciones reales del sistema (P-25): canales, permiso y envío.
 *
 * Al tocar una notificación se abre la app con el enlace `familiasquesuman://actividad/{id}`.
 * La pantalla que registre ese `navDeepLink` es la que se abre: hoy es el detalle temporal
 * de la parte 3; al integrar, el enlace se mueve al DetalleActividad de la parte 2.
 * El enlace se maneja solo: NavHost lo lee del Intent al arrancar, sin tocar MainActivity.
 */
object Notificaciones {

    const val CANAL_RECORDATORIOS = "recordatorios"
    const val CANAL_URGENCIAS = "urgencias"
    const val CANAL_INSCRIPCIONES = "inscripciones"

    /** Prefijo del enlace profundo. El patrón completo es URI_ACTIVIDAD + "{actividadId}". */
    const val URI_ACTIVIDAD = "familiasquesuman://actividad/"

    /** Crea los canales. Llamarlo más de una vez no hace nada, así que se llama antes de cada envío. */
    fun crearCanales(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(CANAL_RECORDATORIOS, "Recordatorios de actividades", NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = "Un aviso 24 horas antes de cada actividad en la que te inscribas." },
                NotificationChannel(CANAL_URGENCIAS, "Urgencias en tu ciudad", NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = "Cuando una asociación que sigues necesita voluntarios pronto." },
                NotificationChannel(CANAL_INSCRIPCIONES, "Inscripciones", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "La confirmación de cada inscripción." }
            )
        )
    }

    /** Android 13+ pide el permiso POST_NOTIFICATIONS en tiempo de ejecución; antes venía dado. */
    fun faltaPermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED

    /** false si falta el permiso o la familia las apagó en Ajustes del sistema. */
    fun estanActivadas(context: Context): Boolean =
        !faltaPermiso(context) && NotificationManagerCompat.from(context).areNotificationsEnabled()

    /**
     * Publica una notificación. Devuelve false si no se pudo (sin permiso), sin tronar.
     * @param actividadId la actividad que se abre al tocarla.
     */
    @SuppressLint("MissingPermission") // se revisa con estanActivadas() justo antes
    fun enviar(
        context: Context,
        titulo: String,
        texto: String,
        actividadId: String,
        canal: String = CANAL_RECORDATORIOS
    ): Boolean {
        if (!estanActivadas(context)) return false
        crearCanales(context)

        val abrirApp = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(URI_ACTIVIDAD + actividadId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        } ?: return false
        val id = (canal + actividadId).hashCode()
        val alTocar = PendingIntent.getActivity(
            context, id, abrirApp, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, canal)
            // Silueta del S+: Android la pinta de un color. El logo a color lo pone el
            // sistema solo, con el ícono de la app; no hace falta una imagen grande.
            .setSmallIcon(R.drawable.ic_notificacion)
            .setColor(0xFFE9B44C.toInt()) // MarcaOro
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(alTocar)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(id, notificacion)
        return true
    }
}

/**
 * Pide el permiso de notificaciones. Para el botón "Activar notificaciones" de P-37 (parte 1):
 *
 *   val pedirPermiso = rememberPedirPermisoNotificaciones { onContinuar() }
 *   PermisoNotificacionesScreen(onActivar = pedirPermiso, ...)
 *
 * En Android 12 o menos, o si ya lo dieron, responde `true` enseguida sin mostrar nada.
 */
@Composable
fun rememberPedirPermisoNotificaciones(onResultado: (concedido: Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) Notificaciones.crearCanales(context)
        onResultado(concedido)
    }
    return {
        if (Notificaciones.faltaPermiso(context)) {
            lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            Notificaciones.crearCanales(context)
            onResultado(true)
        }
    }
}
