package mx.tec.familiasquesuman.ui.screens.sugerencias

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.domain.TipoSugerencia
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraAccionesInferior
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraConRegreso
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPrimario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonSecundario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.CampoTexto
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorRojo
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/** El ícono de cada tipo es el mismo de su pestaña en la barra de abajo. */
internal fun iconoDe(tipo: TipoSugerencia): ImageVector = when (tipo) {
    TipoSugerencia.ACTIVIDAD -> IconosWeb.Personas
    TipoSugerencia.CAMPANA -> IconosWeb.ManoCorazon
    TipoSugerencia.PROYECTO -> IconosWeb.Foco
    TipoSugerencia.CENTRO -> IconosWeb.Ubicacion
}

/**
 * "Sugerir": cualquier familia propone una actividad, una campaña para aportar, un proyecto
 * o un centro para el directorio. Se llega con el tipo ya elegido según la lista de donde
 * vino. Al enviar, queda en la bandeja del admin para revisarla antes de publicar.
 */
@Composable
fun SugerirScreen(
    formulario: FormularioSugerencia,
    onCambio: ((FormularioSugerencia) -> FormularioSugerencia) -> Unit,
    onEnviar: () -> Unit,
    onSugerirOtra: () -> Unit,
    onRegresar: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Fondo).imePadding()) {
        BarraConRegreso("Sugerir", onRegresar)
        if (formulario.enviada) {
            SugerenciaEnviada(formulario.tipo, onSugerirOtra, onRegresar)
            return@Column
        }
        val f = formulario
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(if (f.enviando) 0.5f else 1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "¿Conoces algo que debería estar en Familias que Suman? Cuéntanos y el equipo lo " +
                    "revisa antes de publicarlo.",
                style = MaterialTheme.typography.bodyLarge,
                color = TintaSuave
            )

            Titulo("¿Qué quieres sugerir?")
            TipoSugerencia.entries.chunked(2).forEach { par ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    par.forEach { tipo ->
                        OpcionTipo(
                            tipo = tipo,
                            elegida = tipo == f.tipo,
                            onClick = { onCambio { it.copy(tipo = tipo) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Titulo("Sobre ${f.tipo.articulo}")
            CampoTexto(
                etiqueta = "Nombre *",
                valor = f.nombre,
                onValorChange = { v -> onCambio { it.copy(nombre = v) } },
                placeholder = "Ej. ${ejemploDeNombre(f.tipo)}",
                error = f.errorNombre,
                habilitado = !f.enviando
            )
            CampoLargo(
                etiqueta = "¿De qué se trata? *",
                valor = f.descripcion,
                onValorChange = { v -> onCambio { it.copy(descripcion = v) } },
                placeholder = "Qué hacen, por qué vale la pena y cómo pueden ayudar las familias",
                error = f.errorDescripcion,
                habilitado = !f.enviando
            )
            CampoTexto(
                etiqueta = f.tipo.campoExtra,
                valor = f.detalleExtra,
                onValorChange = { v -> onCambio { it.copy(detalleExtra = v) } },
                placeholder = f.tipo.ejemploExtra,
                habilitado = !f.enviando
            )
            CampoTexto(
                etiqueta = "¿Quién lo organiza? (opcional)",
                valor = f.organizacion,
                onValorChange = { v -> onCambio { it.copy(organizacion = v) } },
                placeholder = "Asociación, escuela, parroquia…",
                habilitado = !f.enviando
            )
            CampoTexto(
                etiqueta = "Ciudad",
                valor = f.ciudad,
                onValorChange = { v -> onCambio { it.copy(ciudad = v) } },
                habilitado = !f.enviando
            )
            CampoTexto(
                etiqueta = "Teléfono, WhatsApp o enlace (opcional)",
                valor = f.contactoCausa,
                onValorChange = { v -> onCambio { it.copy(contactoCausa = v) } },
                placeholder = "Para que el equipo pueda contactarlos",
                habilitado = !f.enviando
            )

            Titulo("Tus datos")
            CampoTexto(
                etiqueta = "Tu nombre *",
                valor = f.tuNombre,
                onValorChange = { v -> onCambio { it.copy(tuNombre = v) } },
                error = f.errorTuNombre,
                habilitado = !f.enviando
            )
            CampoTexto(
                etiqueta = "Tu correo o teléfono *",
                valor = f.tuContacto,
                onValorChange = { v -> onCambio { it.copy(tuContacto = v) } },
                error = f.errorTuContacto,
                habilitado = !f.enviando
            )
            Text(
                "Solo lo usamos para avisarte si se publica o si tenemos dudas.",
                style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave
            )
        }
        BarraAccionesInferior {
            BotonPrimario(
                texto = if (f.enviando) "Enviando…" else "Enviar sugerencia",
                onClick = onEnviar,
                cargando = f.enviando
            )
        }
    }
}

private fun ejemploDeNombre(tipo: TipoSugerencia) = when (tipo) {
    TipoSugerencia.ACTIVIDAD -> "Tarde de juegos en el asilo"
    TipoSugerencia.CAMPANA -> "Útiles para regreso a clases"
    TipoSugerencia.PROYECTO -> "Tutorías de lectura"
    TipoSugerencia.CENTRO -> "Casa Hogar Nuevo Amanecer"
}

@Composable
private fun Titulo(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.titleMedium,
        color = Tinta,
        modifier = Modifier.padding(top = 6.dp)
    )
}

/** Una de las cuatro opciones de tipo, como tarjeta que se marca en azul. */
@Composable
private fun OpcionTipo(tipo: TipoSugerencia, elegida: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .heightIn(min = 92.dp)
            .clip(forma)
            .background(if (elegida) MarcaAzul.copy(alpha = 0.08f) else Superficie)
            .border(if (elegida) 2.dp else 1.dp, if (elegida) MarcaAzul else Borde, forma)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(iconoDe(tipo), contentDescription = null, tint = if (elegida) MarcaAzul else TintaSuave, modifier = Modifier.size(22.dp))
        Text(
            tipo.etiqueta,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
            color = if (elegida) MarcaAzul else Tinta
        )
    }
}

/** Como CampoTexto, pero de varias líneas. */
@Composable
private fun CampoLargo(
    etiqueta: String,
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    habilitado: Boolean
) {
    Column(Modifier.fillMaxWidth()) {
        Text(etiqueta, style = MaterialTheme.typography.labelLarge, color = Tinta)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            enabled = habilitado,
            isError = error != null,
            placeholder = { Text(placeholder, color = TintaSuave.copy(alpha = 0.8f)) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, color = Tinta),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Superficie,
                unfocusedContainerColor = Superficie,
                disabledContainerColor = Fondo,
                errorContainerColor = ErrorFondo.copy(alpha = 0.45f),
                focusedBorderColor = MarcaAzul,
                unfocusedBorderColor = Borde,
                disabledBorderColor = Borde,
                errorBorderColor = ErrorRojo,
                cursorColor = MarcaAzul
            )
        )
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error, style = MaterialTheme.typography.bodyMedium, color = ErrorRojo)
        }
    }
}

/** El "gracias" después de enviar. */
@Composable
private fun SugerenciaEnviada(tipo: TipoSugerencia, onSugerirOtra: () -> Unit, onRegresar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
    ) {
        Box(
            Modifier.size(80.dp).clip(CircleShape).background(ConfirmadoFondo),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = ConfirmadoTexto, modifier = Modifier.size(40.dp))
        }
        Text("¡Gracias por sumar!", style = MaterialTheme.typography.headlineMedium, color = Tinta, textAlign = TextAlign.Center)
        Text(
            "Recibimos tu sugerencia de ${tipo.articulo}. El equipo de Familias que Suman la va a " +
                "revisar y, si se publica, te avisamos.",
            style = MaterialTheme.typography.bodyLarge,
            color = TintaSuave,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        BotonPrimario("Regresar", onClick = onRegresar)
        BotonSecundario("Sugerir algo más", onClick = onSugerirOtra)
    }
}

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun SugerirPreview() {
    FamiliasQueSumanTheme {
        SugerirScreen(
            FormularioSugerencia(tipo = TipoSugerencia.CENTRO, tuNombre = "Ana Rodríguez", intentoEnviar = true),
            {}, {}, {}, {}
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun SugerenciaEnviadaPreview() {
    FamiliasQueSumanTheme {
        SugerirScreen(FormularioSugerencia(enviada = true), {}, {}, {}, {})
    }
}
