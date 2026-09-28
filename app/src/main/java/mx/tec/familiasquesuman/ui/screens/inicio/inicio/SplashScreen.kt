package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import mx.tec.familiasquesuman.R

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(1800)
        visible = false
        delay(200)
        onSplashFinished()
    }

    // Pantalla limpia con fondo completamente blanco
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Unicamente el logo centrado en grande
            Image(
                painter = painterResource(id = R.drawable.logo_familias), // Reemplaza por el nombre de tu recurso del logo
                contentDescription = "Logo Familias que Suman",
                modifier = Modifier
                    .fillMaxWidth(0.65f) // Ocupa el 65% del ancho de la pantalla para verse grande y proporcionado
                    .wrapContentHeight()
            )
        }
    }
}