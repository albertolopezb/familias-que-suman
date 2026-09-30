package mx.tec.familiasquesuman.ui.screens.admin

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.TemaActividad
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BarraDeRegreso
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonAmarillo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.ChipAportacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Etiqueta
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.FilaDato
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconoTema
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IndicadorCupo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.actividadDeMuestra
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.horaDeInicio
import mx.tec.familiasquesuman.ui.screens.admin.componentes.CampoAdmin
import mx.tec.familiasquesuman.ui.screens.admin.componentes.OpcionAdmin
import mx.tec.familiasquesuman.ui.screens.admin.componentes.RotuloAdmin
import mx.tec.familiasquesuman.ui.screens.admin.componentes.TarjetaNumero
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

// ---------------------------------------------------------------------------
// Lista de actividades (/admin/actividades)
// ---------------------------------------------------------------------------

@Composable
fun AdminActividadesScreen(
    actividades: List<Actividad>,
    onNueva: () -> Unit,
    onEditar: (String) -> Unit,
    onDuplicar: (Actividad) -> Unit,
    onEliminar: (String) -> Unit,
    onRegresar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var porBorrar by remember { mutableStateOf<Actividad?>(null) }

    Column(modifier = modifier.fillMaxSize().background(Web.Fondo)) {
        BarraDeRegreso(texto = "Panel Admin", onRegresar = onRegresar)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "titulo") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Actividades", style = TextoWeb.Titulo)
                        Text(
                            "${actividades.count { !it.yaPaso }} próximas · ${actividades.count { it.yaPaso }} pasadas",
                            style = TextoWeb.Cuerpo,
                            color = Web.TextoApagado
                        )
                    }
                    BotonAmarillo(texto = "Nueva", onClick = onNueva, alto = 40.dp)
                }
            }
            items(actividades, key = { it.id }) { actividad ->
                RenglonActividadAdmin(
                    actividad = actividad,
                    onEditar = { onEditar(actividad.id) },
                    onDuplicar = { onDuplicar(actividad) },
                    onEliminar = { porBorrar = actividad }
                )
            }
        }
    }

    porBorrar?.let { actividad ->
        AlertDialog(
            onDismissRequest = { porBorrar = null },
            title = { Text("¿Eliminar actividad?", style = TextoWeb.TituloTarjeta) },
            text = {
                Text(
                    "\"${actividad.titulo}\" (${actividad.fecha}) deja de verse para las familias. " +
                        "No se puede deshacer.",
                    style = TextoWeb.Cuerpo
                )
            },
            confirmButton = {
                TextButton(onClick = { onEliminar(actividad.id); porBorrar = null }) {
                    Text("Eliminar", color = Web.RojoTexto, style = TextoWeb.Chip)
                }
            },
            dismissButton = {
                TextButton(onClick = { porBorrar = null }) {
                    Text("Cancelar", color = Web.TextoApagado, style = TextoWeb.Chip)
                }
            },
            containerColor = Web.Tarjeta
        )
    }
}

@Composable
private fun RenglonActividadAdmin(
    actividad: Actividad,
    onEditar: () -> Unit,
    onDuplicar: () -> Unit,
    onEliminar: () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (actividad.yaPaso) 0.7f else 1f)
            .clip(forma)
            .background(Web.Tarjeta)
            .border(1.dp, Web.Borde, forma)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconoTema(tema = actividad.tema)
            Text(
                actividad.titulo,
                style = TextoWeb.TituloTarjeta,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ChipAportacion(actividad.aportacion)
            when {
                actividad.yaPaso -> Etiqueta("Pasada", Web.Secundario, Web.TextoApagado)
                actividad.tieneCupo && actividad.sinLugares -> Etiqueta("Cupo lleno", Web.RojoFondo, Web.RojoTexto)
            }
        }
        FilaDato(
            IconosWeb.Calendario,
            listOf(actividad.fecha, horaDeInicio(actividad.horario)).filter { it.isNotBlank() }.joinToString(" · ")
                .ifBlank { "Sin fecha" }
        )
        if (actividad.tieneCupo) IndicadorCupo(actividad)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccionAdmin("Editar", IconosWeb.Lapiz, Web.Primario, onEditar, Modifier.weight(1f))
            AccionAdmin("Duplicar", IconosWeb.Copiar, Web.TextoApagado, onDuplicar, Modifier.weight(1f))
            AccionAdmin("Eliminar", IconosWeb.Basura, Web.RojoTexto, onEliminar, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AccionAdmin(
    texto: String,
    icono: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .clip(forma)
            .border(1.dp, Web.Borde, forma)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(texto, style = TextoWeb.Chip, color = color)
    }
}

// ---------------------------------------------------------------------------
// Formulario (crear / editar), con los campos del sitio
// ---------------------------------------------------------------------------

@Composable
fun FormularioActividadScreen(
    formulario: FormularioActividad,
    organizadores: List<Asociacion>,
    onCambio: ((FormularioActividad) -> FormularioActividad) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val f = formulario
    Column(modifier = modifier.fillMaxSize().background(Web.Fondo)) {
        BarraDeRegreso(texto = "Actividades", onRegresar = onCancelar)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                if (f.id == null) "Nueva actividad" else "Editar actividad",
                style = TextoWeb.Titulo,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (f.ocupados > 0) {
                Etiqueta("${f.ocupados} personas ya inscritas", Web.VerdeFondo, Web.VerdeTexto)
            }

            RotuloAdmin("Datos generales", Modifier.padding(top = 8.dp))
            CampoAdmin("Nombre *", f.nombre, { v -> onCambio { it.copy(nombre = v) } }, error = f.errorNombre)
            CampoAdmin("Fecha", f.fecha, { v -> onCambio { it.copy(fecha = v) } }, placeholder = "sábado, 3 de octubre")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoAdmin("Hora de inicio", f.horaInicio, { v -> onCambio { it.copy(horaInicio = v) } },
                    placeholder = "09:30", modifier = Modifier.weight(1f))
                CampoAdmin("Hora de fin", f.horaFin, { v -> onCambio { it.copy(horaFin = v) } },
                    placeholder = "12:00", modifier = Modifier.weight(1f))
            }
            CampoAdmin("Dirección", f.direccion, { v -> onCambio { it.copy(direccion = v) } })
            CampoAdmin("Punto de encuentro", f.puntoDeEncuentro, { v -> onCambio { it.copy(puntoDeEncuentro = v) } })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoAdmin("Cupo máximo (personas)", f.cupo, { v -> onCambio { it.copy(cupo = v.filter(Char::isDigit)) } },
                    teclado = KeyboardType.Number, error = f.errorCupo, placeholder = "Sin límite",
                    modifier = Modifier.weight(1f))
                CampoAdmin("Ciudad *", f.ciudad, { v -> onCambio { it.copy(ciudad = v) } },
                    error = f.errorCiudad, placeholder = "Monterrey", modifier = Modifier.weight(1f))
            }

            Text("Organiza", style = TextoWeb.Chip.copy(fontWeight = FontWeight.Medium), color = Web.Texto)
            FilaDeOpciones(
                opciones = organizadores.map { it.id to it.nombre },
                seleccionada = f.organizadorId,
                onSeleccion = { id -> onCambio { it.copy(organizadorId = id) } }
            )

            Text("Tema de la actividad", style = TextoWeb.Chip.copy(fontWeight = FontWeight.Medium), color = Web.Texto)
            FilaDeOpciones(
                opciones = listOf(
                    TemaActividad.SALUD to "Salud",
                    TemaActividad.CELEBRACION to "Celebración",
                    TemaActividad.MEDIO_AMBIENTE to "Medio ambiente",
                    TemaActividad.GENERAL to "General"
                ),
                seleccionada = f.tema,
                onSeleccion = { t -> onCambio { it.copy(tema = t) } }
            )

            RotuloAdmin("Aportación", Modifier.padding(top = 8.dp))
            FilaDeOpciones(
                opciones = listOf(
                    TipoAportacion.NINGUNA to "Sin aportación",
                    TipoAportacion.DINERO to "Dinero",
                    TipoAportacion.ESPECIE to "Especie"
                ),
                seleccionada = f.tipoAportacion,
                onSeleccion = { t -> onCambio { it.copy(tipoAportacion = t) } }
            )
            when (f.tipoAportacion) {
                TipoAportacion.DINERO -> {
                    CampoAdmin("Monto ($)", f.monto, { v -> onCambio { it.copy(monto = v) } },
                        teclado = KeyboardType.Number, placeholder = "500")
                    CampoAdmin("Información de pago", f.detalleAportacion,
                        { v -> onCambio { it.copy(detalleAportacion = v) } }, lineas = 2)
                }
                TipoAportacion.ESPECIE -> CampoAdmin(
                    "¿Qué se va a aportar en especie?", f.detalleAportacion,
                    { v -> onCambio { it.copy(detalleAportacion = v) } }, lineas = 2,
                    placeholder = "Ej: 1 kg de arroz, ropa en buen estado, juguetes..."
                )
                TipoAportacion.NINGUNA -> Unit
            }

            RotuloAdmin("Contenido", Modifier.padding(top = 8.dp))
            CampoAdmin("Descripción corta", f.descripcionCorta, { v -> onCambio { it.copy(descripcionCorta = v) } }, lineas = 2)
            CampoAdmin("Descripción larga", f.descripcionLarga, { v -> onCambio { it.copy(descripcionLarga = v) } }, lineas = 3)
            CampoAdmin("¿Qué haremos?", f.queHaremos, { v -> onCambio { it.copy(queHaremos = v) } }, lineas = 2)
            CampoAdmin("¿Qué incluye?", f.queIncluye, { v -> onCambio { it.copy(queIncluye = v) } }, lineas = 2)
            CampoAdmin("¿Qué llevar?", f.queLlevar, { v -> onCambio { it.copy(queLlevar = v) } }, lineas = 2)
            CampoAdmin("Recomendaciones", f.recomendaciones, { v -> onCambio { it.copy(recomendaciones = v) } }, lineas = 3)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Web.Tarjeta)
                    .border(1.dp, Web.Borde, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ya pasó", style = TextoWeb.Cuerpo.copy(fontWeight = FontWeight.Medium))
                    Text("Se muestra en \"Actividades pasadas\", sin botón de unirse.", style = TextoWeb.Chico)
                }
                Switch(
                    checked = f.yaPaso,
                    onCheckedChange = { v -> onCambio { it.copy(yaPaso = v) } },
                    colors = SwitchDefaults.colors(checkedTrackColor = Web.Primario)
                )
            }
            Text(
                "La imagen principal y el logo se suben cuando haya servidor.",
                style = TextoWeb.Chico
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Web.Tarjeta)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Cancelar",
                style = TextoWeb.Boton,
                color = Web.TextoApagado,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Web.Borde, RoundedCornerShape(12.dp))
                    .clickable(onClick = onCancelar)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
            BotonAmarillo(
                texto = "Guardar",
                onClick = onGuardar,
                conFlechas = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun <T> FilaDeOpciones(
    opciones: List<Pair<T, String>>,
    seleccionada: T,
    onSeleccion: (T) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEach { (valor, texto) ->
            OpcionAdmin(texto = texto, seleccionada = valor == seleccionada, onClick = { onSeleccion(valor) })
        }
    }
}

// ---------------------------------------------------------------------------
// Inscritos (/admin/inscritos): Eventos · Familias · Personas
// ---------------------------------------------------------------------------

@Composable
fun AdminInscritosScreen(
    actividades: List<Actividad>,
    onActividadClick: (String) -> Unit,
    onRegresar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conInscritos = actividades.filter { !it.yaPaso && it.tieneCupo && it.ocupados > 0 }
    val personas = conInscritos.sumOf { it.ocupados }
    // Sin servidor no sabemos cuántas familias son: se estima con 3 personas por familia.
    val familias = conInscritos.sumOf { (it.ocupados + 2) / 3 }

    Column(modifier = modifier.fillMaxSize().background(Web.Fondo)) {
        BarraDeRegreso(texto = "Panel Admin", onRegresar = onRegresar)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "titulo") { Text("Inscritos", style = TextoWeb.Titulo) }
            item(key = "numeros") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TarjetaNumero("${conInscritos.size}", "Eventos", Web.Primario, Modifier.weight(1f))
                    TarjetaNumero("$familias", "Familias", Web.Amarillo, Modifier.weight(1f))
                    TarjetaNumero("$personas", "Personas", Web.VerdeTema, Modifier.weight(1f))
                }
            }
            if (conInscritos.isEmpty()) {
                item(key = "vacia") {
                    Text(
                        "No hay inscripciones registradas.",
                        style = TextoWeb.Cuerpo,
                        color = Web.TextoApagado,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
            items(conInscritos, key = { it.id }) { a ->
                val forma = RoundedCornerShape(16.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(forma)
                        .background(Web.Tarjeta)
                        .border(1.dp, Web.Borde, forma)
                        .clickable { onActividadClick(a.id) }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            a.titulo,
                            style = TextoWeb.TituloTarjeta,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Etiqueta("${a.ocupados} personas", Web.Secundario, Web.Primario)
                    }
                    FilaDato(IconosWeb.Calendario, "${a.fecha} · ${horaDeInicio(a.horario)}")
                    IndicadorCupo(a)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun AdminActividadesPreview() {
    FamiliasQueSumanTheme {
        AdminActividadesScreen(
            actividades = listOf(actividadDeMuestra(8).actividad, actividadDeMuestra(0).actividad.copy(id = "b")),
            onNueva = {}, onEditar = {}, onDuplicar = {}, onEliminar = {}, onRegresar = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun FormularioPreview() {
    FamiliasQueSumanTheme {
        FormularioActividadScreen(
            formulario = FormularioActividad(nombre = "Posada Sendero", tipoAportacion = TipoAportacion.ESPECIE),
            organizadores = emptyList(),
            onCambio = {}, onGuardar = {}, onCancelar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InscritosPreview() {
    FamiliasQueSumanTheme {
        AdminInscritosScreen(listOf(actividadDeMuestra(8).actividad), {}, {})
    }
}
