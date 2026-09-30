package mx.tec.familiasquesuman.ui.screens.campanas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.BarraMeta
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoCalendario
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoReloj
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.Confirmado
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * P-13: apartado confirmado. La barra arranca en el avance de antes y sube a la vista
 * hasta el de ahora; es el momento más satisfactorio del recorrido.
 */
@Composable
fun ApartadoConfirmadoScreen(
    campana: Campana,
    hecho: ApartadoHecho,
    onVerComoEntregar: () -> Unit,
    onApartarAlgoMas: () -> Unit,
    modifier: Modifier = Modifier,
    nombreAsociacion: String? = null
) {
    val centro = centroDeEntrega(nombreAsociacion)
    val antes = (hecho.progresoAntes * 100).roundToInt()
    val ahora = (campana.progreso * 100).roundToInt()

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.padding(top = 20.dp).size(80.dp).background(ConfirmadoFondo, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = ConfirmadoTexto, modifier = Modifier.size(40.dp))
            }
            Text(
                "Apartaste ${hecho.cantidad} ${aPlural(hecho.articuloNombre, hecho.cantidad)}",
                style = MaterialTheme.typography.headlineMedium,
                color = Tinta,
                textAlign = TextAlign.Center
            )

            Surface(shape = RoundedCornerShape(20.dp), color = ConfirmadoFondo, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${campana.completados} de ${campana.metaTotal} ${campana.unidadMeta} completos",
                        style = MaterialTheme.typography.titleMedium,
                        color = ConfirmadoTexto
                    )
                    BarraMeta(progreso = campana.progreso, color = Confirmado, animarDesde = hecho.progresoAntes)
                    Text(
                        "La meta subió de $antes% a $ahora% para todas las familias",
                        style = MaterialTheme.typography.labelMedium,
                        color = ConfirmadoTexto
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Superficie,
                border = BorderStroke(1.dp, Borde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cómo entregar", style = MaterialTheme.typography.titleMedium, color = Tinta)
                    FilaDato(IconoCalendario, "Antes del ${campana.cierra}")
                    if (centro != null) {
                        FilaDato(Icons.Default.LocationOn, "${centro.nombre}, ${centro.direccion}")
                        FilaDato(IconoReloj, centro.horario)
                    } else {
                        // Esa asociación no tiene centro propio en la lista: se manda a ver todos.
                        FilaDato(Icons.Default.LocationOn, "Consulta los centros de acopio en «Ver cómo entregar»")
                    }
                }
            }
            Text(
                "La app no procesa pagos. La entrega es en especie, en el centro de acopio.",
                style = MaterialTheme.typography.labelMedium,
                color = TintaSuave,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(Superficie).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onVerComoEntregar,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MarcaOro, contentColor = MarcaAzul)
            ) { Text("Ver cómo entregar", style = MaterialTheme.typography.labelLarge) }
            OutlinedButton(
                onClick = onApartarAlgoMas,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                border = BorderStroke(1.5.dp, MarcaAzul),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Apartar algo más", style = MaterialTheme.typography.labelLarge, color = MarcaAzul) }
        }
    }
}

@Composable
private fun FilaDato(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icono, contentDescription = null, tint = TintaSuave, modifier = Modifier.size(18.dp))
        Text(texto, style = MaterialTheme.typography.bodyLarge, color = Tinta)
    }
}

/**
 * "Rosario blanco", 2 → "rosarios blancos". Pluraliza las palabras del nombre hasta
 * la primera preposición o coma ("Mochila con útiles" → "mochilas con útiles").
 */
private fun aPlural(nombre: String, cantidad: Int): String {
    val texto = nombre.lowercase()
    if (cantidad == 1) return texto
    val corte = setOf("con", "de", "en", "para", "y")
    var pluralizando = true
    return texto.split(' ').joinToString(" ") { palabra ->
        val base = palabra.trimEnd(',')
        val coma = if (palabra.endsWith(",")) "," else ""
        when {
            !pluralizando || base in corte || base.any { !it.isLetter() } -> {
                pluralizando = false
                palabra
            }
            else -> {
                if (coma.isNotEmpty()) pluralizando = false
                val plural = when {
                    base.last() in "aeiouáéíóú" -> base + "s"
                    base.endsWith("z") -> base.dropLast(1) + "ces"
                    else -> base + "es"
                }
                plural + coma
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun ApartadoConfirmadoPreview() {
    FamiliasQueSumanTheme {
        ApartadoConfirmadoScreen(
            campana = Campana(
                "c1", "Kits de primera comunión", "a4", "Útiles escolares", "20 de septiembre", true, "",
                "kits", 45, 20, listOf(ArticuloMeta("c1-1", "Rosario blanco", 45, 14))
            ),
            hecho = ApartadoHecho("Rosario blanco", 2, progresoAntes = 18f / 45f),
            onVerComoEntregar = {}, onApartarAlgoMas = {},
            nombreAsociacion = "Parroquia San Bernabé"
        )
    }
}
