package com.tecsup.blinkcare.blink.presentation.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()

    // Google One Tap sign-in client
    val oneTapClient = remember { Identity.getSignInClient(context) }

    val signInRequest = remember {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("TU_CLIENT_ID_DE_FIREBASE") // Cambia por tu client ID
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    loading = true
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(activity) { task ->
                            loading = false
                            if (task.isSuccessful) {
                                onLoginSuccess()
                            } else {
                                errorMessage = task.exception?.localizedMessage ?: "Error en autenticación"
                            }
                        }
                } else {
                    errorMessage = "No se obtuvo el token de Google"
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Error inesperado"
            }
        } else {
            errorMessage = "Inicio de sesión cancelado"
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Iniciar sesión") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            errorMessage = null
                            loading = true
                            oneTapClient.beginSignIn(signInRequest)
                                .addOnSuccessListener(activity) { result ->
                                    loading = false
                                    launcher.launch(
                                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                                    )
                                }
                                .addOnFailureListener(activity) {
                                    loading = false
                                    errorMessage = "No se pudo iniciar sesión con Google"
                                }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Login, contentDescription = "Google Login")
                        Spacer(Modifier.width(8.dp))
                        Text("Iniciar sesión con Google")
                    }
                }
                errorMessage?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}