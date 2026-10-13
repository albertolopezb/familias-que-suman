package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import mx.tec.familiasquesuman.ui.components.BarraSuperior
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.tec.familiasquesuman.FamiliasApplication
import mx.tec.familiasquesuman.R
import mx.tec.familiasquesuman.ui.theme.AzulEncabezado
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.components.BotonPerfil
import mx.tec.familiasquesuman.ui.components.SelectorCiudad

/**
 * La barra de arriba: logo a la izquierda y el perfil a la derecha, sobre blanco
 * y con una línea abajo. La ciudad se elige en el apartado azul de abajo ([EncabezadoAzul]);
 * aquí solo se enseña si una pantalla pasa [ciudad].
 */
@Composable
fun EncabezadoApp(
    modifier: Modifier = Modifier,
    ciudad: String? = null,
    onCiudadSeleccionada: (String) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth().background(Web.Tarjeta)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // El logo trae mucho margen blanco: el recorte deja solo las letras.
            Image(
                painter = painterResource(R.drawable.logo_familias),
                contentDescription = "Familias que Suman",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(120.dp)
                    .height(40.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ciudad != null) {
                    SelectorCiudad(ciudad, onCiudadSeleccionada)
                    Spacer(Modifier.width(8.dp))
                }
                BotonPerfil()
            }
        }
        HorizontalDivider(color = Web.Borde, thickness = 1.dp)
    }
}

/**
 * El encabezado de cada pestaña, todo en azul: arriba el logo y el perfil, luego un
 * rótulo dorado, el título en blanco y la ciudad elegida (Monterrey u Hermosillo), que
 * se puede cambiar desde cualquier pestaña. Debajo, [contenido] lleva lo que cada pestaña
 * necesita tener a la mano: el buscador, las pestañas internas, los filtros.
 *
 * Si [titulo] es null (el Inicio, que saluda fuera del azul), solo quedan el logo, la ciudad y el perfil.
 *
 * Reemplaza a la barra blanca de [EncabezadoApp]. Por defecto lee y cambia la ciudad en las
 * preferencias, que son las mismas que usan el Inicio, las listas y el mapa; el Inicio las
 * pasa por parámetro para usar su ViewModel.
 */
@Composable
fun EncabezadoAzul(
    etiqueta: String?,
    titulo: String?,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    colorEtiqueta: Color = MarcaOro,
    ciudad: String? = null,
    onCiudadSeleccionada: ((String) -> Unit)? = null,
    // El toque largo del título lo usa Actividades para su botón escondido de pruebas.
    modificadorTitulo: Modifier = Modifier,
    contenido: (@Composable ColumnScope.() -> Unit)? = null
) {
    val store = (LocalContext.current.applicationContext as FamiliasApplication).container.preferenciasStore
    val preferencias by store.preferencias.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(AzulEncabezado)
            .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 20.dp)
    ) {
        val selectorDeCiudad: @Composable () -> Unit = {
            SelectorCiudad(
                ciudadActual = ciudad ?: preferencias.ciudad,
                onCiudadSeleccionada = onCiudadSeleccionada ?: store::cambiarCiudad,
                sobreAzul = true
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // El logo en blanco (con el + dorado) para que se lea sobre el azul.
            Image(
                painter = painterResource(R.drawable.logo_familias_blanco),
                contentDescription = "Familias que Suman",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(36.dp)
                    .aspectRatio(900f / 340f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sin título, la ciudad sube junto al perfil.
                if (titulo == null) {
                    selectorDeCiudad()
                    Spacer(Modifier.width(8.dp))
                }
                BotonPerfil(sobreAzul = true)
            }
        }
        if (titulo != null) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (etiqueta != null) {
                        Text(
                            text = etiqueta,
                            style = TextoWeb.Chico.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = colorEtiqueta
                        )
                    }
                    Text(
                        text = titulo,
                        style = TextoWeb.Titulo.copy(fontSize = 28.sp, lineHeight = 34.sp),
                        color = Color.White,
                        modifier = modificadorTitulo.padding(top = 2.dp)
                    )
                    if (subtitulo != null) {
                        Text(
                            text = subtitulo,
                            style = TextoWeb.Cuerpo,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                selectorDeCiudad()
            }
        }
        if (contenido != null) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = contenido
            )
        }
    }
}

/**
 * Migas de pan, título y subtítulo: el arranque de cada página del sitio.
 * El título acepta un toque largo para que la lista pueda esconder ahí su botón
 * de pruebas.
 */
@Composable
fun TituloDePagina(
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    modificadorTitulo: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 24.dp, bottom = 16.dp)) {
        Text(text = titulo, style = TextoWeb.Titulo, modifier = modificadorTitulo)
        Text(
            text = subtitulo,
            style = TextoWeb.Cuerpo.copy(lineHeight = 20.sp),
            color = Web.TextoApagado,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * La barra del detalle: "‹ Actividades" para regresar y, a la derecha, las
 * píldoras que la pantalla necesite (compartir, favorito).
 */
@Composable
fun BarraDeRegreso(
    texto: String,
    onRegresar: () -> Unit,
    modifier: Modifier = Modifier,
    acciones: @Composable () -> Unit = {}
) {
    mx.tec.familiasquesuman.ui.components.BarraSuperior(texto, onRegresar, modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { acciones() }
    }
}

/** El círculo con ícono grande de los estados vacío, error y sin conexión. */
@Composable
fun CirculoDeIcono(
    icono: ImageVector,
    color: Color,
    fondo: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
    }
}
