package mx.tec.familiasquesuman.ui.screens.campanas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.domain.OpcionDonacion
import mx.tec.familiasquesuman.domain.PuntoEntrega
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.fotoDeActividad
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.BarraMeta
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoImagen
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoInstagram
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoMensaje
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoReloj
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoTelefono
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.RenglonArticulo
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.Confirmado
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

private val EtiquetaTexto = Color(0xFFD97706)
private val ComoAyudarFondo = Color(0xFFF8D68F)
private val ComoAyudarBorde = Color(0xFFF5C255)

/**
 * P-11 (con artículos) y P-11c (campaña completa, con [campanaCompleta] = true).
 *
 * Se ve como el detalle de familiasquesuman.com/donar: imagen, datos, opciones de donación,
 * descripción, "¿Cómo ayudar?", contacto y el botón fijo "Quiero ayudar". Lo propio de la app
 * es el apartado de artículos (RF-21): solo aparece si la campaña tiene meta de artículos.
 *
 * Pantalla "tonta": WhatsApp, llamadas, mapas y enlaces los resuelve quien la usa.
 * [nombreAsociacion] se conserva por compatibilidad; el sitio no lo muestra.
 */
@Composable
fun DetalleCampanaScreen(
    campana: Campana,
    campanaCompleta: Boolean,
    onBack: () -> Unit,
    onApartar: (ArticuloMeta) -> Unit,
    onVerOtrasCampanas: () -> Unit,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") nombreAsociacion: String? = null,
    onAyudar: () -> Unit = {},
    onLlamar: () -> Unit = {},
    onOpcion: (OpcionDonacion) -> Unit = {},
    onComoLlegar: (PuntoEntrega) -> Unit = {},
    onAbrirEnlace: (String) -> Unit = {}
) {
    // Corazón solo en memoria (como en Actividades): se llena y se vacía; guardarlo de verdad queda pendiente.
    var esFavorita by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Superficie).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonCircular(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Tinta)
            }
            Text(
                "Detalle de la campaña",
                style = MaterialTheme.typography.labelLarge,
                color = Tinta,
                modifier = Modifier.weight(1f).padding(start = 12.dp)
            )
            BotonCircular(onClick = { esFavorita = !esFavorita }) {
                Icon(
                    if (esFavorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (esFavorita) "Quitar de favoritas" else "Agregar a favoritas",
                    tint = if (esFavorita) Color(0xFFE53935) else MarcaAzul
                )
            }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            ImagenDetalle(campana, campanaCompleta)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaPrincipal(campana, campanaCompleta)

                if (campana.opcionesDonacion.isNotEmpty()) {
                    TarjetaOpciones(campana.opcionesDonacion, onOpcion)
                }

                TarjetaMeta(campana)

                if (campanaCompleta) {
                    CampanaCompleta(campana, onVerOtrasCampanas)
                } else if (campana.sePuedeApartar) {
                    TarjetaApartar(campana, onApartar)
                }

                val descripcion = campana.descripcionLarga.ifBlank { campana.descripcion }
                if (descripcion.isNotBlank()) {
                    TarjetaWeb {
                        Text("Descripción", style = Seccion)
                        Text(descripcion, style = TextoWeb.Cuerpo)
                    }
                }

                if (campana.comoAyudar.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ComoAyudarFondo.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, ComoAyudarBorde.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("¿Cómo ayudar?", style = Seccion)
                            Text(campana.comoAyudar, style = TextoWeb.Cuerpo)
                        }
                    }
                }

                if (campana.puntosEntrega.isNotEmpty()) {
                    TarjetaPuntos(campana.puntosEntrega, onComoLlegar)
                }

                if (campana.telefono != null || campana.whatsapp != null || campana.contactoNombre != null) {
                    TarjetaContacto(campana)
                }

                campana.instagram?.let { enlace -> Instagram(onClick = { onAbrirEnlace(enlace) }) }
            }
        }

        BarraAyudar(campana, onAyudar, onLlamar)
    }
}

private val Seccion = TextoWeb.Seccion.copy(fontSize = 16.sp, lineHeight = 22.sp)

@Composable
private fun BotonCircular(onClick: () -> Unit, contenido: @Composable () -> Unit) {
    Surface(shape = CircleShape, color = Color(0xFFF1F5F9), modifier = Modifier.size(40.dp)) {
        IconButton(onClick = onClick) { contenido() }
    }
}

/** Una tarjeta blanca del sitio: borde fino, esquinas redondas y sombra suave. */
@Composable
private fun TarjetaWeb(contenido: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Web.Tarjeta,
        border = BorderStroke(1.dp, Web.Borde),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            contenido()
        }
    }
}

@Composable
private fun Etiqueta(texto: String, fondo: Color, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = fondo) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = TextoWeb.Chip,
            color = color
        )
    }
}

/** La imagen de la campaña, sobre blanco y sin recortar. Sin archivo, un recuadro con ícono. */
@Composable
private fun ImagenDetalle(campana: Campana, completa: Boolean) {
    val recurso = fotoDeActividad(campana.imagen)
    if (recurso != null) {
        Box(
            modifier = Modifier.fillMaxWidth().background(Web.Tarjeta),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(recurso),
                contentDescription = campana.titulo,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp)
            )
        }
    } else {
        val colores = if (completa) listOf(Color(0xFFD1FAE5), Color(0xFF99F6D0))
        else listOf(AcentoSuave, Color(0xFFF5E2B0))
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp).background(Brush.linearGradient(colores)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconoImagen,
                contentDescription = null,
                tint = Color(0xFF64748B).copy(alpha = 0.55f),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun TarjetaPrincipal(campana: Campana, completa: Boolean) {
    TarjetaWeb {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Etiqueta("Campaña", Web.AmbarSuave, EtiquetaTexto)
            if (completa) Etiqueta("Completa", Web.Secundario, Web.TextoApagado)
            else Etiqueta("Activa", Web.VerdeFondo, Web.VerdeTexto)
        }
        Text(campana.titulo, style = TextoWeb.Titulo)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.LocationOn, null, tint = Web.TextoApagado, modifier = Modifier.size(16.dp))
            Text(campana.ciudad, style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado))
        }
        Text(campana.descripcion, style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado))
    }
}

/** "Opciones de donación": numeradas, en azules cada vez más fuertes. Tocar una abre WhatsApp. */
@Composable
private fun TarjetaOpciones(opciones: List<OpcionDonacion>, onOpcion: (OpcionDonacion) -> Unit) {
    val fondos = listOf(Color(0xFFEFF6FF), Color(0xFFBFDBFE), Color(0xFF93C5FD))
    TarjetaWeb {
        Text("Opciones de donación", style = Seccion)
        Text("Elige cómo quieres ayudar:", style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            opciones.forEachIndexed { i, opcion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(fondos.getOrElse(i) { fondos.last() })
                        .clickable { onOpcion(opcion) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Web.Primario),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${i + 1}", style = TextoWeb.Chip.copy(fontSize = 14.sp), color = Color.White)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(opcion.nombre, style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            opcion.precio,
                            style = TextoWeb.Cuerpo.copy(
                                fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Web.Primario
                            )
                        )
                    }
                    Icon(IconoMensaje, null, tint = Web.Primario.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/** "Meta de la campaña": la meta en texto, el avance (si se puede apartar) y la fecha límite. */
@Composable
private fun TarjetaMeta(campana: Campana) {
    if (campana.metaTexto == null && campana.cierra.isBlank() && !campana.sePuedeApartar) return
    TarjetaWeb {
        Text("Meta de la campaña", style = Seccion)
        campana.metaTexto?.let { Text(it, style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado)) }
        if (campana.sePuedeApartar) BloqueMeta(campana)
        if (campana.cierra.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(IconoReloj, null, tint = Web.Ambar, modifier = Modifier.size(16.dp))
                Text(
                    buildAnnotatedString {
                        append("Fecha límite: ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Web.Texto)) {
                            append(campana.cierra)
                        }
                    },
                    style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado)
                )
            }
        }
    }
}

/** Bloque verde con "100 de 300 cuentos completos" y la barra. */
@Composable
private fun BloqueMeta(campana: Campana) {
    Surface(shape = RoundedCornerShape(12.dp), color = ConfirmadoFondo, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "${campana.completados} de ${campana.metaTotal} ${campana.unidadMeta} completos",
                style = MaterialTheme.typography.titleMedium,
                color = ConfirmadoTexto
            )
            BarraMeta(progreso = campana.progreso, color = Confirmado)
        }
    }
}

/** RF-21: la lista de artículos con su "Apartar". Solo para campañas con meta de artículos. */
@Composable
private fun TarjetaApartar(campana: Campana, onApartar: (ArticuloMeta) -> Unit) {
    TarjetaWeb {
        Text("Aparta lo que vayas a llevar", style = Seccion)
        Text("Así no se juntan cosas repetidas.", style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado))
        Column {
            campana.articulos.forEachIndexed { i, articulo ->
                if (i > 0) HorizontalDivider(color = Borde)
                RenglonArticulo(articulo = articulo, onApartar = { onApartar(articulo) })
            }
        }
    }
}

@Composable
private fun TarjetaPuntos(puntos: List<PuntoEntrega>, onComoLlegar: (PuntoEntrega) -> Unit) {
    TarjetaWeb {
        Text("Puntos de entrega", style = Seccion)
        puntos.forEachIndexed { i, punto ->
            if (i > 0) HorizontalDivider(color = Web.Borde)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Web.Primario),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${i + 1}", style = TextoWeb.Chip.copy(fontSize = 13.sp), color = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text(punto.direccion, style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.SemiBold))
                    Text(punto.colonia, style = TextoWeb.Chico)
                }
                OutlinedButton(
                    onClick = { onComoLlegar(punto) },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Web.Borde),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = Web.Primario, modifier = Modifier.size(14.dp))
                    Text("  Cómo llegar", style = TextoWeb.Chip, color = Web.Primario)
                }
            }
        }
    }
}

@Composable
private fun TarjetaContacto(campana: Campana) {
    TarjetaWeb {
        Text("Contacto", style = Seccion)
        campana.contactoNombre?.let { FilaContacto(Icons.Default.Person, it) }
        campana.telefono?.let { FilaContacto(Icons.Default.Phone, it) }
        campana.whatsapp?.let { FilaContacto(IconoMensaje, it) }
    }
}

@Composable
private fun FilaContacto(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icono, null, tint = Web.TextoApagado, modifier = Modifier.size(16.dp))
        Text(texto, style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado))
    }
}

@Composable
private fun Instagram(onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Síguenos en:", style = TextoWeb.Chip.copy(color = Web.TextoApagado))
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFFDC7D), Color(0xFFF77737), Color(0xFF962FBF)))
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(IconoInstagram, contentDescription = "Instagram", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

/**
 * La barra fija de abajo, como la del sitio: "Quiero ayudar" (WhatsApp, verde) y un botón de
 * teléfono. Si la campaña solo tiene teléfono, un solo botón azul que llama.
 */
@Composable
private fun BarraAyudar(campana: Campana, onAyudar: () -> Unit, onLlamar: () -> Unit) {
    if (campana.whatsapp == null && campana.telefono == null) return
    Column(modifier = Modifier.fillMaxWidth().background(Superficie)) {
        HorizontalDivider(color = Borde)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val soloTelefono = campana.whatsapp == null
            Surface(
                onClick = onAyudar,
                shape = RoundedCornerShape(12.dp),
                color = if (soloTelefono) Web.Primario else Web.Verde,
                modifier = Modifier.heightIn(min = 44.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (soloTelefono) IconoTelefono else IconoMensaje,
                        contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)
                    )
                    Text(campana.textoBoton, style = TextoWeb.Chip.copy(fontSize = 14.sp), color = Color.White)
                }
            }
            if (!soloTelefono && campana.telefono != null) {
                Surface(
                    onClick = onLlamar,
                    shape = RoundedCornerShape(12.dp),
                    color = Web.Tarjeta,
                    border = BorderStroke(1.dp, Web.Borde),
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                        Icon(IconoTelefono, contentDescription = "Llamar", tint = Web.Texto, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/** P-11c: agradece y ofrece otras campañas. */
@Composable
private fun CampanaCompleta(campana: Campana, onVerOtrasCampanas: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ConfirmadoTexto, modifier = Modifier.size(44.dp))
        Text("Esta campaña ya está completa", style = MaterialTheme.typography.titleLarge, color = Tinta, textAlign = TextAlign.Center)
        Text(
            if (campana.cierra.isNotBlank()) "Gracias a las familias que participaron. La entrega es el ${campana.cierra}."
            else "Gracias a las familias que participaron.",
            style = MaterialTheme.typography.bodyLarge,
            color = TintaSuave,
            textAlign = TextAlign.Center
        )
        OutlinedButton(
            onClick = onVerOtrasCampanas,
            border = BorderStroke(1.5.dp, MarcaAzul),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Ver otras campañas", style = MaterialTheme.typography.labelLarge, color = MarcaAzul)
        }
    }
}

// ---------------- Previews ----------------

private val destellos = Campana(
    "c1", "Destellos de Luz", "", "Salud", "", false,
    "Destellos de Luz A.B.P. cuenta con más de 29 años de experiencia brindando atención oftalmológica integral.",
    "", 0, 0, emptyList(),
    whatsapp = "8180924504", telefono = "8180924504", contactoNombre = "Mariana Báez",
    descripcionLarga = "Destellos de Luz A.B.P. es una asociación con más de 29 años de trayectoria.\n\nEn el área Médica: brindamos atención oftalmológica integral.",
    comoAyudar = "Dona herramientas para personas con discapacidad visual.",
    opcionesDonacion = listOf(
        OpcionDonacion("Bastón para desplazamiento", "\$700"),
        OpcionDonacion("Computadora parlante", "\$21,000"),
        OpcionDonacion("Cirugía", "\$14,000")
    ),
    instagram = "https://www.instagram.com/destellosdeluzabp/"
)

private val bibliotecas = Campana(
    "c4", "Bibliotecas Infantiles", "", "Útiles escolares", "", false,
    "Recolección de cuentos infantiles para crear Bibliotecas", "cuentos", 300, 100,
    listOf(ArticuloMeta("c4-1", "Cuentos infantiles", 300, 100)),
    telefono = "8120322281", contactoNombre = "Familias que Suman", metaTexto = "250-300 cuentos",
    descripcionLarga = "Reúne cuentos infantiles para crear Bibliotecas para niños.",
    comoAyudar = "Junta cuentos desde preescolar hasta secundaria en buen estado."
)

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun DetalleConOpcionesPreview() {
    FamiliasQueSumanTheme {
        DetalleCampanaScreen(destellos, false, {}, {}, {})
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun DetalleApartablePreview() {
    FamiliasQueSumanTheme {
        DetalleCampanaScreen(bibliotecas, false, {}, {}, {})
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun DetalleCompletaPreview() {
    FamiliasQueSumanTheme {
        DetalleCampanaScreen(
            bibliotecas.copy(completados = 300, articulos = listOf(ArticuloMeta("c4-1", "Cuentos infantiles", 300, 300))),
            true, {}, {}, {}
        )
    }
}
