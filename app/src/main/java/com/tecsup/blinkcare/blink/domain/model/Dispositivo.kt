package com.tecsup.blinkcare.blink.domain.model

import com.google.gson.annotations.SerializedName

data class Dispositivo(
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val conectado: Boolean = false,

    @SerializedName("last_seen")
    val lastSeen: String? = null  // puede ser null si el backend aún no lo envió
)