package mx.tec.familiasquesuman.ui.screens.inicio

import mx.tec.familiasquesuman.ui.components.BarraSuperior
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.domain.FormaDeApoyo
import mx.tec.familiasquesuman.domain.IconoApoyo
import mx.tec.familiasquesuman.domain.Proyecto
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.FilaDato
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoMensaje
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoTelefono
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * Detalle de un proyecto (RF-09), calcado de familiasquesuman.com/proyectos/{id}:
 * logo y datos, "Acerca del proyecto", descripción, "Elige tu forma de apoyar" (o un solo
 * "¿Cómo ayudar?"), Instagram y los botones de WhatsApp y llamar.
 *
 * Tocar una forma de apoyo abre WhatsApp con "Quiero ayudar: <título>", igual que el sitio.
 * Pantalla "tonta": WhatsApp, llamadas y enlaces los resuelve el grafo.
 */
@Composable
fun DetalleProyectoScreen(
    proyecto: Proyecto,
    onIrAInicio: () -> Unit = {},
    onIrAProyectos: () -> Unit = {},
    onFormaDeApoyo: (FormaDeApoyo) -> Unit = {},
    onWhatsApp: () -> Unit = {},
    onLlamar: (String) -> Unit = {},
    onAbrirEnlace: (String) -> Unit = {}
) {
    Column(Modifier.fillMaxSize().background(Web.Fondo)) {
        BarraSuperior("Proyecto", onRegresar = onIrAProyectos)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaFicha {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Logo(proyecto.logo, proyecto.nombre, tamano = 72)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Etiqueta(
                            if (proyecto.activo) "Activo" else "Terminado",
                            if (proyecto.activo) Web.VerdeFondo else Web.Secundario,
                            if (proyecto.activo) Web.VerdeTexto else Web.TextoApagado
                        )
                        Text(proyecto.nombre, style = TextoWeb.Titulo.copy(fontSize = 20.sp, lineHeight = 26.sp))
                        FilaDato(IconosWeb.Ubicacion, proyecto.ciudad)
                        proyecto.vigencia?.let { FilaDato(IconosWeb.Calendario, it) }
                    }
                }
                proyecto.beneficiarios?.let { FilaDato(IconosWeb.Personas, it) }
                val resumen = proyecto.resumen.ifBlank { proyecto.descripcion }
                Text(resumen, style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado))
            }

            if (proyecto.acercaDe.isNotBlank()) SeccionFicha("Acerca del proyecto", proyecto.acercaDe)
            // Sin resumen, la descripción ya salió arriba; no se repite.
            if (proyecto.resumen.isNotBlank()) SeccionFicha("Descripción", proyecto.descripcion)

            if (proyecto.formasDeApoyo.isNotEmpty()) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Web.Primario)) {
                            append("Elige tu forma de apoyar")
                        }
                        append(" y que más se adapte a ti.")
                    },
                    style = TextoWeb.Cuerpo
                )
                proyecto.formasDeApoyo.forEachIndexed { i, forma ->
                    TarjetaFormaDeApoyo(forma, ColoresApoyo[i % ColoresApoyo.size], onClick = { onFormaDeApoyo(forma) })
                }
            } else if (proyecto.comoAyudar.isNotBlank()) {
                SeccionComoAyudar(proyecto.comoAyudar)
            }

            proyecto.instagram?.let { enlace -> SiguenosEnInstagram(onClick = { onAbrirEnlace(enlace) }) }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (proyecto.whatsapp != null) {
                    BotonFicha("Escribir por WhatsApp", IconoMensaje, Web.WhatsApp, onClick = onWhatsApp)
                }
                proyecto.telefono?.let { telefono ->
                    BotonFicha("Llamar: $telefono", IconoTelefono, Web.Amarillo, onClick = { onLlamar(telefono) })
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(IconosWeb.Escudo, null, tint = Web.TextoApagado, modifier = Modifier.size(14.dp))
                Text(
                    buildAnnotatedString {
                        append("Proyecto curado por ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Web.Primario)) {
                            append("Familias que Suman")
                        }
                    },
                    style = TextoWeb.Chico
                )
            }
        }
    }
}

/** El tono de cada tarjeta de apoyo, en el orden del sitio: ámbar, azul y verde. */
private data class ColorApoyo(val acento: Color, val borde: Color, val fondoIcono: Color)

private val ColoresApoyo = listOf(
    ColorApoyo(Color(0xFFF59E0B), Color(0xFFFDE68A), Color(0xFFFFFBEB)),
    ColorApoyo(Color(0xFF3B82F6), Color(0xFFBFDBFE), Color(0xFFEFF6FF)),
    ColorApoyo(Color(0xFF22C55E), Color(0xFFBBF7D0), Color(0xFFF0FDF4))
)

private fun iconoDe(icono: IconoApoyo): ImageVector = when (icono) {
    IconoApoyo.CORAZON -> IconosWeb.Corazon
    IconoApoyo.LIBRO -> IconosWeb.Libro
    IconoApoyo.DINERO -> IconosWeb.Tarjeta
}

/** Una tarjeta de "Elige tu forma de apoyar": ícono, barra de color, texto y flecha. */
@Composable
private fun TarjetaFormaDeApoyo(forma: FormaDeApoyo, color: ColorApoyo, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Web.Tarjeta,
        border = BorderStroke(1.5.dp, color.borde),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(color.fondoIcono),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconoDe(forma.icono), null, tint = color.acento, modifier = Modifier.size(24.dp))
            }
            Box(Modifier.width(3.dp).height(40.dp).clip(RoundedCornerShape(50)).background(color.acento))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(forma.titulo, style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.Bold), color = Web.Primario)
                Text(forma.detalle, style = TextoWeb.Chico)
            }
            Icon(IconosWeb.FlechaDerecha, contentDescription = "Quiero ayudar", tint = color.acento, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun DetalleProyectoPreview() {
    FamiliasQueSumanTheme {
        DetalleProyectoScreen(
            Proyecto(
                "pr3", "Cocinando de Corazón a Corazón",
                "Comedor comunitario diocesano que lleva alimento y esperanza a personas y familias.",
                null, "Monterrey", null, null,
                resumen = "Comedor comunitario de alimentos preparados por una red de amas de casa.",
                acercaDe = "Desde hace 6 años un conjunto de amas de casa comenzaron a apoyar cocinando.",
                formasDeApoyo = listOf(
                    FormaDeApoyo("Cocinando desde casa", "Elige un día fijo a la semana y cocina 20 platillos."),
                    FormaDeApoyo("Apoya al equipo del comedor", "Sirviendo y emplatando comidas."),
                    FormaDeApoyo("Aporta alimentos", "Cualquier tipo de alimento.")
                ),
                telefono = "8184596229", whatsapp = "8184596229",
                instagram = "https://www.instagram.com/cocinando_de_corazon/"
            )
        )
    }
}
