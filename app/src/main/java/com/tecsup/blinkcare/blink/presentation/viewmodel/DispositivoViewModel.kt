package com.tecsup.blinkcare.blink.presentation.viewmodel

import android.hardware.usb.UsbManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.tecsup.blinkcare.blink.data.repository.FirebaseBlinkRepository
import com.tecsup.blinkcare.blink.domain.model.Dispositivo
import com.tecsup.blinkcare.core.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.Executors

class DispositivosViewModel : ViewModel() {
    private val api = RetrofitClient.apiService

    private val _dispositivos = MutableStateFlow<List<Dispositivo>>(emptyList())
    val dispositivos: StateFlow<List<Dispositivo>> = _dispositivos

    val isLoading = MutableStateFlow(true)
    val blinkCount = MutableStateFlow(0)
    val showAlert = MutableStateFlow(false)

    private val firebaseRepo = FirebaseBlinkRepository()

    init {
        // Escuchar los datos en tiempo real desde Firebase Realtime Database
        firebaseRepo.listenBlinkData(
            onBlinkCountChanged = { count ->
                viewModelScope.launch {
                    blinkCount.value = count
                }
            },
            onAlertChanged = { alert ->
                viewModelScope.launch {
                    showAlert.value = alert
                }
            }
        )
    }

    fun obtenerDispositivos() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                _dispositivos.value = api.getDispositivos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isLoading.value = false
        }
    }

    fun agregarDispositivo(dispositivo: Dispositivo) {
        viewModelScope.launch {
            try {
                api.addDispositivo(dispositivo)
                obtenerDispositivos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun actualizarDispositivo(dispositivo: Dispositivo) {
        viewModelScope.launch {
            try {
                api.updateDispositivo(dispositivo.id, dispositivo)
                obtenerDispositivos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarDispositivo(id: Int) {
        viewModelScope.launch {
            try {
                api.deleteDispositivo(id)
                obtenerDispositivos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun iniciarLecturaUSB(usbManager: UsbManager) {
        viewModelScope.launch(Dispatchers.IO) {
            val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
            if (availableDrivers.isEmpty()) return@launch

            val driver = availableDrivers[0]
            val connection = usbManager.openDevice(driver.device) ?: return@launch
            val port: UsbSerialPort = driver.ports[0]

            try {
                port.open(connection)
                port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

                val ioManager = SerialInputOutputManager(port, object : SerialInputOutputManager.Listener {
                    override fun onNewData(data: ByteArray) {
                        val input = String(data).trim()
                        Log.d("SerialData", input)

                        if (input.startsWith("PARPADEOS:")) {
                            val count = input.removePrefix("PARPADEOS:").toIntOrNull()
                            if (count != null) {
                                viewModelScope.launch {
                                    blinkCount.value = count
                                    showAlert.value = count < 20
                                }
                            }
                        }
                    }

                    override fun onRunError(e: Exception) {
                        Log.e("SerialError", "Error en lectura serial", e)
                    }
                })

                Executors.newSingleThreadExecutor().submit(ioManager as Runnable)

            } catch (e: IOException) {
                Log.e("SerialError", "No se pudo abrir el puerto", e)
            }
        }
    }
}
