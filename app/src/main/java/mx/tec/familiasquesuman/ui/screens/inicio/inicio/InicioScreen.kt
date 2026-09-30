package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.R
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
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
    onVisiteoClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
    // La barra de arriba (logo y perfil) queda fija, igual que en las demás pantallas.
    EncabezadoApp()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisFondo)
            .verticalScroll(rememberScrollState())
    ) {
        // Saludo y selector de ciudad; el perfil ya está en la barra de arriba.
        HeaderInicio(
            nombreFamilia = "Familia Rodríguez",
            ciudadActual = ciudad,
            onCiudadSeleccionada = onCambiarCiudad,
            onNotificacionesClick = { },
            mostrarPerfil = false
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Banner Principal con Imagen de fondo + Overlay azul transparente
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fotobanner),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )

                    // 2. Overlay azul semitransparente
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(AzulBannerFondo.copy(alpha = 0.67f))
                    )

                    // 3. Contenido
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
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onExplorarClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AmbarAcento),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Explorar ahora →",
                                color = AzulBannerFondo,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                color = AzulBannerFondo
            )

            Spacer(modifier = Modifier.height(16.dp))

            // FILA 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaCategoriaUI(
                    titulo = "Actividades en Familia",
                    descripcion = "Actividades en familia para ayudar durante el año.",
                    icono = Icons.Default.Favorite,
                    colorTarjeta = MoradoTarjetaFondo,
                    colorFondo = MoradoCategoriaFondo,
                    colorTexto = MoradoCategoriaTexto,
                    onClick = onActividadesClick,
                    modifier = Modifier.weight(1f)
                )
                TarjetaCategoriaUI(
                    titulo = "Quiero Donar",
                    descripcion = "Apoyo en especie y tiempo.",
                    icono = Icons.Default.Send,
                    colorTarjeta = VerdeTarjetaFondo,
                    colorFondo = VerdeCategoriaFondo,
                    colorTexto = VerdeCategoriaTexto,
                    onClick = onDonarClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FILA 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaCategoriaUI(
                    titulo = "Proyectos",
                    descripcion = "Proyectos con causas y objetivos específicos.",
                    icono = Icons.Default.LocationOn,
                    colorTarjeta = AzulTarjetaFondo,
                    colorFondo = AzulCategoriaFondo,
                    colorTexto = AzulCategoriaTexto,
                    onClick = onProyectosClick,
                    modifier = Modifier.weight(1f)
                )
                TarjetaCategoriaUI(
                    titulo = "Directorio de Visiteo",
                    descripcion = "Centros y espacios para visitar y apoyar en familia.",
                    icono = Icons.Default.Place,
                    colorTarjeta = MentaTarjetaFondo.copy(alpha = 0.75f),
                    colorFondo = MentaCategoriaFondo,
                    colorTexto = MentaCategoriaTexto,
                    onClick = onVisiteoClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjetas de Métricas Estadísticas
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
                    color = AzulBannerFondo
                )
                TextButton(onClick = onExplorarClick) {
                    Text("Ver todas", color = AmbarAcento)
                }
            }

            // Tarjetas de Causas
            TarjetaCausa(
                nombre = "Banco de Alimentos CDMX",
                etiqueta = "Alimentación",
                descripcion = "Recolección y distribución de alimentos para familias en situación ...",
                esFavorito = true
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    color = AzulBannerFondo
                )
                TextButton(onClick = onActividadesClick) {
                    Text("Ver agenda", color = AmbarAcento)
                }
            }

            // Banner Próxima Actividad
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = AzulBannerFondo),
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
                color = AzulBannerFondo
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
                    color = AzulBannerFondo
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