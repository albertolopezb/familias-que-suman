package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.FilaDato
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoMensaje
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoTelefono
import mx.tec.familiasquesuman.ui.theme.AzulCategoriaFondo
import mx.tec.familiasquesuman.ui.theme.AzulCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.AzulTarjetaFondo
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul

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
    Column(
        Modifier
            .fillMaxSize()
            .background(Web.Fondo)
    ) {
        EncabezadoApp()

        // Barra superior con botón para regresar a Proyectos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                    .clickable(onClick = onIrAProyectos),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver a Proyectos",
                    tint = MarcaAzul,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Volver a Proyectos",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MarcaAzul,
                modifier = Modifier.clickable(onClick = onIrAProyectos)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta Principal Hero del Proyecto
            TarjetaHeroProyecto(proyecto)

            // Secciones de Información detallada
            if (proyecto.acercaDe.isNotBlank()) {
                TarjetaContenido(
                    titulo = "Acerca del proyecto",
                    contenido = proyecto.acercaDe
                )
            }

            if (proyecto.resumen.isNotBlank()) {
                TarjetaContenido(
                    titulo = "Descripción",
                    contenido = proyecto.descripcion
                )
            }

            // Opciones para apoyar
            if (proyecto.formasDeApoyo.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MarcaAzul)) {
                                append("Elige tu forma de apoyar")
                            }
                            append(" y que más se adapte a ti.")
                        },
                        fontSize = 16.sp,
                        color = Web.Texto.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                    proyecto.formasDeApoyo.forEachIndexed { i, forma ->
                        TarjetaFormaDeApoyo(
                            forma = forma,
                            color = ColoresApoyo[i % ColoresApoyo.size],
                            onClick = { onFormaDeApoyo(forma) }
                        )
                    }
                }
            } else if (proyecto.comoAyudar.isNotBlank()) {
                SeccionComoAyudar(proyecto.comoAyudar)
            }

            proyecto.instagram?.let { enlace -> SiguenosEnInstagram(onClick = { onAbrirEnlace(enlace) }) }

            // Botones de Contacto Directo
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                if (proyecto.whatsapp != null) {
                    BotonFicha("Escribir por WhatsApp", IconoMensaje, Web.WhatsApp, onClick = onWhatsApp)
                }
                proyecto.telefono?.let { telefono ->
                    BotonFicha("Llamar: $telefono", IconoTelefono, Web.Amarillo, onClick = { onLlamar(telefono) })
                }
            }

            // Pie de confianza
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(IconosWeb.Escudo, null, tint = Web.TextoApagado, modifier = Modifier.size(14.dp))
                Text(
                    buildAnnotatedString {
                        append("Proyecto curado por ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = MarcaAzul)) {
                            append("Familias que Suman")
                        }
                    },
                    style = TextoWeb.Chico
                )
            }
        }
    }
}

/** Tarjeta Hero del Proyecto con esquinas suavizadas y estética de la app */
@Composable
private fun TarjetaHeroProyecto(proyecto: Proyecto) {
    val forma = RoundedCornerShape(28.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(AzulTarjetaFondo)
            .border(1.dp, AzulCategoriaFondo, forma)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogoCircular(proyecto.logo, proyecto.nombre)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (proyecto.activo) "Activo" else "Terminado",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulCategoriaTexto
                    )
                }
                Text(
                    text = proyecto.nombre,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MarcaAzul,
                    lineHeight = 28.sp
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FilaDato(IconosWeb.Ubicacion, proyecto.ciudad)
            proyecto.vigencia?.let { FilaDato(IconosWeb.Calendario, it) }
            proyecto.beneficiarios?.let { FilaDato(IconosWeb.Personas, it) }
        }

        val resumen = proyecto.resumen.ifBlank { proyecto.descripcion }
        if (resumen.isNotBlank()) {
            Text(
                text = resumen,
                fontSize = 14.sp,
                color = MarcaAzul.copy(alpha = 0.85f),
                lineHeight = 20.sp
            )
        }
    }
}

/** Tarjeta blanca estilizada para secciones de texto largo */
@Composable
private fun TarjetaContenido(titulo: String, contenido: String) {
    val forma = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), forma)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = titulo,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MarcaAzul
        )
        Text(
            text = contenido,
            fontSize = 14.sp,
            color = Web.Texto.copy(alpha = 0.8f),
            lineHeight = 20.sp
        )
    }
}

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

/** Tarjeta de forma de apoyo estilizada con esquinas redondeadas */
@Composable
private fun TarjetaFormaDeApoyo(forma: FormaDeApoyo, color: ColorApoyo, onClick: () -> Unit) {
    val formaTarjeta = RoundedCornerShape(20.dp)
    Surface(
        shape = formaTarjeta,
        color = Color.White,
        border = BorderStroke(1.5.dp, color.borde),
        modifier = Modifier
            .fillMaxWidth()
            .clip(formaTarjeta)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.fondoIcono),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconoDe(forma.icono), null, tint = color.acento, modifier = Modifier.size(24.dp))
            }
            Box(
                Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color.acento)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(forma.titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MarcaAzul)
                Text(forma.detalle, fontSize = 13.sp, color = Web.TextoApagado)
            }
            Icon(
                IconosWeb.FlechaDerecha,
                contentDescription = "Quiero ayudar",
                tint = color.acento,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}