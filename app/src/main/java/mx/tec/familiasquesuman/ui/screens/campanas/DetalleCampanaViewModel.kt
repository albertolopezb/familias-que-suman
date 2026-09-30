package mx.tec.familiasquesuman.ui.screens.campanas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.CampanaRepository
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.ui.state.UiState

/** Las tres caras de la hoja de apartar: P-12, P-12b y P-14. */
enum class ModoApartar { Normal, Apartando, YaNoAlcanza }

/** La hoja abierta sobre un artículo. [otrasApartaron] solo cuenta en YaNoAlcanza. */
data class HojaApartar(
    val articulo: ArticuloMeta,
    val cantidad: Int,
    val modo: ModoApartar = ModoApartar.Normal,
    val otrasApartaron: Int = 0
)

/** Lo que muestra P-13 después de apartar. */
data class ApartadoHecho(
    val articuloNombre: String,
    val cantidad: Int,
    val progresoAntes: Float
)

class DetalleCampanaViewModel(private val repo: CampanaRepository) : ViewModel() {

    private var idCargado: String? = null

    private val _estado = MutableStateFlow<UiState<Campana>>(UiState.Cargando)
    val estado: StateFlow<UiState<Campana>> = _estado.asStateFlow()

    private val _hoja = MutableStateFlow<HojaApartar?>(null)
    val hoja: StateFlow<HojaApartar?> = _hoja.asStateFlow()

    private val _ultimoApartado = MutableStateFlow<ApartadoHecho?>(null)
    val ultimoApartado: StateFlow<ApartadoHecho?> = _ultimoApartado.asStateFlow()

    /** Aviso de una sola vez para que el grafo navegue a P-13; se apaga al consumirlo. */
    private val _irAConfirmado = MutableStateFlow(false)
    val irAConfirmado: StateFlow<Boolean> = _irAConfirmado.asStateFlow()

    fun confirmadoNavegado() {
        _irAConfirmado.value = false
    }

    /**
     * Solo para pruebas: en true, el siguiente apartado falla como si otras familias se
     * hubieran llevado casi todo (P-14). Se apaga sola después de dispararse una vez.
     * Cámbiala a true aquí para verlo. Borrar cuando llegue el backend.
     */
    var simularAgotado: Boolean = false

    /** La campaña vive en memoria: los apartados suben la barra hasta que se cierre la app. */
    fun cargar(id: String) {
        if (idCargado == id && _estado.value is UiState.Exito) return
        idCargado = id
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            _estado.value = try {
                UiState.Exito(repo.getCampana(id))
            } catch (e: Exception) {
                UiState.Error("No se encontró la campaña")
            }
        }
    }

    fun campanaCompleta(c: Campana): Boolean =
        (c.metaTotal > 0 && c.completados >= c.metaTotal) ||
                (c.articulos.isNotEmpty() && c.articulos.all { it.completo })

    // ---- Hoja de apartar ----

    fun abrirApartar(articulo: ArticuloMeta) {
        if (articulo.completo) return
        _hoja.value = HojaApartar(articulo = articulo, cantidad = 1)
    }

    fun cambiarCantidad(nueva: Int) {
        _hoja.update { h ->
            if (h == null || h.modo == ModoApartar.Apartando) h
            else h.copy(cantidad = nueva.coerceIn(1, h.articulo.faltan.coerceAtLeast(1)))
        }
    }

    fun cerrarHoja() {
        if (_hoja.value?.modo == ModoApartar.Apartando) return
        _hoja.value = null
    }

    fun confirmarApartado() {
        val h = _hoja.value ?: return
        apartar(h.articulo.id, h.cantidad)
    }

    /**
     * Espera 800 ms (la ventana donde puede fallar) y suma en memoria. Si [simularAgotado]
     * está prendido, en vez de sumar deja la hoja en "ya no alcanza" con lo que sí queda.
     */
    fun apartar(articuloId: String, cantidad: Int) {
        val campana = (_estado.value as? UiState.Exito)?.datos ?: return
        val articulo = campana.articulos.firstOrNull { it.id == articuloId } ?: return
        viewModelScope.launch {
            _hoja.value = HojaApartar(articulo, cantidad, ModoApartar.Apartando)
            delay(800)

            if (simularAgotado && articulo.faltan > 0) {
                simularAgotado = false
                val otras = (articulo.faltan - 1).coerceAtLeast(0)
                val actualizado = articulo.copy(apartados = articulo.apartados + otras)
                _estado.value = UiState.Exito(campana.reemplazando(actualizado))
                _hoja.value = HojaApartar(
                    articulo = actualizado,
                    cantidad = 1.coerceAtMost(actualizado.faltan.coerceAtLeast(1)),
                    modo = ModoApartar.YaNoAlcanza,
                    otrasApartaron = otras
                )
                return@launch
            }

            val cantidadReal = cantidad.coerceAtMost(articulo.faltan)
            val actualizado = articulo.copy(apartados = articulo.apartados + cantidadReal)
            val nueva = campana.reemplazando(actualizado).let {
                it.copy(completados = (it.completados + cantidadReal).coerceAtMost(it.metaTotal))
            }
            _ultimoApartado.value = ApartadoHecho(
                articuloNombre = articulo.nombre,
                cantidad = cantidadReal,
                progresoAntes = campana.progreso
            )
            _estado.value = UiState.Exito(nueva)
            _hoja.value = null
            _irAConfirmado.value = true
        }
    }

    private fun Campana.reemplazando(nuevo: ArticuloMeta): Campana =
        copy(articulos = articulos.map { if (it.id == nuevo.id) nuevo else it })
}
