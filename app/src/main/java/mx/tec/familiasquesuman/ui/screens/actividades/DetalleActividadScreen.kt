package mx.tec.familiasquesuman.ui.screens.actividades

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BloqueCupo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonCircular
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.actividadDeMuestra
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.tinteDeCategoria
import mx.tec.familiasquesuman.ui.theme.Borde
import mx.tec.familiasquesuman.ui.theme.ConfirmadoTexto
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme
import mx.tec.familiasquesuman.ui.theme.Fondo
import mx.tec.familiasquesuman.ui.theme.MarcaAzul
import mx.tec.familiasquesuman.ui.theme.MarcaOro
import mx.tec.familiasquesuman.ui.theme.Superficie
import mx.tec.familiasquesuman.ui.theme.Tinta
import mx.tec.familiasquesuman.ui.theme.TintaSuave

/**
 * El detalle de una actividad (RF-04, RF-05, RF-17).
 *
 * La variante sin cupo no es otra pantalla: es esta misma con el bloque de
 * lugares en rojo y el botón apagado. Lo decide el dominio con `sinLugares`.
 */
@Composable
fun DetalleActividadScreen(
    item: ActividadConAsociacion,
    esFavorito: Boolean,
    onRegresar: () -> Unit,
    onAlternarFavorito: () -> Unit,
    onInscribirme: () -> Unit,
    onVerOtrasActividades: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actividad = item.actividad

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp)
                .background(Superficie)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonCircular(onClick = onRegresar)
            Text(
                text = "Detalle de la actividad",
                style = MaterialTheme.typography.labelLarge,
                color = Tinta,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Fondo)
                    .clickable(onClick = onAlternarFavorito),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (esFavorito) Icons.Default.Favorite
                    else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (esFavorito) {
                        "Quitar de favoritos"
                    } else {
                        "Guardar en favoritos"
                    },
                    tint = if (esFavorito) MarcaOro else TintaSuave
                )
            }
        }

        // Cuerpo
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // El lugar de la foto. Para ponerle imagen, cambia este Box por un
            // Image con contentScale = ContentScale.Crop y el mismo alto.
            val tinte = tinteDeCategoria(item.asociacion.categoria)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Brush.linearGradient(listOf(tinte.first, tinte.second)))
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = actividad.titulo,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Tinta
                    )
                    Text(
                        text = item.asociacion.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TintaSuave
                    )
                    // Familias que Suman verifica cada asociación antes de
                    // publicarla. Decirlo aquí es la mitad de la confianza.
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ConfirmadoTexto,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Asociación verificada",
                            style = MaterialTheme.typography.labelMedium,
                            color = ConfirmadoTexto
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Superficie)
                        .border(1.dp, Borde, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    FilaDeDato(
                        icono = Icons.Outlined.DateRange,
                        texto = "${actividad.fecha} · ${actividad.horario}"
                    )
                    FilaDeDato(
                        icono = Icons.Default.Place,
                        texto = actividad.direccion
                    )
                    FilaDeDato(
                        icono = Icons.Outlined.Person,
                        texto = if (actividad.edadMinima == null) {
                            "Apta para todas las edades"
                        } else {
                            "Apta para niños desde ${actividad.edadMinima} años"
                        }
                    )
                }

                BloqueCupo(actividad = actividad)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "¿QUÉ VAN A HACER?",
                        style = MaterialTheme.typography.labelSmall,
                        color = TintaSuave
                    )
                    Text(
                        text = actividad.descripcion,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Tinta
                    )
                }
            }
        }

        // Pie fijo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Superficie)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onInscribirme,
                enabled = !actividad.sinLugares,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MarcaOro,
                    contentColor = MarcaAzul,
                    disabledContainerColor = Borde,
                    disabledContentColor = TintaSuave
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (actividad.sinLugares) "Sin lugares" else "Inscribirme",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (actividad.sinLugares) {
                Text(
                    text = "Ver otras actividades →",
                    style = MaterialTheme.typography.labelMedium,
                    color = MarcaAzul,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable(onClick = onVerOtrasActividades)
                )
            } else {
                Text(
                    text = "Puedes cancelar hasta 12 horas antes.",
                    style = MaterialTheme.typography.labelMedium,
                    color = TintaSuave,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FilaDeDato(icono: ImageVector, texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = TintaSuave,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            color = Tinta
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetalleConCupoPreview() {
    FamiliasQueSumanTheme {
        DetalleActividadScreen(
            item = actividadDeMuestra(8),
            esFavorito = false,
            onRegresar = {},
            onAlternarFavorito = {},
            onInscribirme = {},
            onVerOtrasActividades = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetalleSinCupoPreview() {
    FamiliasQueSumanTheme {
        DetalleActividadScreen(
            item = actividadDeMuestra(0),
            esFavorito = true,
            onRegresar = {},
            onAlternarFavorito = {},
            onInscribirme = {},
            onVerOtrasActividades = {}
        )
    }
}
