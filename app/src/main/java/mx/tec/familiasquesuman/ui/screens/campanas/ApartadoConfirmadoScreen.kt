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
import androidx.compose.material3.TextButton
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
import mx.tec.familiasquesuman.domain.PuntoEntrega
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.BarraMeta
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoCalendario
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoMensaje
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoTelefono
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
 * hasta el de ahora. Aquí mismo se explica cómo entregar (con los datos reales de la campaña)
 * y se puede avisar por WhatsApp que ya se apartó.
 */
@Composable
fun ApartadoConfirmadoScreen(
    campana: Campana,
    hecho: ApartadoHecho,
    onAvisar: () -> Unit,
    onComoLlegar: (PuntoEntrega) -> Unit,
    onRegresar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val antes = (hecho.progresoAntes * 100).roundToInt()
    val ahora = (campana.progreso * 100).roundToInt()
    val variosArticulos = campana.articulos.size > 1
    val contacto = listOfNotNull(
        campana.contactoNombre,
        campana.telefono ?: campana.whatsapp
    ).joinToString(" · ")
    val hayComoEntregar = campana.comoAyudar.isNotBlank() || campana.cierra.isNotBlank() ||
        campana.puntosEntrega.isNotEmpty() || contacto.isNotBlank()

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
                "Apartaste ${hecho.cantidad} de «${hecho.articuloNombre}»",
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

            if (hayComoEntregar) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Superficie,
                    border = BorderStroke(1.dp, Borde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Cómo entregar", style = MaterialTheme.typography.titleMedium, color = Tinta)
                        if (campana.comoAyudar.isNotBlank()) {
                            Text(campana.comoAyudar, style = MaterialTheme.typography.bodyLarge, color = Tinta)
                        }
                        if (campana.cierra.isNotBlank()) {
                            FilaDato(IconoCalendario, "Antes del ${campana.cierra}")
                        }
                        campana.puntosEntrega.forEach { punto ->
                            Column {
                                FilaDato(Icons.Default.LocationOn, "${punto.direccion}, ${punto.colonia}")
                                TextButton(onClick = { onComoLlegar(punto) }) {
                                    Text("Cómo llegar", color = MarcaAzul)
                                }
                            }
                        }
                        if (contacto.isNotBlank()) {
                            FilaDato(IconoTelefono, contacto)
                        }
                    }
                }
            }
            Text(
                "La app no procesa pagos. La entrega es en especie.",
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
                onClick = onAvisar,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MarcaOro, contentColor = MarcaAzul)
            ) {
                Icon(IconoMensaje, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("  Avisar que aparté", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onRegresar,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                border = BorderStroke(1.5.dp, MarcaAzul),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    if (variosArticulos) "Apartar algo más" else "Regresar",
                    style = MaterialTheme.typography.labelLarge,
                    color = MarcaAzul
                )
            }
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

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun ApartadoConfirmadoPreview() {
    FamiliasQueSumanTheme {
        ApartadoConfirmadoScreen(
            campana = Campana(
                "c4", "Bibliotecas Infantiles", "", "Útiles escolares", "", true, "",
                "cuentos", 300, 136, listOf(ArticuloMeta("c4-1", "Cuentos infantiles", 300, 136)),
                comoAyudar = "Junta cuentos desde preescolar hasta secundaria en buen estado.",
                telefono = "8120322281"
            ),
            hecho = ApartadoHecho("Cuentos infantiles", 36, progresoAntes = 100f / 300f),
            onAvisar = {}, onComoLlegar = {}, onRegresar = {}
        )
    }
}
