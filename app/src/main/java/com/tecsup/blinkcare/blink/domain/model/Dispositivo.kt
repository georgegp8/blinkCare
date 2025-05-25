package com.tecsup.blinkcare.blink.domain.model

data class Dispositivo(
    val id: Int? = null,
    val nombre: String,
    val estado: String,
    val ip: String
)