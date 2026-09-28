package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import mx.tec.familiasquesuman.R
import mx.tec.familiasquesuman.ui.theme.AzulMarinoPrimario

@Composable
fun SplashScreen(
    onNavegarACiudad: () -> Unit,
    onNavegarAInicio: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(1500)
        onNavegarACiudad()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulMarinoPrimario),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = visible, enter = fadeIn()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                     painter = painterResource(id = R.drawable.logo_familias),
                     contentDescription = "Logo Familias que Suman",
                     modifier = Modifier.size(120.dp)
                 )
                 Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Familias que Suman",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}