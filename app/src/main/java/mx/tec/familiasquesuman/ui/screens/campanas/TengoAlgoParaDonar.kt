package mx.tec.familiasquesuman.ui.screens.campanas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.fotoDeActividad
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoCaja
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoChevron
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoEscudo
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoImagen
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoInstagram
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoMensaje
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoNavegacion
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoReloj
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoTelefono
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

private const val Todos = "Todos"
private val AzulBoton = Color(0xFF2563EB)
private val AzulChipFondo = Color(0xFFEFF6FF)
private val AzulChipTexto = Color(0xFF1D4ED8)
private val VerdeSuave = Color(0xFFF0FDF4)
private val CajaRecepcion = Color(0xFFF1F5F9)

/**
 * "Tengo algo para donar", como familiasquesuman.com/donar: los tipos de donación arriba y,
 * debajo, las asociaciones que los reciben, en tarjetas que se despliegan con un toque.
 * Pantalla "tonta": llamar, WhatsApp, mapa y enlaces los resuelve quien la usa.
 */
@Composable
fun TengoAlgoParaDonar(
    onLlamar: (String) -> Unit,
    onWhatsApp: (String) -> Unit,
    onComoLlegar: (String) -> Unit,
    onAbrirEnlace: (String) -> Unit,
    onNoEncontre: () -> Unit,
    modifier: Modifier = Modifier,
    centros: List<CentroRecepcion> = CentrosDeRecepcion
) {
    // Solo estado de pantalla: qué tipo se eligió y cuál tarjeta está abierta.
    var tipo by rememberSaveable { mutableStateOf(Todos) }
    var abierta by rememberSaveable { mutableStateOf<String?>(null) }
    val visibles = if (tipo == Todos) centros else centros.filter { tipo in it.tipos }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("¿Qué quieres aportar?", style = TextoWeb.Seccion.copy(fontSize = 16.sp), color = Web.Texto)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(Todos) + TiposDeDonacion) { nombre ->
                    ChipTipo(nombre = nombre, elegido = nombre == tipo, onClick = { tipo = nombre })
                }
            }
        }
        if (visibles.isEmpty()) {
            item {
                Text(
                    "Todavía no hay asociaciones para este tipo de aportación.",
                    style = TextoWeb.Cuerpo,
                    color = Web.TextoApagado,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
        items(visibles, key = { it.id }) { centro ->
            TarjetaCentro(
                centro = centro,
                abierta = abierta == centro.id,
                onAlternar = { abierta = if (abierta == centro.id) null else centro.id },
                onLlamar = onLlamar,
                onWhatsApp = onWhatsApp,
                onComoLlegar = onComoLlegar,
                onAbrirEnlace = onAbrirEnlace
            )
        }
        item { TarjetaNoEncontre(onClick = onNoEncontre) }
    }
}

// ---------------- Chips de tipo ----------------

/** Un emoji por tipo mientras no haya íconos propios; el color de fondo es de cada tipo. */
private fun adornoDe(tipo: String): Pair<String, Color> = when (tipo) {
    "Juguetes" -> "🧸" to Color(0xFFFFF4D6)
    "Ropa" -> "👕" to Color(0xFFF3E8FF)
    "Alimentos" -> "🍎" to Color(0xFFDCFCE7)
    "Higiene" -> "💧" to Color(0xFFDBEAFE)
    "Sillas de ruedas" -> "♿" to Color(0xFFD1FAF0)
    "Camas hospitalarias" -> "🛏️" to Color(0xFFFFE4E6)
    "Mobiliario" -> "🪑" to Color(0xFFFFEDD5)
    "Artículos de cocina" -> "🍳" to Color(0xFFFEF9C3)
    "Electrónicos" -> "💻" to Color(0xFFE0E7FF)
    "Útiles escolares" -> "✏️" to Color(0xFFFCE7F3)
    "Medicamentos" -> "💊" to Color(0xFFCFFAFE)
    "Material didáctico" -> "📚" to Color(0xFFECFCCB)
    else -> "" to Color(0xFFE2E8F0)
}

@Composable
private fun ChipTipo(nombre: String, elegido: Boolean, onClick: () -> Unit) {
    val (emoji, fondo) = adornoDe(nombre)
    Surface(
        onClick = onClick,
        modifier = Modifier.width(78.dp),
        shape = RoundedCornerShape(16.dp),
        color = Web.Tarjeta,
        border = BorderStroke(if (elegido) 2.dp else 1.dp, if (elegido) Web.Primario else Web.Borde)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(fondo),
                contentAlignment = Alignment.Center
            ) {
                if (nombre == Todos) {
                    Icon(IconoCaja, contentDescription = null, tint = Web.Primario, modifier = Modifier.size(20.dp))
                } else {
                    Text(emoji, fontSize = 18.sp)
                }
            }
            Text(
                nombre,
                style = TextoWeb.Chip,
                color = Web.Texto,
                textAlign = TextAlign.Center,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------- Tarjeta de la asociación ----------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaCentro(
    centro: CentroRecepcion,
    abierta: Boolean,
    onAlternar: () -> Unit,
    onLlamar: (String) -> Unit,
    onWhatsApp: (String) -> Unit,
    onComoLlegar: (String) -> Unit,
    onAbrirEnlace: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Web.Tarjeta,
        border = BorderStroke(1.dp, Web.Borde)
    ) {
        Column {
            // Cabecera: siempre visible; al tocarla se abre o se cierra.
            Column(
                modifier = Modifier.clickable(onClick = onAlternar).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Logo(centro)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (centro.verificado) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(IconoEscudo, null, tint = Web.Primario, modifier = Modifier.size(12.dp))
                                Text("Verificado", style = TextoWeb.Chip, color = Web.Primario)
                            }
                        }
                        Text(centro.nombre, style = TextoWeb.TituloTarjeta, color = Web.Primario)
                        Text(centro.tipo, style = TextoWeb.Cuerpo, color = Web.TextoApagado)
                    }
                    Icon(
                        IconoChevron,
                        contentDescription = if (abierta) "Cerrar" else "Abrir",
                        tint = Web.TextoApagado,
                        modifier = Modifier.size(20.dp).rotate(if (abierta) 180f else 0f)
                    )
                }
                Text(
                    centro.descripcion,
                    style = TextoWeb.Cuerpo,
                    color = Web.TextoApagado,
                    maxLines = if (abierta) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        IconoNavegacion, null, tint = Web.TextoApagado,
                        modifier = Modifier.padding(top = 2.dp).size(13.dp)
                    )
                    Text(
                        centro.direccion,
                        style = TextoWeb.Chico,
                        color = Web.TextoApagado,
                        maxLines = if (abierta) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Cerrada: los primeros 4 tipos y "+N más". Abierta: todos.
                val mostrados = if (abierta) centro.tipos else centro.tipos.take(4)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mostrados.forEach { Etiqueta(it, Web.Secundario, Web.Texto) }
                    if (!abierta && centro.tipos.size > 4) {
                        Etiqueta("+${centro.tipos.size - 4} más", Web.Secundario, Web.TextoApagado)
                    }
                }
            }

            if (abierta) {
                HorizontalDivider(color = Web.Borde)
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Grupo("Destinatarios", centro.destinatarios, AzulChipFondo, AzulChipTexto)
                    Grupo("Condiciones", centro.condiciones, Web.MoradoFondo, Web.MoradoTexto)
                    Grupo("Métodos de entrega", centro.metodosEntrega, VerdeSuave, Web.VerdeTexto)

                    Surface(shape = RoundedCornerShape(12.dp), color = CajaRecepcion, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Condiciones de recepción", style = TextoWeb.Seccion, color = Web.Texto)
                            Text(centro.condicionesRecepcion, style = TextoWeb.Cuerpo, color = Web.TextoApagado)
                        }
                    }

                    centro.horario?.let { horario ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(IconoReloj, null, tint = Web.TextoApagado, modifier = Modifier.size(16.dp))
                            Text(horario, style = TextoWeb.Cuerpo, color = Web.TextoApagado)
                        }
                    }

                    centro.instagram?.let { enlace ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Síguenos en:", style = TextoWeb.Chip, color = Web.TextoApagado)
                            BotonInstagram(onClick = { onAbrirEnlace(enlace) })
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        centro.whatsapp?.let {
                            BotonAccion("WhatsApp", IconoMensaje, Web.WhatsApp, Color.White, null, Modifier.weight(1f)) {
                                onWhatsApp(it)
                            }
                        }
                        centro.telefono?.let {
                            BotonAccion("Llamar", IconoTelefono, Web.Tarjeta, Web.VerdeTexto, Web.Verde, Modifier.weight(1f)) {
                                onLlamar(it)
                            }
                        }
                        BotonAccion("Cómo llegar", IconoNavegacion, AzulBoton, Color.White, null, Modifier.weight(1f)) {
                            onComoLlegar(centro.direccion)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Logo(centro: CentroRecepcion) {
    val recurso = fotoDeActividad(centro.imagen)
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Web.Tarjeta)
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        if (recurso != null) {
            Image(
                painter = painterResource(recurso),
                contentDescription = centro.nombre,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(52.dp)
            )
        } else {
            Icon(IconoImagen, contentDescription = null, tint = Web.TextoApagado, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun Etiqueta(texto: String, fondo: Color, color: Color) {
    Text(
        texto,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(fondo).padding(horizontal = 12.dp, vertical = 4.dp),
        style = TextoWeb.Chip,
        color = color
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Grupo(titulo: String, valores: List<String>, fondo: Color, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = TextoWeb.Seccion, color = Web.Texto)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            valores.forEach { Etiqueta(it, fondo, color) }
        }
    }
}

@Composable
private fun BotonInstagram(onClick: () -> Unit) {
    val degradado = Brush.linearGradient(listOf(Color(0xFF7E3FF2), Color(0xFFE1306C), Color(0xFFFCAF45)))
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).background(degradado).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(IconoInstagram, contentDescription = "Instagram", tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun BotonAccion(
    texto: String,
    icono: ImageVector,
    fondo: Color,
    colorTexto: Color,
    borde: Color?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(12.dp),
        color = fondo,
        border = borde?.let { BorderStroke(2.dp, it) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            Icon(icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(14.dp))
            Text(texto, style = TextoWeb.Chip.copy(fontSize = 13.sp), color = colorTexto, maxLines = 1)
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun TengoAlgoPreview() {
    FamiliasQueSumanTheme {
        TengoAlgoParaDonar({}, {}, {}, {}, {})
    }
}
