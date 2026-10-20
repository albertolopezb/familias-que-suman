package mx.tec.familiasquesuman.ui.screens.actividades

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.PuntoEnMapa
import mx.tec.familiasquesuman.domain.TipoDePunto
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.theme.AzulCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.MoradoCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.VerdeCategoriaTexto
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File

/**
 * El mapa con calles: OpenStreetMap con osmdroid. No pide llave, cuenta ni Google Play Services,
 * y no usa la ubicación del teléfono: es la vista de la ciudad elegida con sus punteros.
 *
 * Privacidad: el mapa descarga las imágenes de las calles de los servidores de OpenStreetMap,
 * que solo ven qué zona de la ciudad se está mirando (lo mismo que cualquier persona que abre
 * su sitio). Se guardan en la caché de la app para no volver a pedirlas.
 */
@Composable
internal fun LienzoDeCalles(
    centroDeCiudad: Coordenada,
    visibles: List<PuntoEnMapa>,
    claveSeleccionada: String,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val mapa = remember { crearMapa(contexto) }

    // osmdroid necesita saber cuándo la pantalla se ve y cuándo no (deja de bajar imágenes).
    val ciclo = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(ciclo, mapa) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_RESUME -> mapa.onResume()
                Lifecycle.Event.ON_PAUSE -> mapa.onPause()
                else -> Unit
            }
        }
        ciclo.addObserver(observador)
        mapa.onResume()
        onDispose {
            ciclo.removeObserver(observador)
            mapa.onPause()
            mapa.onDetach()
        }
    }

    // Encuadra la ciudad (o los punteros) la primera vez y cuando cambia el filtro.
    val claves = visibles.joinToString { it.clave }
    LaunchedEffect(claves, centroDeCiudad) { encuadrar(mapa, centroDeCiudad, visibles) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapa },
            update = { vista -> pintarPunteros(vista, visibles, claveSeleccionada, onSeleccionar) },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotonDeMapa(descripcion = "Acercar", onClick = { mapa.controller.zoomIn() }) {
                Text("+", style = TextoWeb.Boton.copy(fontSize = 20.sp), color = Web.Primario)
            }
            BotonDeMapa(descripcion = "Alejar", onClick = { mapa.controller.zoomOut() }) {
                Text("−", style = TextoWeb.Boton.copy(fontSize = 20.sp), color = Web.Primario)
            }
            BotonDeMapa(descripcion = "Ver toda la ciudad", onClick = { encuadrar(mapa, centroDeCiudad, visibles) }) {
                Icon(IconosWeb.Ubicacion, contentDescription = null, tint = Web.Primario, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
private fun crearMapa(contexto: Context): MapView {
    Configuration.getInstance().apply {
        // OpenStreetMap pide identificar la app que descarga las imágenes.
        userAgentValue = contexto.packageName
        // Todo en la caché de la app: sin permisos de almacenamiento.
        osmdroidBasePath = File(contexto.cacheDir, "osmdroid")
        osmdroidTileCache = File(contexto.cacheDir, "osmdroid/imagenes")
    }
    return MapView(contexto).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        isTilesScaledToDpi = true
        minZoomLevel = 9.0
        maxZoomLevel = 19.0
        // El crédito que pide la licencia de OpenStreetMap.
        overlays.add(CopyrightOverlay(contexto))
        // Mientras el dedo está sobre el mapa, la página del Inicio no se desplaza: el mapa se mueve.
        setOnTouchListener { vista, evento ->
            if (evento.actionMasked == MotionEvent.ACTION_DOWN) vista.parent?.requestDisallowInterceptTouchEvent(true)
            false
        }
    }
}

private fun encuadrar(mapa: MapView, centro: Coordenada, puntos: List<PuntoEnMapa>) {
    val hacer = {
        if (puntos.size >= 2) {
            val caja = BoundingBox.fromGeoPoints(puntos.map { GeoPoint(it.coordenada.latitud, it.coordenada.longitud) })
            mapa.zoomToBoundingBox(caja.increaseByScale(1.3f), false, 48)
        } else {
            val donde = puntos.firstOrNull()?.coordenada ?: centro
            mapa.controller.setZoom(12.5)
            mapa.controller.setCenter(GeoPoint(donde.latitud, donde.longitud))
        }
    }
    // Antes del primer acomodo el mapa no sabe su tamaño: se espera a que lo tenga.
    if (mapa.width > 0 && mapa.height > 0) hacer() else mapa.addOnFirstLayoutListener { _, _, _, _, _ -> hacer() }
}

private fun pintarPunteros(
    mapa: MapView,
    puntos: List<PuntoEnMapa>,
    claveSeleccionada: String,
    onSeleccionar: (String) -> Unit
) {
    mapa.overlays.removeAll { it is Marker || it is MapEventsOverlay }
    // Tocar el mapa fuera de un puntero cierra el mini menú.
    mapa.overlays.add(0, MapEventsOverlay(object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean { onSeleccionar(""); return true }
        override fun longPressHelper(p: GeoPoint?): Boolean = false
    }))
    val densidad = mapa.resources.displayMetrics.density
    puntos.forEach { punto ->
        val elegido = punto.clave == claveSeleccionada
        mapa.overlays.add(Marker(mapa).apply {
            position = GeoPoint(punto.coordenada.latitud, punto.coordenada.longitud)
            title = punto.titulo
            icon = circulo(punto.tipo.colorDeMapa, ((if (elegido) 26 else 18) * densidad).toInt(), (3 * densidad).toInt())
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            setOnMarkerClickListener { _, _ -> onSeleccionar(punto.clave); true }
        })
    }
    mapa.invalidate()
}

/** Un círculo del color de la tarjeta (morado, azul o verde) con borde blanco. */
private fun circulo(color: Color, tamano: Int, borde: Int) = GradientDrawable().apply {
    shape = GradientDrawable.OVAL
    setColor(color.toArgb())
    setStroke(borde, android.graphics.Color.WHITE)
    setSize(tamano, tamano)
}

private val TipoDePunto.colorDeMapa: Color
    get() = when (this) {
        TipoDePunto.ACTIVIDAD -> MoradoCategoriaTexto
        TipoDePunto.PROYECTO -> AzulCategoriaTexto
        TipoDePunto.DONACION -> VerdeCategoriaTexto
    }
