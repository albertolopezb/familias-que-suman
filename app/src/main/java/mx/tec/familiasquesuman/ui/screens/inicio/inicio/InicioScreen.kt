package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.MapaDeCiudad
import mx.tec.familiasquesuman.domain.PuntoEnMapa
import mx.tec.familiasquesuman.domain.cuantoFalta
import mx.tec.familiasquesuman.domain.fechaDeTexto
import mx.tec.familiasquesuman.ui.components.iniciales
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.domain.Testimonio
import mx.tec.familiasquesuman.ui.screens.actividades.MapaGeneral
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoApp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.EncabezadoAzul
import mx.tec.familiasquesuman.ui.state.UiState
import mx.tec.familiasquesuman.ui.screens.inicio.componentes.*
import mx.tec.familiasquesuman.ui.theme.*

@Composable
fun InicioScreen(
    ciudad: String,
    onCambiarCiudad: (String) -> Unit,
    asociacion: Asociacion?,
    nombreFamilia: String?,
    causas: List<Asociacion>,
    favoritas: Set<String>,
    onAlternarFavorita: (String) -> Unit,
    onCausaClick: (String) -> Unit,
    onVerAgenda: () -> Unit,
    onCrearCuenta: () -> Unit,
    onIniciarSesion: () -> Unit,
    onExplorarClick: () -> Unit,
    onVerCausas: () -> Unit = onExplorarClick,
    onActividadesClick: () -> Unit,
    onDonarClick: () -> Unit,
    onProyectosClick: () -> Unit,
    onVisiteoClick: () -> Unit,
    testimonios: List<Testimonio> = emptyList(),
    onVerTestimonios: () -> Unit = {},
    proxima: Actividad? = null,
    cuantasProximas: Int = 0,
    actividadesRealizadas: Int = 0,
    abiertasEnCiudad: Int = 0,
    onProximaClick: (String) -> Unit = {},
    // el mapa general de la ciudad elegida
    mapa: UiState<MapaDeCiudad> = UiState.Cargando,
    onAbrirPunto: (PuntoEnMapa) -> Unit = {},
    onReintentarMapa: () -> Unit = {},
    esAdmin: Boolean = false,
    onIrAlPanel: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().background(GrisFondo)) {
    // El encabezado azul (logo, ciudad y perfil) queda fijo, igual que en las demás pantallas.
    // Sin título: el saludo va debajo, fuera del azul.
    EncabezadoAzul(
        etiqueta = null,
        titulo = null,
        ciudad = ciudad,
        onCiudadSeleccionada = onCambiarCiudad
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisFondo)
            .verticalScroll(rememberScrollState())
    ) {
        // El saludo, fuera del azul.
        HeaderInicio(nombreFamilia = nombreFamilia)

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
                            text = when (abiertasEnCiudad) {
                                0 -> "Por ahora no hay actividades abiertas en $ciudad."
                                1 -> "1 actividad abierta en $ciudad."
                                else -> "$abiertasEnCiudad actividades abiertas en $ciudad."
                            },
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

            // El mapa general de la ciudad, justo después del llamado a la acción.
            // El título con su botón para ocultar o mostrar el mapa (el estado sobrevive a girar el teléfono).
            var mapaVisible by rememberSaveable { mutableStateOf(true) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mapa de $ciudad",
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulBannerFondo,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AzulBannerFondo.copy(alpha = 0.08f))
                        .clickable(onClickLabel = if (mapaVisible) "Ocultar el mapa" else "Mostrar el mapa") { mapaVisible = !mapaVisible }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (mapaVisible) "Ocultar" else "Ver mapa",
                        style = MaterialTheme.typography.labelLarge,
                        color = AzulBannerFondo
                    )
                    Icon(
                        imageVector = if (mapaVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AzulBannerFondo
                    )
                }
            }
            if (mapaVisible) {
                Spacer(modifier = Modifier.height(12.dp))
                MapaGeneral(
                    estado = mapa,
                    onAbrir = onAbrirPunto,
                    onReintentar = onReintentarMapa,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp)
                )
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
                    descripcion = "Para ayudar en familia durante el año.",
                    icono = IconosWeb.Personas,
                    colorTarjeta = MoradoTarjetaFondo,
                    colorFondo = MoradoCategoriaFondo,
                    colorTexto = MoradoCategoriaTexto,
                    onClick = onActividadesClick,
                    modifier = Modifier.weight(1f)
                )
                TarjetaCategoriaUI(
                    titulo = "Quiero Donar",
                    descripcion = "Apoyo en especie y tiempo.",
                    icono = IconosWeb.ManoCorazon,
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
                    descripcion = "Causas con objetivos específicos.",
                    icono = IconosWeb.Foco,
                    colorTarjeta = AzulTarjetaFondo,
                    colorFondo = AzulCategoriaFondo,
                    colorTexto = AzulCategoriaTexto,
                    onClick = onProyectosClick,
                    modifier = Modifier.weight(1f)
                )
                TarjetaCategoriaUI(
                    titulo = "Directorio de Visiteo",
                    descripcion = "Centros para visitar y apoyar en familia.",
                    icono = IconosWeb.Ubicacion,
                    colorTarjeta = MentaTarjetaFondo.copy(alpha = 0.75f),
                    colorFondo = MentaCategoriaFondo,
                    colorTexto = MentaCategoriaTexto,
                    onClick = onVisiteoClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Las métricas son de la familia: solo con sesión, y no para el admin.
            if (nombreFamilia != null && !esAdmin) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricaItem(actividadesRealizadas.toString(), "Actividades realizadas", Modifier.weight(1f))
                    MetricaItem(cuantasProximas.toString(), if (cuantasProximas == 1) "Actividad próxima" else "Actividades próximas", Modifier.weight(1f))
                    MetricaItem(favoritas.size.toString(), "Favoritas guardadas", Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

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
                TextButton(onClick = onVerCausas) {
                    Text("Ver todas", color = AmbarAcento)
                }
            }

            // Tarjetas de Causas
            causas.forEachIndexed { i, causa ->
                if (i > 0) Spacer(modifier = Modifier.height(12.dp))
                TarjetaCausa(
                    nombre = causa.nombre,
                    etiqueta = causa.categoria,
                    descripcion = causa.descripcion,
                    esFavorito = causa.id in favoritas,
                    onClick = { onCausaClick(causa.id) },
                    onAlternarFavorito = { onAlternarFavorita(causa.id) }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Solo testimonios aprobados por Familias que Suman (RF-12).
            if (testimonios.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lo que cuentan las familias",
                        style = MaterialTheme.typography.titleLarge,
                        color = AzulBannerFondo
                    )
                    TextButton(onClick = onVerTestimonios) {
                        Text("Ver todos", color = AmbarAcento)
                    }
                }
                testimonios.forEachIndexed { i, testimonio ->
                    if (i > 0) Spacer(modifier = Modifier.height(12.dp))
                    TarjetaTestimonioPublico(testimonio)
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            if (esAdmin) {
                PanelAdmin(onIrAlPanel)
                Spacer(modifier = Modifier.height(32.dp))
            } else if (nombreFamilia != null) {
                // Sección Tu Próxima Actividad: la de verdad, no una de ejemplo.
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
                    TextButton(onClick = onVerAgenda) {
                        Text("Ver agenda", color = AmbarAcento)
                    }
                }
                ProximaActividad(proxima, cuantasProximas, onClick = { proxima?.let { onProximaClick(it.id) } },
                    onVerActividades = onActividadesClick)
                Spacer(modifier = Modifier.height(32.dp))

            } else {
                InvitacionCuenta(onCrearCuenta = onCrearCuenta, onIniciarSesion = onIniciarSesion)
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
    esFavorito: Boolean,
    onClick: () -> Unit,
    onAlternarFavorito: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
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
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales(nombre),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF92400E)
                )
            }
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
            IconButton(onClick = onAlternarFavorito) {
                Icon(
                    imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (esFavorito) "Quitar de favoritas" else "Guardar en favoritas",
                    tint = if (esFavorito) AmbarAcento else Color.Gray
                )
            }
        }
    }
}

/** Lo que ve quien aún no tiene sesión en lugar de su agenda. */
@Composable
private fun InvitacionCuenta(onCrearCuenta: () -> Unit, onIniciarSesion: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
        colors = CardDefaults.cardColors(containerColor = AzulBannerFondo),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Suma con tu familia", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Crea tu cuenta para inscribirte, ver tu agenda y guardar tus favoritas.",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCrearCuenta,
                colors = ButtonDefaults.buttonColors(containerColor = AmbarAcento),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear cuenta", color = AzulBannerFondo, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onIniciarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Ya tengo cuenta · Iniciar sesión", color = Color.White)
            }
        }
    }
}

/** La próxima actividad de la familia, o un aviso claro de que no tiene ninguna. */
@Composable
private fun ProximaActividad(actividad: Actividad?, total: Int, onClick: () -> Unit, onVerActividades: () -> Unit) {
    if (actividad == null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AzulBannerFondo),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Aún no tienen actividades próximas", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text("Elijan una actividad y súmense en familia.", color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onVerActividades, colors = ButtonDefaults.buttonColors(containerColor = AmbarAcento),
                    shape = RoundedCornerShape(12.dp)) {
                    Text("Ver actividades", color = AzulBannerFondo, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }
    val dia = fechaDeTexto(actividad.fecha)
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AzulBannerFondo),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = VerdeConfirmado, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = "✓ Inscritos",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (dia != null) {
                    Text(text = cuantoFalta(dia), color = AmbarAcento, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.weight(1f))
                if (total > 1) {
                    Text("+${total - 1} más", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                }
            }
            Text(text = actividad.titulo, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${actividad.fecha.replaceFirstChar { it.uppercase() }} · ${actividad.horario}",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/** El admin no se inscribe: en lugar de su agenda ve el acceso a su panel. */
@Composable
private fun PanelAdmin(onIrAlPanel: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AzulBannerFondo),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("MODO ADMINISTRADOR", color = AmbarAcento, style = MaterialTheme.typography.labelMedium)
            Text("Revisa lo que mandaron las familias", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(
                "Sugerencias, testimonios y respuestas de encuestas están en tu panel. " +
                    "En cada sección puedes crear, editar o borrar con el botón +.",
                color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onIrAlPanel, colors = ButtonDefaults.buttonColors(containerColor = AmbarAcento),
                shape = RoundedCornerShape(12.dp)) {
                Text("Ir a mi panel", color = AzulBannerFondo, fontWeight = FontWeight.Bold)
            }
        }
    }
}
