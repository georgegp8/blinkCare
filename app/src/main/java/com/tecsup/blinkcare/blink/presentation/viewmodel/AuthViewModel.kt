package com.tecsup.blinkcare.blink.presentation.viewmodel

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.tecsup.blinkcare.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(private val activity: Activity) : ViewModel() {

    val loading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val userIdToken = mutableStateOf<String?>(null)

    private val firebaseAuth = FirebaseAuth.getInstance()

    private fun generateNonce(length: Int = 32): String {
        val charset = "0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(activity)
        val webClientId = activity.getString(R.string.web_client)
        val nonce = generateNonce()

        viewModelScope.launch {
            loading.value = true
            errorMessage.value = null

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .setNonce(nonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = withContext(Dispatchers.IO) {
                    credentialManager.getCredential(activity, request)
                }
                handleSignInResult(result)
            } catch (e: NoCredentialException) {
                try {
                    val registroOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(webClientId)
                        .setNonce(nonce)
                        .build()
                    val registroRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(registroOption)
                        .build()
                    val registroResult = withContext(Dispatchers.IO) {
                        credentialManager.getCredential(activity, registroRequest)
                    }
                    handleSignInResult(registroResult)
                } catch (ex: Exception) {
                    errorMessage.value = "Error en autenticación: ${ex.localizedMessage}"
                    Log.e("AuthViewModel", "Authentication error", ex)
                }
            } catch (e: Exception) {
                errorMessage.value = "Error inesperado: ${e.localizedMessage}"
                Log.e("AuthViewModel", "Unexpected error", e)
            } finally {
                loading.value = false
            }
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse) {
        val credential = result.credential

        if (credential is CustomCredential) {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    if (idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        firebaseAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    userIdToken.value = idToken
                                } else {
                                    errorMessage.value = it.exception?.localizedMessage ?: "Error en Firebase Auth"
                                }
                            }
                    } else {
                        errorMessage.value = "Token inválido"
                    }
                } catch (e: GoogleIdTokenParsingException) {
                    errorMessage.value = "Error al analizar token"
                }
            } else {
                errorMessage.value = "Tipo inesperado de credencial: ${credential.type}"
                Log.e("AuthViewModel", "Unexpected credential type: ${credential.type}")
            }
        } else {
            errorMessage.value = "Tipo de credencial no reconocido"
            Log.e("AuthViewModel", "Credential is not a CustomCredential")
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage.value = "Por favor, ingrese correo y contraseña"
            return
        }

        viewModelScope.launch {
            loading.value = true
            errorMessage.value = null

            try {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            userIdToken.value = firebaseAuth.currentUser?.uid
                        } else {
                            errorMessage.value = task.exception?.localizedMessage ?: "Error al iniciar sesión"
                            Log.e("AuthViewModel", "Email sign-in error", task.exception)
                        }
                        loading.value = false
                    }
            } catch (e: Exception) {
                errorMessage.value = "Error inesperado: ${e.localizedMessage}"
                Log.e("AuthViewModel", "Unexpected error", e)
                loading.value = false
            }
        }
    }

    fun registerWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage.value = "Por favor, ingrese correo y contraseña"
            return
        }

        viewModelScope.launch {
            loading.value = true
            errorMessage.value = null

            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            userIdToken.value = firebaseAuth.currentUser?.uid
                        } else {
                            errorMessage.value = task.exception?.localizedMessage ?: "Error al registrarse"
                            Log.e("AuthViewModel", "Email registration error", task.exception)
                        }
                        loading.value = false
                    }
            } catch (e: Exception) {
                errorMessage.value = "Error inesperado: ${e.localizedMessage}"
                Log.e("AuthViewModel", "Unexpected error", e)
                loading.value = false
            }
        }
    }

    fun logout() {
        val credentialManager = CredentialManager.create(activity)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                firebaseAuth.signOut()
                userIdToken.value = null
                Log.d("AuthViewModel", "Sesión cerrada exitosamente")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al cerrar sesión", e)
                errorMessage.value = "Error al cerrar sesión: ${e.localizedMessage}"
            }
        }
    }
}
