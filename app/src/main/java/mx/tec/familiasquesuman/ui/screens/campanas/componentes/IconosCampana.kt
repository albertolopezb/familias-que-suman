package mx.tec.familiasquesuman.ui.screens.campanas.componentes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

// Solo tenemos material-icons-core, que no trae filtro ni reloj. Se dibujan aquí
// con los mismos trazos de Material Icons para no agregar la librería extendida.
private fun icono(nombre: String, trazo: String): ImageVector =
    ImageVector.Builder(
        name = nombre,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(trazo),
        fill = SolidColor(Color.Black)
    ).build()

// Íconos de contorno (sin relleno), como la caja del Figma.
private fun iconoTrazo(nombre: String, trazo: String, grosor: Float = 1.6f): ImageVector =
    ImageVector.Builder(
        name = nombre,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(trazo),
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = grosor,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ).build()

/** La caja del Figma: tapa ancha arriba, cuerpo abajo y una división al centro. */
val IconoCaja: ImageVector = iconoTrazo(
    "Caja",
    "M3,4h18v4.5H3z M4,8.5h16V20H4z M12,8.5V20"
)

val IconoFiltro: ImageVector = icono(
    "Filtro",
    "M10,18h4v-2h-4v2zM3,6v2h18V6H3zm3,7h12v-2H6v2z"
)

val IconoReloj: ImageVector = icono(
    "Reloj",
    "M11.99,2C6.47,2 2,6.48 2,12s4.47,10 9.99,10C17.52,22 22,17.52 22,12S17.52,2 11.99,2zM12,20c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8zM12.5,7H11v6l5.25,3.15 0.75,-1.23 -4.5,-2.67z"
)

/** Marcador de "aquí va una foto", como en los marcos del Figma. */
val IconoImagen: ImageVector = icono(
    "Imagen",
    "M19,5v14H5V5h14m0,-2H5c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5c0,-1.1 -0.9,-2 -2,-2zM14.14,11.86l-3,3.87L9,13.14 6,17h12l-3.86,-5.14z"
)

val IconoCalendario: ImageVector = icono(
    "Calendario",
    "M20,3h-1L19,1h-2v2L7,3L7,1L5,1v2L4,3c-1.1,0 -2,0.9 -2,2v16c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2L22,5c0,-1.1 -0.9,-2 -2,-2zM20,21L4,21L4,8h16v13z"
)

// Íconos de contorno con varios trazos (los de lucide, que usa el sitio). Cada trazo va por
// separado porque sus comandos "m" en minúscula dependen de dónde empieza cada uno.
private fun iconoTrazos(nombre: String, vararg trazos: String, grosor: Float = 2f): ImageVector {
    val b = ImageVector.Builder(
        name = nombre,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    )
    trazos.forEach { t ->
        b.addPath(
            pathData = addPathNodes(t),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = grosor,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }
    return b.build()
}

/** Mano con corazón: "3 opciones de donación disponibles". */
val IconoManoCorazon: ImageVector = iconoTrazos(
    "ManoCorazon",
    "M11 14h2a2 2 0 1 0 0-4h-3c-.6 0-1.1.2-1.4.6L3 16",
    "m7 20 1.6-1.4c.3-.4.8-.6 1.4-.6h4c1.1 0 2.1-.4 2.8-1.2l4.6-4.4a2 2 0 0 0-2.75-2.91l-4.2 3.9",
    "m2 15 6 6",
    "M19.5 8.5c.7-.7 1.5-1.6 1.5-2.7A2.73 2.73 0 0 0 16 4a2.78 2.78 0 0 0-5 1.8c0 1.2.8 2 1.5 2.8L16 12Z"
)

/** Burbuja de mensaje: el botón verde que abre WhatsApp. */
val IconoMensaje: ImageVector = iconoTrazos(
    "Mensaje",
    "M7.9 20A9 9 0 1 0 4 16.1L2 22Z"
)

/** Teléfono: el botón que llama cuando la campaña no tiene WhatsApp. */
val IconoTelefono: ImageVector = iconoTrazos(
    "Telefono",
    "M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"
)

/** Cámara de Instagram (solo el contorno; se pinta en blanco sobre el círculo de colores). */
val IconoInstagram: ImageVector = iconoTrazos(
    "Instagram",
    "M7 3h10a4 4 0 0 1 4 4v10a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4V7a4 4 0 0 1 4-4z",
    "M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8z",
    "M17.5 6.5h.01"
)

/** Flecha hacia abajo de las tarjetas que se despliegan (se gira para "cerrar"). */
val IconoChevron: ImageVector = iconoTrazos("Chevron", "m6 9 6 6 6-6")

/** Escudo de "Verificado". */
val IconoEscudo: ImageVector = iconoTrazos(
    "Escudo",
    "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z",
    grosor = 2f
)

/** Flecha de "Cómo llegar" y de la dirección. */
val IconoNavegacion: ImageVector = iconoTrazos("Navegacion", "M3 11 22 2 13 21 11 13 3 11z")
