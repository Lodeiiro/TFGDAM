package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcolodeiro.gamelog.data.model.Game
import com.marcolodeiro.gamelog.data.repository.LibraryRepository
import com.marcolodeiro.gamelog.ui.screens.detail.GameStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la operación de guardar en biblioteca
sealed class LibraryActionState {
    object Idle : LibraryActionState()
    object Loading : LibraryActionState()
    object Success : LibraryActionState()
    data class Error(val message: String) : LibraryActionState()
}

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _actionState = MutableStateFlow<LibraryActionState>(LibraryActionState.Idle)
    val actionState: StateFlow<LibraryActionState> = _actionState

    // Estado actual del juego en la biblioteca (null si no está)
    private val _currentStatus = MutableStateFlow<GameStatus?>(null)
    val currentStatus: StateFlow<GameStatus?> = _currentStatus

    // Comprueba si el juego ya está en la biblioteca al abrir el detalle
    fun checkGameStatus(gameId: Int) {
        viewModelScope.launch {
            try {
                _currentStatus.value = libraryRepository.getGameStatus(gameId)
            } catch (e: Exception) {
                // Si falla simplemente asumimos que no está en la biblioteca
            }
        }
    }

    // Añade el juego a la biblioteca con el estado seleccionado
    fun addToLibrary(game: Game, status: GameStatus) {
        viewModelScope.launch {
            try {
                _actionState.value = LibraryActionState.Loading
                libraryRepository.addGameToLibrary(game, status)
                libraryRepository.publishFeedItem(game, status) // Publica en el feed
                _currentStatus.value = status
                _actionState.value = LibraryActionState.Success
            } catch (e: Exception) {
                _actionState.value = LibraryActionState.Error(
                    e.message ?: "Error al guardar en la biblioteca"
                )
            }
        }
    }
}