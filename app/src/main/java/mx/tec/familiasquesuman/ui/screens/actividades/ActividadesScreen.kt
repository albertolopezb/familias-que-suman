package mx.tec.familiasquesuman.ui.screens.actividades

import mx.tec.familiasquesuman.ui.components.ubicacionValida
import mx.tec.familiasquesuman.ui.components.CampoUbicacion
import mx.tec.familiasquesuman.domain.coordenadaDeTexto
import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.porCercania
import androidx.compose.foundation.border
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import mx.tec.familiasquesuman.domain.fechaDeTexto
import mx.tec.familiasquesuman.domain.fechaPasadaDeTexto
import java.time.LocalDate
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
    /** Ids de las actividades a las que la familia ya está inscrita. */
    inscritas: Set<String> = emptySet(),
    onForzarEstado: () -> Unit = {},
    // callbacks de administración:
    onCrearActividad: () -> Unit = {},
    onDuplicarActividad: (String) -> Unit = {},
    onEditarActividad: (String) -> Unit = {},
    onBorrarActividad: (String) -> Unit = {},
    onSugerir: () -> Unit = {},
    /** "Cerca de mí": la ubicación aproximada, solo en el teléfono (ver CercaDeMi.kt). null = por fecha. */
    cercaDeMi: Coordenada? = null,
    buscandoUbicacion: Boolean = false,
    avisoUbicacion: String? = null,
    onCercaDeMi: () -> Unit = {}
) {
    val titulo = if (esAdmin) "Actividades (Admin)" else "Actividades"
    val modificadorTitulo = Modifier.pointerInput(Unit) {
        detectTapGestures(onLongPress = { onForzarEstado() })
    }

    Scaffold(
        // El Scaffold de FamiliasApp ya respeta barra de estado y de navegación: aquí no se duplica.
        contentWindowInsets = WindowInsets(0),
        modifier = modifier.fillMaxSize(),
        // Si es Admin, mostramos el FAB con el símbolo (+) abajo
        floatingActionButton = {
            if (esAdmin) {
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
            EncabezadoAzul(
                etiqueta = "Suma en familia",
                titulo = titulo,
                subtitulo = "Actividades en $ciudad",
                modificadorTitulo = modificadorTitulo
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
            ) {

                when (estado) {
                    is UiState.Cargando -> items(2) { TarjetaSilueta(Modifier.padding(bottom = 16.dp)) }
                    is UiState.Error -> item(key = "error") {
                        AvisoLista(
                            titulo = "No pudimos cargar las actividades",
                            texto = estado.mensaje,
                            boton = "Reintentar",
                            onBoton = onReintentar
                        )
                    }
                    is UiState.Exito -> {
                        // Las que vienen, de la más cercana a la más lejana; las pasadas, de la más reciente a la más vieja.
                        val (pasadasSinOrden, proximasSinOrden) = estado.datos.partition { it.actividad.yaPaso }
                        val porFecha = proximasSinOrden.sortedBy { fechaDeTexto(it.actividad.fecha) ?: LocalDate.MAX }
                        val proximas = cercaDeMi?.let { aqui -> porFecha.porCercania(aqui) { it.actividad.coordenada } } ?: porFecha
                        val pasadas = pasadasSinOrden.sortedByDescending { fechaPasadaDeTexto(it.actividad.fecha) ?: LocalDate.MIN }

                        if (proximas.isNotEmpty()) {
                            item(key = "cerca-de-mi") {
                                FiltroCercaDeMi(cercaDeMi != null, buscandoUbicacion, avisoUbicacion, onCercaDeMi)
                            }
                        }

                        if (proximas.isEmpty()) {
                            item(key = "sin-actividades") {
                                AvisoLista(
                                    titulo = "No hay actividades disponibles por ahora",
                                    texto = "Cuando una asociación publique una actividad en $ciudad, aparecerá aquí. " +
                                        "¿Conoces una? Sugiérela abajo.",
                                    boton = null,
                                    onBoton = {}
                                )
                            }
                        }

                        tarjetasAdmin(
                            actividades = proximas,
                            esAdmin = esAdmin,
                            inscritas = inscritas,
                            onActividadClick = onActividadClick,
                            onUnirme = onUnirme,
                            onCompartir = onCompartir,
                            onDuplicarActividad = onDuplicarActividad,
                            onEditarActividad = onEditarActividad,
                            onBorrarActividad = onBorrarActividad
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
                                inscritas = inscritas,
                                onActividadClick = onActividadClick,
                                onUnirme = onUnirme,
                                onCompartir = onCompartir,
                                onDuplicarActividad = onDuplicarActividad,
                                onEditarActividad = onEditarActividad,
                                onBorrarActividad = onBorrarActividad,
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

/** El botón "Cerca de mí" y, debajo, qué está pasando con la ubicación. */
@Composable
private fun FiltroCercaDeMi(activo: Boolean, buscando: Boolean, aviso: String?, onClick: () -> Unit) {
    Column(Modifier.padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (activo) Web.Primario else Web.Tarjeta)
                .border(1.dp, if (activo) Web.Primario else Web.Borde, RoundedCornerShape(50))
                .clickable(enabled = !buscando, onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            if (buscando) CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = Web.Primario)
            else Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = if (activo) Color.White else Web.Primario, modifier = Modifier.size(16.dp))
            Text(if (buscando) "Buscando…" else "Cerca de mí", style = TextoWeb.Chip, color = if (activo) Color.White else Web.Texto)
        }
        when {
            aviso != null -> Text(aviso, style = TextoWeb.Chico)
            activo -> Text("De la más cercana a la más lejana. Tu ubicación es aproximada y no sale de tu teléfono.", style = TextoWeb.Chico)
        }
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
        edadMinima: Int?,
        ubicacion: Coordenada?
    ) -> Unit,
    onDescartar: () -> Unit,
    onDuplicar: (() -> Unit)? = null
) {
    var titulo by remember { mutableStateOf(actividadInicial?.titulo.orEmpty()) }
    var descripcion by remember { mutableStateOf(actividadInicial?.descripcion.orEmpty()) }
    var fecha by remember { mutableStateOf(actividadInicial?.fecha ?: "15 de Octubre, 2026") }
    var horario by remember { mutableStateOf(actividadInicial?.horario ?: "10:00 - 13:00") }
    var direccion by remember { mutableStateOf(actividadInicial?.direccion ?: "Monterrey, N.L.") }
    var cupoTotalStr by remember { mutableStateOf(actividadInicial?.cupoTotal?.toString() ?: "20") }
    var lugaresDisponiblesStr by remember { mutableStateOf(actividadInicial?.lugaresDisponibles?.toString() ?: "10") }
    var edadMinimaStr by remember { mutableStateOf(actividadInicial?.edadMinima?.toString().orEmpty()) }
    var ubicacion by remember { mutableStateOf(actividadInicial?.coordenada?.comoTexto().orEmpty()) }

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
                CampoUbicacion(ubicacion, { ubicacion = it })
            }
        },
        confirmButton = {
            Button(
                enabled = ubicacionValida(ubicacion),
                onClick = {
                    if (titulo.isNotBlank() && ubicacionValida(ubicacion)) {
                        onGuardar(
                            titulo,
                            descripcion,
                            fecha,
                            horario,
                            direccion,
                            cupoTotalStr.toIntOrNull() ?: 20,
                            lugaresDisponiblesStr.toIntOrNull() ?: 10,
                            edadMinimaStr.toIntOrNull(),
                            coordenadaDeTexto(ubicacion)
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDuplicar != null) {
                    TextButton(onClick = onDuplicar) {
                        Text("Duplicar")
                    }
                }
                TextButton(onClick = onDescartar) {
                    Text("Cancelar")
                }
            }
        }
    )
}

private fun LazyListScope.tarjetasAdmin(
    actividades: List<ActividadConAsociacion>,
    esAdmin: Boolean,
    inscritas: Set<String>,
    onActividadClick: (String) -> Unit,
    onUnirme: (String) -> Unit,
    onCompartir: (ActividadConAsociacion) -> Unit,
    onDuplicarActividad: (String) -> Unit,
    onEditarActividad: (String) -> Unit,
    onBorrarActividad: (String) -> Unit,
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
                modifier = Modifier.alpha(opacidad),
                inscrito = item.actividad.id in inscritas
            )

            // Si la cuenta es Admin, agregamos la barra de acciones debajo de cada tarjeta
            // En la tarjeta de la actividad dentro de ActividadesScreen.kt
            if (esAdmin) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ícono de Duplicar (al lado del lápiz)
                    IconButton(onClick = { onDuplicarActividad(item.actividad.id) }) {
                        Icon(
                            imageVector = IconosWeb.Copiar,
                            contentDescription = "Duplicar Actividad",
                            tint = Web.Primario // O el color primario de tu paleta
                        )
                    }

                    // Ícono de Editar (Lápiz)
                    IconButton(onClick = { onEditarActividad(item.actividad.id) }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Editar Actividad",
                            tint = Web.Primario
                        )
                    }

                    // Ícono de Eliminar (Bote de basura)
                    IconButton(onClick = { onBorrarActividad(item.actividad.id) }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Eliminar Actividad",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/** El aviso de la lista cuando no hay actividades o no se pudieron cargar. */
@Composable
private fun AvisoLista(titulo: String, texto: String, boton: String?, onBoton: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CirculoDeIcono(icono = IconosWeb.CalendarioVacio, color = Web.Primario, fondo = Web.Secundario)
        Text(titulo, style = TextoWeb.TituloTarjeta, textAlign = TextAlign.Center)
        Text(texto, style = TextoWeb.Cuerpo, color = Web.TextoApagado, textAlign = TextAlign.Center)
        if (boton != null) {
            BotonAmarillo(texto = boton, onClick = onBoton, conFlechas = false, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
