package mx.tec.familiasquesuman.ui.screens.campanas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import mx.tec.familiasquesuman.data.red.ApiExcepcion
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
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

class DetalleCampanaViewModel(
    private val repo: CampanaRepository,
    private val actividadRepo: ActividadRepository
) : ViewModel() {

    private var idCargado: String? = null

    private val _estado = MutableStateFlow<UiState<Campana>>(UiState.Cargando)
    val estado: StateFlow<UiState<Campana>> = _estado.asStateFlow()

    private val _nombreAsociacion = MutableStateFlow<String?>(null)

    /** Nombre de la asociación dueña de la campaña; null mientras carga o si no se encuentra. */
    val nombreAsociacion: StateFlow<String?> = _nombreAsociacion.asStateFlow()

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
     * Cámbiala a true aquí para verlo sin servidor.
     */
    var simularAgotado: Boolean = false

    /** La campaña y sus metas salen del servidor; lo apartado por otras familias ya viene sumado. */
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
            val campana = (_estado.value as? UiState.Exito)?.datos
            // Una campaña sin asociación no la pide: con el id vacío la ruta quedaba con barra final
            // y el servidor la redirigía a http://, lo que la app (solo HTTPS) tomaba por "sin conexión".
            if (campana != null && campana.asociacionId.isNotBlank()) {
                _nombreAsociacion.value = try {
                    actividadRepo.getAsociacion(campana.asociacionId).nombre
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    /**
     * Una campaña sin meta o sin artículos no cuenta como completa: sin esto, una campaña
     * recién creada (metaTotal 0 o lista vacía) se pintaría como "ya está completa".
     */
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

    /** El id de este intento de apartar: si se reintenta por la red, el servidor no lo cuenta dos veces. */
    private var intento: String = UUID.randomUUID().toString()

    /**
     * Le pide al servidor apartar [cantidad]. El servidor bloquea el artículo mientras cuenta lo
     * que falta: si otras familias ya apartaron una parte, responde "ya no alcanza" con lo que de
     * verdad queda, y la hoja pasa a P-14. [simularAgotado] lo muestra sin servidor (demostración).
     */
    fun apartar(articuloId: String, cantidad: Int) {
        val campana = (_estado.value as? UiState.Exito)?.datos ?: return
        val articulo = campana.articulos.firstOrNull { it.id == articuloId } ?: return
        viewModelScope.launch {
            _hoja.value = HojaApartar(articulo, cantidad, ModoApartar.Apartando)

            if (simularAgotado && articulo.faltan > 0) {
                simularAgotado = false
                val otras = (articulo.faltan - 1).coerceAtLeast(0)
                val actualizado = articulo.copy(apartados = articulo.apartados + otras)
                _estado.value = UiState.Exito(campana.reemplazando(actualizado))
                _hoja.value = HojaApartar(actualizado, 1.coerceAtMost(actualizado.faltan.coerceAtLeast(1)), ModoApartar.YaNoAlcanza, otras)
                return@launch
            }

            try {
                val r = repo.apartar(articuloId, cantidad, intento)
                intento = UUID.randomUUID().toString()
                val nueva = campana.reemplazando(r.articulo).copy(completados = r.completadosCampana)
                _ultimoApartado.value = ApartadoHecho(articuloNombre = articulo.nombre, cantidad = r.cantidad, progresoAntes = campana.progreso)
                _estado.value = UiState.Exito(nueva)
                _hoja.value = null
                _irAConfirmado.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiExcepcion) {
                // Si el servidor contestó, este intento ya terminó. Si NO hubo conexión, se conserva el
                // mismo id: al reintentar, el servidor reconoce la aportación y no la cuenta dos veces.
                if (!e.sinConexion) intento = UUID.randomUUID().toString()
                val faltan = e.datoEntero("faltan")
                if (e.codigo == "ya_no_alcanza" && faltan != null) {
                    val actualizado = articulo.copy(apartados = (articulo.meta - faltan).coerceAtLeast(0))
                    _estado.value = UiState.Exito(campana.reemplazando(actualizado))
                    _hoja.value = HojaApartar(actualizado, 1.coerceAtMost(faltan.coerceAtLeast(1)), ModoApartar.YaNoAlcanza,
                        otrasApartaron = (actualizado.apartados - articulo.apartados).coerceAtLeast(0))
                } else {
                    _hoja.value = HojaApartar(articulo, cantidad, ModoApartar.Normal)
                    _mensaje.value = e.message
                }
            }
        }
    }

    private val _mensaje = MutableStateFlow<String?>(null)
    /** Lo que respondió el servidor si no se pudo apartar (campaña cerrada, sin conexión…). */
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun mensajeVisto() {
        _mensaje.value = null
    }

    private fun Campana.reemplazando(nuevo: ArticuloMeta): Campana =
        copy(articulos = articulos.map { if (it.id == nuevo.id) nuevo else it })
}
