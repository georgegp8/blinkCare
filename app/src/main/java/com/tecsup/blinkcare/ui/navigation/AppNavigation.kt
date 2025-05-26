package com.tecsup.blinkcare.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tecsup.blinkcare.blink.presentation.screens.DispositivoCrudView


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dispositivos") {
        composable("dispositivos") {
            DispositivoCrudView()
        }
    }
}