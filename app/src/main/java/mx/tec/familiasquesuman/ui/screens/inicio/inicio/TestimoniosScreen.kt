package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Testimonio
import mx.tec.familiasquesuman.domain.puedeEliminarlo
import mx.tec.familiasquesuman.ui.components.BarraSuperior
import mx.tec.familiasquesuman.ui.components.FotoUsuario
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * Los testimonios publicados (RF-12). Cada cuenta ve "Eliminar" en los suyos y el admin en
 * todos, para quitar lo que haga falta.
 *
 * @param correoDeLaSesion la cuenta con sesión abierta, o null si no hay.
 */
@Composable
fun TestimoniosScreen(
    testimonios: List<Testimonio>,
    correoDeLaSesion: String?,
    esAdmin: Boolean,
    onEliminar: (Testimonio) -> Unit,
    onRegresar: () -> Unit
) {
    var porEliminar by remember { mutableStateOf<Testimonio?>(null) }
    Column(Modifier.fillMaxSize().background(Web.Fondo)) {
        BarraSuperior("Testimonios", onRegresar = onRegresar)
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (testimonios.isEmpty()) {
                item(key = "vacio") {
                    Text(
                        "Aquí aparecerán las experiencias que compartan las familias después de participar.",
                        style = TextoWeb.Cuerpo, color = Web.TextoApagado, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    )
                }
            }
            items(testimonios, key = { it.id }) { t ->
                TarjetaTestimonioPublico(
                    t,
                    onEliminar = if (t.puedeEliminarlo(correoDeLaSesion, esAdmin)) ({ porEliminar = t }) else null
                )
            }
        }
    }

    porEliminar?.let { t ->
        val esPropio = !esAdmin || t.correo.equals(correoDeLaSesion, ignoreCase = true)
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text(if (esPropio) "¿Eliminar tu testimonio?" else "¿Eliminar este testimonio?") },
            text = {
                Text(
                    if (esPropio) "Dejará de aparecer en la sección de testimonios. Después podrás compartir otro."
                    else "Se quitará de la sección de testimonios para todos. ${t.familia} podrá compartir otro."
                )
            },
            confirmButton = {
                TextButton(onClick = { onEliminar(t); porEliminar = null }) { Text("Sí, eliminar", color = Web.RojoTexto) }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

/** Un testimonio publicado; con [onEliminar] muestra el enlace para quitarlo. */
@Composable
fun TarjetaTestimonioPublico(t: Testimonio, modifier: Modifier = Modifier, onEliminar: (() -> Unit)? = null) {
    Surface(
        shape = RoundedCornerShape(16.dp), color = Web.Tarjeta,
        border = BorderStroke(1.dp, Web.Borde), modifier = modifier.fillMaxWidth()
    ) {
        Column {
            t.foto?.let { FotoUsuario(it, "Foto del testimonio de ${t.familia}", Modifier.fillMaxWidth().height(180.dp)) }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("“${t.experiencia}”", style = TextoWeb.Cuerpo)
                Text("${t.familia} · ${t.fecha}", style = TextoWeb.Chip, color = Web.Primario)
                Text(t.actividadTitulo, style = TextoWeb.Chico)
                if (onEliminar != null) {
                    TextButton(onClick = onEliminar, modifier = Modifier.align(Alignment.End)) {
                        Text("Eliminar", style = TextoWeb.Chip, color = Web.RojoTexto)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TestimoniosPreview() {
    FamiliasQueSumanTheme {
        TestimoniosScreen(
            listOf(
                Testimonio(
                    "t1", "p3", "Regalando Estrellas", "Familia Rodríguez", "ana@correo.com",
                    "Fue la mejor tarde en familia que hemos tenido.", null, "3 ago 2026"
                )
            ),
            correoDeLaSesion = "ana@correo.com", esAdmin = false, onEliminar = {}, onRegresar = {}
        )
    }
}
