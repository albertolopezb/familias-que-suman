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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            EncabezadoApp()
            BarraSuperior(onBack = onBack, onAbrirFiltros = onAbrirFiltros, esAdmin = esAdmin)

            when (estado) {
                is UiState.Cargando -> ListaCargando()
                is UiState.Error -> EstadoError(mensaje = estado.mensaje, onReintentar = onReintentar)
                is UiState.Exito -> {
                    if (estado.datos.isEmpty()) {
                        Column {
                            if (filtros.hayActivos) {
                                FiltrosActivos(filtros, onQuitarCategoria, onQuitarUrgentes)
                            }
                            EstadoVacio(filtros, totalAbiertas, onQuitarFiltros)
                        }
                    } else {
                        ListaConDatos(
                            campanas = estado.datos,
                            nombresAsociacion = nombresAsociacion,
                            filtros = filtros,
                            onChipRapido = onChipRapido,
                            onQuitarCategoria = onQuitarCategoria,
                            onQuitarUrgentes = onQuitarUrgentes,
                            onCampanaClick = onCampanaClick,
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
private fun BarraSuperior(
    onBack: () -> Unit,
    onAbrirFiltros: () -> Unit,
    esAdmin: Boolean = false
) {
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
        IconButton(onClick = onAbrirFiltros) {
            Icon(IconoFiltro, contentDescription = "Filtros", tint = MarcaAzul)
        }
    }
}

@Composable
private fun ListaConDatos(
    campanas: List<Campana>,
    nombresAsociacion: Map<String, String>,
    filtros: FiltrosCampanas,
    onChipRapido: (String?) -> Unit,
    onQuitarCategoria: (String) -> Unit,
    onQuitarUrgentes: () -> Unit,
    onCampanaClick: (String) -> Unit,
    esAdmin: Boolean = false,
    onEditarCampana: (String) -> Unit = {},
    onBorrarCampana: (String) -> Unit = {}
) {
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            if (filtros.soloUrgentes || filtros.categorias.size > 1 ||
                filtros.categorias.any { it !in CategoriasRapidas }
            ) {
                // Con filtros de la hoja, se ven como píldoras con ✕.
                FiltrosActivos(filtros, onQuitarCategoria, onQuitarUrgentes)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        PildoraFiltro("Todas", seleccionada = filtros.categorias.isEmpty(), onClick = { onChipRapido(null) })
                    }
                    items(CategoriasRapidas) { cat ->
                        PildoraFiltro(cat, seleccionada = cat in filtros.categorias, onClick = { onChipRapido(cat) })
                    }
                }
            }
        }
        item {
            val n = campanas.size
            Text(
                text = if (n == 1) "1 campaña abierta en Monterrey" else "$n campañas abiertas en Monterrey",
                style = MaterialTheme.typography.bodyLarge,
                color = TintaSuave
            )
        }
        items(campanas, key = { it.id }) { campana ->
            Column {
                TarjetaCampana(
                    campana = campana,
                    onClick = { onCampanaClick(campana.id) },
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
    }
}

/** Píldoras azules con ✕ arriba de la lista: los filtros que están activos. */
@Composable
private fun FiltrosActivos(
    filtros: FiltrosCampanas,
    onQuitarCategoria: (String) -> Unit,
    onQuitarUrgentes: () -> Unit
) {
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
private fun PruebaLista(estado: UiState<List<Campana>>, filtros: FiltrosCampanas = FiltrosCampanas()) {
    FamiliasQueSumanTheme {
        CampanasScreen(
            estado = estado, filtros = filtros, totalAbiertas = 3,
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

@Preview(showBackground = true, heightDp = 780) @Composable
private fun PreviewError() = PruebaLista(UiState.Error("No se pudieron cargar las campañas"))
