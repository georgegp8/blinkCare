package com.tecsup.blinkcare.blink.presentation.screens

import android.hardware.usb.UsbManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.blinkcare.R
import com.tecsup.blinkcare.blink.presentation.viewmodel.DispositivosViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorView() {
    val context = LocalContext.current
    val usbManager = context.getSystemService(UsbManager::class.java)
    val viewModel: DispositivosViewModel = viewModel()

    val blinkCount by viewModel.blinkCount.collectAsState(initial = 0)
    val showAlert by viewModel.showAlert.collectAsState(initial = false)

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.iniciarLecturaUSB(usbManager)
    }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            blinkCount >= 20 -> Color(0xFFD0E8FF)
            blinkCount in 10..19 -> Color(0xFFFFF4CC)
            else -> Color(0xFFFFE0E6)
        },
        animationSpec = tween(700), label = "bgColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Parpadeos/min", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            modifier = Modifier.size(200.dp),
            tonalElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = String.format("%02d", blinkCount),
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_usb),
                contentDescription = "USB conectado",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Arduino")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = blinkCount >= 20,
                onClick = {},
                label = { Text("Saludable") },
                leadingIcon = if (blinkCount >= 20) {
                    { Icon(
                        painter = painterResource(id = R.drawable.ic_usb),
                        contentDescription = "USB conectado",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    ) }
                } else null
            )
            FilterChip(
                selected = blinkCount in 10..19,
                onClick = {},
                label = { Text("Regular") },
                leadingIcon = if (blinkCount in 10..19) {
                    { Icon(
                        painter = painterResource(id = R.drawable.ic_usb),
                        contentDescription = "USB conectado",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    ) }
                } else null
            )
            FilterChip(
                selected = blinkCount < 10,
                onClick = {},
                label = { Text("Insuficiente") },
                leadingIcon = if (blinkCount < 10) {
                    { Icon(
                        painter = painterResource(id = R.drawable.ic_usb),
                        contentDescription = "USB conectado",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    ) }
                } else null
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.blinkCount.value = 0
                    viewModel.showAlert.value = false
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF643A57))
        ) {
            Text("Detener monitoreo")
        }

        if (showAlert) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "¡ALERTA: Parpadeo insuficiente!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}
