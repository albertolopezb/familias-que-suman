package mx.tec.familiasquesuman.ui.screens.campanas

import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoCaja
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoFiltro
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.PildoraFiltro
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.TarjetaCampana
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorRojo
import mx.tec.familiasquesuman.ui.theme.ErrorTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * La web no tiene filtros en "Donar a campaña", así que se ocultan. El código de los filtros
 * (chips, hoja y píldoras) sigue aquí y en CampanasViewModel: para volver a mostrarlos
 * (RF-20) basta con poner esto en `true`.
 */
private const val MostrarFiltrosEnLista = false

/**
 * P-09, P-09b, P-09c y P-09d: la lista de campañas con sus cuatro estados.
 * Pantalla "tonta": recibe datos y funciones, nunca el ViewModel ni el NavController.
 */
@Composable
fun CampanasScreen(
    estado: UiState<List<Campana>>,
    filtros: FiltrosCampanas,
    totalAbiertas: Int,
    onBack: () -> Unit,
    onAbrirFiltros: () -> Unit,
    onChipRapido: (String?) -> Unit,
    onQuitarCategoria: (String) -> Unit,
    onQuitarUrgentes: () -> Unit,
    onQuitarFiltros: () -> Unit,
    onCampanaClick: (String) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
    nombresAsociacion: Map<String, String> = emptyMap(),
    chipElegido: String? = null,
    onAyudar: (Campana) -> Unit = {},
    onNoEncontre: () -> Unit = {},
    onLlamar: (String) -> Unit = {},
    onWhatsAppCentro: (String) -> Unit = {},
    onComoLlegar: (String) -> Unit = {},
    onAbrirEnlace: (String) -> Unit = {},
    esAdmin: Boolean = false,
    onCrearCampana: () -> Unit = {},
    onEditarCampana: (String) -> Unit = {},
    onBorrarCampana: (String) -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (esAdmin) {
                FloatingActionButton(
                    onClick = onCrearCampana,
                    containerColor = MarcaAzul,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear Campaña")
                }
            }
        }
    ) { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background)) {
    // Qué modo se está viendo. Solo es estado de pantalla (como "qué pestaña"), no de datos.
    var modo by rememberSaveable { mutableStateOf(ModoDonar.CAMPANA) }

        EncabezadoApp()
        BarraSuperior(onBack = onBack, esAdmin = esAdmin)
        SelectorModoDonar(modo = modo, onElegir = { modo = it })

        if (modo == ModoDonar.TENGO_ALGO) {
            TengoAlgoParaDonar(
                onLlamar = onLlamar,
                onWhatsApp = onWhatsAppCentro,
                onComoLlegar = onComoLlegar,
                onAbrirEnlace = onAbrirEnlace,
                onNoEncontre = onNoEncontre
            )
            return@Column
        }

        when (estado) {
            is UiState.Cargando -> ListaCargando()
            is UiState.Error -> EstadoError(mensaje = estado.mensaje, onReintentar = onReintentar)
            is UiState.Exito -> {
                if (estado.datos.isEmpty()) {
                    // Lo que está filtrando ahora: el chip tocado o lo aplicado desde la hoja.
                    val efectivos = if (chipElegido != null) FiltrosCampanas(setOf(chipElegido)) else filtros
                    Column {
                        if (MostrarFiltrosEnLista && efectivos.hayActivos) {
                            FiltrosDeArriba(
                                filtros = filtros,
                                chipElegido = chipElegido,
                                onChipRapido = onChipRapido,
                                onAbrirFiltros = onAbrirFiltros,
                                onQuitarCategoria = onQuitarCategoria,
                                onQuitarUrgentes = onQuitarUrgentes,
                                conMargen = true
                            )
                        }
                        EstadoVacio(efectivos, totalAbiertas, onQuitarFiltros)
                    }
                } else {
                    ListaConDatos(
                        campanas = estado.datos,
                        nombresAsociacion = nombresAsociacion,
                        filtros = filtros,
                        chipElegido = chipElegido,
                        onChipRapido = onChipRapido,
                        onAbrirFiltros = onAbrirFiltros,
                        onQuitarCategoria = onQuitarCategoria,
                        onQuitarUrgentes = onQuitarUrgentes,
                        onCampanaClick = onCampanaClick,
                        onAyudar = onAyudar,
                        onNoEncontre = onNoEncontre,
                        esAdmin = esAdmin,
                        onEditarCampana = onEditarCampana,
                        onBorrarCampana = onBorrarCampana
                    )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarraSuperior(onBack: () -> Unit, esAdmin: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = MarcaAzul)
        }
        Text(
            if (esAdmin) "Quiero Donar (Admin)" else "Quiero Donar",
            style = MaterialTheme.typography.titleLarge,
            color = MarcaAzul,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Los dos modos de "Quiero Donar", igual que en la web. */
enum class ModoDonar { CAMPANA, TENGO_ALGO }

/**
 * Los dos botones de arriba. El modo que estás viendo va de color (amarillo / azul) y el otro en
 * gris, para que se entienda dónde estás y cómo cambiar.
 */
@Composable
private fun SelectorModoDonar(modo: ModoDonar, onElegir: (ModoDonar) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BotonModo(
            texto = "Donar a campaña",
            activo = modo == ModoDonar.CAMPANA,
            colorActivo = MarcaOro,
            textoActivo = Tinta,
            onClick = { onElegir(ModoDonar.CAMPANA) },
            modifier = Modifier.weight(1f)
        )
        BotonModo(
            texto = "Tengo algo para donar",
            activo = modo == ModoDonar.TENGO_ALGO,
            colorActivo = MarcaAzul,
            textoActivo = Color.White,
            onClick = { onElegir(ModoDonar.TENGO_ALGO) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BotonModo(
    texto: String,
    activo: Boolean,
    colorActivo: Color,
    textoActivo: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (activo) colorActivo else Color.White,
        border = if (activo) null else BorderStroke(1.dp, Borde)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = texto,
                style = MaterialTheme.typography.labelLarge,
                color = if (activo) textoActivo else TintaSuave,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ListaConDatos(
    campanas: List<Campana>,
    nombresAsociacion: Map<String, String>,
    filtros: FiltrosCampanas,
    chipElegido: String?,
    onChipRapido: (String?) -> Unit,
    onAbrirFiltros: () -> Unit,
    onQuitarCategoria: (String) -> Unit,
    onQuitarUrgentes: () -> Unit,
    onCampanaClick: (String) -> Unit,
    onAyudar: (Campana) -> Unit,
    onNoEncontre: () -> Unit,
    esAdmin: Boolean = false,
    onEditarCampana: (String) -> Unit = {},
    onBorrarCampana: (String) -> Unit = {}
) {
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (MostrarFiltrosEnLista) {
            item {
                FiltrosDeArriba(
                    filtros = filtros,
                    chipElegido = chipElegido,
                    onChipRapido = onChipRapido,
                    onAbrirFiltros = onAbrirFiltros,
                    onQuitarCategoria = onQuitarCategoria,
                    onQuitarUrgentes = onQuitarUrgentes,
                    conMargen = false
                )
            }
        }
        items(campanas, key = { it.id }) { campana ->
            Column {
                TarjetaCampana(
                    campana = campana,
                    onClick = { onCampanaClick(campana.id) },
                    onAyudar = { onAyudar(campana) },
                    nombreAsociacion = nombresAsociacion[campana.asociacionId]
                )
                if (esAdmin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onEditarCampana(campana.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MarcaAzul)
                        }
                        IconButton(onClick = { onBorrarCampana(campana.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = ErrorRojo)
                        }
                    }
                }
            }
        }
        item { TarjetaNoEncontre(onClick = onNoEncontre) }
    }
}

/** Al final de la lista, como en el sitio: la tarjeta punteada para quien no halló dónde donar. */
@Composable
internal fun TarjetaNoEncontre(onClick: () -> Unit) {
    val trazo = Color(0xFFD7E0EA)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = trazo,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                    )
                )
            }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            "¿No encontraste dónde aportarlo?",
            style = MaterialTheme.typography.labelLarge,
            color = TintaSuave
        )
        Text(
            "Cuéntanos y te ayudamos",
            style = MaterialTheme.typography.bodySmall,
            color = TintaSuave
        )
    }
}

/**
 * Lo de arriba de la lista:
 *  - la fila de chips ("Todas" y las categorías), que se desliza. Son botones para navegar: el
 *    elegido va en azul y NUNCA lleva ✕;
 *  - debajo, solo si se aplicó algo desde la hoja de filtros, una píldora azul con ✕ por cada
 *    filtro (categorías y "Urgentes"), para quitarlo.
 * [conMargen]: la lista ya trae su propio margen de 16 dp; el estado vacío no.
 */
@Composable
private fun FiltrosDeArriba(
    filtros: FiltrosCampanas,
    chipElegido: String?,
    onChipRapido: (String?) -> Unit,
    onAbrirFiltros: () -> Unit,
    onQuitarCategoria: (String) -> Unit,
    onQuitarUrgentes: () -> Unit,
    conMargen: Boolean
) {
    val margen = if (conMargen) 16.dp else 0.dp
    Column(
        modifier = if (conMargen) Modifier.padding(vertical = 8.dp) else Modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LazyRow(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = margen),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    PildoraFiltro(
                        "Todas",
                        seleccionada = chipElegido == null && !filtros.hayActivos,
                        onClick = { onChipRapido(null) }
                    )
                }
                items(CategoriasRapidas) { cat ->
                    PildoraFiltro(cat, seleccionada = cat == chipElegido, onClick = { onChipRapido(cat) })
                }
            }
            // Fijo al final de la fila: no se va con el deslizamiento de los chips.
            IconButton(onClick = onAbrirFiltros) {
                Icon(IconoFiltro, contentDescription = "Filtros", tint = MarcaAzul)
            }
        }
        if (filtros.hayActivos) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = margen),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros.categorias.toList()) { cat ->
                    PildoraFiltro(cat, seleccionada = true, onClick = { onQuitarCategoria(cat) }, onQuitar = { onQuitarCategoria(cat) })
                }
                if (filtros.soloUrgentes) {
                    item {
                        PildoraFiltro("Urgentes", seleccionada = true, onClick = onQuitarUrgentes, onQuitar = onQuitarUrgentes)
                    }
                }
            }
        }
    }
}

// ---------------- Estados ----------------

/** P-09b: siluetas con la forma de la tarjeta real, barra incluida. No una rueda. */
@Composable
private fun ListaCargando() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Silueta(ancho = 0.4f, alto = 12.dp)
        repeat(3) { TarjetaSilueta() }
    }
}

@Composable
private fun TarjetaSilueta() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Borde)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Silueta(ancho = 0.85f, alto = 14.dp)
            Silueta(ancho = 0.55f, alto = 12.dp)
            Silueta(ancho = 1f, alto = 6.dp)
            Silueta(ancho = 0.4f, alto = 10.dp)
        }
    }
}

@Composable
private fun Silueta(ancho: Float, alto: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth(ancho)
            .height(alto)
            .background(Color(0xFFE2E8F0), RoundedCornerShape(50))
    )
}

/** P-09c: explica por qué está vacía y ofrece salida. Distinto si la causa son los filtros. */
@Composable
private fun EstadoVacio(filtros: FiltrosCampanas, totalAbiertas: Int, onQuitarFiltros: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(IconoCaja, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(52.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            text = tituloVacio(filtros),
            style = MaterialTheme.typography.titleLarge,
            color = Tinta,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (filtros.hayActivos) {
                if (totalAbiertas == 1) "Hay 1 campaña abierta con otros filtros."
                else "Hay $totalAbiertas campañas abiertas con otros filtros."
            } else {
                "Las campañas nuevas aparecen aquí en cuanto se abren."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = TintaSuave,
            textAlign = TextAlign.Center
        )
        if (filtros.hayActivos) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onQuitarFiltros,
                border = BorderStroke(1.5.dp, MarcaAzul),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Quitar filtros", style = MaterialTheme.typography.labelLarge, color = MarcaAzul)
            }
        }
    }
}

private fun tituloVacio(f: FiltrosCampanas): String {
    val cats = f.categorias.joinToString(", ") { it.lowercase() }
    return when {
        f.categorias.isNotEmpty() && f.soloUrgentes -> "Ninguna campaña de $cats cierra esta semana"
        f.categorias.isNotEmpty() -> "Ninguna campaña de $cats"
        f.soloUrgentes -> "Ninguna campaña cierra esta semana"
        else -> "No hay campañas abiertas en Monterrey"
    }
}

/** P-09d: franja roja y Reintentar. */
@Composable
private fun EstadoError(mensaje: String, onReintentar: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = ErrorFondo,
            border = BorderStroke(1.dp, ErrorRojo.copy(alpha = 0.35f))
        ) {
            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorTexto, modifier = Modifier.size(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(mensaje, style = MaterialTheme.typography.labelLarge, color = ErrorTexto)
                    Text(
                        "Revisa tu conexión. Las metas se actualizan en tiempo real y necesitan internet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ErrorTexto
                    )
                }
            }
        }
        OutlinedButton(
            onClick = onReintentar,
            border = BorderStroke(1.5.dp, MarcaAzul),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Reintentar", style = MaterialTheme.typography.labelLarge, color = MarcaAzul)
        }
    }
}

// ---------------- Previews ----------------

private val campanasDePrueba = listOf(
    Campana(
        "c1", "Kits de primera comunión para San Bernabé", "a4", "Útiles escolares",
        "20 de septiembre", true, "", "kits", 45, 18, listOf(ArticuloMeta("c1-1", "Rosario blanco", 45, 12))
    ),
    Campana(
        "c2", "Despensas de fin de mes", "a1", "Alimentos",
        "30 de septiembre", false, "", "despensas", 300, 120, emptyList()
    ),
    Campana(
        "c3", "Ropa de invierno para el albergue", "a3", "Ropa",
        "15 de octubre", false, "", "prendas", 200, 40, emptyList()
    )
)

@Composable
private fun PruebaLista(
    estado: UiState<List<Campana>>,
    filtros: FiltrosCampanas = FiltrosCampanas(),
    chipElegido: String? = null
) {
    FamiliasQueSumanTheme {
        CampanasScreen(
            estado = estado, filtros = filtros, chipElegido = chipElegido, totalAbiertas = 3,
            nombresAsociacion = mapOf(
                "a4" to "Parroquia San Bernabé",
                "a1" to "Comedor Comunitario San Bernabé",
                "a3" to "Albergue Nuevo Amanecer"
            ),
            onBack = {}, onAbrirFiltros = {}, onChipRapido = {}, onQuitarCategoria = {},
            onQuitarUrgentes = {}, onQuitarFiltros = {}, onCampanaClick = {}, onReintentar = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 780) @Composable
private fun PreviewConDatos() = PruebaLista(UiState.Exito(campanasDePrueba))

@Preview(showBackground = true, heightDp = 780) @Composable
private fun PreviewCargando() = PruebaLista(UiState.Cargando)

@Preview(showBackground = true, heightDp = 780) @Composable
private fun PreviewVaciaPorFiltros() =
    PruebaLista(UiState.Exito(emptyList()), FiltrosCampanas(setOf("Juguetes"), soloUrgentes = true))

/** Un chip tocado sin campañas: el chip se queda como botón azul, sin píldora ni ✕. */
@Preview(showBackground = true, heightDp = 780) @Composable
private fun PreviewVaciaPorChip() =
    PruebaLista(UiState.Exito(emptyList()), chipElegido = "Juguetes")

/** Chip elegido con resultados. */
@Preview(showBackground = true, heightDp = 780) @Composable
private fun PreviewConChip() =
    PruebaLista(UiState.Exito(campanasDePrueba.filter { it.categoria == "Alimentos" }), chipElegido = "Alimentos")

/** Filtros aplicados desde la hoja: píldoras con ✕ debajo de los chips. */
@Preview(showBackground = true, heightDp = 780) @Composable
private fun PreviewConFiltrosDeLaHoja() =
    PruebaLista(
        UiState.Exito(campanasDePrueba.filter { it.categoria in setOf("Alimentos", "Ropa") }),
        FiltrosCampanas(setOf("Alimentos", "Ropa"), soloUrgentes = false)
    )

@Preview(showBackground = true, heightDp = 780) @Composable
private fun PreviewError() = PruebaLista(UiState.Error("No se pudieron cargar las campañas"))
