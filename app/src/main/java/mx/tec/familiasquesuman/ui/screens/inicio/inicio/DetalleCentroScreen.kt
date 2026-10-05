package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.domain.CentroVisiteo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoMensaje
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoNavegacion
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoTelefono
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * Detalle de un centro del Directorio de Visiteo (RF-03), calcado de
 * familiasquesuman.com/directorio/{id}: logo y datos, información general, necesidades,
 * "¿Cómo ayudar?", recomendaciones y los botones de WhatsApp, llamar y cómo llegar.
 *
 * Pantalla "tonta": WhatsApp, llamadas, mapas y enlaces los resuelve el grafo.
 */
@Composable
fun DetalleCentroScreen(
    centro: CentroVisiteo,
    onIrAInicio: () -> Unit = {},
    onIrADirectorio: () -> Unit = {},
    onWhatsApp: (String) -> Unit = {},
    onLlamar: (String) -> Unit = {},
    onComoLlegar: (String) -> Unit = {},
    onAbrirEnlace: (String) -> Unit = {}
) {
    Column(Modifier.fillMaxSize().background(Web.Fondo)) {
        EncabezadoApp()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MigasDePan(listOf("Inicio" to onIrAInicio, "Directorio" to onIrADirectorio), centro.nombre)

            TarjetaFicha {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Logo(centro.logo, centro.nombre, tamano = 72)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Etiqueta(tipoEnSingular(centro.tipo), Web.Secundario, Web.TextoApagado)
                        Text(centro.nombre, style = TextoWeb.Titulo.copy(fontSize = 20.sp, lineHeight = 26.sp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Top) {
                            Icon(IconosWeb.Ubicacion, null, tint = Web.TextoApagado, modifier = Modifier.padding(top = 2.dp).size(12.dp))
                            Text(centro.direccion, style = TextoWeb.Chico)
                        }
                        if (centro.verificado) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(IconosWeb.Escudo, null, tint = Web.Primario, modifier = Modifier.size(14.dp))
                                Text("Verificado", style = TextoWeb.Chip, color = Web.Primario)
                            }
                        }
                    }
                }
                Text(centro.resumen, style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado))
            }

            if (centro.informacion.isNotBlank()) SeccionFicha("Información general", centro.informacion)

            if (centro.necesidades.isNotEmpty()) {
                TarjetaFicha {
                    Text("Necesidades", style = EstiloSeccion)
                    centro.necesidades.forEach { necesidad ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("•", style = TextoWeb.Cuerpo, color = Web.Primario)
                            Text(necesidad, style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado))
                        }
                    }
                }
            }

            if (centro.comoAyudar.isNotBlank()) SeccionComoAyudar(centro.comoAyudar)
            if (centro.recomendaciones.isNotBlank()) SeccionRecomendaciones(centro.recomendaciones)

            centro.instagram?.let { enlace -> SiguenosEnInstagram(onClick = { onAbrirEnlace(enlace) }) }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                centro.whatsapp?.let { numero ->
                    BotonFicha("WhatsApp", IconoMensaje, Web.WhatsApp, onClick = { onWhatsApp(numero) })
                }
                centro.telefono?.let { telefono ->
                    BotonFicha("Llamar: $telefono", IconoTelefono, Web.VerdeTema, onClick = { onLlamar(telefono) }, relleno = false)
                }
                // El sitio no da "Cómo llegar" cuando la dirección es solo la ciudad.
                if (centro.direccion.any { it.isDigit() }) {
                    BotonFicha("Cómo llegar", IconoNavegacion, AzulMapa, onClick = { onComoLlegar(centro.direccion) })
                }
            }

            if (centro.verificado) {
                TarjetaFicha {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(IconosWeb.EscudoPalomita, null, tint = Web.Amarillo, modifier = Modifier.size(32.dp))
                        Column {
                            Text("Centro verificado", style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.SemiBold), color = Web.Primario)
                            Text("por Familias que Suman", style = TextoWeb.Chico)
                        }
                    }
                }
            }
        }
    }
}

/** El filtro de la lista va en plural ("Asilos"); la etiqueta del detalle, en singular. */
internal fun tipoEnSingular(tipo: String): String = when (tipo) {
    "Asilos" -> "Asilo"
    "Casas hogar" -> "Casa hogar"
    "Comedores" -> "Comedor"
    else -> tipo
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun DetalleCentroPreview() {
    FamiliasQueSumanTheme {
        DetalleCentroScreen(
            CentroVisiteo(
                "ce1", "Morada del Anciano Desvalido Cadereyta", "Asilos",
                "Asilo de ancianos donde se atienden 24 horas a 46 adultos mayores.",
                "Atención a adultos mayores en abandono, soledad y falta de apoyo familiar.",
                listOf("Alimentos", "Limpieza"), "Blvd José María González #1000, Cadereyta", null,
                comoAyudar = "Aportación en especie. Visita a los ancianos para convivir y platicar.",
                recomendaciones = "Hablar con anticipación para ver qué actividades se recomiendan.",
                telefono = "8282844311", whatsapp = "3318290852"
            )
        )
    }
}
