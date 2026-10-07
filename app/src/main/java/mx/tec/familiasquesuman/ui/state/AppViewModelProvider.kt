package mx.tec.familiasquesuman.ui.state

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import mx.tec.familiasquesuman.FamiliasApplication
import mx.tec.familiasquesuman.ui.screens.actividades.ActividadesViewModel
import mx.tec.familiasquesuman.ui.screens.actividades.DetalleActividadViewModel
import mx.tec.familiasquesuman.ui.screens.actividades.MapaViewModel
import mx.tec.familiasquesuman.ui.screens.actividades.MisActividadesViewModel
import mx.tec.familiasquesuman.ui.screens.inicio.AsociacionViewModel
import mx.tec.familiasquesuman.ui.screens.inicio.DirectorioViewModel
import mx.tec.familiasquesuman.ui.screens.inicio.ExplorarViewModel
import mx.tec.familiasquesuman.ui.screens.inicio.InicioViewModel
import mx.tec.familiasquesuman.ui.screens.campanas.CampanasViewModel
import mx.tec.familiasquesuman.ui.screens.campanas.DetalleCampanaViewModel
import mx.tec.familiasquesuman.ui.screens.perfil.PerfilViewModel
import mx.tec.familiasquesuman.ui.screens.perfil.InsigniasViewModel
import mx.tec.familiasquesuman.ui.screens.perfil.TestimonioViewModel
import mx.tec.familiasquesuman.ui.screens.perfil.RespuestasEncuestasViewModel
import mx.tec.familiasquesuman.ui.screens.inicio.TestimoniosViewModel
import mx.tec.familiasquesuman.ui.screens.perfil.AvisoPrivacidadViewModel
import mx.tec.familiasquesuman.ui.screens.perfil.AjustesViewModel
import mx.tec.familiasquesuman.ui.screens.inscripcion.AcompanantesViewModel
import mx.tec.familiasquesuman.ui.screens.inscripcion.CuentaViewModel
import mx.tec.familiasquesuman.ui.screens.sugerencias.SugerenciasViewModel

/**
 * Cómo se construye cada ViewModel de la app.
 *
 * Cada quien agrega aquí UNA línea con el initializer de su ViewModel. Ejemplo:
 *
 *   initializer { ActividadesViewModel(familiasApplication().container.actividadRepository) }
 *
 * y necesita: import androidx.lifecycle.viewmodel.initializer
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer { mx.tec.familiasquesuman.ui.screens.campanas.TengoAlgoParaDonarViewModel() }
        initializer { AjustesViewModel(familiasApplication().container.perfilRepository) }
        initializer { AvisoPrivacidadViewModel(familiasApplication().container.perfilRepository) }
        initializer {
            TestimonioViewModel(
                familiasApplication().container.perfilRepository,
                familiasApplication().container.testimonioRepository,
                familiasApplication().contentResolver,
                // Las fotos de los testimonios se guardan en el almacenamiento de la propia app.
                java.io.File(familiasApplication().filesDir, "testimonios")
            )
        }
        initializer { RespuestasEncuestasViewModel(familiasApplication().container.encuestaRepository) }
        initializer { TestimoniosViewModel(familiasApplication().container.testimonioRepository) }
        initializer {
            InsigniasViewModel(familiasApplication().container.perfilRepository)
        }
        initializer {
            PerfilViewModel(familiasApplication().container.perfilRepository)
        }
        // Agregar dentro del Factory initializer
        initializer {
            InicioViewModel(familiasApplication().container.actividadRepository)
        }
        initializer {
            ExplorarViewModel(familiasApplication().container.actividadRepository)
        }
        initializer {
            AsociacionViewModel(familiasApplication().container.actividadRepository)
        }

        // Proyectos y Directorio de Visiteo
        initializer {
            DirectorioViewModel(familiasApplication().container.actividadRepository)
        }

        // Parte 2 · Actividades
        initializer {
            ActividadesViewModel(familiasApplication().container.actividadRepository)
        }
        initializer {
            DetalleActividadViewModel(familiasApplication().container.actividadRepository)
        }
        initializer {
            MisActividadesViewModel(
                familiasApplication().container.perfilRepository,
                familiasApplication().container.actividadRepository,
                familiasApplication().container.testimonioRepository,
                familiasApplication().container.encuestaRepository
            )
        }

        // La pestaña "Mapa" de Actividades: lo que hay cerca de la familia
        initializer { MapaViewModel(familiasApplication().container.mapaRepository) }

        // Parte 4 · Campañas
        initializer {
            CampanasViewModel(
                familiasApplication().container.campanaRepository,
                familiasApplication().container.actividadRepository
            )
        }
        initializer {
            DetalleCampanaViewModel(
                familiasApplication().container.campanaRepository,
                familiasApplication().container.actividadRepository
            )
        }
        initializer {
            val c = familiasApplication().container
            CuentaViewModel(
                c.actividadRepository,
                c.perfilRepository,
                c.campanaRepository,
                mx.tec.familiasquesuman.data.FavoritosStore(familiasApplication())
            )
        }
        initializer { AcompanantesViewModel(familiasApplication().container.actividadRepository) }

        // Sugerencias de actividades, campañas, proyectos y centros
        initializer { SugerenciasViewModel(familiasApplication().container.sugerenciaRepository) }
    }
}

/** El atajo para llegar al contenedor desde dentro de un initializer. */
fun CreationExtras.familiasApplication(): FamiliasApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FamiliasApplication
