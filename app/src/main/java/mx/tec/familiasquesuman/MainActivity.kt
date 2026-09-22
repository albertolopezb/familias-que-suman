package mx.tec.familiasquesuman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.tec.familiasquesuman.ui.navigation.FamiliasApp
import mx.tec.familiasquesuman.ui.theme.FamiliasQueSumanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamiliasQueSumanTheme {
                FamiliasApp()
            }
        }
    }
}
