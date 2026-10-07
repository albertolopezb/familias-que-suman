package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.*
import mx.tec.familiasquesuman.ui.state.UiState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import mx.tec.familiasquesuman.domain.MapaCercano
import mx.tec.familiasquesuman.domain.PuntoEnMapa
import mx.tec.familiasquesuman.domain.TipoSugerencia
import mx.tec.familiasquesuman.ui.components.TarjetaSugerir

@Composable
fun ActividadesScreen(
    estado: UiState<List<ActividadConAsociacion>>,
    ciudad: String,
    onActividadClick: (String) -> Unit,
    onUnirme: (String) -> Unit,
    onCompartir: (ActividadConAsociacion) -> Unit,
    onIrAInicio: () -> Unit,
    onCiudadClick: () -> Unit,
    onReintentar: () -> Unit,
    onVerGuardadas: () -> Unit,
    onVerAsociaciones: () -> Unit,
    modifier: Modifier = Modifier,
    esAdmin: Boolean = false,
    onForzarEstado: () -> Unit = {},
    // callbacks de administración:
    onCrearActividad: () -> Unit = {},
    onEditarActividad: (String) -> Unit = {},
    onBorrarActividad: (String) -> Unit = {},
    onSugerir: () -> Unit = {},
    // la pestaña "Mapa": lo que hay cerca de la familia
    mapa: UiState<MapaCercano> = UiState.Cargando,
    onAbrirPunto: (PuntoEnMapa) -> Unit = {},
    onReintentarMapa: () -> Unit = {}
) {
    // Lista o mapa. Se conserva al ir a un detalle y regresar.
    var enMapa by rememberSaveable { mutableStateOf(false) }
    val titulo = if (esAdmin) "Actividades (Admin)" else "Actividades"
    val modificadorTitulo = Modifier.pointerInput(Unit) {
        detectTapGestures(onLongPress = { onForzarEstado() })
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // Si es Admin, mostramos el FAB con el símbolo (+) abajo. En el mapa estorba.
        floatingActionButton = {
            if (esAdmin && !enMapa) {
                FloatingActionButton(
                    onClick = onCrearActividad,
                    containerColor = Web.Primario,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear Actividad")
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
            if (enMapa) {
                // El mapa no se desplaza con la página: ocupa lo que quede de pantalla.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    TituloDePagina(
                        titulo = titulo,
                        subtitulo = "Cerca de ti en $ciudad",
                        migaAnterior = "Inicio",
                        onMigaAnterior = onIrAInicio,
                        modificadorTitulo = modificadorTitulo
                    )
                    SelectorDeVista(enMapa = true, onCambiar = { enMapa = it })
                    MapaCercaDeTi(
                        estado = mapa,
                        onAbrir = onAbrirPunto,
                        onReintentar = onReintentarMapa,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 12.dp)
                    )
                }
            } else LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
            ) {
                item(key = "titulo") {
                    TituloDePagina(
                        titulo = titulo,
                        subtitulo = "Actividades en $ciudad",
                        migaAnterior = "Inicio",
                        onMigaAnterior = onIrAInicio,
                        modificadorTitulo = modificadorTitulo
                    )
                }
                item(key = "vistas") {
                    SelectorDeVista(
                        enMapa = false,
                        onCambiar = { enMapa = it },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                when (estado) {
                    is UiState.Cargando -> items(2) { TarjetaSilueta(Modifier.padding(bottom = 16.dp)) }
                    is UiState.Error -> item(key = "error") {
                        // Vista de error existente...
                    }
                    is UiState.Exito -> {
                        val (pasadas, proximas) = estado.datos.partition { it.actividad.yaPaso }

                        tarjetasAdmin(
                            actividades = proximas,
                            esAdmin = esAdmin,
                            onActividadClick = onActividadClick,
                            onUnirme = onUnirme,
                            onCompartir = onCompartir,
                            onEditar = onEditarActividad,
                            onBorrar = onBorrarActividad
                        )

                        if (pasadas.isNotEmpty()) {
                            item(key = "rotulo-pasadas") {
                                Text(
                                    text = "ACTIVIDADES PASADAS",
                                    style = TextoWeb.Rotulo,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                                )
                            }
                            tarjetasAdmin(
                                actividades = pasadas,
                                esAdmin = esAdmin,
                                onActividadClick = onActividadClick,
                                onUnirme = onUnirme,
                                onCompartir = onCompartir,
                                onEditar = onEditarActividad,
                                onBorrar = onBorrarActividad,
                                separacion = 12,
                                opacidad = 0.6f
                            )
                        }
                        item(key = "sugerir") {
                            TarjetaSugerir(TipoSugerencia.ACTIVIDAD, onSugerir, Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

/** Las dos pestañas de la pantalla: la lista de siempre y el mapa de lo que hay cerca. */
@Composable
private fun SelectorDeVista(
    enMapa: Boolean,
    onCambiar: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Web.Secundario)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PestanaDeVista("Lista", IconosWeb.Menu, activa = !enMapa, onClick = { onCambiar(false) }, modifier = Modifier.weight(1f))
        PestanaDeVista("Mapa", IconosWeb.Ubicacion, activa = enMapa, onClick = { onCambiar(true) }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PestanaDeVista(
    texto: String,
    icono: ImageVector,
    activa: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (activa) Web.Primario else Web.TextoApagado
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (activa) Web.Tarjeta else Web.Secundario)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(
            text = texto,
            style = TextoWeb.Chip.copy(fontWeight = if (activa) FontWeight.SemiBold else FontWeight.Medium),
            color = color
        )
    }
}

@Composable
fun DialogoFormularioActividad(
    tituloDialogo: String,
    actividadInicial: mx.tec.familiasquesuman.domain.Actividad? = null,
    onGuardar: (
        titulo: String,
        descripcion: String,
        fecha: String,
        horario: String,
        direccion: String,
        cupoTotal: Int,
        lugaresDisponibles: Int,
        edadMinima: Int?
    ) -> Unit,
    onDescartar: () -> Unit
) {
    var titulo by remember { mutableStateOf(actividadInicial?.titulo.orEmpty()) }
    var descripcion by remember { mutableStateOf(actividadInicial?.descripcion.orEmpty()) }
    var fecha by remember { mutableStateOf(actividadInicial?.fecha ?: "15 de Octubre, 2026") }
    var horario by remember { mutableStateOf(actividadInicial?.horario ?: "10:00 - 13:00") }
    var direccion by remember { mutableStateOf(actividadInicial?.direccion ?: "Monterrey, N.L.") }
    var cupoTotalStr by remember { mutableStateOf(actividadInicial?.cupoTotal?.toString() ?: "20") }
    var lugaresDisponiblesStr by remember { mutableStateOf(actividadInicial?.lugaresDisponibles?.toString() ?: "10") }
    var edadMinimaStr by remember { mutableStateOf(actividadInicial?.edadMinima?.toString().orEmpty()) }

    AlertDialog(
        onDismissRequest = onDescartar,
        title = { Text(tituloDialogo) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = horario,
                    onValueChange = { horario = it },
                    label = { Text("Horario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección / Lugar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cupoTotalStr,
                        onValueChange = { cupoTotalStr = it },
                        label = { Text("Cupo Total") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lugaresDisponiblesStr,
                        onValueChange = { lugaresDisponiblesStr = it },
                        label = { Text("Lugares Libres") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = edadMinimaStr,
                    onValueChange = { edadMinimaStr = it },
                    label = { Text("Edad Mínima (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isNotBlank()) {
                        onGuardar(
                            titulo,
                            descripcion,
                            fecha,
                            horario,
                            direccion,
                            cupoTotalStr.toIntOrNull() ?: 20,
                            lugaresDisponiblesStr.toIntOrNull() ?: 10,
                            edadMinimaStr.toIntOrNull()
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDescartar) {
                Text("Cancelar")
            }
        }
    )
}

private fun LazyListScope.tarjetasAdmin(
    actividades: List<ActividadConAsociacion>,
    esAdmin: Boolean,
    onActividadClick: (String) -> Unit,
    onUnirme: (String) -> Unit,
    onCompartir: (ActividadConAsociacion) -> Unit,
    onEditar: (String) -> Unit,
    onBorrar: (String) -> Unit,
    separacion: Int = 16,
    opacidad: Float = 1f
) {
    items(actividades, key = { it.actividad.id }) { item ->
        Column(modifier = Modifier.padding(bottom = separacion.dp)) {
            TarjetaActividad(
                item = item,
                onClick = { onActividadClick(item.actividad.id) },
                onUnirme = { onUnirme(item.actividad.id) },
                onCompartir = { onCompartir(item) },
                modifier = Modifier.alpha(opacidad)
            )

            // Si la cuenta es Admin, agregamos la barra de acciones debajo de cada tarjeta
            if (esAdmin) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onEditar(item.actividad.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Web.Primario)
                    }
                    IconButton(onClick = { onBorrar(item.actividad.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Web.RojoTexto)
                    }
                }
            }
        }
    }
}