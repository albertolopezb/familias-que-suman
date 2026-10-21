package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.MapaDeCiudad
import mx.tec.familiasquesuman.domain.PuntoEnMapa
import mx.tec.familiasquesuman.domain.TipoDePunto
import mx.tec.familiasquesuman.domain.porCercania
import mx.tec.familiasquesuman.domain.textoDeDistancia
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonAmarillo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.FilaDato
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.AzulCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.AzulTarjetaFondo
import mx.tec.familiasquesuman.ui.theme.MentaCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.MentaTarjetaFondo
import mx.tec.familiasquesuman.ui.theme.MoradoCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.MoradoTarjetaFondo
import mx.tec.familiasquesuman.ui.theme.VerdeCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.VerdeTarjetaFondo
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * El mapa del Inicio: actividades, proyectos, donaciones y centros del Directorio de la ciudad
 * elegida (Monterrey u Hermosillo), con filtro por tipo y un mini menú al tocar un puntero.
 *
 * "Buscar cerca de": la persona escribe un municipio, una colonia o una calle (o mantiene el dedo
 * sobre el mapa) y ve lo más cercano a ese punto, con la distancia. No usa la ubicación del
 * teléfono ni pide ningún permiso. Los punteros llevan el color de su tarjeta en el Inicio
 * (actividades en morado, donar en verde, proyectos en azul, directorio en menta).
 *
 * Las calles son de OpenStreetMap ([LienzoDeCalles], en MapaCalles.kt). El botón de arriba a la
 * izquierda lo agranda a pantalla completa, y ahí mismo se cierra.
 */
@Composable
fun MapaGeneral(
    estado: UiState<MapaDeCiudad>,
    onAbrir: (PuntoEnMapa) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
    cercaDe: BusquedaCercana = BusquedaCercana(),
    onBuscarCercaDe: (String) -> Unit = {},
    onElegirPunto: (Coordenada) -> Unit = {},
    onQuitarCercaDe: () -> Unit = {}
) {
    var ampliado by rememberSaveable { mutableStateOf(false) }
    val busqueda = AccionesDeBusqueda(cercaDe, onBuscarCercaDe, onElegirPunto, onQuitarCercaDe)

    CuerpoDelMapa(estado, onAbrir, onReintentar, ampliado = false, onAlternar = { ampliado = true }, busqueda = busqueda, modifier = modifier)

    if (ampliado) {
        Dialog(
            onDismissRequest = { ampliado = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Web.Fondo)
                    .padding(12.dp)
            ) {
                val ciudad = (estado as? UiState.Exito)?.datos?.ciudad.orEmpty()
                Text(
                    text = if (ciudad.isBlank()) "Mapa" else "Mapa de $ciudad",
                    style = TextoWeb.TituloTarjeta,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 12.dp)
                )
                CuerpoDelMapa(
                    estado = estado,
                    onAbrir = { punto -> ampliado = false; onAbrir(punto) },
                    onReintentar = onReintentar,
                    ampliado = true,
                    onAlternar = { ampliado = false },
                    busqueda = busqueda,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
        }
    }
}

/** "Buscar cerca de": el estado y lo que se puede hacer con él, juntos para pasarlos de una vez. */
private class AccionesDeBusqueda(
    val estado: BusquedaCercana,
    val onBuscar: (String) -> Unit,
    val onElegirPunto: (Coordenada) -> Unit,
    val onQuitar: () -> Unit
)

/** Los filtros, "Buscar cerca de", el mapa y el mini menú. Es lo mismo en el Inicio y a pantalla completa. */
@Composable
private fun CuerpoDelMapa(
    estado: UiState<MapaDeCiudad>,
    onAbrir: (PuntoEnMapa) -> Unit,
    onReintentar: () -> Unit,
    ampliado: Boolean,
    onAlternar: () -> Unit,
    busqueda: AccionesDeBusqueda,
    modifier: Modifier = Modifier
) {
    // Texto y no enum para que sobreviva a ir al detalle y regresar sin Saver propio.
    var filtro by rememberSaveable { mutableStateOf(FILTRO_TODO) }
    var claveSeleccionada by rememberSaveable { mutableStateOf("") }

    val todos = (estado as? UiState.Exito)?.datos?.puntos.orEmpty()
    val visibles = if (filtro == FILTRO_TODO) todos else todos.filter { it.tipo.name == filtro }
    val seleccionado = visibles.firstOrNull { it.clave == claveSeleccionada }
    val referencia = busqueda.estado.lugar
    // Con un lugar de referencia, lo más cercano primero (para la lista de abajo del mapa).
    val cercanos = referencia?.let { lugar -> visibles.porCercania(lugar.coordenada) { it.coordenada } }.orEmpty()

    Column(modifier = modifier) {
        FiltrosDelMapa(
            filtro = filtro,
            puntos = todos,
            onFiltro = { nuevo ->
                filtro = nuevo
                // Si el puntero abierto ya no se ve con el filtro nuevo, se cierra su menú.
                if (nuevo != FILTRO_TODO && !claveSeleccionada.startsWith("$nuevo:")) {
                    claveSeleccionada = ""
                }
            }
        )
        Spacer(Modifier.size(10.dp))
        if (estado is UiState.Exito) {
            BuscarCercaDe(busqueda)
            Spacer(Modifier.size(10.dp))
        }

        val forma = RoundedCornerShape(16.dp)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(forma)
                .background(FondoMapa)
                .border(1.dp, Web.Borde, forma),
            contentAlignment = Alignment.Center
        ) {
            when (estado) {
                is UiState.Cargando -> CircularProgressIndicator(color = Web.Primario)

                is UiState.Error -> Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(estado.mensaje, style = TextoWeb.Cuerpo, textAlign = TextAlign.Center)
                    Text(
                        "Reintentar",
                        style = TextoWeb.Boton,
                        color = Web.Primario,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onReintentar)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                is UiState.Exito -> {
                    LienzoDeCalles(
                        centroDeCiudad = estado.datos.centro,
                        visibles = visibles,
                        claveSeleccionada = claveSeleccionada,
                        onSeleccionar = { claveSeleccionada = it },
                        referencia = referencia?.coordenada,
                        onMantener = busqueda.onElegirPunto
                    )

                    // Agrandar a pantalla completa, o cerrar si ya lo está.
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .shadow(3.dp, RoundedCornerShape(50))
                            .clip(RoundedCornerShape(50))
                            .background(Web.Tarjeta)
                            .clickable(onClickLabel = if (ampliado) "Cerrar el mapa" else "Agrandar el mapa", onClick = onAlternar)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (ampliado) IconosWeb.Cerrar else IconosWeb.Ubicacion,
                            contentDescription = null,
                            tint = Web.Primario,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(if (ampliado) "Cerrar" else "Agrandar", style = TextoWeb.Chip, color = Web.Primario)
                    }

                    if (visibles.isEmpty()) {
                        val tipoFiltrado = TipoDePunto.entries.firstOrNull { it.name == filtro }
                        Text(
                            text = when {
                                todos.isEmpty() -> "Pronto habrá actividades, proyectos, donaciones y centros en ${estado.datos.ciudad}."
                                tipoFiltrado != null -> "Por ahora no hay ${tipoFiltrado.plural.lowercase()} en ${estado.datos.ciudad}."
                                else -> "Por ahora no hay nada en ${estado.datos.ciudad}."
                            },
                            style = TextoWeb.Cuerpo,
                            color = Web.TextoApagado,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Web.Tarjeta)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }

                    if (seleccionado != null) {
                        MiniMenuDePunto(
                            punto = seleccionado,
                            distancia = referencia?.let {
                                textoDeDistancia(it.coordenada.distanciaKm(seleccionado.coordenada)) + " de " + it.nombre
                            },
                            onAbrir = { onAbrir(seleccionado) },
                            onCerrar = { claveSeleccionada = "" },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(10.dp)
                        )
                    } else if (referencia != null && cercanos.isNotEmpty()) {
                        LoMasCercano(
                            lugar = referencia.nombre,
                            puntos = cercanos.take(3),
                            distanciaDe = { textoDeDistancia(referencia.coordenada.distanciaKm(it.coordenada)) },
                            onElegir = { claveSeleccionada = it.clave },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

private const val FILTRO_TODO = "TODO"

/** Un gris azulado más frío que el fondo de la página, para que el lienzo se lea como mapa. */
private val FondoMapa = Color(0xFFE6EDF3)

/** El color de cada puntero es el de su tarjeta en el Inicio. */
private val TipoDePunto.color: Color
    get() = when (this) {
        TipoDePunto.ACTIVIDAD -> MoradoCategoriaTexto
        TipoDePunto.PROYECTO -> AzulCategoriaTexto
        TipoDePunto.DONACION -> VerdeCategoriaTexto
        TipoDePunto.DIRECTORIO -> MentaCategoriaTexto
    }

private val TipoDePunto.fondo: Color
    get() = when (this) {
        TipoDePunto.ACTIVIDAD -> MoradoTarjetaFondo
        TipoDePunto.PROYECTO -> AzulTarjetaFondo
        TipoDePunto.DONACION -> VerdeTarjetaFondo
        TipoDePunto.DIRECTORIO -> MentaTarjetaFondo
    }

/** Los mismos íconos que la barra inferior usa para cada sección. */
private val TipoDePunto.icono: ImageVector
    get() = when (this) {
        TipoDePunto.ACTIVIDAD -> IconosWeb.Personas
        TipoDePunto.PROYECTO -> IconosWeb.Foco
        TipoDePunto.DONACION -> IconosWeb.ManoCorazon
        TipoDePunto.DIRECTORIO -> IconosWeb.Casa
    }

// ---------------------------------------------------------------------------
// Filtros
// ---------------------------------------------------------------------------

/** "Todo" y un chip por tipo, con cuántos hay. También sirven de leyenda de colores. */
@Composable
private fun FiltrosDelMapa(
    filtro: String,
    puntos: List<PuntoEnMapa>,
    onFiltro: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChipFiltro(
            texto = "Todo",
            cuantos = puntos.size,
            icono = IconosWeb.Ubicacion,
            color = Web.Primario,
            seleccionado = filtro == FILTRO_TODO,
            onClick = { onFiltro(FILTRO_TODO) }
        )
        TipoDePunto.entries.forEach { tipo ->
            ChipFiltro(
                texto = tipo.plural,
                cuantos = puntos.count { it.tipo == tipo },
                icono = tipo.icono,
                color = tipo.color,
                seleccionado = filtro == tipo.name,
                onClick = { onFiltro(tipo.name) }
            )
        }
    }
}

@Composable
private fun ChipFiltro(
    texto: String,
    cuantos: Int,
    icono: ImageVector,
    color: Color,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(forma)
            .background(if (seleccionado) color else Web.Tarjeta)
            .border(1.dp, if (seleccionado) color else Web.Borde, forma)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (seleccionado) Color.White else color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "$texto ($cuantos)",
            style = TextoWeb.Chip,
            color = if (seleccionado) Color.White else Web.Texto
        )
    }
}

// ---------------------------------------------------------------------------
// Los botones del mapa
// ---------------------------------------------------------------------------

/** Los botones redondos que flotan sobre el mapa: acercar, alejar y ver toda la ciudad. */
@Composable
internal fun BotonDeMapa(
    descripcion: String,
    onClick: () -> Unit,
    contenido: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(3.dp, CircleShape)
            .size(36.dp)
            .background(Web.Tarjeta, CircleShape)
            .clickable(onClickLabel = descripcion, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { contenido() }
}

// ---------------------------------------------------------------------------
// El mini menú del puntero
// ---------------------------------------------------------------------------

/** Lo que sale al tocar un puntero: qué es, cuándo, dónde y el botón a su detalle. */
@Composable
private fun MiniMenuDePunto(
    punto: PuntoEnMapa,
    distancia: String?,
    onAbrir: () -> Unit,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, forma)
            .background(Web.Tarjeta, forma)
            // Que tocar dentro del menú no cuente como tocar el mapa (lo cerraría).
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Etiqueta(
                texto = punto.tipo.etiqueta,
                fondo = punto.tipo.fondo,
                color = punto.tipo.color
            )
            Spacer(Modifier.weight(1f))
            Icon(
                IconosWeb.Cerrar,
                contentDescription = "Cerrar",
                tint = Web.TextoApagado,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onCerrar)
                    .padding(6.dp)
                    .size(16.dp)
            )
        }
        Text(
            text = punto.titulo,
            style = TextoWeb.TituloTarjeta.copy(fontSize = 16.sp, lineHeight = 20.sp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (punto.descripcion.isNotBlank()) {
            Text(
                text = punto.descripcion,
                style = TextoWeb.Cuerpo.copy(lineHeight = 20.sp),
                color = Web.TextoApagado,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        FilaDato(IconosWeb.Calendario, punto.cuando)
        if (punto.lugar.isNotBlank()) {
            FilaDato(IconosWeb.Ubicacion, punto.lugar)
        }
        if (distancia != null) {
            FilaDato(IconosWeb.PuntoDeEncuentro, distancia.replaceFirstChar { it.uppercase() })
        }
        BotonAmarillo(
            texto = punto.tipo.textoBoton,
            onClick = onAbrir,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            conFlechas = false
        )
    }
}

// ---------------------------------------------------------------------------
// Buscar cerca de
// ---------------------------------------------------------------------------

/**
 * El campo "Buscar cerca de": se escribe un municipio, una colonia o una calle y se toca "Buscar"
 * (o la tecla de buscar del teclado). Con un lugar elegido, el campo se vuelve un chip para quitarlo.
 */
@Composable
private fun BuscarCercaDe(busqueda: AccionesDeBusqueda, modifier: Modifier = Modifier) {
    val estado = busqueda.estado
    val lugar = estado.lugar
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (lugar != null) {
            val forma = RoundedCornerShape(50)
            Row(
                modifier = Modifier
                    .clip(forma)
                    .background(Web.Primario)
                    .clickable(onClickLabel = "Quitar el lugar", onClick = busqueda.onQuitar)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(IconosWeb.PuntoDeEncuentro, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text(
                    "Cerca de ${lugar.nombre}",
                    style = TextoWeb.Chip,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(IconosWeb.Cerrar, contentDescription = "Quitar", tint = Color.White, modifier = Modifier.size(12.dp))
            }
        } else {
            var texto by rememberSaveable { mutableStateOf("") }
            val foco = LocalFocusManager.current
            val buscar = {
                foco.clearFocus()
                busqueda.onBuscar(texto)
            }
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it.take(120) },
                singleLine = true,
                textStyle = TextoWeb.Cuerpo,
                placeholder = {
                    Text(
                        "Buscar cerca de: municipio, colonia o calle",
                        style = TextoWeb.Cuerpo,
                        color = Web.TextoApagado,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = { Icon(IconosWeb.Ubicacion, contentDescription = null, tint = Web.Primario, modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (estado.buscando) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Web.Primario)
                    } else {
                        Text(
                            "Buscar",
                            style = TextoWeb.Chip,
                            color = Web.Primario,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = texto.isNotBlank(), onClick = buscar)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { buscar() }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Web.Primario,
                    unfocusedBorderColor = Web.Borde,
                    focusedContainerColor = Web.Tarjeta,
                    unfocusedContainerColor = Web.Tarjeta
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                estado.aviso ?: "O mantén el dedo sobre el mapa. No usamos tu ubicación.",
                style = TextoWeb.Chico,
                color = if (estado.aviso != null) Web.Texto else Web.TextoApagado
            )
        }
    }
}

/** Lo que sale abajo del mapa con un lugar elegido: los tres más cercanos, con su distancia. */
@Composable
private fun LoMasCercano(
    lugar: String,
    puntos: List<PuntoEnMapa>,
    distanciaDe: (PuntoEnMapa) -> String,
    onElegir: (PuntoEnMapa) -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, forma)
            .background(Web.Tarjeta, forma)
            // Que tocar dentro de la lista no cuente como tocar el mapa.
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            "LO MÁS CERCA DE ${lugar.uppercase()}",
            style = TextoWeb.Rotulo,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
        puntos.forEach { punto ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onElegir(punto) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(punto.tipo.fondo, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(punto.tipo.icono, contentDescription = punto.tipo.etiqueta, tint = punto.tipo.color, modifier = Modifier.size(14.dp))
                }
                Text(
                    punto.titulo,
                    style = TextoWeb.Cuerpo,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(distanciaDe(punto), style = TextoWeb.Chip, color = Web.TextoApagado)
            }
        }
    }
}
