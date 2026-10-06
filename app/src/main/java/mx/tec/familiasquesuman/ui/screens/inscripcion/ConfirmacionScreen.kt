package mx.tec.familiasquesuman.ui.screens.inscripcion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.domain.Acompanante
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraAccionesInferior
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPrimario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonSecundario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.IconoEnCirculo
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.Confirmado
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * P-07 · Inscripción confirmada.
 * @param asistentes el titular primero y luego sus acompañantes.
 */
@Composable
fun ConfirmacionScreen(
    actividad: Actividad,
    asociacion: String,
    asistentes: List<Acompanante>,
    onAgregarAlCalendario: () -> Unit,
    onVerMisActividades: () -> Unit,
    encuestaContestada: Boolean = false,
    onResponderEncuesta: () -> Unit = {}
) {
    Column(Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconoEnCirculo(Icons.Filled.Check, ConfirmadoFondo, Confirmado)
            Text("Ya están inscritos", style = MaterialTheme.typography.headlineMedium, color = Tinta)
            Text(
                "$asociacion ya tiene su lista.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                color = TintaSuave,
                textAlign = TextAlign.Center
            )
            TarjetaBlanca {
                Text(actividad.titulo, style = MaterialTheme.typography.titleMedium, color = Tinta)
                Spacer(Modifier.height(8.dp))
                RenglonConIcono(Icons.Outlined.DateRange, "${actividad.fecha}, ${actividad.horario}")
                Spacer(Modifier.height(6.dp))
                RenglonConIcono(Icons.Outlined.LocationOn, actividad.direccion)
            }
            TarjetaBlanca {
                Text("ASISTENTES", style = MaterialTheme.typography.labelSmall, color = TintaSuave)
                Spacer(Modifier.height(6.dp))
                asistentes.forEachIndexed { i, persona ->
                    val edad = edadParaMostrar(persona, esTitular = i == 0)
                    Text(
                        if (edad.isEmpty()) persona.nombre else "${persona.nombre} · $edad",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                        color = Tinta
                    )
                }
            }
            // RF-13: unas preguntas breves antes de la actividad.
            TarjetaBlanca {
                Text("ANTES DE IR", style = MaterialTheme.typography.labelSmall, color = TintaSuave)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (encuestaContestada) "Ya respondieron las preguntas de antes. ¡Gracias!"
                    else "Respondan unas preguntas breves para ayudar a preparar la actividad.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                    color = Tinta
                )
                if (!encuestaContestada) {
                    Spacer(Modifier.height(10.dp))
                    BotonSecundario("Responder preguntas", onResponderEncuesta)
                }
            }
            Text(
                "Si no pueden ir, cancelen desde su perfil hasta 12 horas antes.",
                style = MaterialTheme.typography.labelMedium,
                color = TintaSuave,
                textAlign = TextAlign.Center
            )
        }
        BarraAccionesInferior {
            BotonPrimario("Agregar al calendario", onAgregarAlCalendario)
            BotonSecundario("Ver mis actividades", onVerMisActividades)
        }
    }
}

@Composable
private fun TarjetaBlanca(contenido: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Superficie)
            .border(1.dp, Borde, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) { contenido() }
}

@Composable
private fun RenglonConIcono(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = TintaSuave, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp), color = Tinta)
    }
}

@Preview(showBackground = true, heightDp = 780, name = "P-07 · Inscripción confirmada")
@Composable
private fun ConfirmacionPreview() {
    FamiliasQueSumanTheme {
        ConfirmacionScreen(
            VistaPrevia.actividad, VistaPrevia.asociacion.nombre,
            listOf(VistaPrevia.titular) + VistaPrevia.acompanantes, {}, {}
        )
    }
}
