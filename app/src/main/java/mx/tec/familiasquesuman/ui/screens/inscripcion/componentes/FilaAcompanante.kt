package mx.tec.familiasquesuman.ui.screens.inscripcion.componentes

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.inscripcion.edadEnAnios
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorRojo
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Redondeo = RoundedCornerShape(12.dp)

/**
 * Un renglón de P-06. La primera fila (la persona de la cuenta) va sin ×.
 * Las demás llevan × para quitarlas. Si la edad no alcanza, el renglón se pinta en rojo.
 */
@Composable
fun FilaAcompanante(
    nombre: String,
    edad: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    onQuitar: (() -> Unit)? = null
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Redondeo)
                .background(if (error != null) ErrorFondo.copy(alpha = 0.45f) else Superficie)
                .border(1.dp, if (error != null) ErrorRojo else Borde, Redondeo)
                .padding(start = 14.dp, end = if (onQuitar != null) 2.dp else 14.dp)
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                nombre,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = Tinta,
                modifier = Modifier.weight(1f)
            )
            Text(edad, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
            if (onQuitar != null) {
                IconButton(onClick = onQuitar) {
                    Icon(Icons.Filled.Close, contentDescription = "Quitar a $nombre", tint = TintaSuave, modifier = Modifier.size(18.dp))
                }
            }
        }
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error, style = MaterialTheme.typography.bodyMedium, color = ErrorRojo)
        }
    }
}

/**
 * La fila recién agregada: el nombre se escribe ahí mismo y la fecha de nacimiento se elige
 * en un calendario (no se teclea la edad). Junto a la fecha se ve la edad que resulta.
 */
@Composable
fun FilaAcompananteNueva(
    nombre: String,
    fechaNacimiento: LocalDate?,
    onNombreChange: (String) -> Unit,
    onFechaNacimientoChange: (LocalDate) -> Unit,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier,
    errorNombre: String? = null,
    errorEdad: String? = null,
    habilitada: Boolean = true
) {
    var verCalendario by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Redondeo)
            .background(Superficie)
            .border(1.dp, Borde, Redondeo)
            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            CampoTexto(
                etiqueta = "",
                valor = nombre,
                onValorChange = onNombreChange,
                placeholder = "Nombre completo",
                error = errorNombre,
                habilitado = habilitada,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onQuitar, enabled = habilitada) {
                Icon(Icons.Filled.Close, contentDescription = "Quitar esta fila", tint = TintaSuave, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        CampoFechaNacimiento(
            fecha = fechaNacimiento,
            onClick = { verCalendario = true },
            error = errorEdad,
            habilitado = habilitada,
            modifier = Modifier.padding(end = 48.dp)
        )
    }
    if (verCalendario) {
        CalendarioNacimiento(
            fecha = fechaNacimiento,
            onElegir = { onFechaNacimientoChange(it); verCalendario = false },
            onCerrar = { verCalendario = false }
        )
    }
}

private val Espanol = Locale("es", "MX")
private val FormatoFecha = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Espanol)

/** Se ve como un campo de texto, pero al tocarlo abre el calendario. */
@Composable
private fun CampoFechaNacimiento(
    fecha: LocalDate?,
    onClick: () -> Unit,
    error: String?,
    habilitado: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(Redondeo)
                .background(if (error != null) ErrorFondo.copy(alpha = 0.45f) else Superficie)
                .border(1.dp, if (error != null) ErrorRojo else Borde, Redondeo)
                .clickable(enabled = habilitado, onClickLabel = "Elegir fecha de nacimiento", onClick = onClick)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(IconosWeb.Calendario, contentDescription = null, tint = MarcaAzul, modifier = Modifier.size(18.dp))
            Text(
                fecha?.format(FormatoFecha) ?: "Fecha de nacimiento",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = if (fecha != null) Tinta else TintaSuave.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f)
            )
            if (fecha != null) {
                val edad = edadEnAnios(fecha)
                Text(if (edad == 1) "1 año" else "$edad años", style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
            }
        }
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error, style = MaterialTheme.typography.bodyMedium, color = ErrorRojo)
        }
    }
}

/**
 * El calendario de Material: solo deja elegir fechas de hoy hacia atrás. Va siempre en
 * español (meses, días y encabezado), aunque el teléfono esté en otro idioma.
 */
@Composable
private fun CalendarioNacimiento(
    fecha: LocalDate?,
    onElegir: (LocalDate) -> Unit,
    onCerrar: () -> Unit
) {
    // El calendario toma el idioma de la configuración al crear su estado: se cambia antes.
    val configuracion = LocalConfiguration.current
    val enEspanol = remember(configuracion) { Configuration(configuracion).apply { setLocale(Espanol) } }
    CompositionLocalProvider(LocalConfiguration provides enEspanol) {
        DialogoCalendario(fecha, onElegir, onCerrar)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoCalendario(
    fecha: LocalDate?,
    onElegir: (LocalDate) -> Unit,
    onCerrar: () -> Unit
) {
    val hoy = LocalDate.now()
    val hoyMillis = hoy.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = fecha?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        yearRange = (hoy.year - 100)..hoy.year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= hoyMillis
            override fun isSelectableYear(year: Int) = year <= hoy.year
        }
    )
    DatePickerDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(
                onClick = {
                    estado.selectedDateMillis?.let { millis ->
                        onElegir(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
                enabled = estado.selectedDateMillis != null
            ) { Text("Aceptar", color = MarcaAzul) }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar", color = MarcaAzul) } }
    ) {
        DatePicker(
            state = estado,
            title = {
                Text(
                    "Fecha de nacimiento",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            },
            headline = {
                val elegida = estado.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                Text(
                    elegida?.format(FormatoFecha) ?: "Elige el día",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp)
                )
            },
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = MarcaAzul,
                selectedYearContainerColor = MarcaAzul,
                todayDateBorderColor = MarcaAzul,
                todayContentColor = MarcaAzul
            )
        )
    }
}

/** El botón con borde punteado de "+ Agregar acompañante". */
@Composable
fun BotonAgregarPunteado(texto: String, onClick: () -> Unit, habilitado: Boolean = true) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(Redondeo)
            .drawBehind {
                drawRoundRect(
                    color = TintaSuave.copy(alpha = 0.45f),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 5.dp.toPx()))
                    )
                )
            }
            .clickable(enabled = habilitado, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), color = MarcaAzul)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun FilasPreview() {
    FamiliasQueSumanTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FilaAcompanante("Ana Rodríguez (tú)", "38")
            FilaAcompanante("Mateo Rodríguez", "9 años", onQuitar = {})
            FilaAcompanante("Renata Rodríguez", "4 años", error = "La edad mínima es de 6 años", onQuitar = {})
            FilaAcompananteNueva("", null, {}, {}, {})
            FilaAcompananteNueva("Leo Rodríguez", LocalDate.now().minusYears(4), {}, {}, {}, errorEdad = "La edad mínima para esta actividad es de 6 años")
            BotonAgregarPunteado("+ Agregar acompañante", {})
        }
    }
}
