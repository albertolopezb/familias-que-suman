package mx.tec.familiasquesuman.ui.screens.perfil

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
import mx.tec.familiasquesuman.domain.EstadoTestimonio
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
 * RF-12: compartir una foto y un testimonio breve de una actividad. Lo que se envía queda en
 * revisión y no se publica hasta que Familias que Suman lo apruebe.
 *
 * Según el caso muestra el formulario (primera vez, o cuando pidieron ajustes), el aviso de
 * "enviado" o el estado de un testimonio que ya se mandó.
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
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().imePadding().background(Fondo)) {
        BarraSuperior("Compartir testimonio", onRegresar = onVolver)
        when (actividad) {
            UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(actividad.mensaje, onReintentar)
            is UiState.Exito -> when {
                formulario.enviado -> TestimonioEnviado(onListo = onVolver)
                existente != null && existente.estado != EstadoTestimonio.AJUSTAR ->
                    TestimonioYaEnviado(existente, onListo = onVolver)
                else -> FormularioDeTestimonio(
                    actividad.datos, formulario, existente?.nota.orEmpty(),
                    onExperienciaChange, onAgregarFoto, onEnviar
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.FormularioDeTestimonio(
    actividad: Participacion,
    formulario: FormularioTestimonio,
    notaDeRevision: String,
    onExperienciaChange: (String) -> Unit,
    onAgregarFoto: () -> Unit,
    onEnviar: () -> Unit
) {
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("${actividad.tituloActividad} · ${actividad.fecha}",
            style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
        if (notaDeRevision.isNotBlank()) {
            Surface(shape = RoundedCornerShape(16.dp), color = AcentoSuave) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Familias que Suman te pide un ajuste", style = MaterialTheme.typography.titleMedium, color = AcentoTexto)
                    Text(notaDeRevision, style = MaterialTheme.typography.bodyLarge, color = AcentoTexto)
                }
            }
        }
        val fotoParaMostrar = formulario.foto?.toString() ?: formulario.fotoGuardada
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
                Text("Familias que Suman revisa cada testimonio antes de publicarlo. Nada aparece sin su aprobación.",
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
                Text("Enviar a revisión", style = MaterialTheme.typography.titleLarge)
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
        Text("Recibimos tu testimonio", style = MaterialTheme.typography.headlineMedium, color = Tinta, textAlign = TextAlign.Center)
        Text("Está en revisión. Si lo aprobamos, aparecerá en la sección de testimonios; " +
            "si necesita un ajuste te lo diremos en Mis actividades.",
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

/** Lo que se ve de un testimonio que ya se mandó: no se vuelve a enviar, solo se consulta su estado. */
@Composable
private fun ColumnScope.TestimonioYaEnviado(testimonio: Testimonio, onListo: () -> Unit) {
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("${testimonio.actividadTitulo} · ${testimonio.fecha}", style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
        val (fondo, texto, aviso) = when (testimonio.estado) {
            EstadoTestimonio.APROBADO -> Triple(ConfirmadoFondo, ConfirmadoTexto, "Aprobado: ya aparece en la sección de testimonios.")
            EstadoTestimonio.DESCARTADO -> Triple(ErrorFondo, ErrorTexto, "No se publicó. Si tienes dudas, escribe a Familias que Suman.")
            else -> Triple(AcentoSuave, AcentoTexto, "En revisión: nada se publica sin la aprobación de Familias que Suman.")
        }
        Surface(shape = RoundedCornerShape(16.dp), color = fondo) {
            Text(aviso, style = MaterialTheme.typography.bodyLarge, color = texto, modifier = Modifier.padding(16.dp))
        }
        testimonio.foto?.let { FotoUsuario(it, "Foto del testimonio", Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))) }
        Text(testimonio.experiencia, style = MaterialTheme.typography.bodyLarge, color = Tinta)
    }
    Surface(color = Superficie, shadowElevation = 4.dp) {
        OutlinedButton(onClick = onListo, modifier = Modifier.fillMaxWidth().padding(14.dp).heightIn(min = 56.dp),
            shape = RoundedCornerShape(14.dp)) {
            Text("Volver", style = MaterialTheme.typography.titleLarge, color = MarcaAzul)
        }
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun TestimonioErrorPreview() {
    FamiliasQueSumanTheme {
        TestimonioScreen(UiState.Error("La actividad no está disponible."), FormularioTestimonio(), null, {}, {}, {}, {}, {})
    }
}
