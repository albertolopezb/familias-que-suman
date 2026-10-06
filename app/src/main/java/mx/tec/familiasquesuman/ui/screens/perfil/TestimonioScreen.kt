package mx.tec.familiasquesuman.ui.screens.perfil

import mx.tec.familiasquesuman.ui.components.BarraSuperior
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.*

private val IconoFoto = ImageVector.Builder("Foto", 24.dp, 24.dp, 24f, 24f).apply {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f) {
        moveTo(3f, 3f); lineTo(21f, 3f); lineTo(21f, 21f); lineTo(3f, 21f); close()
        moveTo(3f, 14f); lineTo(8f, 10f); lineTo(14f, 16f); lineTo(18f, 12f); lineTo(21f, 15f)
        moveTo(8f, 7f); lineTo(8.1f, 7f)
    }
}.build()

@Composable
fun TestimonioScreen(
    actividad: UiState<Actividad>,
    formulario: FormularioTestimonio,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    onExperienciaChange: (String) -> Unit,
    onAgregarFoto: () -> Unit,
    onEnviar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().imePadding().background(Fondo)) {
        BarraSuperior("Compartir testimonio", onRegresar = onVolver)
        when (actividad) {
            UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(actividad.mensaje, onReintentar)
            is UiState.Exito -> {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("${actividad.datos.titulo} · ${actividad.datos.fecha}",
                        style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
                    Surface(onClick = onAgregarFoto, enabled = !formulario.revisandoFoto,
                        shape = RoundedCornerShape(16.dp), color = Superficie,
                        modifier = Modifier.fillMaxWidth().drawWithContent {
                            drawContent()
                            drawRoundRect(TintaSuave, cornerRadius = CornerRadius(16.dp.toPx()),
                                style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(7.dp.toPx(), 5.dp.toPx()))))
                        }) {
                        Column(Modifier.heightIn(min = 160.dp).padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
                            Icon(IconoFoto, contentDescription = null, tint = TintaSuave, modifier = Modifier.size(24.dp))
                            Text(when {
                                formulario.revisandoFoto -> "Revisando foto…"
                                formulario.foto != null -> "Foto seleccionada · Cambiar foto"
                                else -> "Agregar una foto"
                            }, style = MaterialTheme.typography.titleLarge, color = MarcaAzul)
                            Text("Máximo 1 foto, hasta 5 MB", style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                        }
                    }
                    Text("TU EXPERIENCIA", style = MaterialTheme.typography.labelSmall, color = TintaSuave)
                    OutlinedTextField(value = formulario.experiencia, onValueChange = onExperienciaChange,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 132.dp),
                        placeholder = { Text("Comparte tu experiencia") },
                        shape = RoundedCornerShape(14.dp), textStyle = MaterialTheme.typography.bodyLarge,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Borde,
                            unfocusedContainerColor = Superficie, focusedContainerColor = Superficie))
                    Surface(shape = RoundedCornerShape(16.dp), color = AcentoSuave) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = AcentoTexto, modifier = Modifier.size(18.dp))
                            Text("Familias que Suman revisa cada testimonio antes de publicarlo. Nada aparece sin su aprobación.",
                                style = MaterialTheme.typography.bodyMedium, color = AcentoTexto)
                        }
                    }
                    formulario.mensaje?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                    }
                }
                Surface(color = Superficie, shadowElevation = 4.dp) {
                    Button(onClick = onEnviar, modifier = Modifier.fillMaxWidth().padding(14.dp).heightIn(min = 56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MarcaOro, contentColor = MarcaAzul)) {
                        Text("Enviar a revisión", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun TestimonioErrorPreview() {
    FamiliasQueSumanTheme {
        TestimonioScreen(UiState.Error("La actividad no está disponible."), FormularioTestimonio(), {}, {}, {}, {}, {})
    }
}
