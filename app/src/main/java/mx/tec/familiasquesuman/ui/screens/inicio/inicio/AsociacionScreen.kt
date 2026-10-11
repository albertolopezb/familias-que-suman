package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.ui.components.BarraSuperior
import mx.tec.familiasquesuman.ui.components.iniciales
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.FilaDato
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoMensaje
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoNavegacion
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoTelefono
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * El detalle de una asociación, con la misma ficha que un centro del Directorio: quién es,
 * sus actividades próximas y cómo contactarla.
 */
@Composable
fun AsociacionScreen(
    asociacion: Asociacion,
    esFavorito: Boolean,
    onToggleFavorito: () -> Unit,
    onVerActividades: () -> Unit,
    actividades: List<Actividad> = emptyList(),
    onActividadClick: (String) -> Unit = {},
    onWhatsApp: (String) -> Unit = {},
    onLlamar: (String) -> Unit = {},
    onComoLlegar: (String) -> Unit = {},
    onRegresar: () -> Unit = {}
) {
    Column(Modifier.fillMaxSize().background(Web.Fondo)) {
        BarraSuperior("Asociación", onRegresar = onRegresar) {
            IconButton(onClick = onToggleFavorito) {
                Icon(
                    if (esFavorito) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (esFavorito) "Quitar de favoritas" else "Guardar en favoritas",
                    tint = if (esFavorito) Web.Amarillo else Web.Primario
                )
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaFicha {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)).background(Web.AmbarFondo),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(iniciales(asociacion.nombre), style = TextoWeb.Titulo.copy(color = Web.AmbarTexto))
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Etiqueta(asociacion.categoria, Web.AmbarFondo, Web.AmbarTexto)
                        Text(asociacion.nombre, style = TextoWeb.Titulo.copy(fontSize = 20.sp, lineHeight = 26.sp))
                        if (asociacion.direccion.isNotBlank()) FilaDato(IconosWeb.Ubicacion, asociacion.direccion)
                    }
                }
                Text(asociacion.descripcion, style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado))
            }

            TarjetaFicha {
                Text("Próximas actividades", style = EstiloSeccion)
                if (actividades.isEmpty()) {
                    Text("Por ahora no tienen actividades abiertas. Guárdala en favoritas para enterarte cuando publiquen una.",
                        style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado))
                } else {
                    actividades.forEach { actividad ->
                        Surface(
                            onClick = { onActividadClick(actividad.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = Web.Secundario.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(actividad.titulo, style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.SemiBold, color = Web.Primario))
                                FilaDato(IconosWeb.Calendario, "${actividad.fecha} · ${actividad.horario}")
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (asociacion.whatsapp.isNotBlank()) {
                    BotonFicha("WhatsApp", IconoMensaje, Web.WhatsApp, onClick = { onWhatsApp(asociacion.whatsapp) })
                }
                if (asociacion.telefono.isNotBlank()) {
                    BotonFicha("Llamar: ${asociacion.telefono}", IconoTelefono, Web.VerdeTema,
                        onClick = { onLlamar(asociacion.telefono) }, relleno = false)
                }
                if (asociacion.direccion.any { it.isDigit() }) {
                    BotonFicha("Cómo llegar", IconoNavegacion, AzulMapa, onClick = { onComoLlegar(asociacion.direccion) })
                }
                if (asociacion.correo.isNotBlank()) {
                    Text("Correo: ${asociacion.correo}", style = TextoWeb.Chico, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
        Surface(color = Web.Tarjeta, shadowElevation = 6.dp) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonAmarillo(
                    texto = "Ver todas las actividades",
                    onClick = onVerActividades,
                    modifier = Modifier.fillMaxWidth(),
                    alto = 50.dp,
                    conFlechas = false
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun AsociacionPreview() {
    FamiliasQueSumanTheme {
        AsociacionScreen(
            Asociacion("a1", "Comedor Comunitario San Bernabé", "Alimentación",
                "Damos comida caliente a 180 familias de la colonia.", "Av. Rómulo Garza 240", "8100000001",
                "5218100000001", "contacto@comedor.org"),
            esFavorito = true, onToggleFavorito = {}, onVerActividades = {}
        )
    }
}
