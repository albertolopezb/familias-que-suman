package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoActividades
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TarjetaActividad
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TarjetaSilueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.actividadDeMuestra
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * Las actividades de voluntariado (RF-04, RF-05), con sus cuatro estados.
 *
 * La pantalla no conoce el ViewModel ni la navegación: recibe el estado y
 * funciones, y por eso sus previews corren sin app.
 */
@Composable
fun ActividadesScreen(
    estado: UiState<List<ActividadConAsociacion>>,
    ciudad: String,
    onActividadClick: (String) -> Unit,
    onRegresar: () -> Unit,
    onReintentar: () -> Unit,
    onVerAsociaciones: () -> Unit,
    modifier: Modifier = Modifier,
    modo: ModoDePrueba = ModoDePrueba.NORMAL,
    onCambiarModo: (ModoDePrueba) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        EncabezadoActividades(
            titulo = "Actividades en Familia",
            onRegresar = onRegresar,
            accionDerecha = {
                Etiqueta(texto = ciudad, fondo = AcentoSuave, color = AcentoTexto)
            }
        )

        SelectorDeEstado(modo = modo, onCambiarModo = onCambiarModo)

        when (estado) {
            is UiState.Cargando -> ListaCargando()

            is UiState.Error -> ErrorDeCarga(
                mensaje = estado.mensaje,
                onReintentar = onReintentar
            )

            is UiState.Exito ->
                if (estado.datos.isEmpty()) {
                    SinActividades(ciudad = ciudad, onVerAsociaciones = onVerAsociaciones)
                } else {
                    ListaConDatos(
                        actividades = estado.datos,
                        ciudad = ciudad,
                        onActividadClick = onActividadClick
                    )
                }
        }
    }
}

@Composable
private fun ListaConDatos(
    actividades: List<ActividadConAsociacion>,
    ciudad: String,
    onActividadClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "${actividades.size} actividades en $ciudad",
                style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave
            )
        }
        items(actividades, key = { it.actividad.id }) { item ->
            TarjetaActividad(
                item = item,
                onClick = { onActividadClick(item.actividad.id) }
            )
        }
    }
}

/** Siluetas con la forma de la tarjeta real, para que el contenido no salte. */
@Composable
private fun ListaCargando() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaSilueta(fraccionTitulo = 0.88f, fraccionSubtitulo = 0.62f)
        TarjetaSilueta(fraccionTitulo = 0.74f, fraccionSubtitulo = 0.55f)
        TarjetaSilueta(fraccionTitulo = 0.81f, fraccionSubtitulo = 0.48f)
    }
}

/** Vacía no es lo mismo que rota: explica por qué y ofrece una salida. */
@Composable
private fun SinActividades(ciudad: String, onVerAsociaciones: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.DateRange,
            contentDescription = null,
            tint = TintaSuave,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = "No hay actividades programadas en $ciudad esta semana",
            style = MaterialTheme.typography.titleLarge,
            color = Tinta,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Las asociaciones publican actividades nuevas cada lunes.",
            style = MaterialTheme.typography.bodyMedium,
            color = TintaSuave,
            textAlign = TextAlign.Center
        )
        OutlinedButton(onClick = onVerAsociaciones) {
            Text("Ver asociaciones")
        }
    }
}

@Composable
private fun ErrorDeCarga(mensaje: String, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ErrorFondo)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "No se pudieron cargar las actividades",
                style = MaterialTheme.typography.labelLarge,
                color = ErrorTexto
            )
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = ErrorTexto
            )
        }
        OutlinedButton(onClick = onReintentar) {
            Text("Reintentar")
        }
    }
}

/**
 * Fila temporal para poder ver los cuatro estados sin red.
 * Se borra en la etapa 2, cuando el servidor los produzca de verdad.
 */
@Composable
private fun SelectorDeEstado(modo: ModoDePrueba, onCambiarModo: (ModoDePrueba) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Superficie)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ver estado:",
            style = MaterialTheme.typography.labelMedium,
            color = TintaSuave
        )
        ModoDePrueba.entries.forEach { opcion ->
            val activo = opcion == modo
            Text(
                text = when (opcion) {
                    ModoDePrueba.NORMAL -> "con datos"
                    ModoDePrueba.VACIA -> "vacía"
                    ModoDePrueba.ERROR -> "error"
                },
                style = MaterialTheme.typography.labelMedium,
                color = if (activo) MarcaAzul else TintaSuave,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.dp,
                        color = if (activo) MarcaAzul else Borde,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onCambiarModo(opcion) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActividadesConDatosPreview() {
    FamiliasQueSumanTheme {
        ActividadesScreen(
            estado = UiState.Exito(
                listOf(actividadDeMuestra(8), actividadDeMuestra(2), actividadDeMuestra(15))
            ),
            ciudad = "Monterrey",
            onActividadClick = {},
            onRegresar = {},
            onReintentar = {},
            onVerAsociaciones = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ActividadesVaciaPreview() {
    FamiliasQueSumanTheme {
        ActividadesScreen(
            estado = UiState.Exito(emptyList()),
            ciudad = "Monterrey",
            onActividadClick = {},
            onRegresar = {},
            onReintentar = {},
            onVerAsociaciones = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ActividadesErrorPreview() {
    FamiliasQueSumanTheme {
        ActividadesScreen(
            estado = UiState.Error("Revisa tu conexión e inténtalo otra vez."),
            ciudad = "Monterrey",
            onActividadClick = {},
            onRegresar = {},
            onReintentar = {},
            onVerAsociaciones = {}
        )
    }
}
