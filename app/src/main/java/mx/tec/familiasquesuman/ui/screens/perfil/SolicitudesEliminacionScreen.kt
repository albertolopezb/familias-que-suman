package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.PerfilRepository
import mx.tec.familiasquesuman.data.red.SolicitudEliminacionDto
import mx.tec.familiasquesuman.ui.components.BarraSuperior
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.state.intentar
import mx.tec.familiasquesuman.ui.state.mensajeParaPersona
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * HU-14 / RF-15, del lado de Familias que Suman: las solicitudes de "eliminar mis datos" que mandaron
 * las familias desde el aviso de privacidad. Atender una borra en el servidor la cuenta y todo lo
 * suyo (acompañantes, inscripciones, encuestas, testimonios y fotos) y deja la solicitud, sin la
 * cuenta, como constancia. Es lo que el aviso promete: así se puede demostrar desde la app.
 *
 * Solo la ve quien tiene el permiso `atender:privacidad`; si no, el servidor responde 403 y aquí
 * sale el mensaje.
 */
class SolicitudesEliminacionViewModel(private val repositorio: PerfilRepository) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<List<SolicitudEliminacionDto>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<SolicitudEliminacionDto>>> = _estado.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            _estado.value = intentar { repositorio.solicitudesDeEliminacion() }
                .fold({ UiState.Exito(it) }, { UiState.Error(it.mensajeParaPersona()) })
        }
    }

    fun atender(solicitud: SolicitudEliminacionDto) {
        viewModelScope.launch {
            intentar { repositorio.atenderEliminacion(solicitud.id) }
                .onSuccess { _mensaje.value = "Listo: se eliminaron los datos de la solicitud ${it.folio}." }
                .onFailure { _mensaje.value = it.mensajeParaPersona() }
            cargar()
        }
    }

    fun mensajeVisto() { _mensaje.value = null }
}

private val formatoFecha = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("es-MX"))

private fun fechaLegible(iso: String): String =
    runCatching { OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault()).format(formatoFecha) }.getOrDefault(iso)

@Composable
fun SolicitudesEliminacionScreen(
    estado: UiState<List<SolicitudEliminacionDto>>,
    mensaje: String?,
    onAtender: (SolicitudEliminacionDto) -> Unit,
    onMensajeVisto: () -> Unit,
    onReintentar: () -> Unit,
    onRegresar: () -> Unit
) {
    var porConfirmar by remember { mutableStateOf<SolicitudEliminacionDto?>(null) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BarraSuperior("Solicitudes de eliminación", onRegresar = onRegresar)
        when (estado) {
            UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(estado.mensaje, onReintentar)
            is UiState.Exito -> {
                val pendientes = estado.datos.filter { it.estado == "pendiente" }
                val atendidas = estado.datos.filter { it.estado != "pendiente" }
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "La ley pide responder en un máximo de 20 días y hacerlo efectivo en los 15 siguientes. " +
                                "Eliminar no se puede deshacer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (pendientes.isEmpty()) {
                        item {
                            Text(
                                "No hay solicitudes pendientes.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(24.dp)
                            )
                        }
                    }
                    items(pendientes, key = { it.id }) { s ->
                        TarjetaSolicitud(s) {
                            Button(
                                onClick = { porConfirmar = s },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E)),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Eliminar los datos") }
                        }
                    }
                    if (atendidas.isNotEmpty()) {
                        item { Text("Atendidas", style = MaterialTheme.typography.titleMedium) }
                        items(atendidas, key = { it.id }) { s -> TarjetaSolicitud(s) {} }
                    }
                }
            }
        }
    }

    porConfirmar?.let { s ->
        AlertDialog(
            onDismissRequest = { porConfirmar = null },
            title = { Text("¿Eliminar los datos de ${s.folio}?") },
            text = {
                Text(
                    "Se borran la cuenta${s.correo?.let { " ($it)" }.orEmpty()}, sus acompañantes, inscripciones, " +
                        "encuestas, testimonios y fotos. Sus sugerencias se quedan sin nombre ni contacto. No se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(onClick = { onAtender(s); porConfirmar = null }) { Text("Eliminar", color = Color(0xFFB3261E)) }
            },
            dismissButton = { TextButton(onClick = { porConfirmar = null }) { Text("Cancelar") } }
        )
    }

    mensaje?.let {
        AlertDialog(
            onDismissRequest = onMensajeVisto,
            text = { Text(it) },
            confirmButton = { TextButton(onClick = onMensajeVisto) { Text("Aceptar") } }
        )
    }
}

@Composable
private fun TarjetaSolicitud(s: SolicitudEliminacionDto, acciones: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(s.folio, style = MaterialTheme.typography.titleMedium)
            Text(s.correo ?: "Cuenta ya eliminada", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Pedida el ${fechaLegible(s.creadaEn)} · ${if (s.estado == "pendiente") "pendiente" else "atendida"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            acciones()
        }
    }
}
