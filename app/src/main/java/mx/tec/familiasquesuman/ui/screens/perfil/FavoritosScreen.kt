package mx.tec.familiasquesuman.ui.screens.perfil

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
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.TarjetaFavorita
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.*

@Composable
fun FavoritosScreen(
    estado: UiState<List<Asociacion>>,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().background(Fondo)) {
        Surface(color = Superficie) {
            Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a Perfil", tint = Tinta)
                }
                Text("Mis favoritos", style = MaterialTheme.typography.headlineMedium, color = Tinta)
            }
        }
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
                    Text("Te avisamos cuando estas asociaciones publiquen una actividad o campaña nueva.",
                        style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
                }
                if (estado.datos.isEmpty()) {
                    item {
                        Text("Todavía no tienes asociaciones favoritas.",
                            style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
                    }
                }
                items(estado.datos, key = { it.id }) { asociacion ->
                    TarjetaFavorita(asociacion.nombre, asociacion.categoria)
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
