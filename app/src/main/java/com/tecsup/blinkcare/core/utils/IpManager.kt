package com.tecsup.blinkcare.core.utils

import android.content.Context
import android.content.SharedPreferences


object IpManager {
    private const val PREF_NAME = "ip_preferences"
    private const val KEY_IP = "ip_address"
    private const val DEFAULT_IP = "192.168.1.43"  // Valor por defecto (puedes cambiarlo)
    private const val PORT = "8000"                 // Puerto del servidor

    fun getBaseUrl(context: Context): String {
        val ip = getSavedIp(context)
        return "http://$ip:$PORT/"
    }

    fun saveIp(context: Context, newIp: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_IP, newIp).apply()
    }

    fun getSavedIp(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_IP, DEFAULT_IP) ?: DEFAULT_IP
    }
}