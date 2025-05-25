package com.tecsup.blinkcare.blink.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.blinkcare.blink.domain.model.Dispositivo
import com.tecsup.blinkcare.core.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DispositivoViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _dispositivos = MutableStateFlow<List<Dispositivo>>(emptyList())
    val dispositivos: StateFlow<List<Dispositivo>> = _dispositivos

    fun cargarDispositivos() {
        viewModelScope.launch {
            _dispositivos.value = api.getDispositivos()
        }
    }

    fun agregarDispositivo(dispositivo: Dispositivo) {
        viewModelScope.launch {
            api.addDispositivo(dispositivo)
            cargarDispositivos()
        }
    }

    fun actualizarDispositivo(dispositivo: Dispositivo) {
        viewModelScope.launch {
            dispositivo.id?.let {
                api.updateDispositivo(it, dispositivo)
                cargarDispositivos()
            }
        }
    }

    fun eliminarDispositivo(id: Int) {
        viewModelScope.launch {
            api.deleteDispositivo(id)
            cargarDispositivos()
        }
    }
}
