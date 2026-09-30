package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.Aportacion
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.TemaActividad
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * La tarjeta de la lista de actividades (RF-04, RF-05), igual que en
 * familiasquesuman.com/actividades: foto, ícono y título, chip de aportación,
 * fecha, lugar, capacidad, lugares tomados, descripción y los botones.
 *
 * Las que ya pasaron se ven sin botones; la lista las pinta más tenues.
 * `cupoEnVivo = false` es para la pantalla sin conexión: sin internet no se
 * puede garantizar cuántos lugares quedan, así que no se enseña un número viejo.
 */
@Composable
fun TarjetaActividad(
    item: ActividadConAsociacion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onUnirme: () -> Unit = {},
    onCompartir: () -> Unit = {},
    cupoEnVivo: Boolean = true
) {
    val actividad = item.actividad
    val forma = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Web.Tarjeta)
            .border(1.dp, Web.Borde, forma)
            .clickable(onClick = onClick)
    ) {
        actividad.foto?.let { foto ->
            FotoTarjeta(foto = foto, tema = actividad.tema, descripcion = actividad.titulo)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconoTema(tema = actividad.tema)
                    Text(text = actividad.titulo, style = TextoWeb.TituloTarjeta)
                }
                ChipAportacion(actividad.aportacion)
            }

            Column(
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilaDato(
                    icono = IconosWeb.Calendario,
                    texto = "${actividad.fecha} · ${horaDeInicio(actividad.horario)}"
                )
                if (actividad.direccion.isNotBlank()) {
                    FilaDato(icono = IconosWeb.Ubicacion, texto = actividad.direccion)
                }
                if (actividad.tieneCupo) {
                    FilaDato(
                        icono = IconosWeb.Personas,
                        texto = "Capacidad: ${actividad.cupoTotal} personas"
                    )
                }
            }

            if (actividad.tieneCupo) {
                if (cupoEnVivo) {
                    IndicadorCupo(actividad, modifier = Modifier.padding(bottom = 12.dp))
                } else {
                    Etiqueta(
                        texto = "Lugares no disponibles sin conexión",
                        fondo = Web.Secundario,
                        color = Web.TextoApagado,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            Text(
                text = actividad.descripcion,
                style = TextoWeb.Chico,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!actividad.yaPaso && cupoEnVivo) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonAmarillo(
                        texto = if (actividad.sinLugares) "Sin lugares" else "Unirme",
                        onClick = onUnirme,
                        habilitado = !actividad.sinLugares,
                        modifier = Modifier.weight(1f)
                    )
                    BotonContorno(
                        icono = IconosWeb.Compartir,
                        descripcion = "Compartir",
                        onClick = onCompartir
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun TarjetaActividadPreview() {
    FamiliasQueSumanTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaActividad(item = actividadDeMuestra(8), onClick = {})
            TarjetaActividad(item = actividadDeMuestra(2), onClick = {})
            TarjetaActividad(item = actividadDeMuestra(0), onClick = {})
            TarjetaActividad(
                item = actividadDeMuestra(0, yaPaso = true),
                onClick = {},
                modifier = Modifier.alpha(0.6f)
            )
        }
    }
}

/**
 * Datos escritos a mano para las previews: una preview no le pide nada al
 * repositorio, ni siquiera cuando el repositorio todavía es una lista en memoria.
 */
internal fun actividadDeMuestra(
    lugaresDisponibles: Int,
    yaPaso: Boolean = false
) = ActividadConAsociacion(
    actividad = Actividad(
        id = "act2",
        titulo = "Posada Sendero",
        asociacionId = "o1",
        fecha = "sábado, 28 de noviembre",
        horario = "09:30 – 12:00",
        direccion = "San Ildefonso SN, Colonia Sendero, Santa Catarina",
        edadMinima = null,
        descripcion = "Posada navideña para las familias del Arroyo en la Colonia Sendero.",
        cupoTotal = 15,
        lugaresDisponibles = lugaresDisponibles,
        municipio = "Monterrey",
        tema = TemaActividad.CELEBRACION,
        aportacion = Aportacion.EnEspecie(),
        puntoDeEncuentro = "Centro Comunitario",
        acercaDelProyecto = "Llevamos una mañana de regalos y juegos a las familias del " +
            "Arroyo en la Colonia Sendero.",
        queHaremos = "Juegos, regalos, merienda y convivencia.",
        queLlevar = "Se junta una cantidad previa para hacer pagos de flautas, juguetes, " +
            "pasteles, etc.",
        recomendaciones = "Ropa cómoda y sencilla.",
        foto = "actividad_posada_sendero",
        yaPaso = yaPaso
    ),
    asociacion = Asociacion(
        id = "o1",
        nombre = "Familias que Suman",
        categoria = "Voluntariado familiar",
        descripcion = "Conectamos familias con oportunidades de voluntariado en su ciudad.",
        direccion = "Monterrey, N.L.",
        telefono = "8100000005",
        whatsapp = "5218100000005",
        correo = "contacto@familiasquesuman.org"
    )
)
