package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import mx.tec.familiasquesuman.domain.EstadoTestimonio
import mx.tec.familiasquesuman.domain.Testimonio
import mx.tec.familiasquesuman.ui.components.BarraSuperior
import mx.tec.familiasquesuman.ui.components.FotoUsuario
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * La revisión de testimonios del admin (RF-12). Cada testimonio por revisar se aprueba (y solo
 * entonces se publica), se devuelve con una nota para que la familia lo ajuste, o se descarta.
 */
@Composable
fun RevisionTestimoniosScreen(
    testimonios: List<Testimonio>,
    esAdmin: Boolean,
    onResolver: (id: String, estado: EstadoTestimonio, nota: String) -> Unit,
    onRegresar: () -> Unit
) {
    var porRevisar by rememberSaveable { mutableStateOf(true) }
    var pidiendoAjuste by rememberSaveable { mutableStateOf<String?>(null) }
    val visibles = testimonios.filter { (it.estado == EstadoTestimonio.EN_REVISION) == porRevisar }

    Column(Modifier.fillMaxSize().background(Web.Fondo)) {
        BarraSuperior("Testimonios", onRegresar = onRegresar)
        if (!esAdmin) {
            Text(
                "Solo el equipo de Familias que Suman puede revisar testimonios.",
                style = TextoWeb.Cuerpo, color = Web.TextoApagado, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(32.dp)
            )
            return@Column
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "filtros") {
                val pendientes = testimonios.count { it.estado == EstadoTestimonio.EN_REVISION }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (pendientes == 1) "1 testimonio por revisar." else "$pendientes testimonios por revisar.",
                        style = TextoWeb.Cuerpo, color = Web.TextoApagado
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip("Por revisar", porRevisar) { porRevisar = true }
                        Chip("Revisados", !porRevisar) { porRevisar = false }
                    }
                }
            }
            if (visibles.isEmpty()) {
                item(key = "vacio") {
                    Text(
                        if (porRevisar) "No hay testimonios por revisar." else "Todavía no hay testimonios revisados.",
                        style = TextoWeb.Cuerpo, color = Web.TextoApagado, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    )
                }
            }
            items(visibles, key = { it.id }) { t ->
                TarjetaTestimonio(
                    t,
                    onAprobar = { onResolver(t.id, EstadoTestimonio.APROBADO, "") },
                    onAjustar = { pidiendoAjuste = t.id },
                    onDescartar = { onResolver(t.id, EstadoTestimonio.DESCARTADO, "") },
                    onVolverARevision = { onResolver(t.id, EstadoTestimonio.EN_REVISION, "") }
                )
            }
        }
    }

    pidiendoAjuste?.let { id ->
        DialogoAjuste(
            onEnviar = { nota ->
                onResolver(id, EstadoTestimonio.AJUSTAR, nota)
                pidiendoAjuste = null
            },
            onCancelar = { pidiendoAjuste = null }
        )
    }
}

@Composable
private fun DialogoAjuste(onEnviar: (String) -> Unit, onCancelar: () -> Unit) {
    var nota by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("¿Qué hay que ajustar?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("La familia verá este mensaje y podrá corregir su testimonio.", style = TextoWeb.Chico)
                OutlinedTextField(
                    value = nota, onValueChange = { nota = it },
                    placeholder = { Text("Ej. La foto no se ve bien; ¿puedes mandar otra?") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onEnviar(nota.trim()) }, enabled = nota.isNotBlank()) { Text("Pedir ajuste") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
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
private fun TarjetaTestimonio(
    t: Testimonio,
    onAprobar: () -> Unit,
    onAjustar: () -> Unit,
    onDescartar: () -> Unit,
    onVolverARevision: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Web.Tarjeta,
        border = BorderStroke(1.dp, Web.Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(t.actividadTitulo, style = TextoWeb.TituloTarjeta.copy(color = Web.Primario), modifier = Modifier.weight(1f))
                when (t.estado) {
                    EstadoTestimonio.APROBADO -> Etiqueta("Aprobado", Web.VerdeFondo, Web.VerdeTexto)
                    EstadoTestimonio.AJUSTAR -> Etiqueta("Pidió ajustes", Web.AmbarFondo, Web.AmbarTexto)
                    EstadoTestimonio.DESCARTADO -> Etiqueta("Descartado", Web.RojoFondo, Web.RojoTexto)
                    EstadoTestimonio.EN_REVISION -> Text(t.fecha, style = TextoWeb.Chico)
                }
            }
            t.foto?.let {
                FotoUsuario(it, "Foto de ${t.familia}", Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)))
            }
            Text(t.experiencia, style = TextoWeb.Cuerpo)
            Text(
                "${t.familia} · ${t.correo}",
                style = TextoWeb.Chico.copy(fontWeight = FontWeight.SemiBold), color = Web.Texto
            )
            if (t.estado == EstadoTestimonio.AJUSTAR && t.nota.isNotBlank()) {
                Text("Pediste: ${t.nota}", style = TextoWeb.Chico, color = Web.AmbarTexto)
            }

            if (t.estado == EstadoTestimonio.EN_REVISION) {
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDescartar, modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Web.Borde)
                    ) { Text("Descartar", style = TextoWeb.Chip, color = Web.RojoTexto) }
                    OutlinedButton(
                        onClick = onAjustar, modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Web.Borde)
                    ) { Text("Ajustar", style = TextoWeb.Chip, color = Web.Primario) }
                    Button(
                        onClick = onAprobar, modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Web.Primario)
                    ) { Text("Aprobar", style = TextoWeb.Chip, color = Color.White) }
                }
            } else {
                Text(
                    "Volver a revisión",
                    style = TextoWeb.Chip, color = Web.Primario,
                    modifier = Modifier.padding(top = 4.dp).clickable(onClick = onVolverARevision)
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RevisionPreview() {
    FamiliasQueSumanTheme {
        RevisionTestimoniosScreen(
            testimonios = listOf(
                Testimonio(
                    "t2", "p2", "Regalando Estrellas", "Familia Rodríguez", "ana.rodriguez@correo.com",
                    "Entregamos kits de higiene y los niños se dieron cuenta de lo mucho que pueden aportar.",
                    null, "7 sep 2026"
                )
            ),
            esAdmin = true, onResolver = { _, _, _ -> }, onRegresar = {}
        )
    }
}
