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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * La tarjeta de la lista de actividades (RF-04, RF-05).
 *
 * `cupoEnVivo = false` es para la pantalla sin conexión: los lugares no se
 * pueden garantizar sin internet, así que no se muestra un número que podría
 * estar viejo.
 */
@Composable
fun TarjetaActividad(
    item: ActividadConAsociacion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cupoEnVivo: Boolean = true
) {
    val actividad = item.actividad

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (cupoEnVivo) 1f else 0.75f)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Superficie)
            .border(1.dp, Borde, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Miniatura(tinte = tinteDeCategoria(item.asociacion.categoria))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = actividad.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = Tinta
                )
                Text(
                    text = item.asociacion.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TintaSuave
                )
                Text(
                    text = "${actividad.fecha} · ${actividad.horario}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Tinta
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EtiquetaEdad(actividad.edadMinima)
            if (cupoEnVivo) {
                ChipCupo(actividad)
            } else {
                Etiqueta(
                    texto = "Lugares no disponibles sin conexión",
                    fondo = Borde,
                    color = TintaSuave
                )
            }
        }

        if (cupoEnVivo) {
            BarraCupo(
                progreso = progresoDeCupo(actividad),
                color = colorDeCupo(actividad)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun TarjetaActividadPreview() {
    FamiliasQueSumanTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaActividad(item = actividadDeMuestra(8), onClick = {})
            TarjetaActividad(item = actividadDeMuestra(2), onClick = {})
            TarjetaActividad(item = actividadDeMuestra(0), onClick = {})
        }
    }
}

/**
 * Datos escritos a mano para las previews: una preview no le pide nada al
 * repositorio, ni siquiera cuando el repositorio todavía es una lista en memoria.
 */
internal fun actividadDeMuestra(lugaresDisponibles: Int) = ActividadConAsociacion(
    actividad = Actividad(
        id = "act1",
        titulo = "Preparar despensas de fin de mes",
        asociacionId = "a1",
        fecha = "Sábado 12 de septiembre",
        horario = "9:00 a 12:00",
        direccion = "Av. Rómulo Garza 240, Col. San Bernabé",
        edadMinima = 6,
        descripcion = "Vamos a armar 300 despensas para las familias de la colonia. " +
            "Se forman equipos de cuatro. Lleven ropa cómoda y agua.",
        cupoTotal = 20,
        lugaresDisponibles = lugaresDisponibles
    ),
    asociacion = Asociacion(
        id = "a1",
        nombre = "Comedor Comunitario San Bernabé",
        categoria = "Alimentación",
        descripcion = "Damos comida caliente a 180 familias de la colonia.",
        direccion = "Av. Rómulo Garza 240, Col. San Bernabé, Monterrey",
        telefono = "8100000001",
        whatsapp = "5218100000001",
        correo = "contacto@comedorsanbernabe.org"
    )
)
