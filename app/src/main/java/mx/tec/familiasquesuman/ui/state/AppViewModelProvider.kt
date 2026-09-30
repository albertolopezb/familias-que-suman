package mx.tec.familiasquesuman.ui.state

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import mx.tec.familiasquesuman.FamiliasApplication
import mx.tec.familiasquesuman.ui.screens.actividades.ActividadesViewModel
import mx.tec.familiasquesuman.ui.screens.actividades.DetalleActividadViewModel
import mx.tec.familiasquesuman.ui.screens.actividades.MisActividadesViewModel
import mx.tec.familiasquesuman.ui.screens.inicio.AsociacionViewModel
import mx.tec.familiasquesuman.ui.screens.inicio.ExplorarViewModel
import mx.tec.familiasquesuman.ui.screens.inicio.InicioViewModel
import mx.tec.familiasquesuman.ui.screens.campanas.CampanasViewModel
import mx.tec.familiasquesuman.ui.screens.campanas.DetalleCampanaViewModel

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

        // Parte 2 · Actividades
        initializer {
            ActividadesViewModel(familiasApplication().container.actividadRepository)
        }
        initializer {
            DetalleActividadViewModel(familiasApplication().container.actividadRepository)
        }
        initializer {
            MisActividadesViewModel(familiasApplication().container.perfilRepository)
        }

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
    }
}

/** El atajo para llegar al contenedor desde dentro de un initializer. */
fun CreationExtras.familiasApplication(): FamiliasApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FamiliasApplication
