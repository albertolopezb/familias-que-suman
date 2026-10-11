package mx.tec.familiasquesuman.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Las ciudades donde opera Familias que Suman. */
val CIUDADES = listOf("Monterrey", "Hermosillo")

/** "Monterrey" → "Monterrey, N.L."; "Hermosillo" → "Hermosillo, Son." */
fun ciudadConEstado(ciudad: String): String = when (ciudad) {
    "Monterrey" -> "Monterrey, N.L."
    "Hermosillo" -> "Hermosillo, Son."
    else -> ciudad
}

/** Lo que la persona elige en Ajustes. Se guarda en el teléfono y sobrevive al cerrar la app. */
data class Preferencias(
    val ciudad: String = CIUDADES.first(),
    /** true cuando ya se eligió ciudad al menos una vez (la pantalla de ciudad solo sale la primera vez). */
    val ciudadElegida: Boolean = false,
    val recordatorioActividades: Boolean = true,
    val avisosFavoritos: Boolean = true,
    val urgenciasCiudad: Boolean = false
)

/**
 * Una sola fuente de la ciudad y los avisos para toda la app: Inicio, Ajustes y las listas
 * leen lo mismo. Vive en el contenedor de la aplicación.
 */
class PreferenciasStore(contexto: Context) {

    private val prefs = contexto.applicationContext.getSharedPreferences("preferencias", Context.MODE_PRIVATE)

    private val _preferencias = MutableStateFlow(leer())
    val preferencias: StateFlow<Preferencias> = _preferencias.asStateFlow()

    private fun leer() = Preferencias(
        ciudad = prefs.getString("ciudad", null)?.takeIf { it in CIUDADES } ?: CIUDADES.first(),
        ciudadElegida = prefs.contains("ciudad"),
        recordatorioActividades = prefs.getBoolean("recordatorio", true),
        avisosFavoritos = prefs.getBoolean("favoritos", true),
        urgenciasCiudad = prefs.getBoolean("urgencias", false)
    )

    private fun guardar(nuevas: Preferencias) {
        prefs.edit()
            .putString("ciudad", nuevas.ciudad)
            .putBoolean("recordatorio", nuevas.recordatorioActividades)
            .putBoolean("favoritos", nuevas.avisosFavoritos)
            .putBoolean("urgencias", nuevas.urgenciasCiudad)
            .apply()
        _preferencias.value = nuevas.copy(ciudadElegida = true)
    }

    fun cambiarCiudad(ciudad: String) {
        if (ciudad in CIUDADES) guardar(_preferencias.value.copy(ciudad = ciudad))
    }

    fun cambiarRecordatorio(activo: Boolean) = guardar(_preferencias.value.copy(recordatorioActividades = activo))
    fun cambiarAvisosFavoritos(activo: Boolean) = guardar(_preferencias.value.copy(avisosFavoritos = activo))
    fun cambiarUrgenciasCiudad(activo: Boolean) = guardar(_preferencias.value.copy(urgenciasCiudad = activo))
}
