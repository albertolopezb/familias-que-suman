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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.MapaDeCiudad
import mx.tec.familiasquesuman.domain.PuntoEnMapa
import mx.tec.familiasquesuman.domain.TipoDePunto
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonAmarillo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.FilaDato
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.AzulCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.AzulTarjetaFondo
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
 * El mapa del Inicio: actividades, proyectos y donaciones de la ciudad elegida
 * (Monterrey u Hermosillo), con filtro por tipo y un mini menú al tocar un puntero.
 *
 * Es una vista general de la ciudad: no usa la ubicación del teléfono ni pide ningún
 * permiso. Los punteros llevan el color de su tarjeta en el Inicio (actividades en
 * morado, donar en verde, proyectos en azul).
 *
 * Todavía no es un mapa de verdad. El lienzo de abajo coloca cada puntero en su lugar
 * real (norte arriba) pero no pinta calles. Cuando se conecte el SDK de mapas solo
 * cambia [LienzoDelMapa]: los filtros, el mini menú y la navegación se quedan igual.
 */
@Composable
fun MapaGeneral(
    estado: UiState<MapaDeCiudad>,
    onAbrir: (PuntoEnMapa) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Texto y no enum para que sobreviva a ir al detalle y regresar sin Saver propio.
    var filtro by rememberSaveable { mutableStateOf(FILTRO_TODO) }
    var claveSeleccionada by rememberSaveable { mutableStateOf("") }

    val todos = (estado as? UiState.Exito)?.datos?.puntos.orEmpty()
    val visibles = if (filtro == FILTRO_TODO) todos else todos.filter { it.tipo.name == filtro }
    val seleccionado = visibles.firstOrNull { it.clave == claveSeleccionada }

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
        Spacer(Modifier.size(12.dp))

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
                    LienzoDelMapa(
                        ciudad = estado.datos.ciudad,
                        centroDeCiudad = estado.datos.centro,
                        todos = todos,
                        visibles = visibles,
                        claveSeleccionada = claveSeleccionada,
                        onSeleccionar = { claveSeleccionada = it }
                    )

                    Text(
                        text = "Vista previa · el mapa con calles llega pronto",
                        style = TextoWeb.Chico.copy(fontSize = 11.sp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Web.Tarjeta.copy(alpha = 0.92f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )

                    if (visibles.isEmpty()) {
                        val tipoFiltrado = TipoDePunto.entries.firstOrNull { it.name == filtro }
                        Text(
                            text = when {
                                todos.isEmpty() -> "Pronto habrá actividades, proyectos y donaciones en ${estado.datos.ciudad}."
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
                            onAbrir = { onAbrir(seleccionado) },
                            onCerrar = { claveSeleccionada = "" },
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
    }

private val TipoDePunto.fondo: Color
    get() = when (this) {
        TipoDePunto.ACTIVIDAD -> MoradoTarjetaFondo
        TipoDePunto.PROYECTO -> AzulTarjetaFondo
        TipoDePunto.DONACION -> VerdeTarjetaFondo
    }

/** Los mismos íconos que la barra inferior usa para cada sección. */
private val TipoDePunto.icono: ImageVector
    get() = when (this) {
        TipoDePunto.ACTIVIDAD -> IconosWeb.Personas
        TipoDePunto.PROYECTO -> IconosWeb.Foco
        TipoDePunto.DONACION -> IconosWeb.ManoCorazon
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
// El lienzo: lo único que se reemplaza cuando llegue el mapa de verdad
// ---------------------------------------------------------------------------

/** Cada cuántos kilómetros se pinta una línea de la cuadrícula; se elige según el acercamiento. */
private val PASOS_KM = listOf(1f, 2f, 5f, 10f, 20f, 50f)

/** Cuánto se ve de la ciudad si todavía no hay punteros (Hermosillo, por ejemplo). */
private const val RADIO_SIN_PUNTOS_KM = 12f

/** Aunque los punteros estén muy juntos, el mapa no se acerca más que esto. */
private const val RADIO_MINIMO_KM = 3f
private const val ESCALA_MINIMA = 0.3f
private const val ESCALA_MAXIMA = 8f

@Composable
private fun LienzoDelMapa(
    ciudad: String,
    centroDeCiudad: Coordenada,
    todos: List<PuntoEnMapa>,
    visibles: List<PuntoEnMapa>,
    claveSeleccionada: String,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Acercar con dos dedos (o con los botones) y arrastrar. Se reinician al salir de la pantalla.
    var escala by remember { mutableStateOf(1f) }
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }

    /** Los botones + y −: acercan o alejan sin mover lo que está al centro del lienzo. */
    fun cambiarEscala(veces: Float) {
        val nueva = (escala * veces).coerceIn(ESCALA_MINIMA, ESCALA_MAXIMA)
        desplazamiento *= nueva / escala
        escala = nueva
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            // Tocar el fondo cierra el mini menú.
            .pointerInput(Unit) { detectTapGestures { onSeleccionar("") } }
            .pointerInput(Unit) {
                // Con un dedo y el mapa a su tamaño normal, el dedo es de la página (para que
                // siga desplazándose); con dos dedos, o ya acercado, el mapa se lo queda.
                gestosDelMapa(
                    arrastraConUnDedo = { escala > 1.01f || desplazamiento != Offset.Zero }
                ) { centroide, arrastre, zoom ->
                    val nueva = (escala * zoom).coerceIn(ESCALA_MINIMA, ESCALA_MAXIMA)
                    val factor = nueva / escala
                    // El zoom se hace alrededor de los dedos, no del centro del lienzo.
                    val desdeElCentro = centroide - Offset(size.width / 2f, size.height / 2f)
                    val movido = (desplazamiento - desdeElCentro) * factor + desdeElCentro + arrastre
                    val limiteX = size.width * nueva.coerceAtLeast(1f)
                    val limiteY = size.height * nueva.coerceAtLeast(1f)
                    desplazamiento = Offset(
                        movido.x.coerceIn(-limiteX, limiteX),
                        movido.y.coerceIn(-limiteY, limiteY)
                    )
                    escala = nueva
                }
            }
    ) {
        val ancho = constraints.maxWidth.toFloat()
        val alto = constraints.maxHeight.toFloat()
        val densidad = LocalDensity.current
        val anchoPin = with(densidad) { 34.dp.toPx() }
        val altoPin = with(densidad) { 42.dp.toPx() }
        val margenX = with(densidad) { 28.dp.toPx() }
        // Arriba queda lugar para el aviso y para lo alto de un puntero; abajo, un respiro.
        val arriba = altoPin + with(densidad) { 40.dp.toPx() }
        val abajo = alto - with(densidad) { 16.dp.toPx() }
        val minimoEntreLineas = with(densidad) { 64.dp.toPx() }

        // Grados a kilómetros desde el centro de la ciudad. A esta escala la Tierra se puede tratar como plana.
        val kmPorGradoLng = 111.320 * cos(Math.toRadians(centroDeCiudad.latitud))
        fun enKm(donde: Coordenada) = Offset(
            x = ((donde.longitud - centroDeCiudad.longitud) * kmPorGradoLng).toFloat(),
            y = (-(donde.latitud - centroDeCiudad.latitud) * 110.574).toFloat()   // el norte va arriba
        )

        // Se abre mostrando todo lo de la ciudad (aunque el filtro esconda algo, para que el
        // mapa no salte al filtrar). Sin punteros, mira el centro de la ciudad.
        val enKmTodos = todos.map { enKm(it.coordenada) }
        val centroDeVistaKm = if (enKmTodos.isEmpty()) Offset.Zero else Offset(
            (enKmTodos.minOf { it.x } + enKmTodos.maxOf { it.x }) / 2f,
            (enKmTodos.minOf { it.y } + enKmTodos.maxOf { it.y }) / 2f
        )
        val mitadAnchoKm = if (enKmTodos.isEmpty()) RADIO_SIN_PUNTOS_KM
        else max(enKmTodos.maxOf { abs(it.x - centroDeVistaKm.x) }, RADIO_MINIMO_KM)
        val mitadAltoKm = if (enKmTodos.isEmpty()) RADIO_SIN_PUNTOS_KM
        else max(enKmTodos.maxOf { abs(it.y - centroDeVistaKm.y) }, RADIO_MINIMO_KM)

        val pxPorKm = min(
            (ancho / 2f - margenX) / mitadAnchoKm,
            ((abajo - arriba) / 2f) / mitadAltoKm
        ).coerceAtLeast(1f)
        val pxPorKmActual = pxPorKm * escala
        val centroDeVistaPx = Offset(ancho / 2f, (arriba + abajo) / 2f) + desplazamiento
        fun enPantalla(donde: Coordenada) = centroDeVistaPx + (enKm(donde) - centroDeVistaKm) * pxPorKmActual

        // La cuadrícula: cada cuadro mide los kilómetros que dice el rótulo de abajo.
        val pasoKm = PASOS_KM.firstOrNull { it * pxPorKmActual >= minimoEntreLineas } ?: PASOS_KM.last()
        val pasoPx = pasoKm * pxPorKmActual

        val medidor = rememberTextMeasurer()
        val estiloCiudad = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 6.sp,
            color = Web.Primario.copy(alpha = 0.10f)
        )

        Canvas(Modifier.fillMaxSize()) {
            val origen = enPantalla(centroDeCiudad)
            if (pasoPx >= 8f) {
                val lineas = Web.Primario.copy(alpha = 0.08f)
                var x = origen.x + ceil(-origen.x / pasoPx) * pasoPx
                while (x <= size.width) {
                    drawLine(lineas, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                    x += pasoPx
                }
                var y = origen.y + ceil(-origen.y / pasoPx) * pasoPx
                while (y <= size.height) {
                    drawLine(lineas, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                    y += pasoPx
                }
            }
            // El nombre de la ciudad, grande y tenue, en el centro de la ciudad.
            val rotulo = medidor.measure(ciudad.uppercase(), estiloCiudad)
            drawText(
                textLayoutResult = rotulo,
                topLeft = Offset(origen.x - rotulo.size.width / 2f, origen.y - rotulo.size.height / 2f)
            )
        }

        // Dos cosas en el mismo lugar (una actividad y su proyecto) no deben taparse.
        val puntas = separar(
            puntas = visibles.map { enPantalla(it.coordenada) },
            minimo = anchoPin * 0.8f
        )
        visibles.forEachIndexed { i, punto ->
            val punta = puntas[i]
            val elegido = punto.clave == claveSeleccionada
            Marcador(
                punto = punto,
                elegido = elegido,
                onClick = {
                    if (elegido) {
                        onSeleccionar("")
                    } else {
                        onSeleccionar(punto.clave)
                        // El mini menú tapa la parte de abajo: si el puntero quedaría
                        // detrás, se sube el mapa para dejarlo a la vista.
                        val tope = alto * 0.3f
                        if (punta.y > tope) desplazamiento += Offset(0f, tope - punta.y)
                    }
                },
                modifier = Modifier
                    // La punta del puntero, no su centro, es la que marca el lugar.
                    .offset { IntOffset((punta.x - anchoPin / 2f).roundToInt(), (punta.y - altoPin).roundToInt()) }
                    .zIndex(if (elegido) 1f else 0f)
            )
        }

        // Cuántos punteros del filtro actual quedaron fuera de lo que se ve.
        val fueraDeVista = puntas.count { it.x < 0f || it.x > ancho || it.y < 0f || it.y > alto + altoPin }
        if (fueraDeVista > 0) {
            Text(
                text = if (fueraDeVista == 1) "1 más fuera de vista · aleja el mapa"
                else "$fueraDeVista más fuera de vista · aleja el mapa",
                style = TextoWeb.Chico.copy(fontSize = 11.sp),
                color = Web.Primario,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 10.dp, top = 42.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Web.Tarjeta.copy(alpha = 0.92f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        Text(
            text = "Cada cuadro: ${pasoKm.roundToInt()} km",
            style = TextoWeb.Chico.copy(fontSize = 10.sp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Web.Tarjeta.copy(alpha = 0.92f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotonDeMapa(descripcion = "Acercar", onClick = { cambiarEscala(1.5f) }) {
                Text("+", style = TextoWeb.Boton.copy(fontSize = 20.sp), color = Web.Primario)
            }
            BotonDeMapa(descripcion = "Alejar", onClick = { cambiarEscala(1 / 1.5f) }) {
                Text("−", style = TextoWeb.Boton.copy(fontSize = 20.sp), color = Web.Primario)
            }
            BotonDeMapa(
                descripcion = "Ver toda la ciudad",
                onClick = {
                    escala = 1f
                    desplazamiento = Offset.Zero
                }
            ) {
                Icon(IconosWeb.Ubicacion, contentDescription = null, tint = Web.Primario, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/**
 * Lo mismo que detectTransformGestures (zoom con dos dedos y arrastre), con una diferencia:
 * un solo dedo solo cuenta si [arrastraConUnDedo] lo permite. Así el mapa, que vive dentro de
 * la página del Inicio, no impide desplazarla cuando se toca encima de él.
 */
private suspend fun PointerInputScope.gestosDelMapa(
    arrastraConUnDedo: () -> Boolean,
    onGesto: (centroide: Offset, arrastre: Offset, zoom: Float) -> Unit
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var zoomTotal = 1f
        var arrastreTotal = Offset.Zero
        var pasoElUmbral = false
        val umbral = viewConfiguration.touchSlop
        do {
            val evento = awaitPointerEvent()
            val cancelado = evento.changes.any { it.isConsumed }
            if (!cancelado) {
                val conDosDedos = evento.changes.count { it.pressed } > 1
                if (conDosDedos || arrastraConUnDedo()) {
                    val zoom = evento.calculateZoom()
                    val arrastre = evento.calculatePan()
                    if (!pasoElUmbral) {
                        zoomTotal *= zoom
                        arrastreTotal += arrastre
                        pasoElUmbral = abs(1f - zoomTotal) > 0.02f || arrastreTotal.getDistance() > umbral
                    }
                    if (pasoElUmbral) {
                        if (zoom != 1f || arrastre != Offset.Zero) {
                            onGesto(evento.calculateCentroid(useCurrent = false), arrastre, zoom)
                        }
                        evento.changes.forEach { if (it.positionChanged()) it.consume() }
                    }
                }
            }
        } while (!cancelado && evento.changes.any { it.pressed })
    }
}

/** Los botones redondos que flotan sobre el mapa: acercar, alejar y ver toda la ciudad. */
@Composable
private fun BotonDeMapa(
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

/** Recorre a la derecha los punteros que caerían encima de otro ya colocado. */
private fun separar(puntas: List<Offset>, minimo: Float): List<Offset> {
    val puestas = mutableListOf<Offset>()
    puntas.forEach { original ->
        var punta = original
        var intentos = 0
        while (intentos < 6 && puestas.any { (it - punta).getDistance() < minimo }) {
            punta = punta.copy(x = punta.x + minimo)
            intentos++
        }
        puestas += punta
    }
    return puestas
}

/** El puntero: una gota del color de su tipo con su ícono adentro. */
@Composable
private fun Marcador(
    punto: PuntoEnMapa,
    elegido: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = punto.tipo.color
    Box(
        modifier = modifier
            .size(width = 34.dp, height = 42.dp)
            .graphicsLayer {
                val tamano = if (elegido) 1.3f else 1f
                scaleX = tamano
                scaleY = tamano
                transformOrigin = TransformOrigin(0.5f, 1f)   // crece desde la punta
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val radio = size.width / 2f
            val borde = 2.dp.toPx()
            fun cola(encogida: Float) = Path().apply {
                val r = radio - encogida
                moveTo(radio, size.height - encogida * 1.6f)
                lineTo(radio - r * 0.62f, radio + r * 0.75f)
                lineTo(radio + r * 0.62f, radio + r * 0.75f)
                close()
            }
            val cabeza = Offset(radio, radio)
            drawPath(cola(0f), Color.White)
            drawCircle(Color.White, radius = radio, center = cabeza)
            drawPath(cola(borde), color)
            drawCircle(color, radius = radio - borde, center = cabeza)
        }
        Icon(
            imageVector = punto.tipo.icono,
            contentDescription = "${punto.tipo.etiqueta}: ${punto.titulo}",
            tint = Color.White,
            modifier = Modifier
                .padding(top = 9.dp)
                .size(16.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// El mini menú del puntero
// ---------------------------------------------------------------------------

/** Lo que sale al tocar un puntero: qué es, cuándo, dónde y el botón a su detalle. */
@Composable
private fun MiniMenuDePunto(
    punto: PuntoEnMapa,
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
