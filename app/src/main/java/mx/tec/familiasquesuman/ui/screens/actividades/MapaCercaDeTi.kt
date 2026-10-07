package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.MapaCercano
import mx.tec.familiasquesuman.domain.PuntoEnMapa
import mx.tec.familiasquesuman.domain.TipoDePunto
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonAmarillo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.FilaDato
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.state.UiState
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * La pestaña "Mapa" de Actividades: actividades, proyectos y donaciones cerca de
 * la familia, con filtro por tipo y un mini menú al tocar un puntero.
 *
 * Todavía no es un mapa de verdad. El lienzo de abajo coloca cada puntero en su
 * lugar real respecto a la familia (norte arriba, anillos de distancia) pero no
 * pinta calles. Cuando se conecte el SDK de mapas solo cambia [LienzoDelMapa]:
 * los filtros, el mini menú y la navegación se quedan como están.
 */
@Composable
fun MapaCercaDeTi(
    estado: UiState<MapaCercano>,
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
                        tuUbicacion = estado.datos.tuUbicacion,
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
                        Text(
                            text = "Por ahora no hay nada de esto cerca de ti.",
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
private val AzulUbicacion = Color(0xFF2563EB)

private val TipoDePunto.color: Color
    get() = when (this) {
        TipoDePunto.ACTIVIDAD -> Web.Primario
        TipoDePunto.PROYECTO -> Web.VerdeTema
        TipoDePunto.DONACION -> Web.Rosa
    }

private val TipoDePunto.fondo: Color
    get() = when (this) {
        TipoDePunto.ACTIVIDAD -> Web.Secundario
        TipoDePunto.PROYECTO -> Web.VerdeFondo
        TipoDePunto.DONACION -> Web.RosaFondo
    }

/** Los mismos íconos que la barra inferior usa para cada sección. */
private val TipoDePunto.icono: ImageVector
    get() = when (this) {
        TipoDePunto.ACTIVIDAD -> IconosWeb.Personas
        TipoDePunto.PROYECTO -> IconosWeb.Foco
        TipoDePunto.DONACION -> IconosWeb.ManoCorazon
    }

private fun textoDeDistancia(km: Double): String =
    if (km < 1) "a ${(km * 1000).roundToInt()} m de ti"
    else "a ${(km * 10).roundToInt() / 10.0} km de ti"

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

private val ANILLOS_KM = listOf(2, 5, 10, 20)

/** Lo que se alcanza a ver al abrir el mapa: unos 6 km a la redonda. Lo demás, alejando. */
private const val RADIO_INICIAL_KM = 6f
private const val ESCALA_MINIMA = 0.3f
private const val ESCALA_MAXIMA = 8f

@Composable
private fun LienzoDelMapa(
    tuUbicacion: Coordenada,
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
                detectTransformGestures { centroide, arrastre, zoom, _ ->
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
        val margen = with(densidad) { 24.dp.toPx() }

        // Grados a kilómetros desde la familia. A esta escala la Tierra se puede tratar como plana.
        val kmPorGradoLng = 111.320 * cos(Math.toRadians(tuUbicacion.latitud))
        fun enKm(donde: Coordenada) = Offset(
            x = ((donde.longitud - tuUbicacion.longitud) * kmPorGradoLng).toFloat(),
            y = (-(donde.latitud - tuUbicacion.latitud) * 110.574).toFloat()   // el norte va arriba
        )

        // Como "cerca de mí" en cualquier mapa: se abre mostrando lo de alrededor,
        // no la ciudad completa. Lo que queda fuera se alcanza alejando o arrastrando.
        val pxPorKm = (min(ancho, alto) / 2f - margen).coerceAtLeast(1f) / RADIO_INICIAL_KM
        val pxPorKmActual = pxPorKm * escala
        val centro = Offset(ancho / 2f, alto / 2f) + desplazamiento

        val medidor = rememberTextMeasurer()
        val estiloAnillo = TextoWeb.Chico.copy(fontSize = 10.sp, color = Web.TextoApagado)

        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Web.Primario.copy(alpha = 0.05f), radius = ANILLOS_KM.first() * pxPorKmActual, center = centro)
            ANILLOS_KM.forEach { km ->
                val radio = km * pxPorKmActual
                drawCircle(
                    color = Web.Primario.copy(alpha = 0.22f),
                    radius = radio,
                    center = centro,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()))
                    )
                )
                val rotulo = medidor.measure("$km km", estiloAnillo)
                drawText(
                    textLayoutResult = rotulo,
                    topLeft = Offset(
                        centro.x - rotulo.size.width / 2f,
                        centro.y - radio - rotulo.size.height - 2.dp.toPx()
                    )
                )
            }
            // La familia: punto azul con halo, como en cualquier app de mapas.
            drawCircle(AzulUbicacion.copy(alpha = 0.18f), radius = 16.dp.toPx(), center = centro)
            drawCircle(Color.White, radius = 8.dp.toPx(), center = centro)
            drawCircle(AzulUbicacion, radius = 5.5.dp.toPx(), center = centro)
        }

        // Dos cosas en el mismo lugar (una actividad y su proyecto) no deben taparse.
        val puntas = separar(
            puntas = visibles.map { centro + enKm(it.coordenada) * pxPorKmActual },
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
                descripcion = "Volver a mi ubicación",
                onClick = {
                    escala = 1f
                    desplazamiento = Offset.Zero
                }
            ) {
                Icon(IconosWeb.Ubicacion, contentDescription = null, tint = AzulUbicacion, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Los botones redondos que flotan sobre el mapa: acercar, alejar y volver a mi ubicación. */
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
            Spacer(Modifier.width(8.dp))
            Text(
                text = textoDeDistancia(punto.distanciaKm),
                style = TextoWeb.Chico,
                modifier = Modifier.weight(1f)
            )
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
