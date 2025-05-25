package com.tecsup.blinkcare.blink.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tecsup.blinkcare.blink.domain.model.Dispositivo

@Composable
fun DispositivoCrudView(viewModel: DispositivoViewModel = hiltViewModel()) {
    val dispositivos by viewModel.dispositivos.collectAsState()
    var nuevoNombre by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Dispositivos conectados", style = MaterialTheme.typography.titleLarge)

        dispositivos.forEach {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(it.nombre)
                Button(onClick = { viewModel.eliminarDispositivo(it.id ?: 0) }) {
                    Text("Eliminar")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = nuevoNombre,
            onValueChange = { nuevoNombre = it },
            label = { Text("Nuevo dispositivo") }
        )
        Button(onClick = {
            if (nuevoNombre.isNotBlank()) {
                viewModel.agregarDispositivo(
                    Dispositivo(nombre = nuevoNombre, estado = "activo", ip = "192.168.1.100")
                )
                nuevoNombre = ""
            }
        }) {
            Text("Agregar")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarDispositivos()
    }
}