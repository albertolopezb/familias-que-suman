package mx.tec.familiasquesuman.ui.screens.inscripcion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.AvisoExito
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraAccionesInferior
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraConRegreso
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPrimario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonSecundario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.CampoTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/** P-27 · Recuperar contraseña. El mensaje es vago a propósito: no confirma si el correo existe. */
@Composable
fun RecuperarScreen(
    ui: RecuperarUi,
    onCorreoChange: (String) -> Unit,
    onEnviarEnlace: () -> Unit,
    onVolverAIniciarSesion: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Fondo).imePadding()) {
        BarraConRegreso("Recuperar acceso", onVolverAIniciarSesion)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Escribe el correo con el que creaste tu cuenta. Te mandamos un enlace para poner una contraseña nueva.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                color = TintaSuave
            )
            CampoTexto(
                etiqueta = "Correo",
                valor = ui.correo,
                onValorChange = onCorreoChange,
                placeholder = "familia@correo.com",
                error = ui.errorCorreo,
                tipoTeclado = KeyboardType.Email,
                accionTeclado = ImeAction.Done,
                habilitado = !ui.enviando
            )
            if (ui.enviado) {
                AvisoExito(
                    "Si ese correo tiene cuenta, el enlace llega en unos minutos. " +
                        "Revisa también tu bandeja de no deseados."
                )
            }
        }
        BarraAccionesInferior {
            BotonPrimario(
                texto = when {
                    ui.enviando -> "Enviando…"
                    ui.enviado -> "Reenviar enlace"
                    else -> "Enviar enlace"
                },
                onClick = onEnviarEnlace,
                habilitado = esCorreoValido(ui.correo),
                cargando = ui.enviando
            )
            BotonSecundario("Volver a iniciar sesión", onVolverAIniciarSesion)
        }
    }
}

@Preview(showBackground = true, heightDp = 780, name = "P-27 · Recuperar contraseña")
@Composable
private fun RecuperarPreview() {
    FamiliasQueSumanTheme { RecuperarScreen(RecuperarUi("ana.rodriguez@correo.com", enviado = true), {}, {}, {}) }
}
