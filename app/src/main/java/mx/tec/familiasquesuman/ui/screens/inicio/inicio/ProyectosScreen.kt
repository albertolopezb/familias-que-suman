package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.domain.Proyecto
import mx.tec.familiasquesuman.domain.TipoSugerencia
import mx.tec.familiasquesuman.ui.components.TarjetaSugerir
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.FilaDato
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.fotoDeActividad
import mx.tec.familiasquesuman.ui.theme.AzulCategoriaFondo
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul

private val ColoresTarjetasAzul = listOf(
    Color(0xFFE0F2FE), // Azul cielo suave
    Color(0xFFD3E2FE), // Azul periwinkle / lavanda
    Color(0xFFBAE6FD), // Azul agua
    Color(0xFFC7D2FE)  // Azul índigo pastel
)

private val ColoresTextoAzul = listOf(
    Color(0xFF0369A1),
    Color(0xFF1E40AF),
    Color(0xFF0284C7),
    Color(0xFF3730A3)
)

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
                    containerColor = MarcaAzul,
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
                verticalArrangement = Arrangement.Top
            ) {
                item(key = "titulo") {
                    Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                        Text(
                            text = if (esAdmin) "Proyectos (Admin)" else "Proyectos",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MarcaAzul
                        )
                    }
                }

                item(key = "pestanas") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFEEF2F6))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PestanaPill("Activos (${proyectos.count { it.activo }})", verActivos, { verActivos = true }, Modifier.weight(1f))
                        PestanaPill("Anteriores (${proyectos.count { !it.activo }})", !verActivos, { verActivos = false }, Modifier.weight(1f))
                    }
                }

                item(key = "buscar") {
                    BuscadorEstiloMinimal(
                        valor = busqueda,
                        onValor = { busqueda = it },
                        placeholder = "Buscar proyectos..."
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (visibles.isEmpty()) {
                    item(key = "vacio") {
                        Text(
                            if (verActivos) "No hay proyectos que coincidan." else "Aún no hay proyectos anteriores.",
                            style = TextoWeb.Cuerpo,
                            color = Web.TextoApagado,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)
                        )
                    }
                }

                //(Stacked Cards)
                itemsIndexed(visibles, key = { _, item -> item.id }) { indice, proyecto ->
                    val colorFondo = ColoresTarjetasAzul[indice % ColoresTarjetasAzul.size]
                    val colorTexto = ColoresTextoAzul[indice % ColoresTextoAzul.size]

                    // Solapamiento negativo (las tarjetas se enciman levemente excepto la primera)
                    val offsetY = if (indice > 0) (-20 * indice).dp else 0.dp

                    Box(
                        modifier = Modifier
                            .offset(y = offsetY)
                    ) {
                        TarjetaProyectoStacked(
                            proyecto = proyecto,
                            colorFondo = colorFondo,
                            colorTexto = colorTexto,
                            onClick = { onProyectoClick(proyecto.id) },
                            esAdmin = esAdmin,
                            onEditar = { onEditarProyecto(proyecto.id) },
                            onBorrar = { onBorrarProyecto(proyecto.id) }
                        )
                    }
                }

                item(key = "sugerir") {
                    val offsetY = if (visibles.isNotEmpty()) (-20 * visibles.size).dp else 0.dp
                    Box(modifier = Modifier.offset(y = offsetY).padding(top = 24.dp)) {
                        TarjetaSugerir(TipoSugerencia.PROYECTO, onSugerir)
                    }
                }
            }
        }
    }
}

/** Tarjeta con estilo redondeado gigante, bloques de color pastel e ícono flotante circular */
@Composable
private fun TarjetaProyectoStacked(
    proyecto: Proyecto,
    colorFondo: Color,
    colorTexto: Color,
    onClick: () -> Unit,
    esAdmin: Boolean = false,
    onEditar: () -> Unit = {},
    onBorrar: () -> Unit = {}
) {
    val formaTarjeta = RoundedCornerShape(28.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(formaTarjeta)
            .background(colorFondo)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo circular limpio
            LogoCircular(proyecto.logo, proyecto.nombre)

            // Chip indicador simple
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (proyecto.activo) "Activo" else "Finalizado",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = proyecto.nombre,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MarcaAzul,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = proyecto.descripcion,
            fontSize = 13.sp,
            color = MarcaAzul.copy(alpha = 0.75f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            proyecto.vigencia?.let { FilaDato(IconosWeb.Calendario, it) }
            proyecto.beneficiarios?.let { FilaDato(IconosWeb.Personas, it) }
            FilaDato(IconosWeb.Ubicacion, proyecto.ciudad)
        }

        if (esAdmin) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MarcaAzul)
                }
                IconButton(onClick = onBorrar) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Web.RojoTexto)
                }
            }
        }
    }
}

/** Logo circular dentro de una burbuja blanca */
@Composable
internal fun LogoCircular(nombreDrawable: String?, descripcion: String) {
    val recurso = fotoDeActividad(nombreDrawable)
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color.White)
            .padding(6.dp),
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
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(AzulCategoriaFondo)
            )
        }
    }
}

/** Pestañas tipo pastilla moderna */
@Composable
private fun PestanaPill(texto: String, activa: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .clip(forma)
            .background(if (activa) MarcaAzul else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            fontSize = 14.sp,
            fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium,
            color = if (activa) Color.White else Web.TextoApagado
        )
    }
}

