package mx.tec.familiasquesuman.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BarraDeRegreso
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.BotonAmarillo
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.IconosWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.TextoWeb
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.Web
import mx.tec.familiasquesuman.ui.screens.admin.componentes.CampoAdmin
import mx.tec.familiasquesuman.ui.screens.admin.componentes.RenglonAdmin
import mx.tec.familiasquesuman.ui.screens.admin.componentes.RotuloAdmin
import mx.tec.familiasquesuman.ui.screens.admin.componentes.TarjetaNumero
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

/** A dónde lleva cada renglón del panel. Los que aún no existen abren "Próximamente". */
enum class SeccionAdmin(val titulo: String, val descripcion: String) {
    INSCRITOS("Ver Inscritos", ""),
    INBOX_CENTROS("Inbox Centros", "Centros que las familias sugieren para el directorio. Aquí se aprueban o rechazan antes de publicarse."),
    INBOX_GALERIA("Inbox Galería", "Fotos que mandan las familias después de una actividad, antes de aparecer en la galería."),
    INBOX_DONACIONES("Inbox: Fallback de donaciones", "Donaciones que no encontraron un centro: \"¿No encontraste dónde donarlo?\"."),
    INBOX_MENSAJES("Inbox Mensajes", "Mensajes del formulario de contacto."),
    ACTIVIDADES("Actividades en Familia", ""),
    PROYECTOS("Proyectos", "Alta y edición de proyectos con causa."),
    DONAR("Quiero Donar", "Campañas, categorías de donación y filtros."),
    DIRECTORIO("Directorio de Visiteo", "Centros verificados, sus necesidades y datos de contacto."),
    FAMILIAS("Directorio de Familias", "Familias registradas: mamá, papá, correo, WhatsApp, ciudad y sus próximas actividades."),
    CONTACTOS("Directorio de Contactos", "Encargados de cada asociación."),
    CONFIGURACION("Configuración", "Ciudades, textos del inicio y datos generales del sitio."),
    AVISO("Aviso de Privacidad", "El texto del aviso de privacidad que ven las familias."),
    METRICAS("Métricas", "Eventos registrados: visitas, inscripciones y usuarios activos.")
}

/**
 * El Panel Admin, calcado de familiasquesuman.com/admin: dos contadores arriba y
 * los bloques Operación, Contenido Público, Configuración y Analítica.
 */
@Composable
fun PanelAdminScreen(
    nombre: String,
    resumen: ResumenAdmin,
    onSeccion: (SeccionAdmin) -> Unit,
    onSalir: () -> Unit,
    onRegresar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(Web.Fondo)) {
        BarraDeRegreso(texto = "Salir del panel", onRegresar = onRegresar)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 64.dp)
        ) {
            Column(modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        IconosWeb.Escudo,
                        contentDescription = null,
                        tint = Web.Primario,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("Panel Admin", style = TextoWeb.Titulo.copy(color = Web.Texto))
                }
                Text(
                    text = "$nombre · admin",
                    style = TextoWeb.Cuerpo,
                    color = Web.TextoApagado,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaNumero(
                    numero = "${resumen.centrosPendientes}",
                    etiqueta = "Centros pendientes",
                    color = Web.Primario,
                    modifier = Modifier.weight(1f)
                )
                TarjetaNumero(
                    numero = "${resumen.pendientesPorRevisar}",
                    etiqueta = "Pendientes por revisar",
                    color = Web.Amarillo,
                    modifier = Modifier.weight(1f)
                )
            }

            Bloque("Operación") {
                RenglonAdmin("Ver Inscritos", IconosWeb.Personas, Web.Primario, Web.Secundario,
                    { onSeccion(SeccionAdmin.INSCRITOS) })
                RenglonAdmin("Inbox Centros", IconosWeb.Ubicacion, Web.VerdeTema, Web.VerdeFondo,
                    { onSeccion(SeccionAdmin.INBOX_CENTROS) }, contador = resumen.centrosPendientes)
                RenglonAdmin("Inbox Galería", IconosWeb.Imagen, Web.Amarillo, Web.AmbarSuave,
                    { onSeccion(SeccionAdmin.INBOX_GALERIA) }, contador = 1)
                RenglonAdmin("Inbox: Fallback de donaciones", IconosWeb.ManoCorazon, Web.AmbarTexto,
                    Web.AmbarSuave, { onSeccion(SeccionAdmin.INBOX_DONACIONES) })
                RenglonAdmin("Inbox Mensajes", IconosWeb.MensajeCuadro, Web.Primario, Web.Secundario,
                    { onSeccion(SeccionAdmin.INBOX_MENSAJES) }, contador = 2)
            }

            Bloque("Contenido Público") {
                RenglonAdmin("Actividades en Familia", IconosWeb.Personas, Web.MoradoTexto,
                    Web.MoradoFondo, { onSeccion(SeccionAdmin.ACTIVIDADES) },
                    contador = 0)
                RenglonAdmin("Proyectos", IconosWeb.Foco, Web.Primario, Web.Secundario,
                    { onSeccion(SeccionAdmin.PROYECTOS) })
                RenglonAdmin("Quiero Donar", IconosWeb.ManoCorazon, Web.VerdeTema, Web.VerdeFondo,
                    { onSeccion(SeccionAdmin.DONAR) })
                RenglonAdmin("Directorio de Visiteo", IconosWeb.Ubicacion, Web.VerdeTema,
                    Web.VerdeFondo, { onSeccion(SeccionAdmin.DIRECTORIO) })
                RenglonAdmin("Directorio de Familias", IconosWeb.Usuario, Web.Rosa, Web.RosaFondo,
                    { onSeccion(SeccionAdmin.FAMILIAS) })
                RenglonAdmin("Directorio de Contactos", IconosWeb.Contactos, Web.Primario,
                    Web.Secundario, { onSeccion(SeccionAdmin.CONTACTOS) })
            }

            Bloque("Configuración") {
                RenglonAdmin("Configuración", IconosWeb.Engrane, Web.TextoApagado, Web.Secundario,
                    { onSeccion(SeccionAdmin.CONFIGURACION) })
                RenglonAdmin("Aviso de Privacidad", IconosWeb.Candado, Web.TextoApagado,
                    Web.Secundario, { onSeccion(SeccionAdmin.AVISO) })
            }

            Bloque("Analítica") {
                RenglonAdmin("Métricas", IconosWeb.Grafica, Web.Primario, Web.Secundario,
                    { onSeccion(SeccionAdmin.METRICAS) })
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Web.RojoBorde, RoundedCornerShape(16.dp))
                    .clickable(onClick = onSalir)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(IconosWeb.Salir, contentDescription = null, tint = Web.RojoTexto, modifier = Modifier.size(16.dp))
                Text("Cerrar sesión de administrador", style = TextoWeb.Chip, color = Web.RojoTexto)
            }
        }
    }
}

@Composable
private fun Bloque(titulo: String, contenido: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        RotuloAdmin(titulo)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { contenido() }
    }
}

/**
 * La entrada al panel. En el sitio se llega con la cuenta de un usuario con rol
 * de admin; aquí, mientras no hay servidor, con credenciales de demostración.
 */
@Composable
fun AccesoAdminScreen(
    formulario: FormularioAcceso,
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onEntrar: () -> Unit,
    onRegresar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(Web.Fondo)) {
        BarraDeRegreso(texto = "Regresar", onRegresar = onRegresar)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Web.Secundario)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(IconosWeb.Escudo, contentDescription = null, tint = Web.Primario, modifier = Modifier.size(28.dp))
            }
            Text(
                text = "Acceso de administradores",
                style = TextoWeb.Titulo,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Solo para el equipo de Familias que Suman: publicar actividades, " +
                    "revisar inscritos y aprobar lo que mandan las familias.",
                style = TextoWeb.Cuerpo,
                color = Web.TextoApagado,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            CampoAdmin(
                etiqueta = "Correo",
                valor = formulario.correo,
                onValorChange = onCorreoChange,
                placeholder = "tu@familiasquesuman.com",
                teclado = KeyboardType.Email
            )
            CampoAdmin(
                etiqueta = "Contraseña",
                valor = formulario.contrasena,
                onValorChange = onContrasenaChange,
                teclado = KeyboardType.Password,
                ocultar = true,
                error = formulario.error
            )
            BotonAmarillo(
                texto = "Entrar al panel",
                onClick = onEntrar,
                conFlechas = false,
                modifier = Modifier.fillMaxWidth()
            )

            // Aviso de prueba: se quita cuando exista la cuenta real (etapa 3).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Web.AmbarSuave)
                    .border(1.dp, Web.AmbarFondo, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Cuenta de prueba", style = TextoWeb.Chip, color = Web.AmbarTexto)
                Text(
                    "${SesionAdmin.CORREO_DEMO} · ${SesionAdmin.CONTRASENA_DEMO}",
                    style = TextoWeb.Chico,
                    color = Web.AmbarTexto
                )
            }
        }
    }
}

/** Las secciones que el sitio tiene y que llegan con el servidor. */
@Composable
fun AdminPendienteScreen(
    seccion: SeccionAdmin,
    onRegresar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(Web.Fondo)) {
        BarraDeRegreso(texto = "Panel Admin", onRegresar = onRegresar)
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                seccion.titulo,
                style = TextoWeb.Titulo,
                modifier = Modifier.padding(top = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Web.Tarjeta)
                    .border(1.dp, Web.Borde, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(IconosWeb.Bandeja, contentDescription = null, tint = Web.TextoApagado, modifier = Modifier.size(36.dp))
                Text(
                    seccion.descripcion,
                    style = TextoWeb.Cuerpo,
                    color = Web.TextoApagado,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Disponible cuando la app tenga servidor.",
                    style = TextoWeb.Chip.copy(fontWeight = FontWeight.SemiBold),
                    color = Web.Primario
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun PanelAdminPreview() {
    FamiliasQueSumanTheme {
        PanelAdminScreen(
            nombre = "Equipo Familias que Suman",
            resumen = ResumenAdmin(2, 4, 2, 7),
            onSeccion = {},
            onSalir = {},
            onRegresar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccesoAdminPreview() {
    FamiliasQueSumanTheme {
        AccesoAdminScreen(FormularioAcceso(), {}, {}, {}, {})
    }
}
