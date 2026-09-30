package mx.tec.familiasquesuman.ui.screens.campanas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.Stepper
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * Hoja de apartar. Una sola pieza con tres caras según [HojaApartar.modo]:
 * P-12 (normal), P-12b (apartando) y P-14 (ya no alcanza).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartarSheet(
    hoja: HojaApartar,
    onCantidad: (Int) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onCerrar, containerColor = Color.White) {
        ApartarContenido(hoja, onCantidad, onConfirmar, onCerrar)
    }
}

/** Lo de adentro de la hoja, separado para poder verlo en @Preview. */
@Composable
fun ApartarContenido(
    hoja: HojaApartar,
    onCantidad: (Int) -> Unit,
    onConfirmar: () -> Unit,
    onOtrosArticulos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val articulo = hoja.articulo
    val apartando = hoja.modo == ModoApartar.Apartando
    val agotado = hoja.modo == ModoApartar.YaNoAlcanza
    val maximo = articulo.faltan.coerceAtLeast(1)

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (agotado) {
            Text(
                text = if (articulo.faltan == 1) "Ya solo queda 1 ${sustantivo(articulo)}"
                else "Ya solo quedan ${articulo.faltan} de este artículo",
                style = MaterialTheme.typography.titleLarge,
                color = Tinta
            )
            Text(
                "Mientras elegías, otras familias apartaron ${hoja.otrasApartaron}. No se registró nada todavía.",
                style = MaterialTheme.typography.bodyLarge,
                color = TintaSuave
            )
        } else {
            Text(articulo.nombre, style = MaterialTheme.typography.titleLarge, color = Tinta)
            Text("Faltan ${articulo.faltan} de ${articulo.meta}.", style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
        }

        Stepper(
            valor = hoja.cantidad,
            minimo = 1,
            maximo = maximo,
            onCambio = onCantidad,
            habilitado = !apartando
        )

        when (hoja.modo) {
            ModoApartar.Normal -> {
                Aviso(
                    "Vas a llevar ${hoja.cantidad} · después faltarán ${(articulo.faltan - hoja.cantidad).coerceAtLeast(0)}",
                    fondo = ConfirmadoFondo, texto = ConfirmadoTexto
                )
                BotonPrincipal("Confirmar mi apoyo", onClick = onConfirmar)
            }
            ModoApartar.Apartando -> {
                BotonPrincipal("Apartando…", onClick = {}, habilitado = false)
            }
            ModoApartar.YaNoAlcanza -> {
                Aviso(
                    if (articulo.faltan == 1) "1 es todo lo que falta de este artículo"
                    else "${articulo.faltan} es todo lo que falta de este artículo",
                    fondo = AcentoSuave, texto = AcentoTexto
                )
                BotonPrincipal("Apartar ${hoja.cantidad}", onClick = onConfirmar, habilitado = articulo.faltan > 0)
                OutlinedButton(
                    onClick = onOtrosArticulos,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    border = BorderStroke(1.5.dp, MarcaAzul),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Ver otros artículos", style = MaterialTheme.typography.labelLarge, color = MarcaAzul)
                }
            }

        }
    }
}

@Composable
private fun Aviso(mensaje: String, fondo: Color, texto: Color) {
    Surface(shape = RoundedCornerShape(14.dp), color = fondo, modifier = Modifier.fillMaxWidth()) {
        Text(
            mensaje,
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = texto,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BotonPrincipal(texto: String, onClick: () -> Unit, habilitado: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MarcaOro,
            contentColor = MarcaAzul,
            // "Apartando…" se ve como el botón dorado apagado, no gris: sigue siendo parte del flujo.
            disabledContainerColor = Color(0xFFF2DDB0),
            disabledContentColor = AcentoTexto
        )
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

/** "Rosario blanco" → "rosario". Para frases como "Ya solo queda 1 rosario". */
private fun sustantivo(a: ArticuloMeta): String =
    a.nombre.substringBefore(' ').trimEnd(',').lowercase()

// ---------------- Previews ----------------

private val rosario = ArticuloMeta("c1-1", "Rosario blanco", 45, 12)

@Preview(showBackground = true)
@Composable
private fun ApartarNormalPreview() {
    FamiliasQueSumanTheme {
        ApartarContenido(HojaApartar(rosario, 2), {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ApartandoPreview() {
    FamiliasQueSumanTheme {
        ApartarContenido(HojaApartar(rosario, 2, ModoApartar.Apartando), {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun YaNoAlcanzaPreview() {
    FamiliasQueSumanTheme {
        ApartarContenido(
            HojaApartar(rosario.copy(apartados = 44), 1, ModoApartar.YaNoAlcanza, otrasApartaron = 32),
            {}, {}, {}
        )
    }
}
