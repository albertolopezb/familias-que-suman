package mx.tec.familiasquesuman

import android.app.Application
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.data.local.CatalogoLocal
import mx.tec.familiasquesuman.data.CampanaRepository
import mx.tec.familiasquesuman.data.CuentaRepository
import mx.tec.familiasquesuman.data.EncuestaRepository
import mx.tec.familiasquesuman.data.EncuestaRepositoryApi
import mx.tec.familiasquesuman.data.MapaRepository
import mx.tec.familiasquesuman.data.PerfilRepository
import mx.tec.familiasquesuman.data.PreferenciasStore
import mx.tec.familiasquesuman.data.SugerenciaRepository
import mx.tec.familiasquesuman.data.TestimonioRepository
import mx.tec.familiasquesuman.data.TestimonioRepositoryApi
import mx.tec.familiasquesuman.data.red.ClienteApi
import mx.tec.familiasquesuman.data.sesion.ServicioSesion
import mx.tec.familiasquesuman.notificaciones.RevisarAvisosWorker
import java.io.File

/**
 * El contenedor de dependencias: quién construye a quién, en un solo lugar.
 * Los repositorios y la sesión hablan con la API (backend/api).
 */
class AppContainer(contexto: android.content.Context) {
    val sesion: ServicioSesion = ServicioSesion(contexto)
    val actividadRepository: ActividadRepository by lazy { ActividadRepository() }
    val campanaRepository: CampanaRepository by lazy { CampanaRepository() }
    val perfilRepository: PerfilRepository by lazy { PerfilRepository() }
    val cuentaRepository: CuentaRepository by lazy { CuentaRepository() }
    val sugerenciaRepository: SugerenciaRepository by lazy { SugerenciaRepository() }
    val testimonioRepository: TestimonioRepository by lazy {
        TestimonioRepositoryApi(File(contexto.cacheDir, "testimonios"))
    }
    val encuestaRepository: EncuestaRepository by lazy { EncuestaRepositoryApi() }
    val mapaRepository: MapaRepository by lazy { MapaRepository(actividadRepository, campanaRepository) }
    /** La ciudad y los avisos que se eligen en Ajustes, guardados en el teléfono. */
    val preferenciasStore: PreferenciasStore = PreferenciasStore(contexto)

    init {
        // Cada llamada a la API lleva el token de la sesión, si la hay.
        ClienteApi.usarProveedorDeToken(sesion)
        // El catálogo público guardado en el teléfono, para verlo sin conexión.
        CatalogoLocal.iniciar(contexto)
    }
}

/** Vive tanto como el proceso. Declarada en el manifiesto con `android:name`. */
class FamiliasApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Cada pocas horas, aunque la app esté cerrada, revisa si toca algún aviso.
        RevisarAvisosWorker.programar(this)
    }
}
