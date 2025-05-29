package com.tecsup.blinkcare.blink.presentation.screens

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.blinkcare.blink.domain.model.Dispositivo
import com.tecsup.blinkcare.blink.presentation.viewmodel.AuthViewModel
import com.tecsup.blinkcare.blink.presentation.viewmodel.AuthViewModelFactory
import com.tecsup.blinkcare.blink.presentation.viewmodel.DispositivosViewModel
import com.tecsup.blinkcare.blink.presentation.viewmodel.ParpadeoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispositivoCrudView() {
    val context = LocalContext.current
    if (context !is Activity) {
        Text(
            text = "Error: Contexto no es una Activity",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
        )
        return
    }

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val dispositivosViewModel: DispositivosViewModel = viewModel()

    val dispositivos by dispositivosViewModel.dispositivos.collectAsState()
    val isLoading by dispositivosViewModel.isLoading
    val authErrorMessage by authViewModel.errorMessage

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Carga inicial
    LaunchedEffect(Unit) {
        dispositivosViewModel.obtenerDispositivos()
    }

    // Mostrar errores
    LaunchedEffect(authErrorMessage) {
        authErrorMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
            authViewModel.errorMessage.value = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dispositivos ESP32") },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            dispositivosViewModel.obtenerDispositivos()
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                    IconButton(onClick = {
                        authViewModel.logout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Cargando dispositivos...", style = MaterialTheme.typography.bodyLarge)

                    }
                }

                dispositivos.isEmpty() -> {
                    Text(
                        text = "No hay dispositivos disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(dispositivos) { dispositivo ->
                            DispositivoCard(
                                dispositivo = dispositivo,
                                viewModel = dispositivosViewModel,
                                snackbarHostState = snackbarHostState,
                                coroutineScope = coroutineScope
                            )
                        }
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
        targetValue = if (conectadoLocal)
            MaterialTheme.colorScheme.tertiaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 1000, delayMillis = 150) // ← MÁS LENTO
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

@Composable
fun ContadorParpadeosView(viewModel: ParpadeoViewModel = viewModel()) {
    val data = viewModel.parpadeoData

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Conteo de Parpadeos", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("${data.conteo}", style = MaterialTheme.typography.displayLarge)

        if (data.alerta) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "¡Alerta! Parpadeos insuficientes",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}