package mx.tec.familiasquesuman.ui.screens.actividades.componentes

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import mx.tec.familiasquesuman.R
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import java.io.File

/*
 * Invitar a otra familia a una actividad (para que más personas se sumen e instalen la app).
 * Se arma una tarjeta vertical, del tamaño de una historia de Instagram o un estado de WhatsApp,
 * con la actividad, la fecha, los lugares que quedan y quién invita; se comparte junto con un
 * texto y el enlace que abre la actividad en la app. La imagen se dibuja en el teléfono.
 */

private const val ANCHO = 1080
private const val ALTO = 1920
private const val AZUL = 0xFF1E293B.toInt()
private const val AZUL_OSCURO = 0xFF0E2148.toInt()
private const val ORO = 0xFFF5B83D.toInt()
private const val BLANCO = 0xFFFFFFFF.toInt()

/** "Familia Rodríguez" → "LA FAMILIA RODRÍGUEZ VA A"; sin familia, "TE INVITO A". */
fun encabezadoInvitacion(familia: String?): String {
    val nombre = familia?.trim().orEmpty()
    return when {
        nombre.isBlank() -> "TE INVITO A"
        nombre.startsWith("familia", ignoreCase = true) -> "LA ${nombre.uppercase()} VA A"
        else -> "${nombre.uppercase()} VA A"
    }
}

/**
 * El texto que acompaña a la imagen: se lee bien solo, por si la imagen no se abre. [enlace] es el
 * que da la API: al tocarlo abre la actividad en la app. Sin él, se manda al sitio.
 */
fun textoInvitacion(item: ActividadConAsociacion, familia: String?, lugares: Int?, enlace: String? = null): String {
    val actividad = item.actividad
    val lugar = actividad.direccion.ifBlank { actividad.municipio }
    val nombre = familia?.trim().orEmpty()
    val quien = when {
        nombre.isBlank() -> "Te invito a"
        nombre.startsWith("familia", ignoreCase = true) -> "La $nombre va a"
        else -> "$nombre va a"
    }
    return buildString {
        appendLine("$quien ${actividad.titulo} 🙌")
        appendLine("📅 ${actividad.fecha.replaceFirstChar { it.uppercase() }} · ${actividad.horario}")
        if (lugar.isNotBlank()) appendLine("📍 $lugar")
        if (lugares != null && lugares > 0 && actividad.tieneCupo) {
            appendLine(if (lugares == 1) "Queda 1 lugar." else "Quedan $lugares lugares.")
        }
        appendLine()
        if (enlace != null) append("¿Se suman? Ábranla en la app Familias que Suman: $enlace")
        else append("¿Se suman? Búsquenla en la app Familias que Suman o en https://familiasquesuman.com/actividades")
    }
}

/** Dibuja la tarjeta de la invitación. Es trabajo pesado: se llama fuera del hilo principal. */
fun dibujarInvitacion(contexto: Context, item: ActividadConAsociacion, familia: String?, lugares: Int?): Bitmap {
    val actividad = item.actividad
    val bmp = Bitmap.createBitmap(ANCHO, ALTO, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val res = contexto.resources
    val negrita = ResourcesCompat.getFont(contexto, R.font.inter_bold) ?: Typeface.DEFAULT_BOLD
    val media = ResourcesCompat.getFont(contexto, R.font.inter_semibold) ?: Typeface.DEFAULT
    val normal = ResourcesCompat.getFont(contexto, R.font.inter_regular) ?: Typeface.DEFAULT

    // Fondo azul con un círculo dorado de adorno.
    val fondo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(0f, 0f, 0f, ALTO.toFloat(), AZUL, AZUL_OSCURO, Shader.TileMode.CLAMP)
    }
    c.drawRect(0f, 0f, ANCHO.toFloat(), ALTO.toFloat(), fondo)
    val adorno = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ORO; alpha = 40 }
    c.drawCircle(ANCHO - 60f, 140f, 260f, adorno)
    c.drawCircle(80f, ALTO - 220f, 180f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BLANCO; alpha = 14 })

    // Logo en una pastilla blanca.
    val pastilla = RectF(60f, 90f, 520f, 250f)
    c.drawRoundRect(pastilla, 40f, 40f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BLANCO })
    BitmapFactory.decodeResource(res, R.drawable.logo_familias)?.let { logo ->
        // El logo trae mucho margen blanco: se recorta a las letras.
        val origen = Rect((logo.width * 0.097f).toInt(), (logo.height * 0.307f).toInt(),
            (logo.width * 0.946f).toInt(), (logo.height * 0.73f).toInt())
        dibujarAjustado(c, logo, origen, RectF(pastilla.left + 34f, pastilla.top + 22f, pastilla.right - 34f, pastilla.bottom - 22f))
    }

    // Quién invita.
    val eyebrow = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = ORO; textSize = 40f; typeface = media; letterSpacing = 0.08f }
    var y = 340f
    y = escribir(c, encabezadoInvitacion(familia), eyebrow, 60f, y, 960, maxLineas = 2)

    // Título de la actividad.
    val titulo = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = BLANCO; textSize = 84f; typeface = negrita }
    y = escribir(c, actividad.titulo, titulo, 60f, y + 14f, 960, maxLineas = 3, interlineado = 1.05f)

    // Imagen de la actividad, o el F+ de la app si no tiene.
    // El recuadro de la imagen se encoge si el título es largo, para que nada choque con el botón.
    val altoImagen = ((ALTO - 360f) - (y + 40f) - 430f).coerceIn(300f, 540f)
    val marco = RectF(60f, y + 40f, 1020f, y + 40f + altoImagen)
    c.drawRoundRect(marco, 48f, 48f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BLANCO })
    val idFoto = actividad.foto?.let { res.getIdentifier(it, "drawable", contexto.packageName) } ?: 0
    val foto = (if (idFoto != 0) BitmapFactory.decodeResource(res, idFoto) else null)
    if (foto != null) {
        dibujarAjustado(c, foto, Rect(0, 0, foto.width, foto.height), RectF(marco.left + 50f, marco.top + 40f, marco.right - 50f, marco.bottom - 40f))
    } else {
        c.drawRoundRect(marco, 48f, 48f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFE8EEF9.toInt() })
        BitmapFactory.decodeResource(res, R.drawable.ic_launcher_foreground)?.let { glifo ->
            val radio = (marco.height() * 0.36f).coerceAtMost(200f)
            val lado = radio * 2.6f
            val cx = marco.centerX(); val cy = marco.centerY()
            c.drawCircle(cx, cy, radio, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AZUL })
            dibujarAjustado(c, glifo, Rect(0, 0, glifo.width, glifo.height), RectF(cx - lado / 2, cy - lado / 2, cx + lado / 2, cy + lado / 2))
        }
    }
    y = marco.bottom + 60f

    // Fecha, lugar y quién organiza.
    val dato = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = BLANCO; textSize = 44f; typeface = media }
    val punto = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ORO }
    val lugar = actividad.direccion.ifBlank { actividad.municipio }
    listOfNotNull(
        "${actividad.fecha.replaceFirstChar { it.uppercase() }} · ${actividad.horario}",
        lugar.takeIf { it.isNotBlank() },
        "Organiza ${item.asociacion.nombre}"
    ).forEach { linea ->
        c.drawCircle(76f, y + 26f, 12f, punto)
        y = escribir(c, linea, dato, 110f, y, 910, maxLineas = 2) + 18f
    }

    // Lugares que quedan.
    if (lugares != null && actividad.tieneCupo && lugares > 0) {
        val texto = when {
            lugares == 1 -> "¡Queda 1 lugar!"
            lugares <= 3 -> "¡Últimos $lugares lugares!"
            else -> "Quedan $lugares lugares"
        }
        val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = AZUL_OSCURO; textSize = 42f; typeface = negrita }
        val ancho = p.measureText(texto) + 80f
        val r = RectF(60f, y + 10f, 60f + ancho, y + 96f)
        c.drawRoundRect(r, 43f, 43f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ORO })
        c.drawText(texto, r.left + 40f, r.centerY() + 15f, p)
    }

    // Llamado a la acción y sitio.
    val boton = RectF(60f, ALTO - 330f, 1020f, ALTO - 190f)
    c.drawRoundRect(boton, 70f, 70f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BLANCO })
    val cta = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = AZUL; textSize = 46f; typeface = negrita; textAlign = Paint.Align.CENTER }
    c.drawText("¿Se suman? Únanse en la app", boton.centerX(), boton.centerY() + 16f, cta)
    val pie = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = BLANCO; alpha = 200; textSize = 38f; typeface = normal; textAlign = Paint.Align.CENTER }
    c.drawText("Familias que Suman · familiasquesuman.com", ANCHO / 2f, ALTO - 100f, pie)
    return bmp
}

/** Abre la hoja de compartir de Android con la imagen y el texto de la invitación. */
fun compartirInvitacion(contexto: Context, imagen: Bitmap, item: ActividadConAsociacion, familia: String?, lugares: Int?, enlace: String? = null) {
    val carpeta = File(contexto.cacheDir, "compartir").apply { mkdirs() }
    carpeta.listFiles()?.forEach { it.delete() } // solo se guarda la última
    val archivo = File(carpeta, "invitacion_${item.actividad.id}.png")
    archivo.outputStream().use { imagen.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(contexto, "${contexto.packageName}.archivos", archivo)
    val intento = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, item.actividad.titulo)
        putExtra(Intent.EXTRA_TEXT, textoInvitacion(item, familia, lugares, enlace))
        clipData = ClipData.newRawUri(null, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    contexto.startActivity(Intent.createChooser(intento, "Invitar a otra familia"))
}

/** Compartir solo con texto (cuando no hay imagen, por ejemplo si falló al dibujarla). */
fun compartirActividad(contexto: Context, item: ActividadConAsociacion, familia: String? = null, lugares: Int? = null, enlace: String? = null) {
    val intento = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, item.actividad.titulo)
        putExtra(Intent.EXTRA_TEXT, textoInvitacion(item, familia, lugares, enlace))
    }
    contexto.startActivity(Intent.createChooser(intento, "Compartir actividad"))
}

private fun dibujarAjustado(c: Canvas, bmp: Bitmap, origen: Rect, destino: RectF) {
    val escala = minOf(destino.width() / origen.width(), destino.height() / origen.height())
    val w = origen.width() * escala; val h = origen.height() * escala
    val dx = destino.left + (destino.width() - w) / 2; val dy = destino.top + (destino.height() - h) / 2
    c.drawBitmap(bmp, origen, RectF(dx, dy, dx + w, dy + h), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
}

/** Escribe texto con salto de línea; devuelve la y donde termina. */
private fun escribir(c: Canvas, texto: String, p: TextPaint, x: Float, y: Float, ancho: Int,
                     maxLineas: Int, interlineado: Float = 1.15f): Float {
    val layout = StaticLayout.Builder.obtain(texto, 0, texto.length, p, ancho)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setLineSpacing(0f, interlineado)
        .setMaxLines(maxLineas)
        .setEllipsize(TextUtils.TruncateAt.END)
        .build()
    c.save(); c.translate(x, y); layout.draw(c); c.restore()
    return y + layout.height
}
