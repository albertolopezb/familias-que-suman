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
 * P-26 · Iniciar sesión. La contraseña NO se escribe aquí: el botón abre la página segura de Auth0
 * (en una pestaña del navegador). Así la app nunca ve ni guarda la contraseña.
 * @param regresoA "Preparar despensas de fin de mes · Sáb 12 sep" si vienen de inscribirse; si no, null.
 */
@Composable
fun IniciarSesionScreen(
    ui: IniciarSesionUi,
    regresoA: String?,
    onCorreoChange: (String) -> Unit,
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
            ui.error?.let { AvisoError("No pudimos entrar", it) }
            if (regresoA != null) NotaAmbar("Al entrar regresas a: $regresoA")
            Text(
                "Entra para inscribirte, guardar favoritos y ver tu historial.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                color = TintaSuave
            )
            Text(
                "Te llevamos a la página segura de inicio de sesión. Tu contraseña se escribe ahí y la app nunca la ve.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                color = TintaSuave
            )
            CampoTexto(
                etiqueta = "Correo (opcional)",
                valor = ui.correo,
                onValorChange = onCorreoChange,
                placeholder = "familia@correo.com",
                error = ui.errorCorreo,
                tipoTeclado = KeyboardType.Email,
                habilitado = !ui.enviando
            )
            TextButton(onClick = onOlvideContrasena, contentPadding = PaddingValues(0.dp)) {
                Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.labelLarge, color = MarcaAzul)
            }
        }
        BarraAccionesInferior {
            BotonPrimario(
                texto = if (ui.enviando) "Entrando…" else "Continuar a inicio de sesión",
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
            IniciarSesionUi("ana.rodriguez@correo.com"),
            "Preparar despensas de fin de mes · Sáb 12 sep",
            {}, {}, {}, {}, {}
        )
    }
}
