package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.marcolodeiro.gamelog.data.model.GameLibraryEntry
import com.marcolodeiro.gamelog.data.repository.LibraryRepository
import com.marcolodeiro.gamelog.ui.screens.detail.GameStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Datos estadísticos del perfil del usuario
data class ProfileStats(
    val totalGames: Int = 0,
    val playing: Int = 0,
    val completed: Int = 0,
    val platinum: Int = 0,
    val pending: Int = 0,
    val abandoned: Int = 0,
    val wishlist: Int = 0,
    val favoriteGenre: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    // Datos del usuario actual
    val currentUser = auth.currentUser

    private val _stats = MutableStateFlow(ProfileStats())
    val stats: StateFlow<ProfileStats> = _stats

    private val _games = MutableStateFlow<List<GameLibraryEntry>>(emptyList())
    val games: StateFlow<List<GameLibraryEntry>> = _games

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadProfile()
    }

    // Carga la biblioteca y calcula las estadísticas
    fun loadProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val library = libraryRepository.getLibrary()
                _games.value = library

                // Calculamos las estadísticas a partir de la biblioteca
                _stats.value = ProfileStats(
                    totalGames = library.size,
                    playing    = library.count { it.status == GameStatus.PLAYING.name },
                    completed  = library.count { it.status == GameStatus.COMPLETED.name },
                    platinum   = library.count { it.status == GameStatus.PLATINUM.name },
                    pending    = library.count { it.status == GameStatus.PENDING.name },
                    abandoned  = library.count { it.status == GameStatus.ABANDONED.name },
                    wishlist   = library.count { it.status == GameStatus.WISHLIST.name }
                )
            } catch (e: Exception) {
                // Si falla la carga dejamos las estadísticas en 0
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Cierra la sesión del usuario
    fun signOut() {
        auth.signOut()
    }
}

