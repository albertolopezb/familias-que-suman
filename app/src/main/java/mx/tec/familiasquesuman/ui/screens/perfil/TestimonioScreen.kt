package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Participacion
import mx.tec.familiasquesuman.domain.Testimonio
import mx.tec.familiasquesuman.ui.components.BarraSuperior
import mx.tec.familiasquesuman.ui.components.CargandoView
import mx.tec.familiasquesuman.ui.components.ErrorView
import mx.tec.familiasquesuman.ui.components.FotoUsuario
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.theme.*

private val IconoFoto = ImageVector.Builder("Foto", 24.dp, 24.dp, 24f, 24f).apply {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f) {
        moveTo(3f, 3f); lineTo(21f, 3f); lineTo(21f, 21f); lineTo(3f, 21f); close()
        moveTo(3f, 14f); lineTo(8f, 10f); lineTo(14f, 16f); lineTo(18f, 12f); lineTo(21f, 15f)
        moveTo(8f, 7f); lineTo(8.1f, 7f)
    }
}.build()

/**
 * RF-12: compartir una foto y un testimonio breve de una actividad. Se publica en cuanto se
 * envía y quien lo escribió puede eliminarlo cuando quiera.
 *
 * Según el caso muestra el formulario (primera vez), el aviso de "publicado" o el testimonio
 * que la cuenta ya publicó, con la opción de eliminarlo.
 */
@Composable
fun TestimonioScreen(
    actividad: UiState<Participacion>,
    formulario: FormularioTestimonio,
    existente: Testimonio?,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    onExperienciaChange: (String) -> Unit,
    onAgregarFoto: () -> Unit,
    onEnviar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().imePadding().background(Fondo)) {
        BarraSuperior("Compartir testimonio", onRegresar = onVolver)
        when (actividad) {
            UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(actividad.mensaje, onReintentar)
            is UiState.Exito -> when {
                formulario.enviado -> TestimonioEnviado(onListo = onVolver)
                existente != null -> TestimonioPublicado(existente, formulario.mensaje, onEliminar, onListo = onVolver)
                else -> FormularioDeTestimonio(
                    actividad.datos, formulario, onExperienciaChange, onAgregarFoto, onEnviar
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.FormularioDeTestimonio(
    actividad: Participacion,
    formulario: FormularioTestimonio,
    onExperienciaChange: (String) -> Unit,
    onAgregarFoto: () -> Unit,
    onEnviar: () -> Unit
) {
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("${actividad.tituloActividad} · ${actividad.fecha}",
            style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
        val fotoParaMostrar = formulario.foto?.toString()
        Surface(onClick = onAgregarFoto, enabled = !formulario.revisandoFoto,
            shape = RoundedCornerShape(16.dp), color = Superficie,
            modifier = Modifier.fillMaxWidth().drawWithContent {
                drawContent()
                if (fotoParaMostrar == null) {
                    drawRoundRect(TintaSuave, cornerRadius = CornerRadius(16.dp.toPx()),
                        style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(7.dp.toPx(), 5.dp.toPx()))))
                }
            }) {
            if (fotoParaMostrar != null) {
                Box {
                    FotoUsuario(fotoParaMostrar, "Foto de tu testimonio",
                        Modifier.fillMaxWidth().height(220.dp))
                    Text(if (formulario.revisandoFoto) "Revisando foto…" else "Cambiar foto",
                        style = MaterialTheme.typography.labelLarge, color = Color.White,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp).clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = 14.dp, vertical = 8.dp))
                }
            } else {
                Column(Modifier.heightIn(min = 160.dp).padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
                    Icon(IconoFoto, contentDescription = null, tint = TintaSuave, modifier = Modifier.size(24.dp))
                    Text(if (formulario.revisandoFoto) "Revisando foto…" else "Agregar una foto",
                        style = MaterialTheme.typography.titleLarge, color = MarcaAzul)
                    Text("Máximo 1 foto, hasta 5 MB", style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                }
            }
        }
        Text("TU EXPERIENCIA", style = MaterialTheme.typography.labelSmall, color = TintaSuave)
        OutlinedTextField(value = formulario.experiencia, onValueChange = onExperienciaChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 132.dp),
            placeholder = { Text("Comparte tu experiencia") },
            supportingText = {
                Text("${formulario.experiencia.trim().length}/$MAX_LETRAS_TESTIMONIO · mínimo $MIN_LETRAS_TESTIMONIO letras",
                    style = MaterialTheme.typography.bodyMedium)
            },
            shape = RoundedCornerShape(14.dp), textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Borde,
                unfocusedContainerColor = Superficie, focusedContainerColor = Superficie))
        Surface(shape = RoundedCornerShape(16.dp), color = AcentoSuave) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = AcentoTexto, modifier = Modifier.size(18.dp))
                Text("Tu testimonio se publica en cuanto lo envíes, con el nombre de tu familia. Puedes eliminarlo cuando quieras.",
                    style = MaterialTheme.typography.bodyMedium, color = AcentoTexto)
            }
        }
        formulario.mensaje?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = ErrorTexto)
        }
    }
    Surface(color = Superficie, shadowElevation = 4.dp) {
        Button(onClick = onEnviar, enabled = !formulario.enviando,
            modifier = Modifier.fillMaxWidth().padding(14.dp).heightIn(min = 56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MarcaOro, contentColor = MarcaAzul)) {
            if (formulario.enviando) {
                CircularProgressIndicator(Modifier.size(20.dp), color = MarcaAzul, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Enviando…", style = MaterialTheme.typography.titleLarge)
            } else {
                Text("Publicar testimonio", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun ColumnScope.TestimonioEnviado(onListo: () -> Unit) {
    Column(Modifier.weight(1f).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
        Box(Modifier.size(64.dp).clip(RoundedCornerShape(50)).background(ConfirmadoFondo), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Confirmado, modifier = Modifier.size(32.dp))
        }
        Text("Tu testimonio ya está publicado", style = MaterialTheme.typography.headlineMedium, color = Tinta, textAlign = TextAlign.Center)
        Text("Ya aparece en la sección de testimonios. Puedes eliminarlo cuando quieras desde Mis actividades.",
            style = MaterialTheme.typography.bodyLarge, color = TintaSuave, textAlign = TextAlign.Center)
    }
    Surface(color = Superficie, shadowElevation = 4.dp) {
        Button(onClick = onListo, modifier = Modifier.fillMaxWidth().padding(14.dp).heightIn(min = 56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MarcaOro, contentColor = MarcaAzul)) {
            Text("Listo", style = MaterialTheme.typography.titleLarge)
        }
    }
}

/** El testimonio que la cuenta ya publicó: se puede consultar y eliminar (después podrá compartir otro). */
@Composable
private fun ColumnScope.TestimonioPublicado(
    testimonio: Testimonio,
    mensaje: String?,
    onEliminar: () -> Unit,
    onListo: () -> Unit
) {
    var confirmando by remember { mutableStateOf(false) }
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("${testimonio.actividadTitulo} · ${testimonio.fecha}", style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
        Surface(shape = RoundedCornerShape(16.dp), color = ConfirmadoFondo) {
            Text("Publicado: ya aparece en la sección de testimonios.",
                style = MaterialTheme.typography.bodyLarge, color = ConfirmadoTexto, modifier = Modifier.padding(16.dp))
        }
        testimonio.foto?.let { FotoUsuario(it, "Foto del testimonio", Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))) }
        Text(testimonio.experiencia, style = MaterialTheme.typography.bodyLarge, color = Tinta)
        mensaje?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = ErrorTexto) }
    }
    Surface(color = Superficie, shadowElevation = 4.dp) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { confirmando = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, ErrorRojo)) {
                Text("Eliminar mi testimonio", style = MaterialTheme.typography.titleMedium, color = ErrorRojo)
            }
            OutlinedButton(onClick = onListo, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(14.dp)) {
                Text("Volver", style = MaterialTheme.typography.titleMedium, color = MarcaAzul)
            }
        }
    }
    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text("¿Eliminar tu testimonio?") },
            text = { Text("Dejará de aparecer en la sección de testimonios. Después podrás compartir otro de esta actividad.") },
            confirmButton = {
                TextButton(onClick = { confirmando = false; onEliminar() }) { Text("Sí, eliminar", color = ErrorRojo) }
            },
            dismissButton = { TextButton(onClick = { confirmando = false }) { Text("Cancelar") } }
        )
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun TestimonioErrorPreview() {
    FamiliasQueSumanTheme {
        TestimonioScreen(UiState.Error("La actividad no está disponible."), FormularioTestimonio(), null, {}, {}, {}, {}, {}, {})
    }
}
