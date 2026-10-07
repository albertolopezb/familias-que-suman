package mx.tec.familiasquesuman.ui.screens.perfil

import mx.tec.familiasquesuman.ui.components.BarraSuperior
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.OpcionEncuesta
import mx.tec.familiasquesuman.ui.theme.*

@Composable
fun EncuestaScreen(
    estado: EstadoEncuesta,
    onVolver: () -> Unit,
    onSeleccionarRespuesta: (Int) -> Unit,
    onSiguiente: () -> Unit,
    onResponderDespues: () -> Unit,
    modifier: Modifier = Modifier,
    contextoActividad: String? = null
) {
    Column(modifier.fillMaxSize().background(Fondo)) {
        BarraSuperior("Encuesta final", onRegresar = onVolver)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                contextoActividad?.let { Text(it, style = MaterialTheme.typography.bodyLarge, color = Tinta) }
                Text("Pregunta ${estado.preguntaActual.numero} de ${estado.definicion.totalPreguntas}",
                    style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                LinearProgressIndicator(progress = { estado.progreso }, modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = MarcaOro, trackColor = Borde, gapSize = 0.dp, drawStopIndicator = {})
            }
            Text(estado.preguntaActual.texto, style = MaterialTheme.typography.headlineMedium, color = Tinta)
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                estado.preguntaActual.opciones.forEachIndexed { indice, opcion ->
                    OpcionEncuesta(opcion, estado.respuestaSeleccionada == indice, { onSeleccionarRespuesta(indice) })
                }
            }
            estado.mensaje?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = TintaSuave) }
        }
        Surface(color = Superficie, shadowElevation = 4.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onSiguiente, enabled = estado.puedeAvanzar && !estado.guardando,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MarcaOro, contentColor = MarcaAzul)) {
                    Text(if (estado.esUltima) "Enviar respuestas" else "Siguiente", style = MaterialTheme.typography.titleLarge)
                }
                OutlinedButton(onClick = onResponderDespues, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MarcaAzul)) {
                    Text("Responder después", style = MaterialTheme.typography.titleLarge, color = MarcaAzul)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 740)
@Composable
private fun EncuestaPreview() {
    FamiliasQueSumanTheme {
        EncuestaScreen(EstadoEncuesta(EncuestaFinal, respuestas = mapOf(1 to 0)), {}, {}, {}, {})
    }
}
