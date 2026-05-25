package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.marcolodeiro.gamelog.data.repository.LibraryRepository
import com.marcolodeiro.gamelog.ui.screens.detail.GameStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    // Estadísticas del usuario
    private val _playing = MutableStateFlow(0)
    val playing: StateFlow<Int> = _playing

    private val _completed = MutableStateFlow(0)
    val completed: StateFlow<Int> = _completed

    private val _pending = MutableStateFlow(0)
    val pending: StateFlow<Int> = _pending

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            try {
                val library = libraryRepository.getLibrary()
                _playing.value   = library.count { it.status == GameStatus.PLAYING.name }
                _completed.value = library.count { it.status == GameStatus.COMPLETED.name }
                _pending.value   = library.count { it.status == GameStatus.PENDING.name }
            } catch (e: Exception) {
                // Si falla dejamos los valores en 0
            }
        }
    }
}