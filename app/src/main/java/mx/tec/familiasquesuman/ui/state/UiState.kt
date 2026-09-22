package mx.tec.familiasquesuman.ui.state

/** Los tres estados de cualquier pantalla que carga datos. Igual que en la Práctica 4. */
sealed interface UiState<out T> {
    data object Cargando : UiState<Nothing>
    data class Exito<T>(val datos: T) : UiState<T>
    data class Error(val mensaje: String) : UiState<Nothing>
}
