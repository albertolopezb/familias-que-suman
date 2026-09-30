package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.CentroVisiteo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.FilaDato
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TituloDePagina
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

private val TIPOS = listOf("Todos", "Asilos", "Casas hogar", "Comedores")

/**
 * Directorio de Visiteo (P-30, RF-01 · RF-03), calcado de
 * familiasquesuman.com/directorio: buscador, filtros por tipo y tarjetas con
 * información general, necesidades y dirección.
 */
@Composable
fun VisiteoScreen(
    onCentroClick: (String) -> Unit,
    centros: List<CentroVisiteo> = emptyList(),
    ciudad: String = "Monterrey, N.L.",
    onIrAInicio: () -> Unit = {},
    onComoAyudar: () -> Unit = {}
) {
    var tipo by remember { mutableStateOf("Todos") }
    var busqueda by remember { mutableStateOf("") }
    val visibles = centros
        .filter { tipo == "Todos" || it.tipo == tipo }
        .filter { busqueda.isBlank() || it.nombre.contains(busqueda.trim(), ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().background(Web.Fondo)) {
        EncabezadoApp(ciudad = ciudad, onCiudadClick = {})
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "titulo") {
                TituloDePagina(
                    titulo = "Directorio de Visiteo",
                    subtitulo = "Centros y espacios verificados para visitar y ayudar en familia.",
                    migaAnterior = "Inicio",
                    onMigaAnterior = onIrAInicio
                )
                Buscador(valor = busqueda, onValor = { busqueda = it }, placeholder = "Buscar centro...")
            }
            item(key = "filtros") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TIPOS.forEach { t ->
                        val activo = t == tipo
                        Text(
                            t,
                            style = TextoWeb.Chip,
                            color = if (activo) Color.White else Web.Texto,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (activo) Web.Primario else Web.Tarjeta)
                                .border(1.dp, if (activo) Web.Primario else Web.Borde, RoundedCornerShape(50))
                                .clickable { tipo = t }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            if (visibles.isEmpty()) {
                item(key = "vacio") {
                    Text(
                        "No encontramos centros con esos filtros.",
                        style = TextoWeb.Cuerpo,
                        color = Web.TextoApagado,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    )
                }
            }
            items(visibles, key = { it.id }) { centro ->
                TarjetaCentro(
                    centro = centro,
                    onComoAyudar = onComoAyudar,
                    onVerDetalles = { onCentroClick(centro.id) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaCentro(
    centro: CentroVisiteo,
    onComoAyudar: () -> Unit,
    onVerDetalles: () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Web.Tarjeta)
            .border(1.dp, Web.Borde, forma)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Logo(centro.logo, centro.nombre)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Etiqueta(centro.tipo, Web.Secundario, Web.TextoApagado)
                    Row(Modifier.weight(1f)) {}
                    if (centro.verificado) {
                        Icon(
                            IconosWeb.Escudo,
                            contentDescription = "Centro verificado",
                            tint = Web.Primario,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(centro.nombre, style = TextoWeb.TituloTarjeta.copy(color = Web.Primario))
                Text(centro.resumen, style = TextoWeb.Chico, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Información general", style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.SemiBold))
            Text(centro.informacion, style = TextoWeb.Chico, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }

        if (centro.necesidades.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row {
                    Text("Necesidades", style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.SemiBold))
                    Text(" (${centro.necesidades.size})", style = TextoWeb.Cuerpo, color = Web.TextoApagado)
                }
                centro.necesidades.take(3).forEach { n ->
                    Text("•  $n", style = TextoWeb.Chico, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                if (centro.necesidades.size > 3) {
                    Text("+ ${centro.necesidades.size - 3} más...", style = TextoWeb.Chip, color = Web.Primario)
                }
            }
        }

        FilaDato(IconosWeb.Ubicacion, centro.direccion)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Cómo ayudar",
                style = TextoWeb.Chip,
                color = Web.Primario,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Web.Primario, RoundedCornerShape(12.dp))
                    .clickable(onClick = onComoAyudar)
                    .padding(vertical = 10.dp)
            )
            Text(
                "Ver detalles",
                style = TextoWeb.Chip,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Web.Primario)
                    .clickable(onClick = onVerDetalles)
                    .padding(vertical = 10.dp)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun VisiteoPreview() {
    FamiliasQueSumanTheme {
        VisiteoScreen(
            onCentroClick = {},
            centros = listOf(
                CentroVisiteo("ce1", "Morada del Anciano Desvalido Cadereyta", "Asilos",
                    "Asilo de ancianos donde se atienden 24 horas a 46 adultos mayores.",
                    "Atención a adultos mayores en abandono, soledad y falta de apoyo familiar.",
                    listOf("Alimentos", "Limpieza"), "Blvd José María González #1000, Cadereyta", null)
            )
        )
    }
}
