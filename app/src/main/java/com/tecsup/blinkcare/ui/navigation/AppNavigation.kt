package com.tecsup.blinkcare.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tecsup.blinkcare.blink.presentation.screens.DispositivoCrudView


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dispositivos") {
        composable("dispositivos") {
            DispositivoCrudView()
        }
    }
}