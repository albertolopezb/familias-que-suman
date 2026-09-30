package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Los íconos del sitio. familiasquesuman.com usa Lucide (licencia ISC), así que
 * estos son sus mismos trazos, pasados a ImageVector. material-icons-core no
 * trae casi ninguno de estos y agregar material-icons-extended pesa megas.
 *
 * Se dibujan con trazo negro de 2 px en una caja de 24: el `tint` de Icon los pinta.
 */
object IconosWeb {

    /** lucide: calendar */
    val Calendario: ImageVector by lazy {
        lucide(
            "Calendario",
            "M8 2v3",
            "M16 2v3",
            "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2z",
            "M3 9h18"
        )
    }

    /** lucide: map-pin */
    val Ubicacion: ImageVector by lazy {
        lucide(
            "Ubicacion",
            "M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0",
            "M9 10a3 3 0 1 0 6 0a3 3 0 1 0 -6 0"
        )
    }

    /** lucide: users */
    val Personas: ImageVector by lazy {
        lucide(
            "Personas",
            "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
            "M16 3.128a4 4 0 0 1 0 7.744",
            "M22 21v-2a4 4 0 0 0-3-3.87",
            "M5 7a4 4 0 1 0 8 0a4 4 0 1 0 -8 0"
        )
    }

    /** lucide: heart */
    val Corazon: ImageVector by lazy {
        lucide(
            "Corazon",
            "M2 9.5a5.5 5.5 0 0 1 9.591-3.676.56.56 0 0 0 .818 0A5.49 5.49 0 0 1 22 9.5c0 2.29-1.5 4-3 5.5l-5.492 5.313a2 2 0 0 1-3 .019L5 15c-1.5-1.5-3-3.2-3-5.5"
        )
    }

    /** lucide: sparkles */
    val Destellos: ImageVector by lazy {
        lucide(
            "Destellos",
            "M11.017 2.814a1 1 0 0 1 1.966 0l1.051 5.558a2 2 0 0 0 1.594 1.594l5.558 1.051a1 1 0 0 1 0 1.966l-5.558 1.051a2 2 0 0 0-1.594 1.594l-1.051 5.558a1 1 0 0 1-1.966 0l-1.051-5.558a2 2 0 0 0-1.594-1.594l-5.558-1.051a1 1 0 0 1 0-1.966l5.558-1.051a2 2 0 0 0 1.594-1.594z",
            "M20 2v4",
            "M22 4h-4",
            "M2 20a2 2 0 1 0 4 0a2 2 0 1 0 -4 0"
        )
    }

    /** lucide: tree-pine */
    val Pino: ImageVector by lazy {
        lucide(
            "Pino",
            "m17 14 3 3.3a1 1 0 0 1-.7 1.7H4.7a1 1 0 0 1-.7-1.7L7 14h-.3a1 1 0 0 1-.7-1.7L9 9h-.2A1 1 0 0 1 8 7.3L12 3l4 4.3a1 1 0 0 1-.8 1.7H15l3 3.3a1 1 0 0 1-.7 1.7H17Z",
            "M12 22v-3"
        )
    }

    /** lucide: plus */
    val Mas: ImageVector by lazy {
        lucide(
            "Mas",
            "M5 12h14",
            "M12 5v14"
        )
    }

    /** lucide: chevron-right */
    val FlechaDerecha: ImageVector by lazy {
        lucide(
            "FlechaDerecha",
            "m9 18 6-6-6-6"
        )
    }

    /** lucide: chevron-down */
    val FlechaAbajo: ImageVector by lazy {
        lucide(
            "FlechaAbajo",
            "m6 9 6 6 6-6"
        )
    }

    /** lucide: chevron-left */
    val FlechaIzquierda: ImageVector by lazy {
        lucide(
            "FlechaIzquierda",
            "m15 18-6-6 6-6"
        )
    }

    /** lucide: share-2 */
    val Compartir: ImageVector by lazy {
        lucide(
            "Compartir",
            "M15 5a3 3 0 1 0 6 0a3 3 0 1 0 -6 0",
            "M3 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0",
            "M15 19a3 3 0 1 0 6 0a3 3 0 1 0 -6 0",
            "M8.59 13.51L15.42 17.49",
            "M15.41 6.51L8.59 10.49"
        )
    }

    /** lucide: info */
    val Informacion: ImageVector by lazy {
        lucide(
            "Informacion",
            "M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0",
            "M12 16v-4",
            "M12 8h.01"
        )
    }

    /** lucide: clock */
    val Reloj: ImageVector by lazy {
        lucide(
            "Reloj",
            "M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0",
            "M12 6v6l4 2"
        )
    }

    /** lucide: navigation */
    val PuntoDeEncuentro: ImageVector by lazy {
        lucide(
            "PuntoDeEncuentro",
            "M3 11 L22 2 L13 21 L11 13 L3 11z"
        )
    }

    /** lucide: backpack */
    val Mochila: ImageVector by lazy {
        lucide(
            "Mochila",
            "M4 10a4 4 0 0 1 4-4h8a4 4 0 0 1 4 4v10a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2z",
            "M8 10h8",
            "M8 18h8",
            "M8 22v-6a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v6",
            "M9 6V4a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2"
        )
    }

    /** lucide: star */
    val Estrella: ImageVector by lazy {
        lucide(
            "Estrella",
            "M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.123 2.123 0 0 0 1.595 1.16l5.166.756a.53.53 0 0 1 .294.904l-3.736 3.638a2.123 2.123 0 0 0-.611 1.878l.882 5.14a.53.53 0 0 1-.771.56l-4.618-2.428a2.122 2.122 0 0 0-1.973 0L6.396 21.01a.53.53 0 0 1-.77-.56l.881-5.139a2.122 2.122 0 0 0-.611-1.879L2.16 9.795a.53.53 0 0 1 .294-.906l5.165-.755a2.122 2.122 0 0 0 1.597-1.16z"
        )
    }

    /** lucide: credit-card */
    val Tarjeta: ImageVector by lazy {
        lucide(
            "Tarjeta",
            "M4 5h16a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-10a2 2 0 0 1 2 -2z",
            "M2 10L22 10",
            "M6 14h2"
        )
    }

    /** lucide: message-circle */
    val Mensaje: ImageVector by lazy {
        lucide(
            "Mensaje",
            "M2.992 16.342a2 2 0 0 1 .094 1.167l-1.065 3.29a1 1 0 0 0 1.236 1.168l3.413-.998a2 2 0 0 1 1.099.092 10 10 0 1 0-4.777-4.719"
        )
    }

    /** lucide: phone */
    val Telefono: ImageVector by lazy {
        lucide(
            "Telefono",
            "M13.832 16.568a1 1 0 0 0 1.213-.303l.355-.465A2 2 0 0 1 17 15h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2A18 18 0 0 1 2 4a2 2 0 0 1 2-2h3a2 2 0 0 1 2 2v3a2 2 0 0 1-.8 1.6l-.468.351a1 1 0 0 0-.292 1.233 14 14 0 0 0 6.392 6.384"
        )
    }

    /** lucide: wifi-off */
    val SinWifi: ImageVector by lazy {
        lucide(
            "SinWifi",
            "M12 20h.01",
            "M8.5 16.429a5 5 0 0 1 7 0",
            "M5 12.859a10 10 0 0 1 5.17-2.69",
            "M19 12.859a10 10 0 0 0-2.007-1.523",
            "M2 8.82a15 15 0 0 1 4.177-2.643",
            "M22 8.82a15 15 0 0 0-11.288-3.764",
            "m2 2 20 20"
        )
    }

    /** lucide: refresh-cw */
    val Recargar: ImageVector by lazy {
        lucide(
            "Recargar",
            "M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8",
            "M21 3v5h-5",
            "M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16",
            "M8 16H3v5"
        )
    }

    /** lucide: calendar-x */
    val CalendarioVacio: ImageVector by lazy {
        lucide(
            "CalendarioVacio",
            "M8 2v3",
            "M16 2v3",
            "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2z",
            "M3 9h18",
            "m14 13-4 4",
            "m10 13 4 4"
        )
    }

    /** lucide: history */
    val Historial: ImageVector by lazy {
        lucide(
            "Historial",
            "M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8",
            "M3 3v5h5",
            "M12 7v5l4 2"
        )
    }

    /** lucide: hand-heart */
    val ManoCorazon: ImageVector by lazy {
        lucide(
            "ManoCorazon",
            "M11 14h2a2 2 0 0 0 0-4h-3c-.6 0-1.1.2-1.4.6L3 16",
            "m14.45 13.39 5.05-4.694C20.196 8 21 6.85 21 5.75a2.75 2.75 0 0 0-4.797-1.837.276.276 0 0 1-.406 0A2.75 2.75 0 0 0 11 5.75c0 1.2.802 2.248 1.5 2.946L16 11.95",
            "m2 15 6 6",
            "m7 20 1.6-1.4c.3-.4.8-.6 1.4-.6h4c1.1 0 2.1-.4 2.8-1.2l4.6-4.4a1 1 0 0 0-2.75-2.91"
        )
    }

    /** lucide: house */
    val Casa: ImageVector by lazy {
        lucide(
            "Casa",
            "M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8",
            "M3 10a2 2 0 0 1 .709-1.528l7-6a2 2 0 0 1 2.582 0l7 6A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"
        )
    }

    /** lucide: lightbulb */
    val Foco: ImageVector by lazy {
        lucide(
            "Foco",
            "M15 14c.2-1 .7-1.7 1.5-2.5 1-.9 1.5-2.2 1.5-3.5A6 6 0 0 0 6 8c0 1 .2 2.2 1.5 3.5.7.7 1.3 1.5 1.5 2.5",
            "M9 18h6",
            "M10 22h4"
        )
    }

    /** lucide: check */
    val Palomita: ImageVector by lazy {
        lucide(
            "Palomita",
            "M20 6 9 17l-5-5"
        )
    }
}

private fun lucide(nombre: String, vararg trazos: String): ImageVector =
    ImageVector.Builder(
        name = nombre,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        trazos.forEach { trazo ->
            addPath(
                pathData = addPathNodes(trazo),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
        }
    }.build()
