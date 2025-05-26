package com.tecsup.blinkcare.blink.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.blinkcare.blink.domain.model.Dispositivo
import kotlinx.coroutines.launch
import com.tecsup.blinkcare.core.network.RetrofitClient

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DispositivosViewModel : ViewModel() {
    private val api = RetrofitClient.apiService

    private val _dispositivos = MutableStateFlow<List<Dispositivo>>(emptyList())
    val dispositivos: StateFlow<List<Dispositivo>> = _dispositivos

    var isLoading = mutableStateOf(true)
        private set

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
}