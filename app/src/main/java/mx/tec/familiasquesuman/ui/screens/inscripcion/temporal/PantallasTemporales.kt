package mx.tec.familiasquesuman.ui.screens.inscripcion.temporal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.ui.screens.inscripcion.Sesion
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BarraConRegreso
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonPrimario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.BotonSecundario
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.Casilla
import mx.tec.familiasquesuman.ui.screens.inscripcion.componentes.NotaAmbar
import mx.tec.familiasquesuman.ui.theme.AcentoTexto
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.ErrorRojo
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

// ⚠ TEMPORAL: ver GrafoTemporalParte3.kt. Se borra al integrar las partes 2 y 5.

@Composable
private fun AvisoTemporal(parte: String) {
    NotaAmbar(
        "Pantalla provisional para probar la parte 3. La real es de la $parte y la reemplaza al integrar.",
        etiqueta = "Temporal"
    )
}

@Composable
private fun Tarjeta(onClick: (() -> Unit)? = null, contenido: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Superficie)
            .border(1.dp, Borde, RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp)
    ) { contenido() }
}

@Composable
private fun Seccion(texto: String) {
    Spacer(Modifier.height(6.dp))
    Text(texto.uppercase(), style = MaterialTheme.typography.labelSmall, color = AcentoTexto)
}

/** Parte 2 · P-02 y P-28 en una sola lista, lo justo para entrar y cancelar. */
@Composable
fun ActividadesTemporalScreen(
    actividades: List<Pair<Actividad, Int>>,
    inscritas: List<Actividad>,
    onVerDetalle: (String) -> Unit,
    onCasoDePrueba: (actividadId: String, simularSinCupo: Boolean) -> Unit,
    onCancelar: (String) -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(Fondo).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Actividades", style = MaterialTheme.typography.headlineMedium, color = Tinta)
        AvisoTemporal("parte 2")

        Seccion("Casos de prueba de la parte 3")
        BotonPrimario("✓ Inscripción exitosa · Preparar despensas", { onCasoDePrueba("act1", false) })
        BotonSecundario("✗ Se acabaron los lugares · Tarde de lectura", { onCasoDePrueba("act2", true) })
        BotonSecundario("! Edad mínima de 15 años · Clasificar ropa", { onCasoDePrueba("act3", false) })

        if (inscritas.isNotEmpty()) {
            Seccion("Mis próximas · inscritas")
            inscritas.forEach { a ->
                Tarjeta {
                    Text(a.titulo, style = MaterialTheme.typography.titleMedium, color = Tinta)
                    Text("${a.fecha} · ${a.horario}", style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onCancelar(a.id) }) {
                            Text("Cancelar inscripción", color = ErrorRojo, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        Seccion("Esta semana")
        actividades.forEach { (a, disponibles) ->
            Tarjeta(onClick = { onVerDetalle(a.id) }) {
                Text(a.titulo, style = MaterialTheme.typography.titleMedium, color = Tinta)
                Text(
                    "${a.fecha} · $disponibles de ${a.cupoTotal} lugares" +
                        (a.edadMinima?.let { " · desde $it años" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TintaSuave
                )
            }
        }
    }
}

/** Parte 2 · P-03, lo mínimo: datos y el botón "Inscribirme" que entra a la parte 3. */
@Composable
fun DetalleTemporalScreen(
    actividad: Actividad,
    lugaresDisponibles: Int,
    yaInscritos: Boolean,
    simularSinCupo: Boolean,
    onSimularSinCupoChange: (Boolean) -> Unit,
    onInscribirme: () -> Unit,
    onRegresar: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Fondo)) {
        BarraConRegreso("Detalle de la actividad", onRegresar)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AvisoTemporal("parte 2")
            Text(actividad.titulo, style = MaterialTheme.typography.headlineMedium, color = Tinta)
            Text("${actividad.fecha}, ${actividad.horario}", style = MaterialTheme.typography.bodyLarge, color = Tinta)
            Text(actividad.direccion, style = MaterialTheme.typography.bodyLarge, color = Tinta)
            actividad.edadMinima?.let {
                Text("Apta para niños desde $it años", style = MaterialTheme.typography.bodyLarge, color = Tinta)
            }
            Text("Quedan $lugaresDisponibles de ${actividad.cupoTotal} lugares", style = MaterialTheme.typography.titleMedium, color = Tinta)
            Text(actividad.descripcion, style = MaterialTheme.typography.bodyLarge, color = TintaSuave)
            Casilla(
                marcada = simularSinCupo,
                onCambio = onSimularSinCupoChange,
                texto = "Prueba: al confirmar, otra familia gana los últimos lugares (P-08)"
            )
        }
        Column(Modifier.fillMaxWidth().background(Superficie).padding(16.dp)) {
            BotonPrimario(
                texto = when {
                    lugaresDisponibles <= 0 -> "Sin lugares"
                    yaInscritos -> "Cambiar quiénes van"
                    else -> "Inscribirme"
                },
                onClick = onInscribirme,
                habilitado = lugaresDisponibles > 0
            )
        }
    }
}

/** Parte 5 · Lo de Perfil y Ajustes que la parte 3 necesita: sesión y el botón de demo. */
@Composable
fun PerfilTemporalScreen(
    sesion: Sesion?,
    notificacionesActivadas: Boolean,
    onIniciarSesion: () -> Unit,
    onCrearCuenta: () -> Unit,
    onCerrarSesion: () -> Unit,
    onPedirPermiso: () -> Unit,
    onRecordatorio: () -> Unit,
    onUrgencia: () -> Unit,
    onInscripcion: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(Fondo).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineMedium, color = Tinta)
        AvisoTemporal("parte 5")

        Seccion("Cuenta")
        Tarjeta {
            if (sesion != null) {
                Text(sesion.familia, style = MaterialTheme.typography.titleMedium, color = Tinta)
                Text(sesion.correo, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
            } else {
                Text("Sin sesión", style = MaterialTheme.typography.titleMedium, color = Tinta)
                Text(
                    "Prueba: ana.rodriguez@correo.com · familia123. Para el error de P-05b crea una cuenta con error@correo.com.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TintaSuave
                )
            }
        }
        if (sesion != null) {
            BotonSecundario("Cerrar sesión", onCerrarSesion)
        } else {
            BotonPrimario("Iniciar sesión", onIniciarSesion)
            BotonSecundario("Crear cuenta", onCrearCuenta)
        }

        Seccion("Ajustes · notificaciones ${if (notificacionesActivadas) "activadas" else "desactivadas"}")
        if (!notificacionesActivadas) BotonSecundario("Activar notificaciones", onPedirPermiso)
        BotonSecundario("Demo: \"Mañana tienen actividad\"", onRecordatorio)
        BotonSecundario("Demo: \"Se necesitan 5 voluntarios urgentes\"", onUrgencia)
        BotonSecundario("Demo: \"Ya están inscritos\"", onInscripcion)
        Spacer(Modifier.height(8.dp))
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun PerfilTemporalPreview() {
    FamiliasQueSumanTheme { PerfilTemporalScreen(null, false, {}, {}, {}, {}, {}, {}, {}) }
}
