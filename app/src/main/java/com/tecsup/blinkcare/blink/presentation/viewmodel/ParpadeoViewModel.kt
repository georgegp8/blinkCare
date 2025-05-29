package com.tecsup.blinkcare.blink.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tecsup.blinkcare.blink.domain.model.ParpadeoData

class ParpadeoViewModel : ViewModel() {
    var parpadeoData by mutableStateOf(ParpadeoData())
        private set

    private var timer = System.currentTimeMillis()

    fun actualizarConteo(nuevoConteo: Int) {
        val tiempoActual = System.currentTimeMillis()
        val tiempoPasado = (tiempoActual - timer) / 1000

        val alerta = tiempoPasado >= 60 && nuevoConteo < 20
        parpadeoData = ParpadeoData(conteo = nuevoConteo, alerta = alerta)

        if (tiempoPasado >= 60) timer = tiempoActual // reinicia cada minuto
    }
}