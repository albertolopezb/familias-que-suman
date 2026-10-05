package mx.tec.familiasquesuman.ui.screens.sugerencias

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.EstadoSugerencia
import mx.tec.familiasquesuman.domain.Sugerencia
import mx.tec.familiasquesuman.domain.TipoSugerencia
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TituloDePagina
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * La bandeja del admin con lo que sugirieron las familias. Se filtra por tipo y por estado;
 * cada sugerencia pendiente se aprueba (para publicarla) o se descarta.
 */
@Composable
fun BandejaSugerenciasScreen(
    sugerencias: List<Sugerencia>,
    esAdmin: Boolean,
    onCambiarEstado: (String, EstadoSugerencia) -> Unit,
    onRegresar: () -> Unit
) {
    var tipo by rememberSaveable { mutableStateOf<TipoSugerencia?>(null) }
    var verPendientes by rememberSaveable { mutableStateOf(true) }
    val visibles = sugerencias
        .filter { tipo == null || it.tipo == tipo }
        .filter { (it.estado == EstadoSugerencia.PENDIENTE) == verPendientes }

    Column(Modifier.fillMaxSize().background(Web.Fondo)) {
        EncabezadoApp()
        if (!esAdmin) {
            Text(
                "Solo el equipo de Familias que Suman puede ver las sugerencias.",
                style = TextoWeb.Cuerpo,
                color = Web.TextoApagado,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(32.dp)
            )
            return@Column
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "titulo") {
                val pendientes = sugerencias.count { it.estado == EstadoSugerencia.PENDIENTE }
                TituloDePagina(
                    titulo = "Sugerencias recibidas",
                    subtitulo = if (pendientes == 1) "1 sugerencia por revisar." else "$pendientes sugerencias por revisar.",
                    migaAnterior = "Perfil",
                    onMigaAnterior = onRegresar
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("Pendientes", verPendientes) { verPendientes = true }
                    Chip("Revisadas", !verPendientes) { verPendientes = false }
                }
            }
            item(key = "tipos") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Chip("Todas", tipo == null) { tipo = null }
                    TipoSugerencia.entries.forEach { t -> Chip(t.etiqueta, tipo == t) { tipo = t } }
                }
            }
            if (visibles.isEmpty()) {
                item(key = "vacio") {
                    Text(
                        if (verPendientes) "No hay sugerencias pendientes." else "Todavía no hay sugerencias revisadas.",
                        style = TextoWeb.Cuerpo,
                        color = Web.TextoApagado,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    )
                }
            }
            items(visibles, key = { it.id }) { s ->
                TarjetaSugerencia(s, onCambiarEstado)
            }
        }
    }
}

@Composable
private fun Chip(texto: String, activo: Boolean, onClick: () -> Unit) {
    Text(
        texto,
        style = TextoWeb.Chip,
        color = if (activo) Color.White else Web.Texto,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (activo) Web.Primario else Web.Tarjeta)
            .border(1.dp, if (activo) Web.Primario else Web.Borde, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun TarjetaSugerencia(s: Sugerencia, onCambiarEstado: (String, EstadoSugerencia) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Web.Tarjeta,
        border = BorderStroke(1.dp, Web.Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(iconoDe(s.tipo), contentDescription = null, tint = Web.Primario, modifier = Modifier.size(16.dp))
                Etiqueta(s.tipo.etiqueta, Web.Secundario, Web.Primario)
                Row(Modifier.weight(1f)) {}
                when (s.estado) {
                    EstadoSugerencia.APROBADA -> Etiqueta("Aprobada", Web.VerdeFondo, Web.VerdeTexto)
                    EstadoSugerencia.DESCARTADA -> Etiqueta("Descartada", Web.RojoFondo, Web.RojoTexto)
                    EstadoSugerencia.PENDIENTE -> Text(s.fecha, style = TextoWeb.Chico)
                }
            }
            Text(s.nombre, style = TextoWeb.TituloTarjeta.copy(color = Web.Primario))
            Text(s.descripcion, style = TextoWeb.Cuerpo.copy(color = Web.TextoApagado))
            if (s.detalleExtra.isNotBlank()) Dato(s.tipo.campoExtra, s.detalleExtra)
            if (s.organizacion.isNotBlank()) Dato("Organiza", s.organizacion)
            Dato("Ciudad", s.ciudad)
            if (s.contactoCausa.isNotBlank()) Dato("Contacto", s.contactoCausa)
            Dato("Sugerida por", "${s.sugeridaPor} · ${s.contactoDeQuienSugiere}")

            if (s.estado == EstadoSugerencia.PENDIENTE) {
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { onCambiarEstado(s.id, EstadoSugerencia.DESCARTADA) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Web.Borde)
                    ) { Text("Descartar", style = TextoWeb.Chip, color = Web.RojoTexto) }
                    Button(
                        onClick = { onCambiarEstado(s.id, EstadoSugerencia.APROBADA) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Web.Primario)
                    ) { Text("Aprobar", style = TextoWeb.Chip, color = Color.White) }
                }
            } else {
                Text(
                    "Volver a pendientes",
                    style = TextoWeb.Chip,
                    color = Web.Primario,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { onCambiarEstado(s.id, EstadoSugerencia.PENDIENTE) }
                )
            }
        }
    }
}

@Composable
private fun Dato(rotulo: String, valor: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("$rotulo:", style = TextoWeb.Chico.copy(fontWeight = FontWeight.SemiBold), color = Web.Texto)
        Text(valor, style = TextoWeb.Chico, modifier = Modifier.weight(1f, fill = false))
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun BandejaPreview() {
    FamiliasQueSumanTheme {
        BandejaSugerenciasScreen(
            sugerencias = listOf(
                Sugerencia(
                    "sg1", TipoSugerencia.CENTRO, "Casa Hogar Nuevo Amanecer",
                    "Casa hogar para niñas de 6 a 17 años.", "Av. Ruiz Cortines 2500, Guadalupe",
                    "Monterrey", "Nuevo Amanecer A.B.P.", "8112345678", "Familia Garza",
                    "garza.familia@correo.com", "2 oct 2026"
                )
            ),
            esAdmin = true,
            onCambiarEstado = { _, _ -> },
            onRegresar = {}
        )
    }
}
