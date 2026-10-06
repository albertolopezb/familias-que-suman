package mx.tec.familiasquesuman.ui.screens.perfil

import mx.tec.familiasquesuman.ui.components.BarraSuperior
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.OpcionEncuesta
import mx.tec.familiasquesuman.ui.theme.*

@Composable
fun EncuestaPreviaScreen(
    estado: EstadoEncuesta,
    onVolver: () -> Unit,
    onSeleccionarRespuesta: (Int) -> Unit,
    onSiguiente: () -> Unit,
    modifier: Modifier = Modifier,
    contextoActividad: String? = null
) {
    Column(modifier.fillMaxSize().background(Fondo)) {
        BarraSuperior("Antes de ir", onRegresar = onVolver)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Surface(shape = RoundedCornerShape(16.dp), color = AcentoSuave) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.DateRange, contentDescription = null, tint = AcentoTexto, modifier = Modifier.size(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        // Pendiente: actividad de entrada y fecha/hora relativa. No se afirma "mañana" sin datos.
                        contextoActividad?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = AcentoTexto) }
                        Text("Responde estas 3 preguntas antes de ir.", style = MaterialTheme.typography.bodyMedium, color = AcentoTexto)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            Button(onClick = onSiguiente, enabled = estado.puedeAvanzar,
                modifier = Modifier.fillMaxWidth().padding(18.dp).heightIn(min = 56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MarcaOro, contentColor = MarcaAzul)) {
                Text("Siguiente", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 740)
@Composable
private fun EncuestaPreviaPreview() {
    FamiliasQueSumanTheme {
        // Texto de referencia exclusivo del preview; no indica una actividad real programada mañana.
        EncuestaPreviaScreen(EstadoEncuesta(EncuestaPreviaP33, respuestas = mapOf(1 to 0)), {}, {}, {},
            contextoActividad = "Preparar despensas · mañana a las 9:00.")
    }
}
