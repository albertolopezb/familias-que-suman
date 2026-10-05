package mx.tec.familiasquesuman.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

// WhatsApp, llamadas, mapas y enlaces: lo que abren los botones de contacto de
// campañas, proyectos y centros. Las pantallas no lo llaman; lo resuelven los grafos.

/** El WhatsApp general de Familias que Suman (el de la burbuja verde del sitio). */
internal const val WhatsAppGeneral = "528120322281"

/** Los números del sitio traen 10 dígitos; WhatsApp pide la lada de país (52 = México). */
private fun conLadaDePais(numero: String) = if (numero.length == 10) "52$numero" else numero

internal fun abrir(contexto: Context, intent: Intent) {
    try {
        contexto.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Sin app que lo abra (por ejemplo, un emulador sin navegador): no pasa nada.
    }
}

internal fun abrirEnlace(contexto: Context, enlace: String) =
    abrir(contexto, Intent(Intent.ACTION_VIEW, Uri.parse(enlace)))

internal fun abrirWhatsApp(contexto: Context, numero: String, mensaje: String) {
    val url = "https://wa.me/${conLadaDePais(numero.filter { it.isDigit() })}?text=${Uri.encode(mensaje)}"
    abrirEnlace(contexto, url)
}

/** ACTION_DIAL solo abre el marcador: no pide permiso ni llama sola. */
internal fun llamar(contexto: Context, telefono: String) =
    abrir(contexto, Intent(Intent.ACTION_DIAL, Uri.parse("tel:${telefono.filter { it.isDigit() }}")))

/** "Cómo llegar": abre la dirección en el mapa (Google Maps o, si no hay, el navegador). */
internal fun abrirMapa(contexto: Context, direccion: String) =
    abrirEnlace(contexto, "https://www.google.com/maps/search/?api=1&query=${Uri.encode(direccion)}")
