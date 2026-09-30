package mx.tec.familiasquesuman.ui.screens.campanas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.BarraMeta
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.IconoImagen
import mx.tec.familiasquesuman.ui.screens.campanas.componentes.RenglonArticulo
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.Confirmado
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * P-11 (con artículos) y P-11c (campaña completa, con [campanaCompleta] = true).
 * [nombreAsociacion] es opcional: Campana solo trae asociacionId.
 */
@Composable
fun DetalleCampanaScreen(
    campana: Campana,
    campanaCompleta: Boolean,
    onBack: () -> Unit,
    onApartar: (ArticuloMeta) -> Unit,
    onVerComoDonar: () -> Unit,
    onVerOtrasCampanas: () -> Unit,
    modifier: Modifier = Modifier,
    nombreAsociacion: String? = null
) {
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Superficie).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonCircular(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Tinta)
            }
            Text(
                "Detalle de la campaña",
                style = MaterialTheme.typography.labelLarge,
                color = Tinta,
                modifier = Modifier.weight(1f).padding(start = 12.dp)
            )
            // El corazón de campañas favoritas no está en el alcance: solo se ve.
            BotonCircular(onClick = { /* sin acción en esta etapa */ }) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorita", tint = MarcaAzul)
            }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Foto(completa = campanaCompleta)
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(campana.titulo, style = MaterialTheme.typography.headlineMedium, color = Tinta)
                val cierre = "Cierra el ${campana.cierra}"
                Text(
                    text = if (nombreAsociacion != null) "$nombreAsociacion · $cierre" else cierre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TintaSuave
                )
                BloqueMeta(campana)

                if (campanaCompleta) {
                    CampanaCompleta(campana, onVerOtrasCampanas)
                } else {
                    Text(campana.descripcion, style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Superficie,
                        border = BorderStroke(1.dp, Borde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            campana.articulos.forEachIndexed { i, articulo ->
                                if (i > 0) HorizontalDivider(color = Borde)
                                RenglonArticulo(articulo = articulo, onApartar = { onApartar(articulo) })
                            }
                        }
                    }
                }
            }
        }

        // Fijo abajo: cómo y dónde entregar.
        HorizontalDivider(color = Borde)
        TextButton(
            onClick = onVerComoDonar,
            modifier = Modifier.fillMaxWidth().background(Superficie).padding(vertical = 4.dp)
        ) {
            Text("Ver cómo y dónde entregar", style = MaterialTheme.typography.labelLarge, color = MarcaAzul)
            Spacer(Modifier.size(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MarcaAzul, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun BotonCircular(onClick: () -> Unit, contenido: @Composable () -> Unit) {
    Surface(shape = CircleShape, color = Color(0xFFF1F5F9), modifier = Modifier.size(40.dp)) {
        IconButton(onClick = onClick) { contenido() }
    }
}

/** Hueco para la foto de la campaña (extra: drawable por campaña). Verde si ya se completó. */
@Composable
private fun Foto(completa: Boolean) {
    val colores = if (completa) listOf(Color(0xFFD1FAE5), Color(0xFF99F6D0))
    else listOf(AcentoSuave, Color(0xFFF5E2B0))
    Box(
        modifier = Modifier.fillMaxWidth().height(140.dp).background(Brush.linearGradient(colores)),
        contentAlignment = Alignment.Center
    ) {
        // Avisa que aquí va la foto de la campaña (extra: un drawable por campaña).
        Icon(
            imageVector = IconoImagen,
            contentDescription = null,
            tint = Color(0xFF64748B).copy(alpha = 0.55f),
            modifier = Modifier.size(40.dp)
        )
    }
}

/** Bloque verde con "18 de 45 kits completos" y la barra. */
@Composable
private fun BloqueMeta(campana: Campana) {
    Surface(shape = RoundedCornerShape(16.dp), color = ConfirmadoFondo, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "${campana.completados} de ${campana.metaTotal} ${campana.unidadMeta} completos",
                style = MaterialTheme.typography.titleMedium,
                color = ConfirmadoTexto
            )
            BarraMeta(progreso = campana.progreso, color = Confirmado)
        }
    }
}

/** P-11c: agradece y ofrece otras campañas. */
@Composable
private fun CampanaCompleta(campana: Campana, onVerOtrasCampanas: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ConfirmadoTexto, modifier = Modifier.size(44.dp))
        Text("Esta campaña ya está completa", style = MaterialTheme.typography.titleLarge, color = Tinta, textAlign = TextAlign.Center)
        Text(
            "Gracias a las familias que participaron. La entrega es el ${campana.cierra}.",
            style = MaterialTheme.typography.bodyLarge,
            color = TintaSuave,
            textAlign = TextAlign.Center
        )
        OutlinedButton(
            onClick = onVerOtrasCampanas,
            border = BorderStroke(1.5.dp, MarcaAzul),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Ver otras campañas", style = MaterialTheme.typography.labelLarge, color = MarcaAzul)
        }
    }
}

// ---------------- Previews ----------------

private val kits = Campana(
    "c1", "Kits de primera comunión", "a4", "Útiles escolares", "20 de septiembre", true,
    "Cuarenta y cinco niños hacen su primera comunión el 4 de octubre. Aparta lo que vayas a llevar para que no se junten cosas repetidas.",
    "kits", 45, 18,
    listOf(
        ArticuloMeta("c1-1", "Rosario blanco", 45, 12),
        ArticuloMeta("c1-2", "Biblia infantil", 45, 30),
        ArticuloMeta("c1-3", "Vela decorada", 45, 45),
        ArticuloMeta("c1-4", "Mochila con útiles", 45, 8)
    )
)

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun DetallePreview() {
    FamiliasQueSumanTheme {
        DetalleCampanaScreen(kits, false, {}, {}, {}, {}, nombreAsociacion = "Parroquia San Bernabé")
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun DetalleCompletaPreview() {
    FamiliasQueSumanTheme {
        DetalleCampanaScreen(
            kits.copy(titulo = "Despensas de fin de mes", cierra = "30 de septiembre", unidadMeta = "despensas",
                metaTotal = 300, completados = 300),
            true, {}, {}, {}, {},
            nombreAsociacion = "Banco de Alimentos Cáritas Monterrey"
        )
    }
}
