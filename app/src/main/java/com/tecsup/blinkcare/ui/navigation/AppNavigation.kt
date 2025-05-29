package com.tecsup.blinkcare.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tecsup.blinkcare.blink.presentation.screens.DispositivoCrudView
import com.tecsup.blinkcare.blink.presentation.screens.LoginGoogleScreen
import com.tecsup.blinkcare.blink.presentation.screens.RegisterScreen
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val usuarioActual by auth.authStateFlow().collectAsState(initial = auth.currentUser)

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

            // Si el usuario cierra sesión, volver al login
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

fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    trySend(currentUser) // Emitir el estado inicial
    awaitClose { removeAuthStateListener(listener) }
}