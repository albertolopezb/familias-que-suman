package mx.tec.familiasquesuman.ui.screens.perfil.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import mx.tec.familiasquesuman.domain.Acompanante
import mx.tec.familiasquesuman.domain.Sexo
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPrimario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonSecundario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.CalendarioNacimiento
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.CampoFechaNacimiento
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.CampoTexto
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.HojaInferior
import mx.tec.familiasquesuman.ui.screens.inscripcion.edadEnAnios
import mx.tec.familiasquesuman.ui.theme.AcentoSuave
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave
import java.time.LocalDate

/**
 * "Mi familia" en Mi Perfil: las personas registradas y el botón para agregar a otra.
 * Las registradas aparecen solas al inscribirse a una actividad.
 */
@Composable
fun SeccionFamilia(
    familiares: List<Acompanante>,
    onAgregar: (Acompanante) -> Unit,
    onQuitar: (Acompanante) -> Unit,
    modifier: Modifier = Modifier
) {
    var verFormulario by rememberSaveable { mutableStateOf(false) }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (familiares.isEmpty()) {
            Text(
                "Registra a tu familia una sola vez y aparecerá lista cada vez que te inscribas a una actividad.",
                style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave
            )
        } else {
            familiares.forEach { persona -> FilaFamiliar(persona, onQuitar = { onQuitar(persona) }) }
        }
        BotonSecundario("+ Registrar familia", onClick = { verFormulario = true })
    }

    if (verFormulario) {
        FormularioFamiliar(
            onGuardar = { onAgregar(it); verFormulario = false },
            onCerrar = { verFormulario = false }
        )
    }
}

@Composable
private fun FilaFamiliar(persona: Acompanante, onQuitar: () -> Unit) {
    val edad = persona.fechaNacimiento?.let { edadEnAnios(it) } ?: persona.edad.takeIf { it > 0 }
    val detalle = listOfNotNull(
        persona.sexo?.etiqueta,
        edad?.let { if (it == 1) "1 año" else "$it años" }
    ).joinToString(" · ")
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Superficie,
        border = BorderStroke(1.dp, Borde)
    ) {
        Row(
            Modifier.padding(start = 14.dp, top = 6.dp, bottom = 6.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(persona.nombre, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp), color = Tinta)
                if (detalle.isNotEmpty()) {
                    Text(detalle, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                }
            }
            IconButton(onClick = onQuitar) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Quitar a ${persona.nombre} de mi familia",
                    tint = TintaSuave,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** Hoja con nombre, sexo y fecha de nacimiento (que se elige en el calendario). */
@Composable
private fun FormularioFamiliar(onGuardar: (Acompanante) -> Unit, onCerrar: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var sexoElegido by rememberSaveable { mutableStateOf<String?>(null) }
    var diaNacimiento by rememberSaveable { mutableStateOf<Long?>(null) }
    var verCalendario by rememberSaveable { mutableStateOf(false) }

    val sexo = sexoElegido?.let { Sexo.valueOf(it) }
    val fecha = diaNacimiento?.let { LocalDate.ofEpochDay(it) }
    val errorNombre = if (nombre.isNotEmpty() && nombre.trim().length < 3) "Escribe su nombre" else null
    val puedeGuardar = nombre.trim().length >= 3 && sexo != null && fecha != null

    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        HojaInferior(onCerrar = onCerrar, conVelo = false) {
            Text("Registrar familia", style = MaterialTheme.typography.titleMedium, color = Tinta)
            Spacer(Modifier.height(4.dp))
            Text(
                "Cuando te inscribas a una actividad, esta persona aparecerá ya anotada.",
                style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave
            )
            Spacer(Modifier.height(16.dp))
            CampoTexto(
                etiqueta = "Nombre",
                valor = nombre,
                onValorChange = { nombre = it },
                placeholder = "Nombre completo",
                error = errorNombre
            )
            Spacer(Modifier.height(14.dp))
            Text("Sexo", style = MaterialTheme.typography.labelLarge, color = Tinta)
            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Sexo.entries.chunked(2).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        fila.forEach { opcion ->
                            OpcionSexo(
                                texto = opcion.etiqueta,
                                elegida = sexo == opcion,
                                onClick = { sexoElegido = opcion.name },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Fecha de nacimiento", style = MaterialTheme.typography.labelLarge, color = Tinta)
            Spacer(Modifier.height(6.dp))
            CampoFechaNacimiento(fecha = fecha, onClick = { verCalendario = true }, error = null, habilitado = true)
            Spacer(Modifier.height(20.dp))
            BotonPrimario(
                texto = "Guardar",
                onClick = {
                    if (sexo != null && fecha != null) {
                        onGuardar(Acompanante(nombre.trim(), edadEnAnios(fecha), fecha, sexo))
                    }
                },
                habilitado = puedeGuardar
            )
        }
    }
    if (verCalendario) {
        CalendarioNacimiento(
            fecha = fecha,
            onElegir = { diaNacimiento = it.toEpochDay(); verCalendario = false },
            onCerrar = { verCalendario = false }
        )
    }
}

@Composable
private fun OpcionSexo(texto: String, elegida: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (elegida) AcentoSuave else Superficie,
        border = BorderStroke(if (elegida) 1.5.dp else 1.dp, if (elegida) MarcaAzul else Borde)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                texto,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = if (elegida) AcentoTexto else Tinta
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun SeccionFamiliaPreview() {
    FamiliasQueSumanTheme {
        Column(Modifier.padding(16.dp)) {
            SeccionFamilia(
                familiares = listOf(
                    Acompanante("Mateo Rodríguez", 9, LocalDate.now().minusYears(9), Sexo.MASCULINO),
                    Acompanante("Renata Rodríguez", 7, LocalDate.now().minusYears(7), Sexo.FEMENINO)
                ),
                onAgregar = {}, onQuitar = {}
            )
        }
    }
}
