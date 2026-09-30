package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonAmarillo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.CirculoDeIcono
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TarjetaActividad
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TarjetaSilueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TituloDePagina
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.actividadDeMuestra
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * Las actividades de voluntariado (RF-04, RF-05), calcadas de
 * familiasquesuman.com/actividades, con sus cuatro estados.
 *
 * Primero las próximas y abajo "Actividades pasadas", más tenues y sin botones.
 * La pantalla no conoce el ViewModel ni la navegación: recibe el estado y
 * funciones, y por eso sus previews corren sin app.
 */
@Composable
fun ActividadesScreen(
    estado: UiState<List<ActividadConAsociacion>>,
    ciudad: String,
    onActividadClick: (String) -> Unit,
    onUnirme: (String) -> Unit,
    onCompartir: (ActividadConAsociacion) -> Unit,
    onIrAInicio: () -> Unit,
    onCiudadClick: () -> Unit,
    onReintentar: () -> Unit,
    onVerGuardadas: () -> Unit,
    onVerAsociaciones: () -> Unit,
    modifier: Modifier = Modifier,
    onForzarEstado: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Web.Fondo)
    ) {
        EncabezadoApp()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 64.dp)
        ) {
            item(key = "titulo") {
                TituloDePagina(
                    titulo = "Actividades",
                    subtitulo = "Actividades en $ciudad",
                    migaAnterior = "Inicio",
                    onMigaAnterior = onIrAInicio,
                    // El botón oculto para revisar vacío y error sin servidor.
                    modificadorTitulo = Modifier.pointerInput(onForzarEstado) {
                        detectTapGestures(onLongPress = { onForzarEstado() })
                    }
                )
            }

            when (estado) {
                is UiState.Cargando -> items(2) { TarjetaSilueta(Modifier.padding(bottom = 16.dp)) }

                is UiState.Error -> item(key = "error") {
                    ErrorDeCarga(
                        mensaje = estado.mensaje,
                        onReintentar = onReintentar,
                        onVerGuardadas = onVerGuardadas
                    )
                }

                is UiState.Exito -> {
                    val (pasadas, proximas) = estado.datos.partition { it.actividad.yaPaso }

                    if (proximas.isEmpty()) {
                        item(key = "vacia") {
                            SinActividades(ciudad = ciudad, onVerAsociaciones = onVerAsociaciones)
                        }
                    }

                    tarjetas(
                        actividades = proximas,
                        onActividadClick = onActividadClick,
                        onUnirme = onUnirme,
                        onCompartir = onCompartir
                    )

                    if (pasadas.isNotEmpty()) {
                        item(key = "rotulo-pasadas") {
                            Text(
                                text = "ACTIVIDADES PASADAS",
                                style = TextoWeb.Rotulo,
                                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                            )
                        }
                        tarjetas(
                            actividades = pasadas,
                            onActividadClick = onActividadClick,
                            onUnirme = onUnirme,
                            onCompartir = onCompartir,
                            separacion = 12,
                            opacidad = 0.6f
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.tarjetas(
    actividades: List<ActividadConAsociacion>,
    onActividadClick: (String) -> Unit,
    onUnirme: (String) -> Unit,
    onCompartir: (ActividadConAsociacion) -> Unit,
    separacion: Int = 16,
    opacidad: Float = 1f
) {
    items(actividades, key = { it.actividad.id }) { item ->
        TarjetaActividad(
            item = item,
            onClick = { onActividadClick(item.actividad.id) },
            onUnirme = { onUnirme(item.actividad.id) },
            onCompartir = { onCompartir(item) },
            modifier = Modifier
                .padding(bottom = separacion.dp)
                .alpha(opacidad)
        )
    }
}

/** Vacía no es lo mismo que rota: explica por qué y ofrece una salida. */
@Composable
private fun SinActividades(ciudad: String, onVerAsociaciones: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CirculoDeIcono(
            icono = IconosWeb.CalendarioVacio,
            color = Web.Primario,
            fondo = Web.Secundario
        )
        Text(
            text = "No hay actividades próximas en $ciudad",
            style = TextoWeb.TituloTarjeta,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Las asociaciones publican fechas nuevas seguido. Mientras tanto " +
                "puedes conocer a los centros que visitamos.",
            style = TextoWeb.Cuerpo,
            color = Web.TextoApagado,
            textAlign = TextAlign.Center
        )
        EnlaceBorde(texto = "Ver asociaciones", onClick = onVerAsociaciones)
    }
}

@Composable
private fun ErrorDeCarga(
    mensaje: String,
    onReintentar: () -> Unit,
    onVerGuardadas: () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Web.Tarjeta)
            .border(1.dp, Web.Borde, forma)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CirculoDeIcono(icono = IconosWeb.SinWifi, color = Web.RojoTexto, fondo = Web.RojoFondo)
        Text(
            text = "No se pudieron cargar las actividades",
            style = TextoWeb.TituloTarjeta,
            textAlign = TextAlign.Center
        )
        Text(
            text = mensaje,
            style = TextoWeb.Cuerpo,
            color = Web.TextoApagado,
            textAlign = TextAlign.Center
        )
        BotonAmarillo(
            texto = "Reintentar",
            onClick = onReintentar,
            conFlechas = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
        EnlaceBorde(texto = "Ver las que tengo guardadas", onClick = onVerGuardadas)
    }
}

@Composable
private fun EnlaceBorde(texto: String, onClick: () -> Unit) {
    val forma = RoundedCornerShape(12.dp)
    Text(
        text = texto,
        style = TextoWeb.Chip.copy(fontWeight = FontWeight.SemiBold),
        color = Web.Primario,
        modifier = Modifier
            .clip(forma)
            .border(1.dp, Web.Borde, forma)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun ActividadesPreview(estado: UiState<List<ActividadConAsociacion>>) {
    FamiliasQueSumanTheme {
        ActividadesScreen(
            estado = estado,
            ciudad = "Monterrey, N.L.",
            onActividadClick = {},
            onUnirme = {},
            onCompartir = {},
            onIrAInicio = {},
            onCiudadClick = {},
            onReintentar = {},
            onVerGuardadas = {},
            onVerAsociaciones = {}
        )
    }
}

@Preview(name = "Con datos", showBackground = true, heightDp = 1400)
@Composable
private fun ActividadesConDatosPreview() {
    ActividadesPreview(
        UiState.Exito(
            listOf(
                actividadDeMuestra(0),
                actividadDeMuestra(8).let { it.copy(actividad = it.actividad.copy(id = "b")) },
                actividadDeMuestra(3, yaPaso = true)
                    .let { it.copy(actividad = it.actividad.copy(id = "c")) }
            )
        )
    )
}

@Preview(name = "Cargando", showBackground = true)
@Composable
private fun ActividadesCargandoPreview() {
    ActividadesPreview(UiState.Cargando)
}

@Preview(name = "Vacía", showBackground = true)
@Composable
private fun ActividadesVaciaPreview() {
    ActividadesPreview(UiState.Exito(emptyList()))
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun ActividadesErrorPreview() {
    ActividadesPreview(UiState.Error("Revisa tu conexión e inténtalo otra vez."))
}
