package com.tecsup.blinkcare.core.network
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("devices")
    suspend fun getDevices(): Response<List<DeviceDto>>

    @POST("devices")
    suspend fun addDevice(@Body device: DeviceDto): Response<DeviceDto>

    @PUT("devices/{id}")
    suspend fun updateDevice(@Path("id") id: Int, @Body device: DeviceDto): Response<DeviceDto>

    @DELETE("devices/{id}")
    suspend fun deleteDevice(@Path("id") id: Int): Response<Unit>
}