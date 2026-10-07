package mx.tec.familiasquesuman.data

import android.content.Context

/**
 * Los corazones de cada cuenta, guardados en el teléfono para que sigan ahí al cerrar la app.
 * Se guardan por correo; la cuenta en sí sigue en memoria, igual que el resto de la etapa.
 */
class FavoritosStore(contexto: Context) {

    private val prefs = contexto.applicationContext.getSharedPreferences("favoritos", Context.MODE_PRIVATE)

    private fun llave(correo: String) = "fav:${correo.trim().lowercase()}"

    /** null = esta cuenta nunca ha guardado nada (se usan los de ejemplo). */
    fun cargar(correo: String): Set<String>? = prefs.getStringSet(llave(correo), null)?.toSet()

    fun guardar(correo: String, favoritas: Set<String>) {
        prefs.edit().putStringSet(llave(correo), favoritas).apply()
    }
}
