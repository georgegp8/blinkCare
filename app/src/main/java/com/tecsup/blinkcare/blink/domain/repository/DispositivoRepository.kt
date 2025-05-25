package com.tecsup.blinkcare.blink.domain.repository

import com.tecsup.blinkcare.blink.domain.model.Dispositivo
import com.tecsup.blinkcare.core.network.ApiService

class DispositivoRepository(private val api: ApiService) {

    suspend fun listar() = api.getDispositivos()
    suspend fun agregar(dispositivo: Dispositivo) = api.addDispositivo(dispositivo)
    suspend fun editar(id: Int, dispositivo: Dispositivo) = api.updateDispositivo(id, dispositivo)
    suspend fun eliminar(id: Int) = api.deleteDispositivo(id)
}