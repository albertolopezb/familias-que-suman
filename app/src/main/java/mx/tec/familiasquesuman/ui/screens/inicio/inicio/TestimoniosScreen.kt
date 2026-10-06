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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Testimonio
import mx.tec.familiasquesuman.ui.components.BarraSuperior
import mx.tec.familiasquesuman.ui.components.FotoUsuario
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/** Los testimonios publicados: solo los que Familias que Suman aprobó (RF-12). */
@Composable
fun TestimoniosScreen(testimonios: List<Testimonio>, onRegresar: () -> Unit) {
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
            items(testimonios, key = { it.id }) { TarjetaTestimonioPublico(it) }
        }
    }
}

@Composable
fun TarjetaTestimonioPublico(t: Testimonio, modifier: Modifier = Modifier) {
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
            ), {}
        )
    }
}
