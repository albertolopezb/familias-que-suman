package mx.tec.familiasquesuman.ui.state

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import mx.tec.familiasquesuman.FamiliasApplication
import mx.tec.familiasquesuman.ui.screens.inicio.AsociacionViewModel
import mx.tec.familiasquesuman.ui.screens.inicio.ExplorarViewModel
import mx.tec.familiasquesuman.ui.screens.inicio.inicio.InicioViewModel

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
    }
}

/** El atajo para llegar al contenedor desde dentro de un initializer. */
fun CreationExtras.familiasApplication(): FamiliasApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FamiliasApplication
