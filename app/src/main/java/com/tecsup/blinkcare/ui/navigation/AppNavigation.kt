package com.tecsup.blinkcare.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.tecsup.blinkcare.blink.presentation.screens.DispositivoCrudView
import com.tecsup.blinkcare.blink.presentation.screens.LoginGoogleScreen
import com.tecsup.blinkcare.blink.presentation.screens.RegisterScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onCompletion

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val authStateFlow = auth.authStateFlow()
    val usuarioActual by authStateFlow.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (usuarioActual != null) "dispositivos" else "login"
    ) {
        composable("login") {
            LoginGoogleScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate("dispositivos") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                navController = navController,
                onRegisterSuccess = {
                    navController.navigate("dispositivos") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("dispositivos") {
            DispositivoCrudView()
            LaunchedEffect(usuarioActual) {
                if (usuarioActual == null) {
                    navController.navigate("login") {
                        popUpTo("dispositivos") { inclusive = true }
                    }
                }
            }
        }
    }
}

fun FirebaseAuth.authStateFlow(): StateFlow<com.google.firebase.auth.FirebaseUser?> {
    val stateFlow = MutableStateFlow(currentUser)
    val listener = AuthStateListener { auth ->
        stateFlow.value = auth.currentUser
    }
    addAuthStateListener(listener)
    stateFlow.onCompletion {
        removeAuthStateListener(listener)
    }
    return stateFlow
}