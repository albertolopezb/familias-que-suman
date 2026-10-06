package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.MomentoEncuesta
import mx.tec.familiasquesuman.domain.RespuestaEncuesta
import mx.tec.familiasquesuman.domain.RespuestaPregunta
import mx.tec.familiasquesuman.ui.components.BarraSuperior
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * Consulta de las respuestas de las encuestas (RF-13), agrupadas por actividad. La familia ve las
 * suyas; el admin ve las de todas ([mostrarFamilia]) para medir la experiencia y el aprendizaje.
 */
@Composable
fun RespuestasEncuestasScreen(
    respuestas: List<RespuestaEncuesta>,
    mostrarFamilia: Boolean,
    onRegresar: () -> Unit
) {
    val porActividad = respuestas.groupBy { it.actividadId }.values.toList()
    Column(Modifier.fillMaxSize().background(Web.Fondo)) {
        BarraSuperior(if (mostrarFamilia) "Respuestas de encuestas" else "Mis respuestas", onRegresar = onRegresar)
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (porActividad.isEmpty()) {
                item(key = "vacio") {
                    Text(
                        "Todavía no hay respuestas. Aparecerán aquí al contestar las encuestas de antes y después de cada actividad.",
                        style = TextoWeb.Cuerpo, color = Web.TextoApagado, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    )
                }
            }
            items(porActividad, key = { it.first().actividadId }) { grupo ->
                TarjetaActividad(grupo, mostrarFamilia)
            }
        }
    }
}

@Composable
private fun TarjetaActividad(grupo: List<RespuestaEncuesta>, mostrarFamilia: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp), color = Web.Tarjeta,
        border = BorderStroke(1.dp, Web.Borde), modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(grupo.first().actividadTitulo, style = TextoWeb.TituloTarjeta.copy(color = Web.Primario))
            // Antes primero, después al final, y dentro de cada momento lo más reciente arriba.
            grupo.sortedBy { it.momento }.forEach { encuesta ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Etiqueta(
                            encuesta.momento.etiqueta,
                            if (encuesta.momento == MomentoEncuesta.ANTES) Web.AmbarFondo else Web.VerdeFondo,
                            if (encuesta.momento == MomentoEncuesta.ANTES) Web.AmbarTexto else Web.VerdeTexto
                        )
                        Text(
                            if (mostrarFamilia) "${encuesta.familia} · ${encuesta.fecha}" else encuesta.fecha,
                            style = TextoWeb.Chico
                        )
                    }
                    encuesta.respuestas.forEach { r ->
                        Column {
                            Text(r.pregunta, style = TextoWeb.Chico)
                            Text(r.respuesta, style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun RespuestasPreview() {
    FamiliasQueSumanTheme {
        RespuestasEncuestasScreen(
            respuestas = listOf(
                RespuestaEncuesta(
                    "e1", "p3", "Regalando Estrellas", "ana@correo.com", "Familia Rodríguez", MomentoEncuesta.DESPUES,
                    listOf(RespuestaPregunta(1, "¿Cómo se sintieron en la actividad?", "Muy bien, volveríamos")),
                    "2 ago 2026"
                )
            ),
            mostrarFamilia = true, onRegresar = {}
        )
    }
}
