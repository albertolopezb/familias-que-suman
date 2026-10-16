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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.AvisoError
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraAccionesInferior
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraConRegreso
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPrimario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.CampoTexto
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.Casilla
import mx.tec.familiasquesuman.ui.screens.perfil.Responsable
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * P-05 · Crear cuenta. Con `estadoValidacion` dibuja P-05b (campo en rojo y franja del servidor).
 * La contraseña viaja una sola vez a la API, que guarda solo su hash (nunca la contraseña).
 */
@Composable
fun CrearCuentaScreen(
    ui: CrearCuentaUi,
    estadoValidacion: EstadoValidacion,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onAceptaAvisoChange: (Boolean) -> Unit,
    onCrearCuenta: () -> Unit,
    onIniciarSesion: () -> Unit,
    onRegresar: () -> Unit,
    onVerAviso: () -> Unit = {}
) {
    Column(Modifier.fillMaxSize().background(Fondo).imePadding()) {
        BarraConRegreso("Crear cuenta", onRegresar)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (estadoValidacion.errorServidor) {
                AvisoError(
                    "No se pudo crear la cuenta",
                    ui.mensaje ?: "Revisa tu conexión e inténtalo otra vez. Lo que escribiste no se borró."
                )
            }
            Text(
                "Tu cuenta guarda tus inscripciones, tus favoritos y tu historial de servicio.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                color = TintaSuave
            )
            CampoTexto(
                etiqueta = "Nombre de la familia",
                valor = ui.nombre,
                onValorChange = onNombreChange,
                placeholder = "Familia Rodríguez",
                error = estadoValidacion.errorNombre,
                habilitado = !ui.enviando
            )
            CampoTexto(
                etiqueta = "Correo",
                valor = ui.correo,
                onValorChange = onCorreoChange,
                placeholder = "familia@correo.com",
                error = estadoValidacion.errorCorreo,
                tipoTeclado = KeyboardType.Email,
                habilitado = !ui.enviando
            )
            CampoTexto(
                etiqueta = "Contraseña",
                valor = ui.contrasena,
                onValorChange = onContrasenaChange,
                placeholder = "Mínimo 8 caracteres",
                error = estadoValidacion.errorContrasena,
                tipoTeclado = KeyboardType.Password,
                accionTeclado = ImeAction.Done,
                esContrasena = true,
                habilitado = !ui.enviando
            )
            // Aviso de privacidad simplificado (LFPDPPP art. 16-II): fracciones I a IV del art. 15 y dónde leer el completo.
            Text(
                "Aviso de privacidad simplificado. ${Responsable.NOMBRE} (${Responsable.DOMICILIO}) es responsable de tus datos: " +
                    "tu correo, tu nombre y los de tu familia, con fechas de nacimiento y sexo, y lo que hagas en la app. " +
                    "Los usamos para tu cuenta, tus inscripciones y tu historial; testimonios, encuestas y avisos son opcionales. " +
                    "No pedimos datos sensibles ni los vendemos. Puedes limitar su uso en Ajustes o pedir que los borremos.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = TintaSuave
            )
            TextButton(onClick = onVerAviso, contentPadding = PaddingValues(0.dp)) {
                Text("Leer el aviso de privacidad completo", style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline), color = MarcaAzul)
            }
        }
        // La casilla va junto al botón: así sube con el teclado y se acepta sin tener que cerrarlo.
        BarraAccionesInferior {
            Casilla(
                marcada = ui.aceptaAviso,
                onCambio = onAceptaAvisoChange,
                texto = "He leído el aviso de privacidad y acepto cómo se usan los datos de mi familia.",
                habilitada = !ui.enviando
            )
            BotonPrimario(
                texto = if (ui.enviando) "Creando tu cuenta…" else "Crear cuenta",
                onClick = onCrearCuenta,
                habilitado = ui.puedeEnviar,
                cargando = ui.enviando
            )
            TextButton(onClick = onIniciarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("¿Ya tienes cuenta? Inicia sesión", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp), color = TintaSuave)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 780, name = "P-05 · Crear cuenta")
@Composable
private fun CrearCuentaPreview() {
    val ui = CrearCuentaUi("Familia Rodríguez", "ana.rodriguez@correo.com", "una frase segura", aceptaAviso = true)
    FamiliasQueSumanTheme { CrearCuentaScreen(ui, ui.validacion, {}, {}, {}, {}, {}, {}, {}) }
}

@Preview(showBackground = true, heightDp = 780, name = "P-05b · Validación y error")
@Composable
private fun CrearCuentaErrorPreview() {
    val ui = CrearCuentaUi("Familia Rodríguez", "ana.rodriguez@correo", "corta", errorServidor = true)
    FamiliasQueSumanTheme { CrearCuentaScreen(ui, ui.validacion, {}, {}, {}, {}, {}, {}, {}) }
}
