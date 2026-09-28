package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.ui.screens.inicio.componentes.PildoraCiudad
import mx.tec.familiasquesuman.ui.screens.inicio.componentes.TarjetaCategoria

@Composable
fun InicioScreen(
    ciudad: String,
    asociacion: Asociacion?,
    onExplorarClick: () -> Unit,
    onActividadesClick: () -> Unit,
    onDonarClick: () -> Unit,
    onProyectosClick: () -> Unit,
    onVisiteoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¡Hola, Familia!", style = MaterialTheme.typography.headlineSmall)
            PildoraCiudad(ciudad = ciudad, onClick = {})
        }

        Spacer(Modifier.height(16.dp))

        // Tarjeta Azul de Llamado a la Acción
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("¿Listos para sumar este fin de semana?", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onExplorarClick) {
                    Text("Explorar ahora")
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("¿Cómo quieres ayudar hoy?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        // Cuadrícula 2x2
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TarjetaCategoria("Actividades", "En familia", Icons.Default.DateRange, onActividadesClick, Modifier.weight(1f))
                TarjetaCategoria("Quiero Donar", "Apoyo en especie", Icons.Default.Favorite, onDonarClick, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TarjetaCategoria("Proyectos", "Causas especiales", Icons.Default.List, onProyectosClick, Modifier.weight(1f))
                TarjetaCategoria("Directorio", "Visiteo y centros", Icons.Default.Place, onVisiteoClick, Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(24.dp))

        // Fila de Métricas
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("12", style = MaterialTheme.typography.headlineMedium)
                Text("Actividades", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("3", style = MaterialTheme.typography.headlineMedium)
                Text("Campañas", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("4", style = MaterialTheme.typography.headlineMedium)
                Text("Asociaciones", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}