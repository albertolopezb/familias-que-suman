package mx.tec.familiasquesuman.ui.screens.inscripcion.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.R
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorRojo
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * Etiqueta en negritas arriba, caja blanca redondeada y, si hay, el error en rojo abajo (P-05b).
 * Con `esContrasena` aparece el ojo para mostrarla u ocultarla.
 */
@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    accionTeclado: ImeAction = ImeAction.Next,
    esContrasena: Boolean = false,
    habilitado: Boolean = true
) {
    var mostrar by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (etiqueta.isNotEmpty()) {
            Text(etiqueta, style = MaterialTheme.typography.labelLarge, color = Tinta)
            Spacer(Modifier.height(6.dp))
        }
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = habilitado,
            singleLine = true,
            isError = error != null,
            placeholder = { Text(placeholder, color = TintaSuave.copy(alpha = 0.8f)) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, color = Tinta),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado, imeAction = accionTeclado),
            visualTransformation = if (esContrasena && !mostrar) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (esContrasena) {
                {
                    IconButton(onClick = { mostrar = !mostrar }) {
                        Icon(
                            painterResource(if (mostrar) R.drawable.ic_ojo_tachado else R.drawable.ic_ojo),
                            contentDescription = if (mostrar) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = TintaSuave
                        )
                    }
                }
            } else {
                null
            },
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

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun CampoTextoPreview() {
    FamiliasQueSumanTheme {
        Column {
            CampoTexto("Correo", "ana.rodriguez@correo", {}, error = "Escribe un correo válido, por ejemplo familia@correo.com")
            Spacer(Modifier.height(12.dp))
            CampoTexto("Contraseña", "familia123", {}, esContrasena = true)
        }
    }
}
