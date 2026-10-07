package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.R
import mx.tec.familiasquesuman.domain.CentroVisiteo
import mx.tec.familiasquesuman.domain.TipoSugerencia
import mx.tec.familiasquesuman.ui.components.BotonCrearAdmin
import mx.tec.familiasquesuman.ui.components.FilaAccionesAdmin
import mx.tec.familiasquesuman.ui.components.TarjetaSugerir
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.FilaDato
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.theme.AmbarAcento
import mx.tec.familiasquesuman.ui.theme.AzulBannerFondo
import mx.tec.familiasquesuman.ui.theme.AzulCategoriaFondo
import mx.tec.familiasquesuman.ui.theme.AzulCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.AzulTarjetaFondo
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MentaCategoriaFondo
import mx.tec.familiasquesuman.ui.theme.MentaCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.MentaTarjetaFondo
import mx.tec.familiasquesuman.ui.theme.MoradoCategoriaFondo
import mx.tec.familiasquesuman.ui.theme.MoradoCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.MoradoTarjetaFondo
import mx.tec.familiasquesuman.ui.theme.VerdeCategoriaFondo
import mx.tec.familiasquesuman.ui.theme.VerdeCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.VerdeTarjetaFondo

private val TIPOS = listOf("Todos", "Asilos", "Casas hogar", "Comedores")

/** Los tres tonos de cada tipo de centro, los mismos pasteles de las categorías del Inicio. */
private data class PaletaTipo(val fondo: Color, val fondoFuerte: Color, val texto: Color)

private fun paletaDe(tipo: String): PaletaTipo = when (tipo) {
    "Asilos" -> PaletaTipo(AzulTarjetaFondo, AzulCategoriaFondo, AzulCategoriaTexto)
    "Casas hogar" -> PaletaTipo(MoradoTarjetaFondo, MoradoCategoriaFondo, MoradoCategoriaTexto)
    "Comedores" -> PaletaTipo(VerdeTarjetaFondo, VerdeCategoriaFondo, VerdeCategoriaTexto)
    else -> PaletaTipo(MentaTarjetaFondo, MentaCategoriaFondo, MentaCategoriaTexto)
}

private val PALABRAS_SUELTAS = setOf("de", "del", "y", "en", "para", "la", "el", "los", "las", "con")

/**
 * "Alimentos como: azúcar, leche..." → "Alimentos". Se queda con lo primero de cada necesidad
 * para mostrarla como chip corto; el texto completo se ve en el detalle.
 */
private fun resumenDeNecesidad(necesidad: String): String {
    val corto = necesidad.substringBefore(" como").substringBefore(":").trim()
    val palabras = corto.split(" ").filter { it.isNotBlank() }.take(3).toMutableList()
    while (palabras.size > 1 && palabras.last().lowercase() in PALABRAS_SUELTAS) palabras.removeAt(palabras.lastIndex)
    return palabras.joinToString(" ").replaceFirstChar { it.uppercase() }
}

/**
 * Directorio de Visiteo (P-30, RF-01 · RF-03). Banner como el del Inicio, filtros y tarjetas
 * con el color de cada tipo de centro. El texto largo se queda en el detalle.
 */
@Composable
fun VisiteoScreen(
    onCentroClick: (String) -> Unit,
    centros: List<CentroVisiteo> = emptyList(),
    ciudad: String = "Monterrey, N.L.",
    onIrAInicio: () -> Unit = {},
    onComoAyudar: () -> Unit = {},
    esAdmin: Boolean = false,
    onCrearCentro: () -> Unit = {},
    onEditarCentro: (CentroVisiteo) -> Unit = {},
    onBorrarCentro: (CentroVisiteo) -> Unit = {},
    onSugerir: () -> Unit = {}
) {
    var tipo by remember { mutableStateOf("Todos") }
    var busqueda by remember { mutableStateOf("") }
    val visibles = centros
        .filter { tipo == "Todos" || it.tipo == tipo }
        .filter { busqueda.isBlank() || it.nombre.contains(busqueda.trim(), ignoreCase = true) }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().background(Web.Fondo)) {
        EncabezadoApp()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "banner") {
                BannerDirectorio(ciudad = ciudad, total = centros.size)
            }
            item(key = "buscador") {
                Buscador(valor = busqueda, onValor = { busqueda = it }, placeholder = "Buscar centro...")
            }
            item(key = "filtros") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TIPOS.forEach { t ->
                        val activo = t == tipo
                        val paleta = paletaDe(t)
                        val fondo = when {
                            !activo -> Web.Tarjeta
                            t == "Todos" -> AzulBannerFondo
                            else -> paleta.fondoFuerte
                        }
                        val texto = when {
                            !activo -> Web.Texto
                            t == "Todos" -> Color.White
                            else -> paleta.texto
                        }
                        val borde = when {
                            !activo -> Web.Borde
                            t == "Todos" -> AzulBannerFondo
                            else -> paleta.texto
                        }
                        Text(
                            t,
                            style = TextoWeb.Chip,
                            color = texto,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(fondo)
                                .border(1.dp, borde, RoundedCornerShape(50))
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
                Column {
                    TarjetaCentro(
                        centro = centro,
                        onComoAyudar = onComoAyudar,
                        onVerDetalles = { onCentroClick(centro.id) }
                    )
                    if (esAdmin) {
                        FilaAccionesAdmin(
                            onEditar = { onEditarCentro(centro) },
                            onBorrar = { onBorrarCentro(centro) }
                        )
                    }
                }
            }
            item(key = "sugerir") { TarjetaSugerir(TipoSugerencia.CENTRO, onSugerir) }
        }
    }
    if (esAdmin) {
        BotonCrearAdmin(
            descripcion = "Crear centro",
            onClick = onCrearCentro,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
    }
}

/** El mismo banner del Inicio: foto, capa azul semitransparente, etiqueta ámbar y título blanco. */
@Composable
private fun BannerDirectorio(ciudad: String, total: Int) {
    val forma = RoundedCornerShape(24.dp)
    Box(modifier = Modifier.fillMaxWidth().clip(forma)) {
        Image(
            painter = painterResource(id = R.drawable.fotobanner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(modifier = Modifier.matchParentSize().background(AzulBannerFondo.copy(alpha = 0.67f)))
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "DIRECTORIO · $ciudad".uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = AmbarAcento
            )
            Text(
                text = "Centros que necesitan tu ayuda",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Text(
                text = "$total centros verificados para visitar y ayudar en familia.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun TarjetaCentro(
    centro: CentroVisiteo,
    onComoAyudar: () -> Unit,
    onVerDetalles: () -> Unit
) {
    val paleta = paletaDe(centro.tipo)
    val forma = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Web.Tarjeta)
            .border(1.dp, Web.Borde, forma)
    ) {
        // Franja del color del tipo: logo, tipo y nombre.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(paleta.fondo)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Logo(centro.logo, centro.nombre)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Etiqueta(centro.tipo, paleta.fondoFuerte, paleta.texto)
                    Row(Modifier.weight(1f)) {}
                    if (centro.verificado) {
                        Icon(
                            IconosWeb.Escudo,
                            contentDescription = "Centro verificado",
                            tint = paleta.texto,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(centro.nombre, style = TextoWeb.TituloTarjeta.copy(color = AzulBannerFondo))
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(centro.resumen, style = TextoWeb.Cuerpo, maxLines = 2, overflow = TextOverflow.Ellipsis)

            if (centro.necesidades.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Necesitan", style = TextoWeb.Chip, color = Web.TextoApagado)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        centro.necesidades.map(::resumenDeNecesidad).distinct().forEach { n ->
                            Etiqueta(n, paleta.fondo, paleta.texto)
                        }
                    }
                }
            }

            FilaDato(IconosWeb.Ubicacion, centro.direccion)

            // Cómo ayudar, junto con la información del centro.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(paleta.fondo)
                    .clickable(onClick = onComoAyudar)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Cómo ayudar", style = TextoWeb.Chip, color = paleta.texto)
                    Text(
                        centro.comoAyudar.ifBlank { "Conoce cómo puedes apoyar a este centro." },
                        style = TextoWeb.Chico,
                        color = Web.Texto,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text("Ver cómo →", style = TextoWeb.Chip, color = paleta.texto)
            }

            Button(
                onClick = onVerDetalles,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AmbarAcento),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ver detalles →", color = AzulBannerFondo, fontWeight = FontWeight.Bold)
            }
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
                    listOf("Alimentos como: azúcar, leche", "Limpieza: trapeadores, cubetas"),
                    "Blvd José María González #1000, Cadereyta", null,
                    comoAyudar = "Aportación en especie. Visita a los ancianos para convivir y platicar."),
                CentroVisiteo("ce2", "Casa de la Misericordia", "Casas hogar",
                    "Casa Hogar que ofrece albergue, alimento y atención a jóvenes y adultos.",
                    "Vida digna a personas con enfermedades irreversibles.",
                    listOf("Pañales de adulto tamaño mediano y grande", "Productos de limpieza como fabuloso"),
                    "Monterrey, N.L.", null)
            )
        )
    }
}
