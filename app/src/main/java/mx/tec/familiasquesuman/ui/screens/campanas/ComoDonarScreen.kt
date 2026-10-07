package mx.tec.familiasquesuman.ui.screens.campanas

import mx.tec.familiasquesuman.ui.components.BarraSuperior
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoCaja
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoReloj
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * Centros y necesidades tal como vienen en el Figma. El modelo (Modelos.kt) todavía no
 * trae centros de acopio; si el equipo los agrega en domain/, se reemplazan por eso.
 */
data class CentroAcopio(val nombre: String, val direccion: String, val horario: String)
data class Necesidad(val titulo: String, val detalle: String)

val CentrosDeAcopio = listOf(
    CentroAcopio("Comedor Comunitario San Bernabé", "Av. Rómulo Garza 240, Col. San Bernabé", "Lunes a viernes, 9:00 a 18:00"),
    CentroAcopio("Parroquia San Bernabé", "Av. Aztlán 1500", "Sábados, 10:00 a 14:00")
)

/** El centro donde se entrega lo apartado: el de la asociación dueña de la campaña, si tiene uno. */
fun centroDeEntrega(nombreAsociacion: String?): CentroAcopio? =
    CentrosDeAcopio.firstOrNull { it.nombre == nombreAsociacion }

val NecesidadesActuales = listOf(
    Necesidad("Arroz y frijol", "Bolsas de 1 kg cerradas"),
    Necesidad("Cobijas", "Usadas en buen estado"),
    Necesidad("Útiles escolares", "Cuadernos y lápices nuevos")
)

/** P-24: cómo y dónde donar, con el aviso de que la app no procesa pagos. */
@Composable
fun ComoDonarScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BarraSuperior("Cómo donar", onRegresar = onBack)

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(14.dp), color = AcentoSuave, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AcentoTexto, modifier = Modifier.size(20.dp))
                    Text(
                        "La app no recibe pagos ni donativos en línea. La entrega es en especie, en los centros de acopio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AcentoTexto
                    )
                }
            }

            Text("QUÉ SE NECESITA AHORA", style = MaterialTheme.typography.labelSmall, color = TintaSuave)
            Tarjeta {
                NecesidadesActuales.forEach { n ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(IconoCaja, contentDescription = null, tint = TintaSuave, modifier = Modifier.size(20.dp))
                        Column {
                            Text(n.titulo, style = MaterialTheme.typography.labelLarge, color = Tinta)
                            Text(n.detalle, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                        }
                    }
                }
            }

            Text("CENTROS DE ACOPIO EN MONTERREY", style = MaterialTheme.typography.labelSmall, color = TintaSuave)
            CentrosDeAcopio.forEach { c ->
                Tarjeta {
                    Text(c.nombre, style = MaterialTheme.typography.labelLarge, color = Tinta)
                    DatoConIcono(Icons.Default.LocationOn, c.direccion)
                    DatoConIcono(IconoReloj, c.horario)
                }
            }
        }
    }
}

@Composable
private fun Tarjeta(contenido: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Superficie,
        border = BorderStroke(1.dp, Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            contenido()
        }
    }
}

@Composable
private fun DatoConIcono(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icono, contentDescription = null, tint = TintaSuave, modifier = Modifier.size(16.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
    }
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun ComoDonarPreview() {
    FamiliasQueSumanTheme { ComoDonarScreen(onBack = {}) }
}
