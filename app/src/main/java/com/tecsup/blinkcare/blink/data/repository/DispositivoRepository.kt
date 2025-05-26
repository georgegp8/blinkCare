package com.tecsup.blinkcare.blink.data.repository

import android.util.Log
import com.tecsup.blinkcare.blink.domain.model.Dispositivo
import com.tecsup.blinkcare.core.network.RetrofitClient

class DispositivoRepository {

    private val api = RetrofitClient.apiService

    suspend fun getAll(): List<Dispositivo> {
        return try {
            api.getDispositivos()
        } catch (e: Exception) {
            Log.e("Repo", "Error al obtener dispositivos: ${e.message}")
            emptyList()
        }
    }

    suspend fun add(dispositivo: Dispositivo): Boolean {
        return try {
            val response = api.addDispositivo(dispositivo)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("Repo", "Error al agregar dispositivo: ${e.message}")
            false
        }
    }

    suspend fun update(id: Int, dispositivo: Dispositivo): Boolean {
        return try {
            val response = api.updateDispositivo(id, dispositivo)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("Repo", "Error al actualizar dispositivo: ${e.message}")
            false
        }
    }

    suspend fun delete(id: Int): Boolean {
        return try {
            val response = api.deleteDispositivo(id)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("Repo", "Error al eliminar dispositivo: ${e.message}")
            false
        }
    }
}