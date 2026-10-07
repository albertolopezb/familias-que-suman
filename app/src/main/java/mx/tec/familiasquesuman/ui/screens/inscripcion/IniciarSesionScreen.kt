package mx.tec.familiasquesuman.ui.screens.inscripcion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.AvisoError
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraAccionesInferior
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraConRegreso
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPrimario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.CampoTexto
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.NotaAmbar
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * P-26 · Iniciar sesión.
 * @param regresoA "Preparar despensas de fin de mes · Sáb 12 sep" si vienen de inscribirse; si no, null.
 */
@Composable
fun IniciarSesionScreen(
    ui: IniciarSesionUi,
    regresoA: String?,
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onEntrar: () -> Unit,
    onOlvideContrasena: () -> Unit,
    onCrearCuenta: () -> Unit,
    onRegresar: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Fondo).imePadding()) {
        BarraConRegreso("Iniciar sesión", onRegresar)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (ui.error) {
                // No se dice cuál de los dos falló: así nadie averigua qué correos tienen cuenta.
                AvisoError("No pudimos entrar", "El correo o la contraseña no coinciden. Revísalos o recupera tu acceso.")
            }
            Text(
                "Entra para inscribirte, guardar favoritos y ver tu historial.",
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
                habilitado = !ui.enviando
            )
            CampoTexto(
                etiqueta = "Contraseña",
                valor = ui.contrasena,
                onValorChange = onContrasenaChange,
                placeholder = "Tu contraseña",
                tipoTeclado = KeyboardType.Password,
                accionTeclado = ImeAction.Done,
                esContrasena = true,
                habilitado = !ui.enviando
            )
            TextButton(onClick = onOlvideContrasena, contentPadding = PaddingValues(0.dp)) {
                Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.labelLarge, color = MarcaAzul)
            }
        }
        BarraAccionesInferior {
            BotonPrimario(
                texto = if (ui.enviando) "Entrando…" else "Entrar",
                onClick = onEntrar,
                habilitado = ui.puedeEnviar,
                cargando = ui.enviando
            )
            TextButton(onClick = onCrearCuenta, modifier = Modifier.fillMaxWidth()) {
                Text("¿No tienes cuenta? Crear una", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp), color = TintaSuave)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 780, name = "P-26 · Iniciar sesión")
@Composable
private fun IniciarSesionPreview() {
    FamiliasQueSumanTheme {
        IniciarSesionScreen(
            IniciarSesionUi("ana.rodriguez@correo.com", "familia123"),
            "Preparar despensas de fin de mes · Sáb 12 sep",
            {}, {}, {}, {}, {}, {}
        )
    }
}
