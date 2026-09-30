package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonAmarillo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/**
 * El Inicio (P-01), calcado de familiasquesuman.com: "¿Cómo quieres ayudar hoy?"
 * con sus cuatro tarjetas, "Mantente informado" y "Conoce nuestra historia".
 */
@Composable
fun InicioScreen(
    ciudad: String,
    onCambiarCiudad: (String) -> Unit,
    asociacion: Asociacion?,
    onExplorarClick: () -> Unit,
    onActividadesClick: () -> Unit,
    onDonarClick: () -> Unit,
    onProyectosClick: () -> Unit,
    onVisiteoClick: () -> Unit,
    onCiudadClick: () -> Unit = {},
    onRecibirInformacion: () -> Unit = {},
    onConoceHistoria: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().background(Web.Fondo)) {
        EncabezadoApp(ciudad = ciudad, onCiudadClick = onCiudadClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Pequeñas acciones, gran impacto",
                style = TextoWeb.Cuerpo,
                color = Web.TextoApagado,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                "¿CÓMO QUIERES AYUDAR HOY?",
                style = TextoWeb.Titulo.copy(fontSize = 20.sp, letterSpacing = 0.5.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Separador()

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaAyuda(
                    titulo = "Actividades en Familia",
                    descripcion = "Actividades en familia para ayudar durante el año.",
                    icono = IconosWeb.Personas,
                    color = Color(0xFF9333EA), borde = Color(0xFFE9D5FF), fondo = Color(0xFFF3E8FF),
                    onClick = onActividadesClick, modifier = Modifier.weight(1f)
                )
                TarjetaAyuda(
                    titulo = "Quiero Donar",
                    descripcion = "Apoyo en especie y tiempo.",
                    icono = IconosWeb.ManoCorazon,
                    color = Color(0xFF16A34A), borde = Color(0xFFBBF7D0), fondo = Color(0xFFDCFCE7),
                    onClick = onDonarClick, modifier = Modifier.weight(1f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                TarjetaAyuda(
                    titulo = "Proyectos",
                    descripcion = "Proyectos con causas y objetivos específicos.",
                    icono = IconosWeb.Foco,
                    color = Color(0xFF2563EB), borde = Color(0xFFBFDBFE), fondo = Color(0xFFDBEAFE),
                    onClick = onProyectosClick, modifier = Modifier.weight(1f)
                )
                TarjetaAyuda(
                    titulo = "Directorio de Visiteo",
                    descripcion = "Centros y espacios para visitar y apoyar en familia.",
                    icono = IconosWeb.Ubicacion,
                    color = Color(0xFF0D9488), borde = Color(0xFF99F6E4), fondo = Color(0xFFCCFBF1),
                    onClick = onVisiteoClick, modifier = Modifier.weight(1f)
                )
            }

            MantenteInformado(onRecibirInformacion)
            NuestraHistoria(onConoceHistoria)
        }
    }
}

/** La línea con un punto al centro que va debajo del título. */
@Composable
private fun Separador() {
    Row(
        modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.width(64.dp).height(1.dp).background(Web.Borde))
        Box(Modifier.size(9.dp).clip(CircleShape).background(Web.Primario))
        Box(Modifier.width(64.dp).height(1.dp).background(Web.Borde))
    }
}

@Composable
private fun TarjetaAyuda(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    color: Color,
    borde: Color,
    fondo: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .heightIn(min = 220.dp)
            .clip(forma)
            .background(Web.Tarjeta)
            .border(2.dp, borde, forma)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(fondo),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(30.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                titulo,
                style = TextoWeb.Seccion.copy(fontSize = 15.sp, lineHeight = 19.sp),
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                IconosWeb.FlechaDerecha,
                contentDescription = null,
                tint = color,
                modifier = Modifier.padding(start = 4.dp).size(14.dp)
            )
        }
        Text(
            descripcion,
            style = TextoWeb.Chico.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MantenteInformado(onRecibirInformacion: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(top = 32.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Web.Primario)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(IconosWeb.Campana, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Text("Mantente informado", style = TextoWeb.Titulo.copy(fontSize = 20.sp), color = Color.White)
        Text(
            "Recibe información sobre actividades, proyectos y oportunidades para ayudar.",
            style = TextoWeb.Cuerpo,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
        BotonAmarillo(
            texto = "Quiero recibir información",
            onClick = onRecibirInformacion,
            conFlechas = false,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun NuestraHistoria(onConoceHistoria: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFFDF6EC))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(IconosWeb.Corazon, contentDescription = null, tint = Web.Amarillo, modifier = Modifier.size(22.dp))
        Text("No se trata solo de ayudar...", style = TextoWeb.Cuerpo.copy(fontSize = 15.sp))
        Text(
            "se trata de hacerlo juntos.",
            style = TextoWeb.Titulo.copy(fontSize = 18.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold),
            color = Color(0xFFD99A1E)
        )
        Box(Modifier.padding(vertical = 12.dp).fillMaxWidth().height(1.dp).background(Color(0xFFF1E4CF)))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Web.Tarjeta)
                    .border(1.dp, Web.Borde, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(IconosWeb.EscudoPalomita, contentDescription = null, tint = Web.Amarillo, modifier = Modifier.size(22.dp))
            }
            Column {
                Text("Centros verificados", style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.SemiBold))
                Text("por Familias que Suman", style = TextoWeb.Chico)
            }
        }
    }

    Column(
        modifier = Modifier.padding(top = 32.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("¿De dónde venimos? ¿Qué nos mueve?", style = TextoWeb.Chico)
        Row(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onConoceHistoria).padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Conoce nuestra historia", style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.SemiBold), color = Web.Primario)
            Icon(IconosWeb.FlechaDerecha, contentDescription = null, tint = Web.Primario, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun InicioPreview() {
    FamiliasQueSumanTheme {
        InicioScreen(
            ciudad = "Monterrey",
            onCambiarCiudad = {},
            asociacion = null,
            onExplorarClick = {},
            onActividadesClick = {},
            onDonarClick = {},
            onProyectosClick = {},
            onVisiteoClick = {}
        )
    }
}
