package mx.tec.familiasquesuman.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.R
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web

/**
 * Abre el menú lateral desde cualquier encabezado. Si vale null, el encabezado
 * no enseña la hamburguesa (por ejemplo, en las previews).
 */
val LocalAbrirMenu = compositionLocalOf<(() -> Unit)?> { null }

/** Lo que se puede tocar en el menú. */
enum class OpcionMenu { MIS_ACTIVIDADES, PERFIL, DONAR, EXPLORAR, AJUSTES, PANEL_ADMIN, ACCESO_ADMIN, SALIR_ADMIN }

/**
 * El menú de la hamburguesa, como el del sitio: "Tengo algo para donar...",
 * "Mi perfil" y, si la cuenta es de administrador, "Panel Admin".
 */
@Composable
fun MenuPrincipal(
    esAdmin: Boolean,
    onOpcion: (OpcionMenu) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Web.Tarjeta)
            .padding(vertical = 16.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.logo_familias),
            contentDescription = "Familias que Suman",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(150.dp)
                .height(50.dp)
        )
        Text(
            "Pequeñas acciones, gran impacto",
            style = TextoWeb.Chico,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )
        HorizontalDivider(color = Web.Borde, thickness = 1.dp)

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Opcion("Mis Actividades", IconosWeb.Calendario, Web.Primario) { onOpcion(OpcionMenu.MIS_ACTIVIDADES) }
            Opcion("Mi perfil", IconosWeb.Usuario, Web.Primario) { onOpcion(OpcionMenu.PERFIL) }
            Opcion("Tengo algo para donar...", IconosWeb.ManoCorazon, Web.VerdeTema) { onOpcion(OpcionMenu.DONAR) }
            Opcion("Explorar asociaciones", IconosWeb.Buscar, Web.Amarillo) { onOpcion(OpcionMenu.EXPLORAR) }
            Opcion("Ajustes", IconosWeb.Engrane, Web.TextoApagado) { onOpcion(OpcionMenu.AJUSTES) }
        }
        HorizontalDivider(color = Web.Borde, thickness = 1.dp)

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            if (esAdmin) {
                Opcion("Panel Admin", IconosWeb.Escudo, Web.VerdeTema, negritas = true) {
                    onOpcion(OpcionMenu.PANEL_ADMIN)
                }
                Opcion("Salir del modo admin", IconosWeb.Salir, Web.RojoTexto) { onOpcion(OpcionMenu.SALIR_ADMIN) }
            } else {
                Opcion("Acceso administradores", IconosWeb.Candado, Web.Primario) {
                    onOpcion(OpcionMenu.ACCESO_ADMIN)
                }
            }
        }
    }
}

@Composable
private fun Opcion(
    texto: String,
    icono: ImageVector,
    color: Color,
    negritas: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(
            texto,
            style = TextoWeb.Cuerpo.copy(fontWeight = if (negritas) FontWeight.SemiBold else FontWeight.Medium),
            color = Web.Texto
        )
    }
}
