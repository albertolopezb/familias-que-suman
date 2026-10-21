package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.FamiliasApplication
import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.municipioConocido
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Para la administración: elegir dónde sale algo en el mapa sin ir a Google Maps. Se busca la
 * dirección (o el municipio) y el mapa va ahí; un toque en el mapa mueve el puntero al lugar exacto.
 * "Usar este punto" regresa la coordenada al formulario.
 */
@Composable
fun DialogoElegirUbicacion(
    inicial: Coordenada?,
    onElegir: (Coordenada) -> Unit,
    onCancelar: () -> Unit
) {
    val contenedor = (LocalContext.current.applicationContext as FamiliasApplication).container
    val ciudad = contenedor.preferenciasStore.preferencias.value.ciudad
    val alcance = rememberCoroutineScope()
    val foco = LocalFocusManager.current

    var punto by remember { mutableStateOf(inicial) }
    var texto by remember { mutableStateOf("") }
    var buscando by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf<String?>(null) }

    val contexto = LocalContext.current
    val mapa = remember { crearMapa(contexto) }
    DisposableEffect(mapa) {
        mapa.onResume()
        onDispose { mapa.onPause(); mapa.onDetach() }
    }
    // La primera vez: el punto que ya tenía o, si no, el centro de la ciudad elegida.
    LaunchedEffect(Unit) {
        centrarEn(mapa, inicial ?: municipioConocido(ciudad)?.coordenada ?: Coordenada(25.6694, -100.3098), if (inicial != null) 16.0 else 12.0)
    }

    val buscar = {
        foco.clearFocus()
        if (texto.isNotBlank() && !buscando) {
            buscando = true
            aviso = null
            alcance.launch {
                val lugar = contenedor.buscadorDeLugares.buscar(texto, ciudad)
                buscando = false
                if (lugar != null) {
                    punto = lugar.coordenada
                    centrarEn(mapa, lugar.coordenada, 16.0)
                    aviso = "Encontrado: ${lugar.nombre}. Si no es el lugar exacto, toca el mapa donde va."
                } else {
                    aviso = "No lo encontramos. Prueba con calle y colonia, o mueve el mapa y toca el lugar."
                }
            }
        }
    }

    Dialog(onDismissRequest = onCancelar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Web.Fondo)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("¿Dónde sale en el mapa?", style = TextoWeb.TituloTarjeta)
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it.take(120) },
                singleLine = true,
                label = { Text("Dirección, colonia o municipio") },
                trailingIcon = {
                    if (buscando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Web.Primario)
                    else TextButton(onClick = buscar, enabled = texto.isNotBlank()) { Text("Buscar") }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { buscar() }),
                modifier = Modifier.fillMaxWidth()
            )
            Text(aviso ?: "Busca la dirección o toca el mapa donde va el puntero.", style = TextoWeb.Chico)

            val forma = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(forma)
                    .border(1.dp, Web.Borde, forma)
            ) {
                AndroidView(
                    factory = { mapa },
                    update = { vista -> pintarElegido(vista, punto) { nuevo -> punto = nuevo; aviso = null } },
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonDeMapa(descripcion = "Acercar", onClick = { mapa.controller.zoomIn() }) {
                        Text("+", style = TextoWeb.Boton, color = Web.Primario)
                    }
                    BotonDeMapa(descripcion = "Alejar", onClick = { mapa.controller.zoomOut() }) {
                        Text("−", style = TextoWeb.Boton, color = Web.Primario)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(IconosWeb.Ubicacion, contentDescription = null, tint = Web.Primario, modifier = Modifier.size(16.dp))
                Text(punto?.comoTexto() ?: "Todavía no hay punto", style = TextoWeb.Chico, modifier = Modifier.weight(1f))
                TextButton(onClick = onCancelar) { Text("Cancelar") }
                Button(onClick = { punto?.let(onElegir) }, enabled = punto != null) { Text("Usar este punto") }
            }
        }
    }
}

/** El puntero dorado en el punto elegido, y tocar el mapa para moverlo. */
private fun pintarElegido(mapa: MapView, punto: Coordenada?, onTocar: (Coordenada) -> Unit) {
    mapa.overlays.removeAll { it is Marker || it is MapEventsOverlay }
    mapa.overlays.add(0, MapEventsOverlay(object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            p?.let { onTocar(Coordenada(it.latitude, it.longitude)) }
            return true
        }
        override fun longPressHelper(p: GeoPoint?): Boolean = singleTapConfirmedHelper(p)
    }))
    if (punto != null) {
        val densidad = mapa.resources.displayMetrics.density
        mapa.overlays.add(Marker(mapa).apply {
            position = GeoPoint(punto.latitud, punto.longitud)
            icon = circulo(MarcaOro, (24 * densidad).toInt(), (4 * densidad).toInt())
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            setOnMarkerClickListener { _, _ -> true }
        })
    }
    mapa.invalidate()
}
