package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.domain.Aviso
import mx.tec.familiasquesuman.domain.TipoAviso
import mx.tec.familiasquesuman.domain.haceCuanto
import mx.tec.familiasquesuman.ui.components.BarraSuperior
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.CirculoDeIcono
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.AzulCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.AzulTarjetaFondo
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.theme.MentaCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.MentaTarjetaFondo
import mx.tec.familiasquesuman.ui.theme.MoradoCategoriaTexto
import mx.tec.familiasquesuman.ui.theme.MoradoTarjetaFondo
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * Mi perfil → Notificaciones: lo que le ha llegado a la familia. Las que no habían visto salen
 * arriba, en "Nuevas"; al abrir la pantalla se dan por vistas y el número del círculo se quita.
 */
@Composable
fun NotificacionesScreen(
    avisos: List<Aviso>,
    /** Las que estaban sin leer al abrir la pantalla. */
    nuevas: Set<String>,
    ahora: Long,
    onAviso: (Aviso) -> Unit,
    onAjustes: () -> Unit,
    onRegresar: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Fondo)) {
        BarraSuperior("Notificaciones", onRegresar) {
            IconButton(onClick = onAjustes) {
                Icon(IconosWeb.Engrane, contentDescription = "Elegir qué notificaciones reciben", tint = MarcaAzul)
            }
        }
        if (avisos.isEmpty()) {
            SinAvisos(onAjustes)
            return@Column
        }
        val (recientes, anteriores) = avisos.partition { it.id in nuevas }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (recientes.isNotEmpty()) {
                item(key = "t-nuevas") { Etiqueta("NUEVAS") }
                items(recientes, key = { it.id }) { FilaAviso(it, nueva = true, ahora, onClick = { onAviso(it) }) }
            }
            if (anteriores.isNotEmpty()) {
                if (recientes.isNotEmpty()) item(key = "t-anteriores") { Etiqueta("ANTERIORES") }
                items(anteriores, key = { it.id }) { FilaAviso(it, nueva = false, ahora, onClick = { onAviso(it) }) }
            }
            item(key = "ajustes") {
                TextButton(onClick = onAjustes, modifier = Modifier.fillMaxWidth()) {
                    Text("Elegir qué notificaciones reciben", color = MarcaAzul, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, letterSpacing = 0.8.sp, fontWeight = FontWeight.Bold),
        color = TintaSuave,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun FilaAviso(aviso: Aviso, nueva: Boolean, ahora: Long, onClick: () -> Unit) {
    val (icono, color, fondo) = estilo(aviso.tipo)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (nueva) Color(0xFFFFFBEB) else Superficie,
        border = BorderStroke(1.dp, if (nueva) MarcaOro.copy(alpha = 0.45f) else Borde)
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(fondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        aviso.titulo,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        color = Tinta,
                        modifier = Modifier.weight(1f)
                    )
                    Text(haceCuanto(aviso.creado, ahora), style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                    if (nueva) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(MarcaOro))
                    }
                }
                Text(aviso.texto, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
            }
        }
    }
}

/** Ícono, color y fondo de cada tipo de aviso. */
private fun estilo(tipo: TipoAviso): Triple<ImageVector, Color, Color> = when (tipo) {
    TipoAviso.BIENVENIDA -> Triple(IconosWeb.ManosCorazon, MentaCategoriaTexto, MentaTarjetaFondo)
    TipoAviso.INSCRIPCION -> Triple(IconosWeb.CirculoPalomita, ConfirmadoTexto, ConfirmadoFondo)
    TipoAviso.CANCELACION -> Triple(IconosWeb.CalendarioVacio, TintaSuave, Borde)
    TipoAviso.RECORDATORIO -> Triple(IconosWeb.Reloj, AzulCategoriaTexto, AzulTarjetaFondo)
    TipoAviso.FAVORITO -> Triple(IconosWeb.Corazon, MoradoCategoriaTexto, MoradoTarjetaFondo)
    TipoAviso.CIERRE -> Triple(IconosWeb.Calendario, AcentoTexto, AcentoSuave)
    TipoAviso.URGENCIA -> Triple(IconosWeb.Personas, ErrorTexto, ErrorFondo)
}

@Composable
private fun SinAvisos(onAjustes: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CirculoDeIcono(IconosWeb.Campana, color = MarcaAzul, fondo = AcentoSuave)
        Text("No tienen notificaciones", style = MaterialTheme.typography.titleMedium, color = Tinta, textAlign = TextAlign.Center)
        Text(
            "Aquí verán los recordatorios de sus actividades, sus inscripciones y lo nuevo de sus favoritos.",
            style = MaterialTheme.typography.bodyMedium,
            color = TintaSuave,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onAjustes) {
            Text("Elegir qué notificaciones reciben", color = MarcaAzul, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 760)
@Composable
private fun NotificacionesPreview() {
    val ahora = System.currentTimeMillis()
    val avisos = listOf(
        Aviso("1", TipoAviso.RECORDATORIO, "Mañana tienen actividad", "Clasificar ropa de invierno, a las 10:00 en Albergue Nuevo Amanecer.", ahora - 120_000),
        Aviso("2", TipoAviso.INSCRIPCION, "Ya están inscritos", "Van 2 personas a Clasificar ropa de invierno, el sábado 17 de octubre a las 10:00.", ahora - 7_200_000),
        Aviso("3", TipoAviso.CIERRE, "Suma a su Mesa cierra pronto", "Reciben donaciones hasta el 30 de octubre. Faltan 19 días.", ahora - 90_000_000, leido = true)
    )
    FamiliasQueSumanTheme { NotificacionesScreen(avisos, setOf("1", "2"), ahora, {}, {}, {}) }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 500, name = "Sin notificaciones")
@Composable
private fun SinNotificacionesPreview() {
    FamiliasQueSumanTheme { NotificacionesScreen(emptyList(), emptySet(), 0, {}, {}, {}) }
}
