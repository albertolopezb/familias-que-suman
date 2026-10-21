package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.PerfilRepository
import mx.tec.familiasquesuman.data.red.ResumenDto
import mx.tec.familiasquesuman.ui.components.BarraSuperior
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.TarjetaMetrica
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.state.intentar
import mx.tec.familiasquesuman.ui.state.mensajeParaPersona
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * Para la administración: cuántas familias, actividades, inscripciones y propuestas hay. Son solo
 * números (GET /v1/admin/resumen): ningún nombre ni correo. Los mismos números los grafica Grafana
 * en el tablero de la API, panel "Cuántos hay", para ver cómo crecen con el tiempo.
 */
class ResumenAdminViewModel(private val repositorio: PerfilRepository) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<ResumenDto>>(UiState.Cargando)
    val estado: StateFlow<UiState<ResumenDto>> = _estado.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            _estado.value = intentar { repositorio.resumen() }
                .fold({ UiState.Exito(it) }, { UiState.Error(it.mensajeParaPersona()) })
        }
    }
}

@Composable
fun ResumenAdminScreen(
    estado: UiState<ResumenDto>,
    onReintentar: () -> Unit,
    onRegresar: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BarraSuperior("Resumen", onRegresar = onRegresar)
        when (estado) {
            UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(estado.mensaje, onReintentar)
            is UiState.Exito -> {
                val r = estado.datos
                Column(
                    Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Seccion("PERSONAS REGISTRADAS")
                    Fila(
                        r.familias + r.acompanantes to "Personas\nen total",
                        r.familias to "Cuentas\nde familia",
                        r.acompanantes to "Acompañantes\nregistrados"
                    )
                    Seccion("CATÁLOGO")
                    Fila(r.actividadesProximas to "Actividades\npróximas", r.actividadesPasadas to "Actividades\nrealizadas", r.proyectosActivos to "Proyectos\nactivos")
                    Fila(r.campanas to "Campañas\nde donación", r.centros to "Centros del\nDirectorio", r.asociaciones to "Asociaciones")
                    Seccion("PARTICIPACIÓN")
                    Fila(r.inscripcionesVigentes to "Inscripciones\nvigentes", r.personasInscritas to "Personas\ninscritas", r.aportaciones to "Artículos\napartados")
                    Seccion("PROPUESTAS DE LAS FAMILIAS")
                    Fila(
                        (r.sugerencias["actividad"] ?: 0) to "Actividades",
                        (r.sugerencias["proyecto"] ?: 0) to "Proyectos",
                    )
                    Fila(
                        (r.sugerencias["campana"] ?: 0) to "Donaciones",
                        (r.sugerencias["centro"] ?: 0) to "Centros",
                    )
                    Seccion("POR ATENDER")
                    Fila(r.testimoniosPorRevisar to "Testimonios\npor revisar", r.sugerenciasPorRevisar to "Propuestas\npor revisar", r.eliminacionesPendientes to "Solicitudes de\neliminación")
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        "Solo son conteos: aquí no sale ningún nombre ni correo. La gráfica de cómo cambian con el tiempo " +
                            "está en Grafana, tablero \"API · Familias que Suman\", panel \"Cuántos hay\". " +
                            "${r.administradores} cuenta(s) de administración no se cuentan como familias.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TintaSuave
                    )
                }
            }
        }
    }
}

@Composable
private fun Seccion(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, letterSpacing = 0.8.sp, fontWeight = FontWeight.Bold),
        color = TintaSuave,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun Fila(vararg metricas: Pair<Int, String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        metricas.forEach { (numero, etiqueta) -> TarjetaMetrica(numero, etiqueta, Modifier.weight(1f)) }
    }
}
