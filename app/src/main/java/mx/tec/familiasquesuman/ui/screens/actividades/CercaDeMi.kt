package mx.tec.familiasquesuman.ui.screens.actividades

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import mx.tec.familiasquesuman.domain.Coordenada

/**
 * "Cerca de mí" (RF-02), como lo decidió el equipo por privacidad:
 *  · Se pide la ubicación solo cuando la persona toca el botón, nunca al abrir la app.
 *  · Es APROXIMADA (ACCESS_COARSE_LOCATION, del orden de 1 a 3 km): para ordenar una lista de
 *    actividades no hace falta saber en qué casa está alguien.
 *  · Vive solo en memoria y solo en el teléfono: no se manda a la API, no se guarda en disco ni
 *    en el estado guardado de la pantalla (por eso `remember` y no `rememberSaveable`).
 *  · Si se niega el permiso, todo sigue igual: la lista se queda ordenada por fecha.
 */
class EstadoCercaDeMi internal constructor() {
    /** Dónde está la persona, aproximado; null = la lista va por fecha. */
    var aqui by mutableStateOf<Coordenada?>(null)
        internal set
    var buscando by mutableStateOf(false)
        internal set
    var aviso by mutableStateOf<String?>(null)
        internal set
    internal var pedir: () -> Unit = {}

    /** Prender (pide el permiso si hace falta) o apagar (olvida la ubicación). */
    fun alternar() {
        if (aqui != null) {
            aqui = null
            aviso = null
        } else if (!buscando) {
            pedir()
        }
    }
}

@Composable
fun rememberCercaDeMi(): EstadoCercaDeMi {
    val contexto = LocalContext.current
    val estado = remember { EstadoCercaDeMi() }

    fun leer() {
        estado.buscando = true
        estado.aviso = null
        leerUbicacionAproximada(contexto) { c ->
            estado.buscando = false
            estado.aqui = c
            estado.aviso = if (c == null) "No pudimos saber dónde estás. Revisa que la ubicación del teléfono esté encendida." else null
        }
    }

    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) leer() else estado.aviso = "Sin permiso de ubicación: las actividades siguen ordenadas por fecha."
    }
    estado.pedir = {
        val concedido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (concedido) leer() else permiso.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    return estado
}

/** Una sola lectura (no se queda escuchando). Responde en el hilo principal. */
@SuppressLint("MissingPermission") // solo se llama con el permiso concedido; si lo quitan, cae en SecurityException
private fun leerUbicacionAproximada(contexto: Context, alTerminar: (Coordenada?) -> Unit) {
    val lm = contexto.getSystemService(LocationManager::class.java)
    val proveedor = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        .firstOrNull { runCatching { lm?.isProviderEnabled(it) == true }.getOrDefault(false) }
    if (lm == null || proveedor == null) {
        alTerminar(null)
        return
    }
    try {
        LocationManagerCompat.getCurrentLocation(lm, proveedor, null as CancellationSignal?, ContextCompat.getMainExecutor(contexto)) { lugar ->
            val conRespaldo = lugar ?: runCatching { lm.getLastKnownLocation(proveedor) }.getOrNull()
            alTerminar(conRespaldo?.let { Coordenada(it.latitude, it.longitude) })
        }
    } catch (e: SecurityException) {
        alTerminar(null)
    }
}
