package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import mx.tec.familiasquesuman.domain.Proyecto
import mx.tec.familiasquesuman.domain.TipoSugerencia
import mx.tec.familiasquesuman.ui.components.TarjetaSugerir
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.FilaDato
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TituloDePagina
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.fotoDeActividad
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * Proyectos (P-29, RF-09), calcado de familiasquesuman.com/proyectos:
 * pestañas activos / anteriores, buscador y tarjetas con logo.
 */
@Composable
fun ProyectosScreen(
    proyectos: List<Proyecto> = emptyList(),
    ciudad: String = "Monterrey, N.L.",
    esAdmin: Boolean = false,
    onIrAInicio: () -> Unit = {},
    onProyectoClick: (String) -> Unit = {},
    onCrearProyecto: () -> Unit = {},
    onEditarProyecto: (String) -> Unit = {},
    onBorrarProyecto: (String) -> Unit = {},
    onSugerir: () -> Unit = {}
) {
    var verActivos by remember { mutableStateOf(true) }
    var busqueda by remember { mutableStateOf("") }
    val visibles = proyectos
        .filter { it.activo == verActivos }
        .filter { busqueda.isBlank() || it.nombre.contains(busqueda.trim(), ignoreCase = true) }

    Scaffold(
        floatingActionButton = {
            if (esAdmin) {
                FloatingActionButton(
                    onClick = onCrearProyecto,
                    containerColor = Web.Primario,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear Proyecto")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Web.Fondo)
        ) {
            EncabezadoApp()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "titulo") {
                    TituloDePagina(
                        titulo = if (esAdmin) "Proyectos (Admin)" else "Proyectos",
                        subtitulo = "Proyectos con causas y objetivos específicos.",
                        migaAnterior = "Inicio",
                        onMigaAnterior = onIrAInicio
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Pestana("Proyectos activos", verActivos, { verActivos = true }, Modifier.weight(1f))
                        Pestana("Proyectos anteriores", !verActivos, { verActivos = false }, Modifier.weight(1f))
                    }
                }
                item(key = "buscar") {
                    Buscador(valor = busqueda, onValor = { busqueda = it }, placeholder = "Buscar proyectos...")
                }
                if (visibles.isEmpty()) {
                    item(key = "vacio") {
                        Text(
                            if (verActivos) "No hay proyectos que coincidan." else "Aún no hay proyectos anteriores.",
                            style = TextoWeb.Cuerpo,
                            color = Web.TextoApagado,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                        )
                    }
                }
                items(visibles, key = { it.id }) { proyecto ->
                    TarjetaProyecto(
                        proyecto = proyecto,
                        onClick = { onProyectoClick(proyecto.id) },
                        esAdmin = esAdmin,
                        onEditar = { onEditarProyecto(proyecto.id) },
                        onBorrar = { onBorrarProyecto(proyecto.id) }
                    )
                }
                item(key = "sugerir") { TarjetaSugerir(TipoSugerencia.PROYECTO, onSugerir) }
            }
        }
    }
}

@Composable
private fun TarjetaProyecto(
    proyecto: Proyecto,
    onClick: () -> Unit,
    esAdmin: Boolean = false,
    onEditar: () -> Unit = {},
    onBorrar: () -> Unit = {}
) {
    val forma = RoundedCornerShape(16.dp)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(forma)
                .background(Web.Tarjeta)
                .border(1.dp, Web.Borde, forma)
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Logo(proyecto.logo, proyecto.nombre)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        proyecto.nombre,
                        style = TextoWeb.TituloTarjeta.copy(color = Web.Primario),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    Etiqueta(
                        if (proyecto.activo) "Activo" else "Terminado",
                        if (proyecto.activo) Web.VerdeFondo else Web.Secundario,
                        if (proyecto.activo) Web.VerdeTexto else Web.TextoApagado
                    )
                }
                Text(
                    proyecto.descripcion,
                    style = TextoWeb.Chico.copy(color = Web.Texto.copy(alpha = 0.75f)),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                proyecto.vigencia?.let { FilaDato(IconosWeb.Calendario, it) }
                proyecto.beneficiarios?.let { FilaDato(IconosWeb.Personas, it) }
                FilaDato(IconosWeb.Ubicacion, proyecto.ciudad)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Ver detalles", style = TextoWeb.Chip, color = Web.Primario)
                    Icon(IconosWeb.FlechaDerecha, contentDescription = null, tint = Web.Primario, modifier = Modifier.size(14.dp))
                }
            }
        }
        if (esAdmin) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Web.Primario)
                }
                IconButton(onClick = onBorrar) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Web.RojoTexto)
                }
            }
        }
    }
}

/** El logo cuadrado de un proyecto o centro, sin recortar, como en el sitio. */
@Composable
internal fun Logo(nombreDrawable: String?, descripcion: String, tamano: Int = 56) {
    val recurso = fotoDeActividad(nombreDrawable)
    Box(
        modifier = Modifier.size(tamano.dp).clip(RoundedCornerShape(12.dp)).background(Web.Tarjeta),
        contentAlignment = Alignment.Center
    ) {
        if (recurso != null) {
            Image(
                painter = painterResource(recurso),
                contentDescription = descripcion,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize().background(Web.Secundario))
        }
    }
}

@Composable
private fun Pestana(texto: String, activa: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            texto,
            style = TextoWeb.Cuerpo.copy(fontWeight = if (activa) FontWeight.SemiBold else FontWeight.Medium),
            color = if (activa) Web.Primario else Web.TextoApagado,
            modifier = Modifier.padding(vertical = 10.dp)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(if (activa) 2.dp else 1.dp)
                .background(if (activa) Web.Primario else Web.Borde)
        )
    }
}

/** La caja de búsqueda del sitio: borde gris, lupa y texto gris. */
@Composable
internal fun Buscador(valor: String, onValor: (String) -> Unit, placeholder: String) {
    val forma = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Web.Tarjeta)
            .border(1.dp, Web.Borde, forma)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(IconosWeb.Buscar, contentDescription = null, tint = Web.TextoApagado, modifier = Modifier.size(16.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (valor.isEmpty()) Text(placeholder, style = TextoWeb.Cuerpo, color = Web.TextoApagado)
            BasicTextField(
                value = valor,
                onValueChange = onValor,
                singleLine = true,
                textStyle = TextoWeb.Cuerpo,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun ProyectosPreview() {
    FamiliasQueSumanTheme {
        ProyectosScreen(
            proyectos = listOf(
                Proyecto("pr1", "Trazo... Escribiendo una nueva historia",
                    "Somos un grupo de mujeres voluntarias que realizamos visitas quincenales.",
                    "25 Mujeres", "Monterrey", null, null),
                Proyecto("pr8", "Escucha Corazón", "Una visita al mes al colegio Mano Amiga.",
                    null, "Monterrey", "Hasta 29 jun 2026", null)
            )
        )
    }
}
