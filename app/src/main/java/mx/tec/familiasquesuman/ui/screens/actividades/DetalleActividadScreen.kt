package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.Aportacion
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.Casilla
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.AvisoSinLugares
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BarraDeRegreso
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonAmarillo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.ChipAportacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconoTema
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IndicadorCupo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.actividadDeMuestra
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.fotoDeActividad
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.tituloDeAportacion
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * El detalle de una actividad (RF-04, RF-05, RF-17), con las mismas secciones
 * que el sitio: información, descripción, acerca del proyecto, qué haremos,
 * qué llevar, recomendaciones, aportación y contacto.
 *
 * La variante sin cupo (P-03b) no es otra pantalla: es esta misma con el bloque
 * rojo arriba, el botón apagado y el enlace a otras actividades. Lo decide el
 * dominio con `sinLugares`.
 */
@Composable
fun DetalleActividadScreen(
    item: ActividadConAsociacion,
    esFavorito: Boolean,
    onRegresar: () -> Unit,
    onCompartir: () -> Unit,
    onAlternarFavorito: () -> Unit,
    onInscribirme: () -> Unit,
    onVerOtrasActividades: () -> Unit,
    modifier: Modifier = Modifier,
    simularSinCupo: Boolean = false,
    onSimularSinCupoChange: ((Boolean) -> Unit)? = null
) {
    val actividad = item.actividad

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Web.Fondo)
    ) {
        BarraDeRegreso(texto = "Actividades", onRegresar = onRegresar) {
            PildoraFavorito(esFavorito = esFavorito, onClick = onAlternarFavorito)
            PildoraCompartir(onClick = onCompartir)
        }

        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                FotoDetalle(foto = actividad.foto, descripcion = actividad.titulo)

                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Encabezado(item)

                    if (actividad.sinLugares && actividad.tieneCupo && !actividad.yaPaso) {
                        AvisoSinLugares(cupoTotal = actividad.cupoTotal)
                    }

                    Informacion(item)

                    // La descripción corta va en gris claro, sin borde, como en el sitio.
                    Text(
                        text = actividad.descripcion,
                        style = TextoWeb.Cuerpo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Web.Secundario.copy(alpha = 0.6f))
                            .padding(16.dp)
                    )

                    SeccionDeTexto("Acerca del proyecto", actividad.acercaDelProyecto)
                    SeccionDeTexto("¿Qué haremos?", actividad.queHaremos)
                    SeccionDeTexto("¿Qué incluye?", actividad.queIncluye)
                    SeccionDeTexto("¿Qué llevar?", actividad.queLlevar, IconosWeb.Mochila)
                    SeccionDeTexto("Recomendaciones", actividad.recomendaciones, IconosWeb.Estrella)
                    SeccionAportacion(actividad.aportacion)
                    Contacto(nombre = item.asociacion.nombre)
                }
            }

            PieDeAccion(
                item = item,
                onInscribirme = onInscribirme,
                onVerOtrasActividades = onVerOtrasActividades,
                simularSinCupo = simularSinCupo,
                onSimularSinCupoChange = onSimularSinCupoChange,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun FotoDetalle(foto: String?, descripcion: String) {
    if (foto == null) return
    val recurso = fotoDeActividad(foto)
    if (recurso != null) {
        Image(
            painter = painterResource(recurso),
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 260.dp)
        )
    } else {
        // Todavía no está el archivo en res/drawable: se deja el hueco a la vista.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Web.Secundario)
        )
    }
}

@Composable
private fun Encabezado(item: ActividadConAsociacion) {
    val actividad = item.actividad
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        IconoTema(tema = actividad.tema, tamano = 40.dp, modifier = Modifier.padding(top = 2.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = actividad.titulo, style = TextoWeb.Titulo)
            if (actividad.municipio.isNotBlank()) {
                Text(
                    text = actividad.municipio,
                    style = TextoWeb.Cuerpo,
                    color = Web.TextoApagado,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        ChipAportacion(actividad.aportacion)
    }
}

@Composable
private fun Informacion(item: ActividadConAsociacion) {
    val actividad = item.actividad
    Tarjeta {
        TituloDeSeccion("Información", IconosWeb.Informacion)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DatoConDetalle(
                icono = IconosWeb.Calendario,
                texto = actividad.fecha.replaceFirstChar { it.uppercase() },
                iconoDetalle = IconosWeb.Reloj,
                detalle = actividad.horario
            )
            DatoConDetalle(
                icono = IconosWeb.Ubicacion,
                texto = actividad.direccion.ifBlank { actividad.municipio },
                iconoDetalle = IconosWeb.PuntoDeEncuentro,
                detalle = actividad.puntoDeEncuentro
                    .takeIf { it.isNotBlank() }
                    ?.let { "Punto de encuentro: $it" }
            )
            if (actividad.tieneCupo) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconoPrimario(IconosWeb.Personas)
                    Text(
                        text = buildAnnotatedString {
                            append("Capacidad: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                                append("${actividad.cupoTotal} personas")
                            }
                        },
                        style = TextoWeb.Cuerpo
                    )
                }
                IndicadorCupo(actividad)
            }
        }
    }
}

@Composable
private fun DatoConDetalle(
    icono: ImageVector,
    texto: String,
    iconoDetalle: ImageVector,
    detalle: String?
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        IconoPrimario(icono, modifier = Modifier.padding(top = 3.dp))
        Column {
            Text(text = texto, style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.Medium))
            if (detalle != null) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        iconoDetalle,
                        contentDescription = null,
                        tint = Web.TextoApagado,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(text = detalle, style = TextoWeb.Chico)
                }
            }
        }
    }
}

/** Una sección de texto del detalle. Si la asociación no la llenó, no se ve. */
@Composable
private fun SeccionDeTexto(titulo: String, texto: String, icono: ImageVector? = null) {
    if (texto.isBlank()) return
    Tarjeta {
        TituloDeSeccion(titulo, icono)
        Text(text = texto, style = TextoWeb.Cuerpo, color = Web.TextoApagado)
    }
}

@Composable
private fun SeccionAportacion(aportacion: Aportacion) {
    val texto = when (aportacion) {
        Aportacion.Ninguna -> return
        is Aportacion.EnEspecie -> aportacion.detalle
        is Aportacion.Monetaria -> listOf("Aportación: ${aportacion.monto}", aportacion.detalle)
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }
    Tarjeta {
        TituloDeSeccion(tituloDeAportacion(aportacion), IconosWeb.Tarjeta, conEspacio = texto.isNotBlank())
        if (texto.isNotBlank()) {
            Text(text = texto, style = TextoWeb.Cuerpo, color = Web.TextoApagado)
        }
    }
}

@Composable
private fun Contacto(nombre: String) {
    Tarjeta {
        TituloDeSeccion("Contacto")
        Text(
            text = nombre,
            style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // RF-08: intents en la siguiente etapa
            BotonVerde("WhatsApp", IconosWeb.Mensaje, onClick = {}, modifier = Modifier.weight(1f))
            // RF-08: intents en la siguiente etapa
            BotonVerde("Llamar", IconosWeb.Telefono, onClick = {}, modifier = Modifier.weight(1f))
        }
    }
}

/**
 * El botón de abajo, siempre a la vista. Normal: "Unirme a esta actividad".
 * Sin cupo: apagado, con el enlace a otras actividades. Pasada: apagado.
 */
@Composable
private fun PieDeAccion(
    item: ActividadConAsociacion,
    onInscribirme: () -> Unit,
    onVerOtrasActividades: () -> Unit,
    simularSinCupo: Boolean,
    onSimularSinCupoChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val actividad = item.actividad
    Column(
        modifier = modifier
            .fillMaxWidth()
            // Un desvanecido para que el texto no se corte seco detrás del botón.
            .background(Brush.verticalGradient(listOf(Color.Transparent, Web.Fondo, Web.Fondo)))
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            actividad.yaPaso -> BotonAmarillo(
                texto = "Esta actividad ya pasó",
                onClick = {},
                habilitado = false,
                radio = 16.dp,
                alto = 52.dp,
                modifier = Modifier.fillMaxWidth()
            )

            actividad.sinLugares && actividad.tieneCupo -> {
                BotonAmarillo(
                    texto = "Sin lugares disponibles",
                    onClick = {},
                    habilitado = false,
                    radio = 16.dp,
                    alto = 52.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onVerOtrasActividades)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ver otras actividades",
                        style = TextoWeb.Chip,
                        color = Web.Primario
                    )
                    Icon(
                        IconosWeb.FlechaDerecha,
                        contentDescription = null,
                        tint = Web.Primario,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            else -> {
                // Solo para probar P-08: al confirmar, otra familia gana los lugares.
                if (onSimularSinCupoChange != null) {
                    Casilla(
                        marcada = simularSinCupo,
                        onCambio = onSimularSinCupoChange,
                        texto = "Prueba: al confirmar, otra familia gana los últimos lugares (P-08)"
                    )
                }
                BotonAmarillo(
                    texto = "Unirme a esta actividad",
                    onClick = onInscribirme,
                    radio = 16.dp,
                    alto = 52.dp,
                    negritas = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Piezas del detalle
// ---------------------------------------------------------------------------

@Composable
private fun Tarjeta(contenido: @Composable ColumnScope.() -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Web.Tarjeta)
            .border(1.dp, Web.Borde, forma)
            .padding(16.dp),
        content = contenido
    )
}

@Composable
private fun TituloDeSeccion(texto: String, icono: ImageVector? = null, conEspacio: Boolean = true) {
    Row(
        modifier = Modifier.padding(bottom = if (conEspacio) 12.dp else 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icono != null) IconoPrimario(icono)
        Text(text = texto, style = TextoWeb.Seccion)
    }
}

@Composable
private fun IconoPrimario(icono: ImageVector, modifier: Modifier = Modifier) {
    Icon(icono, contentDescription = null, tint = Web.Primario, modifier = modifier.size(16.dp))
}

@Composable
private fun BotonVerde(
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Web.Verde)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Text(text = texto, style = TextoWeb.Chip, color = Color.White)
    }
}

@Composable
private fun PildoraCompartir(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Web.Secundario)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            IconosWeb.Compartir,
            contentDescription = null,
            tint = Web.TextoApagado,
            modifier = Modifier.size(14.dp)
        )
        Text(text = "Compartir", style = TextoWeb.Chip, color = Web.TextoApagado)
    }
}

/** Favorito en memoria (RF-17). El corazón se llena al tocarlo. */
@Composable
private fun PildoraFavorito(esFavorito: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (esFavorito) Web.RosaFondo else Web.Secundario)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (esFavorito) Icons.Default.Favorite else IconosWeb.Corazon,
            contentDescription = if (esFavorito) "Quitar de favoritos" else "Guardar en favoritos",
            tint = if (esFavorito) Web.Rosa else Web.TextoApagado,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Composable
private fun DetallePreview(item: ActividadConAsociacion, esFavorito: Boolean = false) {
    FamiliasQueSumanTheme {
        DetalleActividadScreen(
            item = item,
            esFavorito = esFavorito,
            onRegresar = {},
            onCompartir = {},
            onAlternarFavorito = {},
            onInscribirme = {},
            onVerOtrasActividades = {}
        )
    }
}

@Preview(name = "Con cupo", showBackground = true, heightDp = 1500)
@Composable
private fun DetalleConCupoPreview() {
    DetallePreview(actividadDeMuestra(8))
}

@Preview(name = "Sin cupo", showBackground = true, heightDp = 1500)
@Composable
private fun DetalleSinCupoPreview() {
    DetallePreview(actividadDeMuestra(0), esFavorito = true)
}
