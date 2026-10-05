package mx.tec.familiasquesuman.ui.screens.campanas.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.domain.OpcionDonacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.fotoDeActividad
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

private val EtiquetaTexto = Color(0xFFD97706)   // text-amber-600 del sitio

/**
 * Tarjeta de la lista de campañas, calcada de familiasquesuman.com/donar: imagen arriba,
 * etiqueta "Campaña", título, descripción, una línea de dato (opciones de donación o fecha
 * límite) y los botones "Quiero ayudar" / "Ver más".
 *
 * Lo propio de la app: si la campaña se puede apartar (RF-21), se ve su barra de avance.
 * [onAyudar] abre WhatsApp o la llamada de la campaña; lo resuelve el grafo, no la tarjeta.
 * [nombreAsociacion] queda por compatibilidad: el sitio no lo muestra en la tarjeta.
 */
@Composable
fun TarjetaCampana(
    campana: Campana,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAyudar: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") nombreAsociacion: String? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Web.Tarjeta,
        border = BorderStroke(1.dp, Web.Borde),
        shadowElevation = 2.dp
    ) {
        Column {
            ImagenCampana(campana)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(shape = RoundedCornerShape(50), color = Web.AmbarSuave) {
                    Text(
                        "Campaña",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = TextoWeb.Chip,
                        color = EtiquetaTexto
                    )
                }
                Text(
                    campana.titulo,
                    style = TextoWeb.TituloTarjeta.copy(
                        fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp
                    ),
                    color = Web.Primario
                )
                Text(
                    campana.descripcion,
                    style = TextoWeb.Chico.copy(lineHeight = 19.sp),
                    color = Web.TextoApagado
                )
                DatoDeLaCampana(campana)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonAyudar(
                        texto = campana.textoBoton,
                        conWhatsApp = campana.whatsapp != null,
                        onClick = onAyudar,
                        modifier = Modifier.weight(1f)
                    )
                    BotonVerMas(onClick = onClick)
                }
            }
        }
    }
}

/** La imagen de arriba: sobre blanco, centrada y sin recortar, con un tope de alto. */
@Composable
private fun ImagenCampana(campana: Campana) {
    val recurso = fotoDeActividad(campana.imagen)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Web.Tarjeta)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (recurso != null) {
            Image(
                painter = painterResource(recurso),
                contentDescription = campana.titulo,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 192.dp)
            )
        } else {
            // La foto todavía no está en res/drawable: se ve dónde va.
            Icon(
                IconoImagen,
                contentDescription = null,
                tint = Web.TextoApagado,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

/** Opciones de donación, fecha límite o avance de apartado, según lo que traiga la campaña. */
@Composable
private fun DatoDeLaCampana(campana: Campana) {
    when {
        campana.opcionesDonacion.isNotEmpty() -> {
            val n = campana.opcionesDonacion.size
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Web.Primario.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(IconoManoCorazon, null, tint = Web.Primario, modifier = Modifier.size(14.dp))
                    Text(
                        if (n == 1) "1 opción para aportar" else "$n opciones para aportar",
                        style = TextoWeb.Chip,
                        color = Web.Primario
                    )
                }
            }
        }
        campana.sePuedeApartar -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BarraMeta(progreso = campana.progreso, alto = 6.dp)
                Text(
                    "${campana.completados} de ${campana.metaTotal} ${campana.unidadMeta} completos",
                    style = TextoWeb.Chip,
                    color = Web.VerdeTexto
                )
            }
        }
        campana.cierra.isNotBlank() -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(IconoReloj, null, tint = Web.TextoApagado, modifier = Modifier.size(12.dp))
                Text("Hasta: ${campana.cierra}", style = TextoWeb.Chico, color = Web.TextoApagado)
            }
        }
    }
}

/** El botón verde. Con WhatsApp lleva la burbuja; sin WhatsApp, el teléfono. */
@Composable
private fun BotonAyudar(
    texto: String,
    conWhatsApp: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(12.dp),
        color = Web.Verde
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            Icon(
                if (conWhatsApp) IconoMensaje else IconoTelefono,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(texto, style = TextoWeb.Chip.copy(fontSize = 13.sp), color = Color.White)
        }
    }
}

@Composable
private fun BotonVerMas(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(12.dp),
        color = Web.Tarjeta,
        border = BorderStroke(1.dp, Web.Borde)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ver más", style = TextoWeb.Chip.copy(fontSize = 13.sp), color = Web.TextoApagado, textAlign = TextAlign.Center)
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Web.TextoApagado,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private val tapitas = Campana(
    "c2", "Tapitas que Suman", "", "", "30 de diciembre", false,
    "Recolección de tapitas de plástico para reciclaje", "", 0, 0, emptyList(),
    textoBoton = "Quiero juntar", whatsapp = "8110809078", telefono = "8110809078"
)

@Preview(showBackground = true)
@Composable
private fun TarjetaConFechaPreview() {
    FamiliasQueSumanTheme {
        TarjetaCampana(campana = tapitas, onClick = {}, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaConOpcionesPreview() {
    FamiliasQueSumanTheme {
        TarjetaCampana(
            campana = tapitas.copy(
                titulo = "Destellos de Luz", cierra = "", textoBoton = "Quiero ayudar",
                opcionesDonacion = listOf(
                    OpcionDonacion("Bastón para desplazamiento", "\$700"),
                    OpcionDonacion("Cirugía", "\$14,000")
                )
            ),
            onClick = {}, modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaApartablePreview() {
    FamiliasQueSumanTheme {
        TarjetaCampana(
            campana = tapitas.copy(
                titulo = "Bibliotecas Infantiles", cierra = "", whatsapp = null,
                textoBoton = "Quiero ayudar", unidadMeta = "cuentos", metaTotal = 300, completados = 100,
                articulos = listOf(ArticuloMeta("c4-1", "Cuentos infantiles", 300, 100))
            ),
            onClick = {}, modifier = Modifier.padding(16.dp)
        )
    }
}
