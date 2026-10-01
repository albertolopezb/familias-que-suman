package mx.tec.familiasquesuman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
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
 */
@Composable
fun BotonPerfil(
    modifier: Modifier = Modifier,
    hayNotificaciones: Boolean = true
) {
    val conSesion = cuentaViewModel().sesion.collectAsStateWithLifecycle().value != null
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
                .background(Color(0xFFF1F5F9))
                .clickable { abierto = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                IconosWeb.Usuario,
                contentDescription = "Mi perfil",
                tint = AzulMarinoPrimario,
                modifier = Modifier.size(20.dp)
            )
            if (hayNotificaciones && conSesion) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-3).dp, y = 3.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AmbarAcento)
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }
        }

        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            offset = DpOffset(0.dp, 6.dp),
            modifier = Modifier.background(Color.White)
        ) {
            if (conSesion) {
                Opcion("Mi perfil", IconosWeb.Usuario) { ir(DestinoPerfil.PERFIL) }
                Opcion("Mis actividades", IconosWeb.Calendario) { ir(DestinoPerfil.MIS_ACTIVIDADES) }
                if (hayNotificaciones) {
                    Opcion("Notificaciones", IconosWeb.Campana) { ir(DestinoPerfil.NOTIFICACIONES) }
                }
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
private fun Opcion(texto: String, icono: ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(texto, style = MaterialTheme.typography.labelLarge, color = AzulMarinoPrimario) },
        leadingIcon = { Icon(icono, contentDescription = null, tint = AzulMarinoPrimario, modifier = Modifier.size(18.dp)) },
        onClick = onClick
    )
}
