package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import android.content.Context
import android.content.Intent
import mx.tec.familiasquesuman.domain.ActividadConAsociacion

/**
 * Abre la hoja de compartir de Android con los datos de la actividad, como el
 * botón de compartir del sitio. Es texto plano: no sale nada a ningún servidor.
 */
fun compartirActividad(contexto: Context, item: ActividadConAsociacion) {
    val actividad = item.actividad
    val lugar = actividad.direccion.ifBlank { actividad.municipio }
    val texto = buildString {
        appendLine(actividad.titulo)
        appendLine("${actividad.fecha.replaceFirstChar { it.uppercase() }} · ${actividad.horario}")
        if (lugar.isNotBlank()) appendLine(lugar)
        appendLine()
        append("Súmate con tu familia en Familias que Suman.")
    }
    val intento = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, actividad.titulo)
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    contexto.startActivity(Intent.createChooser(intento, "Compartir actividad"))
}
