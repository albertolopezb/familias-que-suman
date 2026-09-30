package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonAmarillo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.ui.screens.inicio.componentes.*
import mx.tec.familiasquesuman.ui.theme.*

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
    Column(modifier = Modifier.fillMaxSize()) {
    // La barra del sitio (menú, logo y ciudad) queda fija arriba.
    EncabezadoApp(ciudad = ciudad, onCiudadClick = onCiudadClick)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisFondo)
            .verticalScroll(rememberScrollState())
    ) {
        // Saludo; la ciudad ya está en la barra de arriba.
        HeaderInicio(
            nombreFamilia = "Familia Rodríguez",
            ciudadActual = ciudad,
            onCiudadSeleccionada = onCambiarCiudad,
            onNotificacionesClick = { },
            mostrarCiudad = false
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Banner Principal (Llamado a la Acción)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = AzulMarinoPrimario)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "LLAMADO A LA ACCIÓN · $ciudad".uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = AmbarAcento
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "¿Listos para sumar este fin de semana?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "3 actividades cerca de ti en $ciudad.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onExplorarClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AmbarAcento),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Explorar ahora →", color = AzulMarinoPrimario)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtítulo
            Text(
                text = "PEQUEÑAS ACCIONES, GRAN IMPACTO",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "¿CÓMO QUIERES AYUDAR HOY?",
                style = MaterialTheme.typography.titleLarge,
                color = AzulMarinoPrimario
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grid de 2x2 de Categorías con Colores Fieles
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaCategoriaUI(
                    titulo = "Actividades en Familia",
                    descripcion = "Actividades en familia para ayudar durante el año.",
                    icono = Icons.Default.Favorite,
                    colorFondo = MoradoCategoriaFondo,
                    colorTexto = MoradoCategoriaTexto,
                    onClick = onActividadesClick,
                    modifier = Modifier.weight(1f)
                )
                TarjetaCategoriaUI(
                    titulo = "Quiero Donar",
                    descripcion = "Apoyo en especie y tiempo.",
                    icono = Icons.Default.Send,
                    colorFondo = VerdeCategoriaFondo,
                    colorTexto = VerdeCategoriaTexto,
                    onClick = onDonarClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaCategoriaUI(
                    titulo = "Proyectos",
                    descripcion = "Proyectos con causas y objetivos específicos.",
                    icono = Icons.Default.LocationOn,
                    colorFondo = AzulCategoriaFondo,
                    colorTexto = AzulCategoriaTexto,
                    onClick = onProyectosClick,
                    modifier = Modifier.weight(1f)
                )
                TarjetaCategoriaUI(
                    titulo = "Directorio de Visiteo",
                    descripcion = "Centros y espacios para visitar y apoyar en familia.",
                    icono = Icons.Default.Place,
                    colorFondo = MentaCategoriaFondo,
                    colorTexto = MentaCategoriaTexto,
                    onClick = onVisiteoClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjetas de Métricas Estadísticas (3 Cards Blancas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricaItem("12", "Actividades realizadas", Modifier.weight(1f))
                MetricaItem("3", "Próximas esta semana", Modifier.weight(1f))
                MetricaItem("4", "Favoritas guardadas", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sección Causas Destacadas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Causas Destacadas",
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulMarinoPrimario
                )
                TextButton(onClick = onExplorarClick) {
                    Text("Ver todas", color = AmbarAcento)
                }
            }

            // Tarjeta Causa 1
            TarjetaCausa(
                nombre = "Banco de Alimentos CDMX",
                etiqueta = "Alimentación",
                descripcion = "Recolección y distribución de alimentos para familias en situación ...",
                esFavorito = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tarjeta Causa 2
            TarjetaCausa(
                nombre = "Tejiendo Redes Educativas",
                etiqueta = "Educación",
                descripcion = "Apoyo escolar y talleres creativos para niños de comunidades marginadas.",
                esFavorito = false
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Sección Tu Próxima Actividad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tu próxima actividad",
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulMarinoPrimario
                )
                TextButton(onClick = onActividadesClick) {
                    Text("Ver agenda", color = AmbarAcento)
                }
            }

            // Banner Próxima Actividad (Inscrita / Mañana)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = AzulMarinoPrimario),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = VerdeConfirmado,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "✓ Inscrita",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mañana",
                            color = AmbarAcento,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Apoyo en Comedor Comunitario",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Cierre tomado del sitio: "Mantente informado" y la historia.
            MantenteInformado(onRecibirInformacion)
            NuestraHistoria(onConoceHistoria)
        }
    }
    }
}

@Composable
private fun MetricaItem(numero: String, etiqueta: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = numero,
                style = MaterialTheme.typography.headlineMedium,
                color = AzulMarinoPrimario
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun TarjetaCausa(
    nombre: String,
    etiqueta: String,
    descripcion: String,
    esFavorito: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF3C7))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulMarinoPrimario
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = etiqueta,
                        color = Color(0xFF92400E),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = if (esFavorito) AmbarAcento else Color.Gray
            )
        }
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
