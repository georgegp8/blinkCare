package com.tecsup.blinkcare.blink.presentation.screens

import android.hardware.usb.UsbManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.blinkcare.blink.domain.model.Dispositivo
import com.tecsup.blinkcare.blink.presentation.viewmodel.DispositivosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import com.tecsup.blinkcare.R

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DispositivoCrudView() {
    val context = LocalContext.current
    val usbManager = context.getSystemService(UsbManager::class.java)

    val viewModel: DispositivosViewModel = viewModel()
    val dispositivos by viewModel.dispositivos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val blinkCount by viewModel.blinkCount.collectAsState()
    val showAlert by viewModel.showAlert.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.obtenerDispositivos()
        viewModel.iniciarLecturaUSB(usbManager)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                coroutineScope.launch {
                    viewModel.obtenerDispositivos()
                }
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refrescar dispositivos")
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (showAlert) {
                Text(
                    "¡ALERTA: Parpadeo insuficiente!",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Text(
                "Parpadeos por minuto: $blinkCount",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.padding(32.dp))

                dispositivos.isEmpty() -> Text(
                    "No hay dispositivos disponibles",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dispositivos) { dispositivo ->
                        DispositivoCard(
                            dispositivo = dispositivo,
                            viewModel = viewModel,
                            snackbarHostState = snackbarHostState,
                            coroutineScope = coroutineScope
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DispositivoCard(
    dispositivo: Dispositivo,
    viewModel: DispositivosViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    var conectadoLocal by remember { mutableStateOf(dispositivo.conectado) }

    val cardColor by animateColorAsState(
        targetValue = if (conectadoLocal) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(1000)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(dispositivo.nombre, style = MaterialTheme.typography.titleLarge)
            Text(dispositivo.descripcion, style = MaterialTheme.typography.bodyMedium)

            val fechaFormateada = dispositivo.lastSeen?.let {
                try {
                    val fecha = OffsetDateTime.parse(it)
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    fecha.format(formatter)
                } catch (_: Exception) {
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
                                    snackbarHostState.showSnackbar("Es seguro retirar el dispositivo")
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
