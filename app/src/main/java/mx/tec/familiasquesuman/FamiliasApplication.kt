package mx.tec.familiasquesuman

import android.app.Application
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.data.CampanaRepository
import mx.tec.familiasquesuman.data.PerfilRepository

/**
 * El contenedor de dependencias: quién construye a quién, en un solo lugar.
 * Hoy los repositorios no necesitan nada; cuando entren la API y DataStore,
 * se construyen aquí y ningún ViewModel cambia.
 */
class AppContainer {
    val actividadRepository: ActividadRepository by lazy { ActividadRepository() }
    val campanaRepository: CampanaRepository by lazy { CampanaRepository() }
    val perfilRepository: PerfilRepository by lazy { PerfilRepository() }
}

/** Vive tanto como el proceso. Declarada en el manifiesto con `android:name`. */
class FamiliasApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
