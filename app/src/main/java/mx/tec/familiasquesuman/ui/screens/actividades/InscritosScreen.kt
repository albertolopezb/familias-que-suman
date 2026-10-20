package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.data.red.InscritoDto
import mx.tec.familiasquesuman.ui.components.BarraSuperior
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.state.intentar
import mx.tec.familiasquesuman.ui.state.mensajeParaPersona

/**
 * RF-06, del lado de quien organiza: las familias inscritas a una actividad y quiénes van, para
 * saber a quién esperar y registrar la asistencia. Solo nombres y edades (sin correos ni teléfonos).
 * El servidor pide el permiso `registrar:asistencias` y deja cada consulta en la auditoría.
 */
class InscritosViewModel(private val repositorio: ActividadRepository) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<List<InscritoDto>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<InscritoDto>>> = _estado.asStateFlow()

    fun cargar(actividadId: String) {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            _estado.value = intentar { repositorio.inscritos(actividadId) }
                .fold({ UiState.Exito(it) }, { UiState.Error(it.mensajeParaPersona()) })
        }
    }
}

@Composable
fun InscritosScreen(
    titulo: String,
    estado: UiState<List<InscritoDto>>,
    onReintentar: () -> Unit,
    onRegresar: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BarraSuperior("Quiénes van", onRegresar = onRegresar)
        when (estado) {
            UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(estado.mensaje, onReintentar)
            is UiState.Exito -> {
                val familias = estado.datos
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Text(titulo, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${familias.size} ${if (familias.size == 1) "familia" else "familias"} · " +
                                "${familias.sumOf { it.personas }} personas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (familias.isEmpty()) {
                        item {
                            Text(
                                "Todavía nadie se inscribe.",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(24.dp)
                            )
                        }
                    }
                    items(familias, key = { it.cuentaId }) { f ->
                        Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(f.familia, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${f.personas} ${if (f.personas == 1) "persona" else "personas"}" +
                                        (if (f.hayMenores) " · con menores (consentimiento del tutor)" else "") +
                                        (if (f.asistenciaRegistrada) " · asistencia registrada" else ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                f.integrantes.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                            }
                        }
                    }
                }
            }
        }
    }
}
