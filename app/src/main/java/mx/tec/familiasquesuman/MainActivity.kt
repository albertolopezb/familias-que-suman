package mx.tec.familiasquesuman

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import mx.tec.familiasquesuman.ui.navigation.FamiliasApp
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

class MainActivity : ComponentActivity() {

    /** La respuesta del permiso llega antes de pintar la app: así la primera carga ya tiene red. */
    private val pedirRedLocal = registerForActivityResult(ActivityResultContracts.RequestPermission()) { mostrarApp() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null && necesitaRedLocal()) pedirRedLocal.launch(PERMISO_RED_LOCAL) else mostrarApp()
    }

    private fun mostrarApp() {
        setContent {
            FamiliasQueSumanTheme {
                FamiliasApp()
            }
        }
    }

    /**
     * Solo en depuración: desde Android 17 hablar con la red local (la API en tu máquina,
     * 10.0.2.2 desde el emulador) pide este permiso. Lo declara src/debug/AndroidManifest.xml; la
     * versión de entrega habla con la API por internet y no lo pide.
     */
    private fun necesitaRedLocal(): Boolean =
        BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 37 &&
            checkSelfPermission(PERMISO_RED_LOCAL) != PackageManager.PERMISSION_GRANTED

    private companion object {
        const val PERMISO_RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK"
    }
}
