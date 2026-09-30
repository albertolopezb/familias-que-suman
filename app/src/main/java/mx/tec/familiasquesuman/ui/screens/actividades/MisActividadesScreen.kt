package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.EstadoParticipacion
import mx.tec.familiasquesuman.domain.Participacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonAmarillo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.CirculoDeIcono
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.FilaDato
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconoTema
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TituloDePagina
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.actividadDeMuestra
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.horaDeInicio
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * La pestaña "Mis Actividades" (RF-11): lo que viene y lo que ya pasó, con el
 * mismo lenguaje visual que familiasquesuman.com/actividades.
 *
 * El historial se arma solo con asistencias registradas por la asociación,
 * no con lo que la familia declare por su cuenta.
 */
@Composable
fun MisActividadesScreen(
    datos: MisActividades,
    ciudad: String,
    onCiudadClick: () -> Unit,
    onProximaClick: (String) -> Unit,
    onCancelar: (String) -> Unit,
    onResponderEncuesta: (String) -> Unit,
    onCompartirTestimonio: (String) -> Unit,
    onVerActividades: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Web.Fondo)
    ) {
        EncabezadoApp()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "titulo") {
                TituloDePagina(
                    titulo = "Mis Actividades",
                    subtitulo = "${datos.proximas.size} próximas · ${datos.historial.size} realizadas"
                )
            }

            if (datos.proximas.isEmpty() && datos.historial.isEmpty()) {
                item(key = "vacia") { TodaviaSinActividades(onVerActividades = onVerActividades) }
            }

            if (datos.proximas.isNotEmpty()) {
                item(key = "rotulo-proximas") { Rotulo("PRÓXIMAS") }
                items(datos.proximas, key = { "prox-${it.actividad.id}" }) { item ->
                    TarjetaProxima(
                        item = item,
                        onClick = { onProximaClick(item.actividad.id) },
                        onCancelar = { onCancelar(item.actividad.id) }
                    )
                }
            }

            if (datos.historial.isNotEmpty()) {
                item(key = "rotulo-historial") { Rotulo("HISTORIAL") }
                datos.historial.groupBy { it.mes }.forEach { (mes, participaciones) ->
                    item(key = "mes-$mes") {
                        Text(
                            text = mes.lowercase().replaceFirstChar { it.uppercase() },
                            style = TextoWeb.Chico.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    items(participaciones, key = { it.id }) { participacion ->
                        TarjetaParticipacion(
                            participacion = participacion,
                            onResponderEncuesta = { onResponderEncuesta(participacion.id) },
                            onCompartirTestimonio = { onCompartirTestimonio(participacion.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Rotulo(texto: String) {
    Text(text = texto, style = TextoWeb.Rotulo, modifier = Modifier.padding(top = 8.dp))
}

/**
 * Todavía no hay nada que mostrar. El historial no se llena solo: se construye
 * con las asistencias que registra la asociación, no con lo que la familia diga.
 */
@Composable
private fun TodaviaSinActividades(onVerActividades: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CirculoDeIcono(icono = IconosWeb.Calendario, color = Web.Primario, fondo = Web.Secundario)
        Text(
            text = "Todavía no se han unido a ninguna actividad",
            style = TextoWeb.TituloTarjeta,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Aquí van a aparecer las actividades a las que se unan y las que ya " +
                "hayan hecho, conforme la asociación registre su asistencia.",
            style = TextoWeb.Cuerpo,
            color = Web.TextoApagado,
            textAlign = TextAlign.Center
        )
        BotonAmarillo(
            texto = "Ver actividades",
            onClick = onVerActividades,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
    }
}

@Composable
private fun TarjetaBase(
    onClick: (() -> Unit)?,
    contenido: @Composable () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Web.Tarjeta)
            .border(1.dp, Web.Borde, forma)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp)
    ) { contenido() }
}

@Composable
private fun TarjetaProxima(
    item: ActividadConAsociacion,
    onClick: () -> Unit,
    onCancelar: () -> Unit
) {
    val actividad = item.actividad
    TarjetaBase(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconoTema(tema = actividad.tema)
                Text(
                    text = actividad.titulo,
                    style = TextoWeb.TituloTarjeta.copy(fontSize = 16.sp, lineHeight = 20.sp),
                    modifier = Modifier.weight(1f)
                )
                Etiqueta("Inscritos", Web.VerdeFondo, Web.VerdeTexto)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FilaDato(
                    icono = IconosWeb.Calendario,
                    texto = "${actividad.fecha} · ${horaDeInicio(actividad.horario)}"
                )
                if (actividad.direccion.isNotBlank()) {
                    FilaDato(icono = IconosWeb.Ubicacion, texto = actividad.direccion)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EnlaceAccion("Ver detalle", Web.Primario, onClick)
                EnlaceAccion("Cancelar inscripción", Web.RojoTexto, onCancelar)
            }
        }
    }
}

@Composable
private fun TarjetaParticipacion(
    participacion: Participacion,
    onResponderEncuesta: () -> Unit,
    onCompartirTestimonio: () -> Unit
) {
    TarjetaBase(onClick = null) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Web.Secundario),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        IconosWeb.Historial,
                        contentDescription = null,
                        tint = Web.Primario,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = participacion.tituloActividad,
                        style = TextoWeb.TituloTarjeta.copy(fontSize = 16.sp, lineHeight = 20.sp)
                    )
                    Text(
                        text = "${participacion.nombreAsociacion} · ${participacion.fecha}",
                        style = TextoWeb.Chico
                    )
                }
            }

            when (participacion.estado) {
                EstadoParticipacion.TESTIMONIO_PUBLICADO ->
                    Etiqueta("Testimonio publicado", Web.VerdeFondo, Web.VerdeTexto)

                EstadoParticipacion.TESTIMONIO_EN_REVISION ->
                    Etiqueta("Testimonio en revisión", Web.MoradoFondo, Web.MoradoTexto)

                EstadoParticipacion.ENCUESTA_PENDIENTE -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Etiqueta("Encuesta pendiente", Web.AmbarFondo, Web.AmbarTexto)
                    BotonAmarillo(
                        texto = "Responder",
                        onClick = onResponderEncuesta,
                        conFlechas = false,
                        alto = 34.dp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                EstadoParticipacion.SIN_PENDIENTES ->
                    EnlaceAccion("Compartir testimonio", Web.Primario, onCompartirTestimonio)
            }
        }
    }
}

@Composable
private fun EnlaceAccion(texto: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = texto, style = TextoWeb.Chip, color = color)
        Icon(
            IconosWeb.FlechaDerecha,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun MisActividadesPreview() {
    FamiliasQueSumanTheme {
        MisActividadesScreen(
            datos = MisActividades(
                proximas = listOf(actividadDeMuestra(8)),
                historial = listOf(
                    Participacion(
                        id = "p1",
                        tituloActividad = "Mega Limpieza San Pedro",
                        nombreAsociacion = "Cíclica",
                        fecha = "sábado, 26 de septiembre",
                        mes = "SEPTIEMBRE",
                        estado = EstadoParticipacion.ENCUESTA_PENDIENTE
                    ),
                    Participacion(
                        id = "p2",
                        tituloActividad = "Regalando Estrellas Visita al Materno Infantil",
                        nombreAsociacion = "Regalando Estrellas",
                        fecha = "sábado, 1 de agosto",
                        mes = "AGOSTO",
                        estado = EstadoParticipacion.TESTIMONIO_PUBLICADO
                    ),
                    Participacion(
                        id = "p3",
                        tituloActividad = "Mega Limpieza",
                        nombreAsociacion = "Cíclica",
                        fecha = "sábado, 4 de julio",
                        mes = "JULIO",
                        estado = EstadoParticipacion.SIN_PENDIENTES
                    )
                )
            ),
            ciudad = "Monterrey, N.L.",
            onCiudadClick = {},
            onProximaClick = {},
            onCancelar = {},
            onResponderEncuesta = {},
            onCompartirTestimonio = {},
            onVerActividades = {}
        )
    }
}

@Preview(name = "Vacía", showBackground = true)
@Composable
private fun MisActividadesVaciaPreview() {
    FamiliasQueSumanTheme {
        MisActividadesScreen(
            datos = MisActividades(proximas = emptyList(), historial = emptyList()),
            ciudad = "Monterrey, N.L.",
            onCiudadClick = {},
            onProximaClick = {},
            onCancelar = {},
            onResponderEncuesta = {},
            onCompartirTestimonio = {},
            onVerActividades = {}
        )
    }
}
