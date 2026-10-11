package mx.tec.familiasquesuman.data

import android.content.Context
import mx.tec.familiasquesuman.domain.Aviso
import mx.tec.familiasquesuman.domain.TipoAviso
import org.json.JSONArray
import org.json.JSONObject

/**
 * La bandeja de notificaciones de cada cuenta, guardada en el teléfono: lo que llegó y si ya se
 * leyó. Se guarda por correo, igual que los favoritos. Con el backend la bandeja vive en el servidor.
 */
class AvisosStore(contexto: Context) {

    private val prefs = contexto.applicationContext.getSharedPreferences("avisos", Context.MODE_PRIVATE)

    private fun llave(correo: String) = "avisos:${correo.trim().lowercase()}"

    fun cargar(correo: String): List<Aviso> = runCatching {
        val arreglo = JSONArray(prefs.getString(llave(correo), null) ?: return emptyList())
        (0 until arreglo.length()).mapNotNull { leer(arreglo.getJSONObject(it)) }
    }.getOrDefault(emptyList())

    /** Guarda los [MAXIMO] más recientes; los viejos se van solos. */
    fun guardar(correo: String, avisos: List<Aviso>) {
        val arreglo = JSONArray(avisos.sortedByDescending { it.creado }.take(MAXIMO).map { escribir(it) })
        prefs.edit().putString(llave(correo), arreglo.toString()).apply()
    }

    private fun escribir(a: Aviso) = JSONObject()
        .put("id", a.id)
        .put("tipo", a.tipo.name)
        .put("titulo", a.titulo)
        .put("texto", a.texto)
        .put("creado", a.creado)
        .put("leido", a.leido)
        .put("actividad", a.actividadId)
        .put("campana", a.campanaId)

    /** Uno guardado con otra versión de la app que ya no se puede leer se ignora. */
    private fun leer(json: JSONObject): Aviso? = runCatching {
        Aviso(
            id = json.getString("id"),
            tipo = TipoAviso.valueOf(json.getString("tipo")),
            titulo = json.getString("titulo"),
            texto = json.getString("texto"),
            creado = json.getLong("creado"),
            leido = json.optBoolean("leido"),
            actividadId = json.optString("actividad").takeIf { it.isNotEmpty() },
            campanaId = json.optString("campana").takeIf { it.isNotEmpty() }
        )
    }.getOrNull()

    private companion object {
        const val MAXIMO = 50
    }
}
