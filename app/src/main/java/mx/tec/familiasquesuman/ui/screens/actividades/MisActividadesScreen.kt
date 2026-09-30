package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.EstadoParticipacion
import mx.tec.familiasquesuman.domain.Participacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Miniatura
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.actividadDeMuestra
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.tinteDeCategoria
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * La pestaña "Mis Actividades" (RF-11): lo que viene y lo que ya pasó.
 *
 * El historial se arma solo con asistencias registradas por la asociación,
 * no con lo que la familia declare por su cuenta.
 */
@Composable
fun MisActividadesScreen(
    datos: MisActividades,
    onProximaClick: (String) -> Unit,
    onParticipacionClick: (String) -> Unit,
    onVerActividades: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp)
                .background(Superficie)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Mis Actividades",
                style = MaterialTheme.typography.titleLarge,
                color = Tinta
            )
            Text(
                text = "${datos.proximas.size} próximas · ${datos.historial.size} realizadas",
                style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave
            )
        }

        if (datos.proximas.isEmpty() && datos.historial.isEmpty()) {
            TodaviaSinActividades(onVerActividades = onVerActividades)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (datos.proximas.isNotEmpty()) {
                    item { RotuloDeSeccion("PRÓXIMAS") }
                    items(datos.proximas, key = { "prox-${it.actividad.id}" }) { item ->
                        TarjetaProxima(
                            item = item,
                            onClick = { onProximaClick(item.actividad.id) }
                        )
                    }
                }

                datos.historial.groupBy { it.mes }.forEach { (mes, participaciones) ->
                    item(key = "mes-$mes") { RotuloDeSeccion(mes) }
                    items(participaciones, key = { it.id }) { participacion ->
                        TarjetaParticipacion(
                            participacion = participacion,
                            onClick = { onParticipacionClick(participacion.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Todavía no hay nada que mostrar. El historial no se llena solo: se construye
 * con las asistencias que registra la asociación, no con lo que la familia diga.
 */
@Composable
private fun TodaviaSinActividades(onVerActividades: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.DateRange,
            contentDescription = null,
            tint = TintaSuave,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = "Todavía no se han inscrito a nada",
            style = MaterialTheme.typography.titleLarge,
            color = Tinta,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Aquí van a aparecer las actividades en las que se inscriban y " +
                "las que ya hayan hecho, conforme la asociación registre su asistencia.",
            style = MaterialTheme.typography.bodyMedium,
            color = TintaSuave,
            textAlign = TextAlign.Center
        )
        OutlinedButton(onClick = onVerActividades) {
            Text("Ver actividades")
        }
    }
}

@Composable
private fun RotuloDeSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        color = TintaSuave,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun TarjetaProxima(item: ActividadConAsociacion, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Superficie)
            .border(1.dp, Borde, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Miniatura(
                tinte = tinteDeCategoria(item.asociacion.categoria),
                tamano = 46.dp,
                radio = 10.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.actividad.titulo,
                    style = MaterialTheme.typography.labelLarge,
                    color = Tinta
                )
                Text(
                    text = "${item.asociacion.nombre} · ${item.actividad.fecha}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TintaSuave
                )
            }
        }
        Etiqueta(texto = "Inscrita", fondo = ConfirmadoFondo, color = ConfirmadoTexto)
    }
}

@Composable
private fun TarjetaParticipacion(participacion: Participacion, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Superficie)
            .border(1.dp, Borde, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = participacion.tituloActividad,
            style = MaterialTheme.typography.labelLarge,
            color = Tinta
        )
        Text(
            text = "${participacion.nombreAsociacion} · ${participacion.fecha}",
            style = MaterialTheme.typography.labelMedium,
            color = TintaSuave
        )
        when (participacion.estado) {
            EstadoParticipacion.TESTIMONIO_PUBLICADO -> Etiqueta(
                texto = "Testimonio publicado",
                fondo = ConfirmadoFondo,
                color = ConfirmadoTexto
            )
            EstadoParticipacion.ENCUESTA_PENDIENTE -> Etiqueta(
                texto = "Encuesta pendiente",
                fondo = AcentoSuave,
                color = AcentoTexto
            )
            EstadoParticipacion.TESTIMONIO_EN_REVISION -> Etiqueta(
                texto = "Testimonio en revisión",
                fondo = AcentoSuave,
                color = AcentoTexto
            )
            EstadoParticipacion.SIN_PENDIENTES -> Unit
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MisActividadesPreview() {
    FamiliasQueSumanTheme {
        MisActividadesScreen(
            datos = MisActividades(
                proximas = listOf(actividadDeMuestra(8)),
                historial = listOf(
                    Participacion(
                        id = "p1",
                        tituloActividad = "Tarde de lectura con abuelitos",
                        nombreAsociacion = "Casa de Día Los Robles",
                        fecha = "Domingo 6 de septiembre",
                        mes = "SEPTIEMBRE",
                        estado = EstadoParticipacion.TESTIMONIO_PUBLICADO
                    ),
                    Participacion(
                        id = "p2",
                        tituloActividad = "Clasificar ropa de invierno",
                        nombreAsociacion = "Albergue Nuevo Amanecer",
                        fecha = "Sábado 30 de agosto",
                        mes = "AGOSTO",
                        estado = EstadoParticipacion.ENCUESTA_PENDIENTE
                    )
                )
            ),
            onProximaClick = {},
            onParticipacionClick = {},
            onVerActividades = {}
        )
    }
}
