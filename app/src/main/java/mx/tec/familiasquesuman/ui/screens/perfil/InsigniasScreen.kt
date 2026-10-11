package mx.tec.familiasquesuman.ui.screens.perfil

import mx.tec.familiasquesuman.ui.components.BarraSuperior
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Impacto
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.screens.perfil.componentes.Insignia
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.*

// Vectores locales para los iconos de P-19, sin dependencias adicionales.
private val Medalla = ImageVector.Builder("Medalla", 24.dp, 24.dp, 24f, 24f).apply {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
        moveTo(17f, 8f); curveTo(17f, 14.7f, 7f, 14.7f, 7f, 8f)
        curveTo(7f, 1.3f, 17f, 1.3f, 17f, 8f); close()
        moveTo(8.5f, 12f); lineTo(7.5f, 21f); lineTo(12f, 18.5f)
        lineTo(16.5f, 21f); lineTo(15.5f, 12f)
    }
}.build()
private val Regalo = ImageVector.Builder("Regalo", 24.dp, 24.dp, 24f, 24f).apply {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
        moveTo(3f, 5f); lineTo(21f, 5f); lineTo(21f, 9f); lineTo(3f, 9f); close()
        moveTo(5f, 9f); lineTo(5f, 20f); lineTo(19f, 20f); lineTo(19f, 9f)
        moveTo(12f, 5f); lineTo(12f, 20f)
    }
}.build()
private val FamiliaIcono = ImageVector.Builder("Familia", 24.dp, 24.dp, 24f, 24f).apply {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
        moveTo(12f, 7f); curveTo(12f, 12.3f, 4f, 12.3f, 4f, 7f)
        curveTo(4f, 1.7f, 12f, 1.7f, 12f, 7f); close()
        moveTo(2f, 21f); curveTo(2f, 11f, 14f, 11f, 14f, 21f)
        moveTo(16f, 3f); curveTo(22f, 3f, 22f, 11f, 16f, 11f)
        moveTo(17f, 14f); curveTo(21f, 14f, 22f, 17f, 22f, 21f)
    }
}.build()

@Composable
fun InsigniasScreen(
    estado: UiState<DatosInsignias>,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().background(Fondo)) {
        BarraSuperior("Reconocimiento", onRegresar = onVolver)
        when (estado) {
            UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(estado.mensaje, onReintentar)
            is UiState.Exito -> ContenidoInsignias(estado.datos)
        }
    }
}

private val Megafono = ImageVector.Builder("Invitar", 24.dp, 24.dp, 24f, 24f).apply {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
        moveTo(3f, 10f); lineTo(3f, 14f); lineTo(7f, 14f); lineTo(15f, 19f); lineTo(15f, 5f); lineTo(7f, 10f); close()
        moveTo(18f, 9f); curveTo(19.5f, 10.5f, 19.5f, 13.5f, 18f, 15f)
        moveTo(7f, 14f); lineTo(8.5f, 20f)
    }
}.build()

@Composable
private fun ContenidoInsignias(datos: DatosInsignias) {
    val nivel = datos.nivel
    val obtenidas = datos.insignias.count { it.obtenida }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(shape = RoundedCornerShape(18.dp), color = MarcaAzul) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("NIVEL ACTUAL", style = MaterialTheme.typography.labelSmall, color = MarcaOro)
                Text(nivel.nombre, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(
                    if (datos.impacto.actividadesRealizadas == 1) "1 actividad completada"
                    else "${datos.impacto.actividadesRealizadas} actividades completadas",
                    style = MaterialTheme.typography.bodyLarge, color = Color(0xFFBED0EA)
                )
                if (nivel.siguiente != null) {
                    LinearProgressIndicator(
                        progress = { nivel.actual.toFloat() / nivel.meta },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MarcaOro, trackColor = Color.White.copy(alpha = 0.2f), gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                    val faltan = nivel.meta - nivel.actual
                    val cuantas = if (faltan == 1) "Te falta 1 actividad" else "Te faltan $faltan actividades"
                    Text("$cuantas para ${nivel.siguiente}", style = MaterialTheme.typography.bodyMedium,
                        color = Color.White)
                } else {
                    Text("Llegaron al nivel más alto. Gracias por sumar.",
                        style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }
        }
        Text("TUS INSIGNIAS · $obtenidas DE ${datos.insignias.size}", style = MaterialTheme.typography.labelSmall,
            color = TintaSuave)
        datos.insignias.chunked(2).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Max)) {
                fila.forEach { insignia ->
                    val icono = when (insignia.tipo) {
                        TipoInsignia.PRIMERA_VEZ -> Medalla
                        TipoInsignia.MANOS_LLENAS -> Regalo
                        TipoInsignia.EN_FAMILIA -> FamiliaIcono
                        TipoInsignia.ANO_COMPLETO -> Icons.Outlined.DateRange
                        TipoInsignia.EMBAJADORA -> Megafono
                    }
                    Insignia(insignia.titulo, insignia.descripcion, icono, insignia.actual, insignia.meta,
                        Modifier.weight(1f).fillMaxHeight())
                }
                if (fila.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Text("Compartan una actividad con otra familia para ganar la de Familia que invita.",
            style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun InsigniasPreview() {
    FamiliasQueSumanTheme {
        InsigniasScreen(UiState.Exito(DatosInsignias(Impacto(4, 10, 2), nivelPara(4), listOf(
            EstadoInsignia(TipoInsignia.PRIMERA_VEZ, "Primera vez", "Tu primera actividad", 1, 1),
            EstadoInsignia(TipoInsignia.MANOS_LLENAS, "Manos llenas", "5 campañas apoyadas", 2, 5)
        ))), {}, {})
    }
}
