package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import mx.tec.familiasquesuman.FamiliasApplication
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.tec.familiasquesuman.domain.ActividadConAsociacion

/**
 * Antes de compartir se ve la tarjeta tal como le va a llegar a la otra familia. "Compartir"
 * abre WhatsApp, Instagram y demás con la imagen y el texto.
 */
@Composable
fun DialogoInvitacion(
    item: ActividadConAsociacion,
    familia: String?,
    lugares: Int?,
    onCerrar: () -> Unit,
    onCompartida: () -> Unit
) {
    val contexto = LocalContext.current
    // El enlace lo arma la API (con la dirección pública del túnel o el dominio propio): al
    // tocarlo, abre esta actividad en la app. Si no hay red, se comparte sin él.
    val enlace by produceState<String?>(null, item.actividad.id) {
        val repositorio = (contexto.applicationContext as FamiliasApplication).container.actividadRepository
        value = runCatching { repositorio.enlaceDe(item.actividad.id) }.getOrNull()
    }
    val imagen by produceState<Bitmap?>(null, item.actividad.id, familia, lugares) {
        value = withContext(Dispatchers.Default) {
            runCatching { dibujarInvitacion(contexto, item, familia, lugares) }.getOrNull()
        }
    }
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Web.Tarjeta,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Invita a otra familia", style = TextoWeb.Titulo.copy(fontSize = 21.sp))
                Text(
                    "Mándala por WhatsApp o súbela a tus historias. Entre más familias se sumen, mejor.",
                    style = TextoWeb.Cuerpo, color = Web.TextoApagado
                )
                Box(
                    Modifier.fillMaxWidth().height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val lista = imagen
                    if (lista == null) {
                        CircularProgressIndicator(color = Web.Primario)
                    } else {
                        Image(
                            bitmap = lista.asImageBitmap(),
                            contentDescription = "Vista previa de la invitación a ${item.actividad.titulo}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(400.dp)
                                .aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Web.Secundario)
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCerrar) { Text("Ahora no", color = Web.Primario) }
                    Button(
                        onClick = {
                            val lista = imagen
                            if (lista != null) compartirInvitacion(contexto, lista, item, familia, lugares, enlace)
                            else compartirActividad(contexto, item, familia, lugares, enlace)
                            onCompartida()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Web.Amarillo, contentColor = Web.Primario)
                    ) {
                        Icon(IconosWeb.Compartir, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("Compartir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
