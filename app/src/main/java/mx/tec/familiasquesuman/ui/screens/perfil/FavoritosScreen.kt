package mx.tec.familiasquesuman.ui.screens.perfil

import mx.tec.familiasquesuman.ui.components.BarraSuperior
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.TarjetaFavorita
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.*

enum class TipoFavorito(val etiqueta: String) {
    ASOCIACION("Asociación"), ACTIVIDAD("Actividad"), CAMPANA("Campaña")
}

/** Algo que la familia marcó con el corazón: una asociación, una actividad o una campaña. */
data class ItemFavorito(val id: String, val tipo: TipoFavorito, val titulo: String, val detalle: String)

@Composable
fun FavoritosScreen(
    estado: UiState<List<ItemFavorito>>,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
    onAbrir: (ItemFavorito) -> Unit = {},
    onQuitar: (ItemFavorito) -> Unit = {}
) {
    Column(modifier.fillMaxSize().background(Fondo)) {
        BarraSuperior("Mis favoritos", onRegresar = onVolver)
        when (estado) {
            UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(estado.mensaje, onReintentar)
            is UiState.Exito -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    // Texto del diseño. El envío de notificaciones no está implementado en esta etapa.
                    Text("Aquí están las asociaciones, actividades y campañas que guardaste.",
                        style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
                }
                if (estado.datos.isEmpty()) {
                    item {
                        Text("Todavía no tienes favoritos. Toca el corazón en una asociación, actividad o campaña para guardarla aquí.",
                            style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
                    }
                }
                items(estado.datos, key = { it.id }) { item ->
                    TarjetaFavorita(
                        nombre = item.titulo,
                        categoria = item.detalle,
                        onClick = { onAbrir(item) },
                        onQuitar = { onQuitar(item) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun FavoritosVaciosPreview() {
    FamiliasQueSumanTheme { FavoritosScreen(UiState.Exito(emptyList()), {}, {}) }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun FavoritosErrorPreview() {
    FamiliasQueSumanTheme { FavoritosScreen(UiState.Error("No se pudieron cargar tus favoritos."), {}, {}) }
}
