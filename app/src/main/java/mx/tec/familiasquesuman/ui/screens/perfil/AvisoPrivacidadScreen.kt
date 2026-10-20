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
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
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

/**
 * Quién es responsable de los datos (LFPDPPP art. 15-I). Un solo lugar para cambiarlo.
 * Tomado del aviso oficial del sitio (familiasquesuman.com/aviso-de-privacidad, julio 2026): una
 * asociación civil con domicilio en Monterrey y el correo hola@. El sitio todavía no publica la
 * razón social ni el domicilio completo; cuando lo haga, se cambia aquí.
 */
object Responsable {
    const val NOMBRE = "Familias que Suman, asociación civil"
    const val DOMICILIO = "Monterrey, Nuevo León, México"
    const val CORREO = "hola@familiasquesuman.com"
    const val AVISO_DEL_SITIO = "familiasquesuman.com/aviso-de-privacidad"
    const val ACTUALIZADO = "20 de octubre de 2026"
}

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
                            Text("Tu folio es $folio. Guárdalo: con él puedes preguntar por tu solicitud en ${Responsable.CORREO}. " +
                                "Respondemos en un máximo de 20 días y, si procede, tus datos se borran en los 15 siguientes.",
                                style = MaterialTheme.typography.bodyLarge, color = ConfirmadoTexto)
                        }
                    }
                }
            }
            Text("En lenguaje claro: qué datos guardamos de tu familia, para qué y cómo puedes pedir que los cambiemos o borremos.",
                style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
            // Aviso de privacidad integral, LFPDPPP (DOF 20-03-2025) art. 15, fracciones I a VI.
            // Las tarjetas 1 a 4 son el aviso simplificado del art. 16-II (el que se muestra al crear la cuenta).
            TarjetaPrivacidad("1. Quién es responsable",
                "${Responsable.NOMBRE}, con domicilio en ${Responsable.DOMICILIO}. " +
                    "Para cualquier tema de tus datos escríbenos a ${Responsable.CORREO}. " +
                    "Este aviso es el de la app; el de las actividades y el sitio está en ${Responsable.AVISO_DEL_SITIO}.",
                Icons.Outlined.Home)
            TarjetaPrivacidad("2. Qué datos guardamos",
                "De la cuenta: tu correo, tu contraseña (guardada como huella Argon2id; nadie puede leerla), el nombre de tu familia, " +
                    "tu nombre, fecha de nacimiento y sexo, tu ciudad y, si la subes, tu foto de perfil.\n" +
                    "De tu familia: nombre, fecha de nacimiento o edad y sexo de quienes registras como acompañantes. " +
                    "Si son menores de edad, guardamos que tú, como madre, padre o tutor, diste tu consentimiento.\n" +
                    "De tu participación: inscripciones, asistencias, favoritos, ajustes de avisos, encuestas, testimonios con foto y sugerencias.\n" +
                    "No pedimos datos sensibles (salud, religión, origen, preferencias, datos financieros ni biométricos).",
                IconoDatosPrivacidad)
            TarjetaPrivacidad("3. Para qué los usamos",
                "Necesarias para el servicio: crear tu cuenta e identificarte, mandarte a tu correo el código para cambiar tu contraseña " +
                    "cuando lo pides, inscribirte a actividades, avisar a quien organiza cuántas " +
                    "personas van y de qué edades, registrar tu asistencia y armar tu historial, y proteger la app contra abusos.\n" +
                    "Voluntarias (solo si tú las usas): publicar tu testimonio, contestar encuestas para mejorar las actividades, " +
                    "recibir recordatorios y avisos de tu ciudad y mandar sugerencias. No usamos tus datos para publicidad.",
                Icons.Outlined.Info)
            TarjetaPrivacidad("4. Cómo limitar su uso",
                "En Ajustes apagas los recordatorios y avisos. Testimonios, encuestas, sugerencias y foto de perfil son opcionales y puedes " +
                    "borrarlos. \"Cerca de mí\" solo se activa si lo tocas y das permiso. Y puedes pedir que borremos todo con el botón de abajo.",
                Icons.Outlined.Settings)
            TarjetaPrivacidad("Quién más los ve",
                "El equipo de Familias que Suman que lo necesita para su trabajo (por ejemplo, quien registra la asistencia) y, " +
                    "solo para coordinar una actividad, la institución que la organiza: recibe lo indispensable, como los nombres y " +
                    "edades de quienes van. No vendemos tus datos ni los usamos para publicidad. Para mandar el código de tu contraseña " +
                    "usamos un servicio de correo, que solo recibe tu dirección y ese mensaje. Para que la app llegue al servidor " +
                    "de forma cifrada usamos Cloudflare, y el servidor es una máquina de Microsoft Azure en Estados Unidos; los dos actúan como encargados, " +
                    "que no los usan para nada más. Solo los entregaríamos a una autoridad que los pida conforme a la ley.",
                IconoVisibilidadPrivacidad)
            TarjetaPrivacidad("Tu ubicación",
                "\"Cerca de mí\" usa tu ubicación aproximada (no la exacta) solo en tu teléfono, para ordenar las actividades por cercanía. " +
                    "No la recibimos ni la guardamos, y si no das permiso la app funciona igual.",
                Icons.Outlined.LocationOn)
            TarjetaPrivacidad("Las fotos y testimonios",
                "Se guardan en nuestro servidor (una máquina de Microsoft Azure en Estados Unidos), no en servicios de terceros. Antes de publicarse, el equipo los revisa; mientras tanto solo los ves tú. " +
                    "Al publicarse aparecen con el nombre de tu familia, nunca con tu correo. Puedes eliminarlos cuando quieras.",
                IconoFotosPrivacidad)
            TarjetaPrivacidad("Cómo los cuidamos",
                "Todo viaja por HTTPS. En el teléfono, tu sesión se guarda cifrada con una llave del propio teléfono, y para verlo sin internet solo se guarda el catálogo público (actividades, asociaciones, proyectos y campañas), nunca tus datos. En el servidor, " +
                    "cada cuenta solo puede ver lo suyo, los intentos de adivinar contraseñas se bloquean y queda una bitácora de las acciones " +
                    "sensibles (con una huella de la IP, no la IP).",
                Icons.Outlined.Lock)
            TarjetaPrivacidad("Cuánto tiempo los guardamos",
                "Mientras tengas tu cuenta. La sesión del teléfono vence a los 30 días sin uso. Al eliminar tu cuenta se borran tus datos, " +
                    "los de tus acompañantes, tus inscripciones, encuestas, testimonios y fotos. Solo se conserva la constancia de tu solicitud " +
                    "(folio, correo y fecha) y la bitácora de seguridad, que no tiene tu nombre; tus sugerencias se quedan sin tu nombre ni contacto.",
                Icons.Outlined.DateRange)
            TarjetaPrivacidad("Tus derechos (ARCO)",
                "Acceso y rectificación: tus datos están en Perfil y ahí los corriges. Cancelación: el botón de abajo genera un folio. " +
                    "Oposición, o cualquier duda: escribe a ${Responsable.CORREO} con tu folio o el correo de tu cuenta. " +
                    "Te respondemos en un máximo de 20 días y, si procede, se hace efectivo en los 15 días siguientes (art. 31). " +
                    "Si no te respondemos, puedes acudir a la Secretaría Anticorrupción y Buen Gobierno.",
                Icons.Outlined.Delete)
            TarjetaPrivacidad("Si este aviso cambia",
                "Publicaremos la nueva versión en esta pantalla, con su fecha, y lo anunciaremos en la app antes de que aplique.",
                Icons.Outlined.Refresh)
            Text("Fundamento: Ley Federal de Protección de Datos Personales en Posesión de los Particulares (DOF 20-03-2025), " +
                    "arts. 14 a 16, 18 y 21 a 34. Última actualización: ${Responsable.ACTUALIZADO}.",
                style = MaterialTheme.typography.bodySmall, color = TintaSuave)
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
                    "Enviaremos tu solicitud para borrar tu cuenta, tus acompañantes, inscripciones, encuestas, testimonios y fotos. Esta acción no se puede deshacer."
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
