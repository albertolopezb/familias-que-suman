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
