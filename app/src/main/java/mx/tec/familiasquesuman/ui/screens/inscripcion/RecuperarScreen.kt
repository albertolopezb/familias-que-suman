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
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.AvisoError
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.AvisoExito
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraAccionesInferior
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraConRegreso
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPrimario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonSecundario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.CampoTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * P-27 · Recuperar contraseña (RF-18), en dos pasos:
 *  1. El correo → la API manda un código de 6 dígitos (vence en 15 minutos, sirve una vez).
 *     El mensaje es vago a propósito: no confirma si el correo tiene cuenta.
 *  2. El código y la contraseña nueva → se cierran todas las sesiones y se entra con la nueva.
 */
@Composable
fun RecuperarScreen(
    ui: RecuperarUi,
    onCorreoChange: (String) -> Unit,
    onEnviarEnlace: () -> Unit,
    onVolverAIniciarSesion: () -> Unit,
    onCodigoChange: (String) -> Unit = {},
    onContrasenaChange: (String) -> Unit = {},
    onCambiar: () -> Unit = {}
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
            if (ui.cambiada) {
                AvisoExito("Listo: tu contraseña cambió y se cerraron tus sesiones abiertas. Ya puedes entrar con la nueva.")
                return@Column
            }
            Text(
                "Escribe el correo con el que creaste tu cuenta y te mandaremos un código de 6 dígitos para poner una contraseña nueva.",
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
                habilitado = !ui.enviando && !ui.cambiando
            )
            if (ui.enviado) {
                AvisoExito(
                    "Si ese correo tiene cuenta, te mandamos un código. Vence en 15 minutos y sirve una sola vez. " +
                        "Revisa también tu bandeja de no deseados."
                )
                CampoTexto(
                    etiqueta = "Código de 6 dígitos",
                    valor = ui.codigo,
                    onValorChange = onCodigoChange,
                    placeholder = "123456",
                    tipoTeclado = KeyboardType.NumberPassword,
                    habilitado = !ui.cambiando
                )
                CampoTexto(
                    etiqueta = "Contraseña nueva",
                    valor = ui.contrasena,
                    onValorChange = onContrasenaChange,
                    placeholder = "Mínimo 8 caracteres",
                    error = ui.errorContrasena,
                    tipoTeclado = KeyboardType.Password,
                    accionTeclado = ImeAction.Done,
                    esContrasena = true,
                    habilitado = !ui.cambiando
                )
            }
            ui.error?.let { AvisoError("No se pudo cambiar", it) }
        }
        BarraAccionesInferior {
            when {
                ui.cambiada -> BotonPrimario(texto = "Iniciar sesión", onClick = onVolverAIniciarSesion, habilitado = true)
                ui.enviado -> {
                    BotonPrimario(
                        texto = if (ui.cambiando) "Cambiando…" else "Cambiar contraseña",
                        onClick = onCambiar,
                        habilitado = ui.puedeCambiar,
                        cargando = ui.cambiando
                    )
                    BotonSecundario(if (ui.enviando) "Enviando…" else "Mandar otro código", onEnviarEnlace)
                }
                else -> {
                    BotonPrimario(
                        texto = if (ui.enviando) "Enviando…" else "Mandarme un código",
                        onClick = onEnviarEnlace,
                        habilitado = esCorreoValido(ui.correo),
                        cargando = ui.enviando
                    )
                    BotonSecundario("Volver a iniciar sesión", onVolverAIniciarSesion)
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 780, name = "P-27 · Recuperar contraseña")
@Composable
private fun RecuperarPreview() {
    FamiliasQueSumanTheme { RecuperarScreen(RecuperarUi("ana.rodriguez@correo.com", enviado = true, codigo = "4821"), {}, {}, {}) }
}
