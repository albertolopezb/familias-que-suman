package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.BuscadorDeLugares
import mx.tec.familiasquesuman.data.MapaRepository
import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.LugarDeReferencia
import mx.tec.familiasquesuman.domain.MapaDeCiudad
import mx.tec.familiasquesuman.ui.state.UiState

/**
 * "Buscar cerca de": desde dónde se mide la distancia. Es un lugar que la persona escribe o un
 * punto que toca en el mapa, nunca la ubicación del teléfono (la app no la pide).
 */
data class BusquedaCercana(
    val lugar: LugarDeReferencia? = null,
    val buscando: Boolean = false,
    val aviso: String? = null
)

/** Lo que enseña el mapa del Inicio: los punteros de la ciudad elegida y desde dónde se busca. */
class MapaViewModel(
    private val mapaRepository: MapaRepository,
    private val buscadorDeLugares: BuscadorDeLugares
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<MapaDeCiudad>>(UiState.Cargando)
    val estado: StateFlow<UiState<MapaDeCiudad>> = _estado.asStateFlow()

    private val _cercaDe = MutableStateFlow(BusquedaCercana())
    val cercaDe: StateFlow<BusquedaCercana> = _cercaDe.asStateFlow()

    /**
     * Se llama cada vez que el Inicio vuelve a mostrarse o cambia la ciudad, para que el
     * mapa refleje lo que se haya creado o borrado. Si ya había punteros de esa misma
     * ciudad, se quedan a la vista mientras llegan los nuevos.
     */
    fun cargar(ciudad: String) {
        viewModelScope.launch {
            val actual = _estado.value
            if (actual !is UiState.Exito || actual.datos.ciudad != ciudad) {
                _estado.value = UiState.Cargando
                _cercaDe.value = BusquedaCercana()       // otra ciudad: el lugar de antes ya no aplica
            }
            _estado.value = try {
                UiState.Exito(mapaRepository.getMapaDeCiudad(ciudad))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UiState.Error("No pudimos cargar el mapa. Intenta de nuevo.")
            }
        }
    }

    /** Busca lo que se escribió en "Buscar cerca de" (un municipio, una colonia o una calle). */
    fun buscarCercaDe(texto: String) {
        val ciudad = (_estado.value as? UiState.Exito)?.datos?.ciudad ?: return
        if (texto.isBlank() || _cercaDe.value.buscando) return
        _cercaDe.value = _cercaDe.value.copy(buscando = true, aviso = null)
        viewModelScope.launch {
            val lugar = buscadorDeLugares.buscar(texto, ciudad)
            _cercaDe.value = if (lugar != null) {
                BusquedaCercana(lugar = lugar)
            } else {
                _cercaDe.value.copy(
                    buscando = false,
                    aviso = "No encontramos \"${texto.trim()}\". Prueba con el municipio o la colonia, " +
                        "o mantén el dedo sobre el mapa para elegir el punto."
                )
            }
        }
    }

    /** Mantener el dedo sobre el mapa: busca cerca de ese punto. */
    fun buscarCercaDelPunto(coordenada: Coordenada) {
        _cercaDe.value = BusquedaCercana(lugar = LugarDeReferencia("el punto que elegiste", coordenada))
    }

    fun quitarCercaDe() {
        _cercaDe.value = BusquedaCercana()
    }
}
