package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcolodeiro.gamelog.data.model.GameLibraryEntry
import com.marcolodeiro.gamelog.data.model.User
import com.marcolodeiro.gamelog.data.repository.PublicProfileRepository
import com.marcolodeiro.gamelog.data.repository.SocialRepository
import com.marcolodeiro.gamelog.ui.screens.detail.GameStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PublicProfileState {
    object Loading : PublicProfileState()
    data class Success(val user: User, val library: List<GameLibraryEntry>) : PublicProfileState()
    data class Error(val message: String) : PublicProfileState()
}

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val publicProfileRepository: PublicProfileRepository,
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PublicProfileState>(PublicProfileState.Loading)
    val state: StateFlow<PublicProfileState> = _state

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing

    // Carga el perfil y biblioteca de un usuario
    fun loadProfile(uid: String) {
        viewModelScope.launch {
            try {
                _state.value = PublicProfileState.Loading
                val user = publicProfileRepository.getUserProfile(uid)
                val library = publicProfileRepository.getUserLibrary(uid)
                if (user != null) {
                    _state.value = PublicProfileState.Success(user, library)
                } else {
                    _state.value = PublicProfileState.Error("Usuario no encontrado")
                }
                _isFollowing.value = socialRepository.isFollowing(uid)
            } catch (e: Exception) {
                _state.value = PublicProfileState.Error(e.message ?: "Error al cargar el perfil")
            }
        }
    }

    // Alterna entre seguir y dejar de seguir
    fun toggleFollow(uid: String) {
        viewModelScope.launch {
            try {
                if (_isFollowing.value) {
                    socialRepository.unfollowUser(uid)
                } else {
                    socialRepository.followUser(uid)
                }
                _isFollowing.value = !_isFollowing.value
            } catch (e: Exception) {
                _state.value = PublicProfileState.Error(e.message ?: "Error al seguir usuario")
            }
        }
    }
}