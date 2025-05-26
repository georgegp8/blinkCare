package com.tecsup.blinkcare.blink.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.blinkcare.blink.domain.model.Dispositivo
import com.tecsup.blinkcare.blink.presentation.viewmodel.DispositivosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispositivoCrudView() {
    val viewModel: DispositivosViewModel = viewModel()
    val dispositivos by viewModel.dispositivos.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var cargando by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect("autoRefresh") {
        cargando = true
        progress = 0f

        // Ejecutamos ambas tareas en paralelo
        val jobProgress = launch {
            while (progress < 1f && cargando) {
                progress += 0.05f
                delay(100)
            }
        }

        val jobConsulta = launch {
            while (cargando) {
                viewModel.obtenerDispositivos()
                if (viewModel.dispositivos.value.isNotEmpty()) {
                    cargando = false
                } else {
                    delay(500) // sigue intentando si aún no hay dispositivos
                }
            }
        }

        jobProgress.join()
        jobConsulta.cancel() // si ya terminó la carga, cancelamos el polling
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dispositivos ESP32") },
                actions = {
                    IconButton(onClick = {
                        cargando = true
                        progress = 0f
                        coroutineScope.launch {
                            while (progress < 1f) {
                                progress += 0.05f
                                kotlinx.coroutines.delay(100)
                            }
                            viewModel.obtenerDispositivos()
                            cargando = false
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (cargando) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Cargando dispositivos...", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceAtMost(1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dispositivos) { dispositivo ->
                        DispositivoCard(dispositivo, viewModel, snackbarHostState, coroutineScope)
                    }
                }
            }
        }
    }
}

@Composable
fun DispositivoCard(
    dispositivo: Dispositivo,
    viewModel: DispositivosViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    var conectadoLocal by remember { mutableStateOf(dispositivo.conectado) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (conectadoLocal)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(dispositivo.nombre, style = MaterialTheme.typography.titleLarge)
            Text(dispositivo.descripcion, style = MaterialTheme.typography.bodyMedium)

            val fechaFormateada = dispositivo.lastSeen?.let {
                try {
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                    parser.timeZone = TimeZone.getTimeZone("UTC")
                    val fecha = parser.parse(it)
                    val salida = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    salida.format(fecha!!)
                } catch (e: Exception) {
                    "Fecha no válida"
                }
            } ?: "Sin registro"

            Text("Último registro: $fechaFormateada", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (conectadoLocal) "Conectado" else "Desconectado")
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = conectadoLocal,
                        onCheckedChange = { nuevoEstado ->
                            conectadoLocal = nuevoEstado
                            viewModel.actualizarDispositivo(dispositivo.copy(conectado = nuevoEstado))

                            if (!nuevoEstado) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Es seguro retirar su dispositivo")
                                }
                            }
                        }
                    )
                }

                OutlinedButton(
                    onClick = {
                        viewModel.eliminarDispositivo(dispositivo.id)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}