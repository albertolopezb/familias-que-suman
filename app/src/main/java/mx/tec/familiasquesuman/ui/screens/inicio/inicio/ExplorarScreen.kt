package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.ui.components.BarraSuperior
import mx.tec.familiasquesuman.ui.components.BotonCrearAdmin
import mx.tec.familiasquesuman.ui.components.FilaAccionesAdmin
import mx.tec.familiasquesuman.ui.components.iniciales
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * "Causas": las asociaciones de la ciudad, con buscador y filtro por categoría. Se llega desde
 * "Causas destacadas → Ver todas" del Inicio.
 */
@Composable
fun ExplorarScreen(
    textoBusqueda: String,
    onBusquedaChange: (String) -> Unit,
    categoriaSeleccionada: String?,
    onCategoriaSelect: (String?) -> Unit,
    asociaciones: List<Asociacion>,
    onAsociacionClick: (String) -> Unit,
    esAdmin: Boolean = false,
    onCrearAsociacion: () -> Unit = {},
    onEditarAsociacion: (Asociacion) -> Unit = {},
    onBorrarAsociacion: (Asociacion) -> Unit = {},
    categorias: List<String> = emptyList(),
    favoritas: Set<String> = emptySet(),
    onAlternarFavorita: (String) -> Unit = {},
    onRegresar: () -> Unit = {}
) {
    Box(Modifier.fillMaxSize().background(Web.Fondo)) {
        Column(Modifier.fillMaxSize()) {
            BarraSuperior("Causas", onRegresar = onRegresar)
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "buscar") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Buscador(textoBusqueda, onBusquedaChange, "Buscar asociaciones…")
                        if (categorias.isNotEmpty()) {
                            Row(
                                Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Chip("Todas", categoriaSeleccionada == null) { onCategoriaSelect(null) }
                                categorias.forEach { c -> Chip(c, categoriaSeleccionada == c) { onCategoriaSelect(c) } }
                            }
                        }
                        Text(
                            if (asociaciones.size == 1) "1 asociación" else "${asociaciones.size} asociaciones",
                            style = TextoWeb.Chico
                        )
                    }
                }
                if (asociaciones.isEmpty()) {
                    item(key = "vacio") {
                        Text(
                            "No encontramos asociaciones con esa búsqueda. Prueba con otra palabra.",
                            style = TextoWeb.Cuerpo, color = Web.TextoApagado, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                        )
                    }
                }
                items(asociaciones, key = { it.id }) { asociacion ->
                    Column {
                        TarjetaAsociacion(
                            asociacion,
                            esFavorita = asociacion.id in favoritas,
                            onClick = { onAsociacionClick(asociacion.id) },
                            onAlternarFavorita = { onAlternarFavorita(asociacion.id) }
                        )
                        if (esAdmin) {
                            FilaAccionesAdmin(
                                onEditar = { onEditarAsociacion(asociacion) },
                                onBorrar = { onBorrarAsociacion(asociacion) }
                            )
                        }
                    }
                }
            }
        }
        if (esAdmin) {
            BotonCrearAdmin(
                descripcion = "Crear asociación",
                onClick = onCrearAsociacion,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }
}

@Composable
private fun Chip(texto: String, activo: Boolean, onClick: () -> Unit) {
    Text(
        texto,
        style = TextoWeb.Chip,
        color = if (activo) Color.White else Web.Texto,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (activo) Web.Primario else Web.Tarjeta)
            .border(1.dp, if (activo) Web.Primario else Web.Borde, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun TarjetaAsociacion(
    asociacion: Asociacion,
    esFavorita: Boolean,
    onClick: () -> Unit,
    onAlternarFavorita: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Web.Tarjeta,
        border = BorderStroke(1.dp, Web.Borde),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(Web.AmbarFondo),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales(asociacion.nombre), style = TextoWeb.TituloTarjeta, color = Web.AmbarTexto)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(asociacion.nombre, style = TextoWeb.TituloTarjeta.copy(color = Web.Primario))
                Etiqueta(asociacion.categoria, Web.AmbarFondo, Web.AmbarTexto)
                Text(
                    asociacion.descripcion, style = TextoWeb.Chico, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onAlternarFavorita, modifier = Modifier.size(36.dp)) {
                Icon(
                    if (esFavorita) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (esFavorita) "Quitar de favoritas" else "Guardar en favoritas",
                    tint = if (esFavorita) Web.Amarillo else Web.TextoApagado
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExplorarPreview() {
    FamiliasQueSumanTheme {
        ExplorarScreen(
            "", {}, null, {},
            listOf(Asociacion("a1", "Comedor Comunitario San Bernabé", "Alimentación",
                "Damos comida caliente a 180 familias.", "", "", "", "")),
            {}, categorias = listOf("Alimentación", "Adultos mayores")
        )
    }
}
