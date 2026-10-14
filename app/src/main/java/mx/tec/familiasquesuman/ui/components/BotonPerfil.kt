package mx.tec.familiasquesuman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.tec.familiasquesuman.ui.screens.inscripcion.cuentaViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.theme.AmbarAcento
import mx.tec.familiasquesuman.ui.theme.AzulMarinoPrimario

/** A dónde puede llevar el cuadrito de perfil. */
enum class DestinoPerfil { PERFIL, MIS_ACTIVIDADES, NOTIFICACIONES, AJUSTES, INICIAR_SESION, CREAR_CUENTA, CERRAR_SESION }

/** Lo provee FamiliasApp, que es quien navega. Sin proveedor (previews) no hace nada. */
val LocalIrAPerfil = compositionLocalOf<(DestinoPerfil) -> Unit> { {} }

/**
 * El botón de perfil de la esquina de arriba. Al tocarlo se abre un cuadrito con
 * lo importante: con sesión, Mi perfil, Mis actividades, Notificaciones y Ajustes; sin sesión, entrar o crear cuenta.
 * Con notificaciones sin leer lleva encima cuántas son.
 */
@Composable
fun BotonPerfil(modifier: Modifier = Modifier, sobreAzul: Boolean = false) {
    val cuenta = cuentaViewModel()
    val sesion by cuenta.sesion.collectAsStateWithLifecycle()
    val foto by cuenta.foto.collectAsStateWithLifecycle()
    val sinLeer by cuenta.sinLeer.collectAsStateWithLifecycle()
    val conSesion = sesion != null
    val esAdmin = sesion?.esAdmin == true

    var abierto by remember { mutableStateOf(false) }
    val irA = LocalIrAPerfil.current
    val ir: (DestinoPerfil) -> Unit = { destino ->
        abierto = false
        irA(destino)
    }

    Box(modifier) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (sobreAzul) Color.White.copy(alpha = 0.14f) else Color(0xFFF1F5F9))
                .clickable { abierto = true }
                .semantics(mergeDescendants = true) {
                    contentDescription = when {
                        !conSesion || sinLeer == 0 -> "Mi perfil"
                        sinLeer == 1 -> "Mi perfil, 1 notificación sin leer"
                        else -> "Mi perfil, $sinLeer notificaciones sin leer"
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val s = sesion
            if (s != null && !esAdmin) {
                AvatarFamilia(foto, s.familia, 40.dp)
            } else {
                Icon(
                    IconosWeb.Usuario,
                    contentDescription = "Mi perfil",
                    tint = if (sobreAzul) Color.White else AzulMarinoPrimario,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        // Fuera del círculo recortado para que el número no se corte.
        if (conSesion && sinLeer > 0) {
            ContadorAvisos(sinLeer, Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-5).dp))
        }

        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            offset = DpOffset(0.dp, 6.dp),
            modifier = Modifier.background(Color.White)
        ) {
            if (conSesion) {
                Opcion("Mi perfil", IconosWeb.Usuario) { ir(DestinoPerfil.PERFIL) }

                // Mostrar "Mis actividades" solo si NO es administrador
                if (!esAdmin) {
                    Opcion("Mis actividades", IconosWeb.Calendario) { ir(DestinoPerfil.MIS_ACTIVIDADES) }
                }

                Opcion("Notificaciones", IconosWeb.Campana, contador = sinLeer) { ir(DestinoPerfil.NOTIFICACIONES) }
                Opcion("Ajustes", IconosWeb.Engrane) { ir(DestinoPerfil.AJUSTES) }
                Opcion("Cerrar sesión", IconosWeb.Usuario) { ir(DestinoPerfil.CERRAR_SESION) }
            } else {
                Opcion("Iniciar sesión", IconosWeb.Usuario) { ir(DestinoPerfil.INICIAR_SESION) }
                Opcion("Crear cuenta", IconosWeb.Usuario) { ir(DestinoPerfil.CREAR_CUENTA) }
            }
        }
    }
}

@Composable
private fun Opcion(texto: String, icono: ImageVector, contador: Int = 0, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(texto, style = MaterialTheme.typography.labelLarge, color = AzulMarinoPrimario) },
        leadingIcon = { Icon(icono, contentDescription = null, tint = AzulMarinoPrimario, modifier = Modifier.size(18.dp)) },
        trailingIcon = if (contador > 0) ({ ContadorAvisos(contador) }) else null,
        onClick = onClick
    )
}

/** La pastilla ámbar con cuántas notificaciones no han leído; de 10 en adelante dice "9+". */
@Composable
private fun ContadorAvisos(cuantas: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .clip(CircleShape)
            .background(AmbarAcento)
            .border(1.5.dp, Color.White, CircleShape)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (cuantas > 9) "9+" else "$cuantas",
            color = AzulMarinoPrimario,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 12.sp
        )
    }
}