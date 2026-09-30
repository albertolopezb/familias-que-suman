package mx.tec.familiasquesuman.ui.screens.inscripcion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraAccionesInferior
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraConRegreso
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonAgregarPunteado
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPrimario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.Casilla
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.FilaAcompanante
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.FilaAcompananteNueva
import mx.tec.familiasquesuman.domain.Acompanante
import mx.tec.familiasquesuman.ui.theme.ConfirmadoFondo
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.ErrorFondo
import mx.tec.familiasquesuman.ui.theme.ErrorTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * P-06 · ¿Quiénes van? y P-06b · Enviando (todo en gris y "Reservando tus lugares…").
 * La hoja de P-08 se dibuja encima con SinLugaresScreen.
 */
@Composable
fun AcompanantesScreen(
    ui: AcompanantesUi,
    onAgregarFila: () -> Unit,
    onQuitarFila: (Int) -> Unit,
    onNombreChange: (Int, String) -> Unit,
    onEdadChange: (Int, String) -> Unit,
    onConsentimientoChange: (Boolean) -> Unit,
    onConfirmar: () -> Unit,
    onRegresar: () -> Unit
) {
    val a = ui.actividad
    Column(Modifier.fillMaxSize().background(Fondo).imePadding()) {
        BarraConRegreso("¿Quiénes van?", onRegresar)

        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(if (ui.enviando) 0.5f else 1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "${a.titulo} · ${fechaCorta(a.fecha)}, ${horaDeInicio(a.horario)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave
            )
            FilaAcompanante("${ui.titular.nombre} (tú)", edadParaMostrar(ui.titular, esTitular = true))
            ui.filas.forEach { fila ->
                if (fila.nueva) {
                    FilaAcompananteNueva(
                        nombre = fila.nombre,
                        edad = fila.edad,
                        onNombreChange = { onNombreChange(fila.id, it) },
                        onEdadChange = { onEdadChange(fila.id, it) },
                        onQuitar = { onQuitarFila(fila.id) },
                        errorNombre = ui.errorNombre(fila),
                        errorEdad = ui.errorEdad(fila),
                        habilitada = !ui.enviando
                    )
                } else {
                    FilaAcompanante(
                        nombre = fila.nombre,
                        edad = edadParaMostrar(Acompanante(fila.nombre, fila.edad.toIntOrNull() ?: 0), esTitular = false),
                        error = ui.errorEdad(fila),
                        onQuitar = if (ui.enviando) null else ({ onQuitarFila(fila.id) })
                    )
                }
            }
            BotonAgregarPunteado("+ Agregar acompañante", onAgregarFila, habilitado = !ui.enviando)
            if (ui.hayMenores) {
                Casilla(
                    marcada = ui.consentimiento,
                    onCambio = onConsentimientoChange,
                    texto = "Soy la madre, padre o tutora de los menores que registré y autorizo su participación.",
                    habilitada = !ui.enviando
                )
            }
        }

        PieDeLugares(ui, Modifier.padding(horizontal = 16.dp, vertical = 10.dp))

        BarraAccionesInferior {
            BotonPrimario(
                texto = if (ui.enviando) "Reservando tus lugares…" else "Confirmar inscripción",
                onClick = onConfirmar,
                habilitado = ui.puedeConfirmar,
                cargando = ui.enviando
            )
        }
    }
}

/** El pie verde "Vas a ocupar 3 de los 8 lugares disponibles"; rojo si no alcanzan. */
@Composable
private fun PieDeLugares(ui: AcompanantesUi, modifier: Modifier = Modifier) {
    val (texto, fondo, color) = when {
        ui.lugaresDisponibles <= 0 ->
            Triple("Ya no quedan lugares en esta actividad.", ErrorFondo, ErrorTexto)
        ui.excedeLugares ->
            Triple(
                "Son ${ui.personas} personas y solo ${if (ui.lugaresDisponibles == 1) "queda" else "quedan"} " +
                    "${lugares(ui.lugaresDisponibles)}. Quita a alguien para continuar.",
                ErrorFondo, ErrorTexto
            )
        else ->
            Triple(
                "Vas a ocupar ${ui.personas} de los ${lugares(ui.lugaresDisponibles)} disponibles",
                ConfirmadoFondo, ConfirmadoTexto
            )
    }
    Text(
        texto,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(fondo)
            .padding(12.dp)
    )
}

private val uiDePrueba = AcompanantesUi(
    actividad = VistaPrevia.actividad,
    titular = VistaPrevia.titular,
    filas = VistaPrevia.acompanantes.mapIndexed { i, p -> FilaUi(i, p.nombre, "${p.edad}", nueva = false) },
    lugaresDisponibles = 8,
    consentimiento = true
)

@Preview(showBackground = true, heightDp = 780, name = "P-06 · Acompañantes")
@Composable
private fun AcompanantesPreview() {
    FamiliasQueSumanTheme { AcompanantesScreen(uiDePrueba, {}, {}, { _, _ -> }, { _, _ -> }, {}, {}, {}) }
}

@Preview(showBackground = true, heightDp = 780, name = "P-06 · Fila nueva con error")
@Composable
private fun AcompanantesFilaNuevaPreview() {
    FamiliasQueSumanTheme {
        AcompanantesScreen(
            uiDePrueba.copy(filas = uiDePrueba.filas + FilaUi(9, "Leo", "4", nueva = true)),
            {}, {}, { _, _ -> }, { _, _ -> }, {}, {}, {}
        )
    }
}

@Preview(showBackground = true, heightDp = 780, name = "P-06b · Enviando")
@Composable
private fun AcompanantesEnviandoPreview() {
    FamiliasQueSumanTheme {
        AcompanantesScreen(uiDePrueba.copy(enviando = true), {}, {}, { _, _ -> }, { _, _ -> }, {}, {}, {})
    }
}
