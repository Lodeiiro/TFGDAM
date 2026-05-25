package com.marcolodeiro.gamelog.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.marcolodeiro.gamelog.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthState {
    object Idle          : AuthState()
    object Loading       : AuthState()
    object Authenticated : AuthState()

    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    val currentUser: FirebaseUser? get() = auth.currentUser

    init {
        _authState.value = AuthState.Unauthenticated
    }


  //  init {
  //      if (auth.currentUser != null) {
  //          _authState.value = AuthState.Authenticated
  //      }
  //  }

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = auth.signInWithCredential(credential).await()
                result.user?.let { saveUserToFirestore(it) }
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    private suspend fun saveUserToFirestore(firebaseUser: FirebaseUser) {
        val userRef = firestore.collection("users").document(firebaseUser.uid)
        val snapshot = userRef.get().await()
        if (!snapshot.exists()) {
            val user = User(
                uid         = firebaseUser.uid,
                displayName = firebaseUser.displayName ?: "",
                email       = firebaseUser.email ?: "",
                photoUrl    = firebaseUser.photoUrl?.toString() ?: ""
            )
            userRef.set(user).await()
        }
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        _authState.value = AuthState.Idle
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { saveUserToFirestore(it) }
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    when {
                        e.message?.contains("no user record") == true -> "No existe una cuenta con ese email"
                        e.message?.contains("password is invalid") == true -> "Contraseña incorrecta"
                        e.message?.contains("badly formatted") == true -> "El email no es válido"
                        else -> e.message ?: "Error al iniciar sesión"
                    }
                )
            }
        }
    }

    // Registra un nuevo usuario con email y contraseña
    fun registerWithEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                // Actualizamos el nombre del usuario en Firebase Auth
                val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                    this.displayName = displayName
                }
                result.user?.updateProfile(profileUpdates)?.await()
                result.user?.let { saveUserToFirestore(it) }
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    when {
                        e.message?.contains("email address is already") == true -> "Ya existe una cuenta con ese email"
                        e.message?.contains("badly formatted") == true -> "El email no es válido"
                        e.message?.contains("password") == true -> "La contraseña debe tener al menos 6 caracteres"
                        else -> e.message ?: "Error al registrarse"
                    }
                )
            }
        }
    }

}
