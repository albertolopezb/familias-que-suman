package mx.tec.familiasquesuman.ui.screens.perfil

import mx.tec.familiasquesuman.ui.components.BarraSuperior
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.TarjetaPrivacidad
import mx.tec.familiasquesuman.ui.theme.*

private val IconoDatosPrivacidad = ImageVector.Builder("Datos", 24.dp, 24.dp, 24f, 24f).apply {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
        moveTo(4f, 4f); lineTo(20f, 4f); lineTo(20f, 20f); lineTo(4f, 20f); close()
        moveTo(8f, 8f); lineTo(16f, 8f); moveTo(8f, 12f); lineTo(13f, 12f)
    }
}.build()
private val IconoVisibilidadPrivacidad = ImageVector.Builder("Visibilidad", 24.dp, 24.dp, 24f, 24f).apply {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
        moveTo(1f, 12f); curveTo(7f, 2f, 17f, 2f, 23f, 12f)
        curveTo(17f, 22f, 7f, 22f, 1f, 12f); close()
        moveTo(15f, 12f); curveTo(15f, 16f, 9f, 16f, 9f, 12f)
        curveTo(9f, 8f, 15f, 8f, 15f, 12f); close()
    }
}.build()
private val IconoFotosPrivacidad = ImageVector.Builder("Fotos", 24.dp, 24.dp, 24f, 24f).apply {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
        moveTo(4f, 4f); lineTo(20f, 4f); lineTo(20f, 20f); lineTo(4f, 20f); close()
        moveTo(4f, 14f); lineTo(8f, 10f); lineTo(13f, 15f); lineTo(17f, 11f); lineTo(20f, 14f)
        moveTo(8f, 7f); lineTo(8.1f, 7f)
    }
}.build()

@Composable
fun AvisoPrivacidadScreen(
    estado: EstadoAvisoPrivacidad,
    conSesion: Boolean,
    onVolver: () -> Unit,
    onSolicitarEliminacion: () -> Unit,
    onConfirmarEliminacion: () -> Unit,
    onCancelarConfirmacion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().background(Fondo)) {
        BarraSuperior("Aviso de privacidad", onRegresar = onVolver)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            estado.folio?.let { folio ->
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = ConfirmadoFondo) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = ConfirmadoTexto)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Solicitud enviada", style = MaterialTheme.typography.titleMedium, color = ConfirmadoTexto)
                            Text("Tu folio es $folio. Eliminaremos tus datos en un máximo de 30 días y te avisaremos por correo.",
                                style = MaterialTheme.typography.bodyLarge, color = ConfirmadoTexto)
                        }
                    }
                }
            }
            Text("En lenguaje claro: qué datos guardamos de tu familia y para qué.",
                style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
            // Contenido transcrito de P-23, no descripción de servicios ya implementados.
            TarjetaPrivacidad("Qué guardamos",
                "El nombre de tu familia, tu correo, y el nombre y la edad de quienes registras como acompañantes.",
                IconoDatosPrivacidad)
            TarjetaPrivacidad("Para qué sirve",
                "Para que la asociación sepa a quién esperar el día de la actividad y para armar tu historial de participación.",
                Icons.Outlined.Info)
            TarjetaPrivacidad("Quién lo ve",
                "Solo Familias que Suman y la asociación de la actividad en la que te inscribes. No se vende ni se comparte con nadie más.",
                IconoVisibilidadPrivacidad)
            TarjetaPrivacidad("Las fotos",
                "Las fotos de los testimonios se guardan en nuestra propia infraestructura y solo se publican si tú las envías. Puedes eliminar tu testimonio cuando quieras.",
                IconoFotosPrivacidad)
            TarjetaPrivacidad("Tus derechos",
                "Puedes pedir que eliminemos tus datos cuando quieras. Al solicitarlo se eliminan tu cuenta, tus acompañantes y tus inscripciones.",
                Icons.Outlined.Delete)
            estado.mensaje?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = TintaSuave) }
        }
        Surface(color = Superficie, shadowElevation = 4.dp) {
            OutlinedButton(onClick = onSolicitarEliminacion,
                enabled = estado.folio == null && !estado.enviando,
                modifier = Modifier.fillMaxWidth().padding(12.dp).heightIn(min = 52.dp),
                shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, if (estado.folio == null) ErrorRojo else Borde),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRojo)) {
                if (estado.enviando) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = ErrorRojo, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Enviando solicitud…", style = MaterialTheme.typography.titleMedium)
                } else {
                    Text(if (estado.folio == null) "Solicitar eliminación de mis datos" else "Solicitud en proceso",
                        style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (estado.confirmando) {
        AlertDialog(
            onDismissRequest = onCancelarConfirmacion,
            title = { Text(if (conSesion) "¿Eliminar tus datos?" else "Inicia sesión primero") },
            text = {
                Text(if (conSesion)
                    "Enviaremos tu solicitud para borrar tu cuenta, tus acompañantes y tus inscripciones. Esta acción no se puede deshacer."
                else "Para solicitar la eliminación necesitamos saber de qué cuenta son los datos.")
            },
            confirmButton = {
                TextButton(onClick = onConfirmarEliminacion) {
                    Text(if (conSesion) "Sí, eliminar" else "Entendido", color = ErrorRojo)
                }
            },
            dismissButton = if (conSesion) {
                { TextButton(onClick = onCancelarConfirmacion) { Text("Cancelar") } }
            } else null
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 740)
@Composable
private fun AvisoPrivacidadPreview() {
    FamiliasQueSumanTheme { AvisoPrivacidadScreen(EstadoAvisoPrivacidad(), true, {}, {}, {}, {}) }
}
