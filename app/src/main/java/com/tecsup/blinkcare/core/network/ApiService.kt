package com.tecsup.blinkcare.core.network
import com.tecsup.blinkcare.blink.domain.model.Dispositivo
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("dispositivos")
    suspend fun getDispositivos(): List<Dispositivo>

    @POST("dispositivos")
    suspend fun addDispositivo(@Body dispositivo: Dispositivo): Response<Unit>

    @PUT("dispositivos/{id}")
    suspend fun updateDispositivo(
        @Path("id") id: Int,
        @Body dispositivo: Dispositivo
    ): Response<Unit>

    @DELETE("dispositivos/{id}")
    suspend fun deleteDispositivo(@Path("id") id: Int): Response<Unit>
}
