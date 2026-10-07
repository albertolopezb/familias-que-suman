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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.fotoDeActividad
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoChevron
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoEscudo
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoImagen
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoInstagram
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoMensaje
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoNavegacion
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoReloj
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoTelefono
import mx.tec.familiasquesuman.ui.theme.*
import mx.tec.familiasquesuman.ui.state.UiState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription

private const val Todos = TengoAlgoParaDonarUiState.TODOS
private val AzulChipFondo = Color(0xFFEFF6FF)
private val AzulChipTexto = Color(0xFF1D4ED8)
private val VerdeSuave = Color(0xFFF0FDF4)

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
    estado: TengoAlgoParaDonarUiState,
    onTipoSeleccionado: (String) -> Unit,
    onCentroSeleccionado: (String) -> Unit,
    onReintentar: () -> Unit
) {
    val visibles = estado.centrosVisibles

    LazyColumn(
        modifier = modifier.background(Fondo).testTag("centrosLista"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("¿Qué quieres aportar?", style = MaterialTheme.typography.titleLarge, color = MarcaAzul)
        }
        item {
            LazyRow(modifier = Modifier.testTag("tiposDonacion"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(Todos) + TiposDeDonacion) { nombre ->
                    ChipTipo(nombre = nombre, elegido = nombre == estado.tipoSeleccionado, onClick = { onTipoSeleccionado(nombre) })
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Lugares donde puedes donar", style = MaterialTheme.typography.titleLarge, color = MarcaAzul)
                if (estado.datos is UiState.Exito) Text(
                    if (visibles.size == 1) "1 centro acepta lo que seleccionaste"
                    else "${visibles.size} centros aceptan lo que seleccionaste",
                    style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
            }
        }
        when (val datos = estado.datos) {
            UiState.Cargando -> item {
                Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = MarcaAzul)
                    Text("Cargando centros…", color = TintaSuave)
                }
            }
            is UiState.Error -> item {
                Surface(shape = RoundedCornerShape(16.dp), color = ErrorFondo) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(datos.mensaje, color = ErrorTexto, style = MaterialTheme.typography.bodyLarge)
                        TextButton(onClick = onReintentar) { Text("Reintentar", color = MarcaAzul) }
                    }
                }
            }
            is UiState.Exito -> {
                if (visibles.isEmpty()) item {
                    Surface(shape = RoundedCornerShape(16.dp), color = AzulTarjetaFondo) {
                        Text("Todavía no hay asociaciones para este tipo de aportación.",
                            style = MaterialTheme.typography.bodyLarge, color = TintaSuave,
                            modifier = Modifier.fillMaxWidth().padding(20.dp))
                    }
                }
                items(visibles, key = { it.id }) { centro ->
                    TarjetaCentro(centro = centro, abierta = estado.centroExpandido == centro.id,
                        onAlternar = { onCentroSeleccionado(centro.id) },
                        onLlamar = onLlamar, onWhatsApp = onWhatsApp,
                        onComoLlegar = onComoLlegar, onAbrirEnlace = onAbrirEnlace)
                }
            }
        }
        item { TarjetaNoEncontre(onClick = onNoEncontre) }
    }
}

// ---------------- Chips de tipo ----------------

/** Un emoji por tipo mientras no haya íconos propios; el color de fondo es de cada tipo. */
private fun adornoDe(tipo: String): Pair<String, Color> = when (tipo) {
    "Juguetes" -> "🧸" to Color(0xFFFFF4D6)
    "Ropa" -> "👕" to MoradoTarjetaFondo
    "Alimentos" -> "🍎" to VerdeTarjetaFondo
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
        modifier = Modifier.width(100.dp).semantics { selected = elegido },
        shape = RoundedCornerShape(16.dp),
        color = if (elegido) VerdeTarjetaFondo else fondo.copy(alpha = 0.45f),
        border = BorderStroke(if (elegido) 2.dp else 1.dp, if (elegido) MarcaAzul else Web.Borde)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(fondo),
                contentAlignment = Alignment.Center
            ) {
                if (nombre == Todos) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MarcaAzul, modifier = Modifier.size(20.dp))
                } else {
                    Text(emoji, fontSize = 18.sp)
                }
            }
            Text(
                nombre,
                style = MaterialTheme.typography.labelMedium,
                color = Tinta,
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
        color = Superficie,
        shadowElevation = 2.dp
    ) {
        Column {
            // Cabecera: siempre visible; al tocarla se abre o se cierra.
            Column(
                modifier = Modifier.semantics { stateDescription = if (abierta) "Expandida" else "Contraída" }.clickable(onClickLabel = if (abierta) "Contraer asociación" else "Expandir asociación", onClick = onAlternar).padding(16.dp),
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
                                Icon(IconoEscudo, null, tint = MarcaAzul, modifier = Modifier.size(12.dp))
                                Text("Verificado", style = MaterialTheme.typography.labelMedium, color = MarcaAzul)
                            }
                        }
                        Text(centro.nombre, style = MaterialTheme.typography.titleMedium, color = MarcaAzul)
                        Text(centro.tipo, style = MaterialTheme.typography.labelLarge, color = TintaSuave)
                    }
                    Icon(
                        IconoChevron,
                        contentDescription = if (abierta) "Cerrar" else "Abrir",
                        tint = TintaSuave,
                        modifier = Modifier.size(20.dp).rotate(if (abierta) 180f else 0f)
                    )
                }
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Place, null, tint = TintaSuave,
                        modifier = Modifier.padding(top = 2.dp).size(13.dp)
                    )
                    Text(
                        centro.direccion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TintaSuave,
                        maxLines = if (abierta) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Cerrada: los primeros 3 tipos y "+N más". Abierta: todos.
                val mostrados = if (abierta) centro.tipos else centro.tipos.take(3)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mostrados.forEach { Etiqueta(it, VerdeTarjetaFondo, VerdeCategoriaTexto) }
                    if (!abierta && centro.tipos.size > 3) {
                        Etiqueta("+${centro.tipos.size - 3} más", Web.Secundario, TintaSuave)
                    }
                }
            }

            if (abierta) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Borde)
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Acerca del centro", style = MaterialTheme.typography.titleMedium, color = MarcaAzul)
                        Text(centro.descripcion, style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
                    }
                    Grupo("Destinatarios", centro.destinatarios, AzulChipFondo, AzulChipTexto)
                    Grupo("Condiciones", centro.condiciones, Web.MoradoFondo, Web.MoradoTexto)
                    Grupo("Métodos de entrega", centro.metodosEntrega, VerdeSuave, Web.VerdeTexto)

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Condiciones de recepción", style = MaterialTheme.typography.titleMedium, color = MarcaAzul)
                        Text(centro.condicionesRecepcion, style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
                    }

                    centro.horario?.let { horario ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(IconoReloj, null, tint = TintaSuave, modifier = Modifier.size(16.dp))
                            Text(horario, style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
                        }
                    }

                    centro.instagram?.let { enlace ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Síguenos en:", style = MaterialTheme.typography.labelMedium, color = TintaSuave)
                            BotonInstagram(onClick = { onAbrirEnlace(enlace) })
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HorizontalDivider(color = Borde)
                        centro.whatsapp?.let {
                            BotonAccion("WhatsApp", IconoMensaje, Web.WhatsApp, Color.White, null, Modifier.fillMaxWidth()) {
                                onWhatsApp(it)
                            }
                        }
                        centro.telefono?.let {
                            BotonAccion("Llamar", IconoTelefono, Superficie, MarcaAzul, MarcaAzul, Modifier.fillMaxWidth()) {
                                onLlamar(it)
                            }
                        }
                        BotonAccion("Cómo llegar", IconoNavegacion, MarcaAzul, Color.White, null, Modifier.fillMaxWidth()) {
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
            .background(VerdeTarjetaFondo)
            .padding(2.dp),
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
            Icon(IconoImagen, contentDescription = null, tint = TintaSuave, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun Etiqueta(texto: String, fondo: Color, color: Color) {
    Text(
        texto,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(fondo).padding(horizontal = 12.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = color
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Grupo(titulo: String, valores: List<String>, fondo: Color, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, color = MarcaAzul)
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
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        color = fondo,
        border = borde?.let { BorderStroke(1.dp, it) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Icon(icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(18.dp))
            Text(texto, style = MaterialTheme.typography.labelLarge, color = colorTexto, maxLines = 1)
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun TengoAlgoPreview() {
    FamiliasQueSumanTheme {
        TengoAlgoParaDonar({}, {}, {}, {}, {}, estado = TengoAlgoParaDonarUiState(UiState.Exito(CentrosDeRecepcion)), onTipoSeleccionado = {}, onCentroSeleccionado = {}, onReintentar = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun TengoAlgoCargandoPreview() = PreviewEstadoCentros(TengoAlgoParaDonarUiState())

@Preview(showBackground = true)
@Composable
private fun TengoAlgoErrorPreview() = PreviewEstadoCentros(
    TengoAlgoParaDonarUiState(UiState.Error("No se pudieron cargar los centros de donación."))
)

@Preview(showBackground = true)
@Composable
private fun TengoAlgoVacioPreview() = PreviewEstadoCentros(
    TengoAlgoParaDonarUiState(UiState.Exito(emptyList()), tipoSeleccionado = "Ropa")
)

@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun TengoAlgoExpandidoPreview() = PreviewEstadoCentros(
    TengoAlgoParaDonarUiState(UiState.Exito(CentrosDeRecepcion), centroExpandido = "apadrina")
)

@Composable
private fun PreviewEstadoCentros(estado: TengoAlgoParaDonarUiState) {
    FamiliasQueSumanTheme {
        TengoAlgoParaDonar({}, {}, {}, {}, {}, estado = estado,
            onTipoSeleccionado = {}, onCentroSeleccionado = {}, onReintentar = {})
    }
}
