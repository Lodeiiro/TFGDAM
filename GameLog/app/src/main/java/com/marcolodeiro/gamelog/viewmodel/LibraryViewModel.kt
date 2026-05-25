package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcolodeiro.gamelog.data.model.GameLibraryEntry
import com.marcolodeiro.gamelog.data.repository.LibraryRepository
import com.marcolodeiro.gamelog.ui.screens.detail.GameStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la pantalla de biblioteca
sealed class LibraryState {
    object Loading : LibraryState()
    data class Success(val games: List<GameLibraryEntry>) : LibraryState()
    data class Error(val message: String) : LibraryState()
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LibraryState>(LibraryState.Loading)
    val state: StateFlow<LibraryState> = _state

    // Filtro activo — null significa mostrar todos
    private val _activeFilter = MutableStateFlow<GameStatus?>(null)
    val activeFilter: StateFlow<GameStatus?> = _activeFilter

    init {
        loadLibrary()
    }

    // Carga todos los juegos de la biblioteca desde Firestore
    fun loadLibrary() {
        viewModelScope.launch {
            try {
                _state.value = LibraryState.Loading
                val games = libraryRepository.getLibrary()
                _state.value = LibraryState.Success(games)
            } catch (e: Exception) {
                _state.value = LibraryState.Error(e.message ?: "Error al cargar la biblioteca")
            }
        }
    }

    // Cambia el filtro activo por estado
    fun setFilter(status: GameStatus?) {
        _activeFilter.value = status
    }

    // Elimina un juego de la biblioteca
    fun removeGame(gameId: Int) {
        viewModelScope.launch {
            try {
                libraryRepository.removeGameFromLibrary(gameId)
                loadLibrary() // Recargamos después de eliminar
            } catch (e: Exception) {
                _state.value = LibraryState.Error(e.message ?: "Error al eliminar el juego")
            }
        }
    }
}