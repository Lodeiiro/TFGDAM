package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcolodeiro.gamelog.data.model.User
import com.marcolodeiro.gamelog.data.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchUsersState {
    object Idle : SearchUsersState()
    object Loading : SearchUsersState()
    data class Success(val users: List<User>) : SearchUsersState()
    data class Error(val message: String) : SearchUsersState()
}

@HiltViewModel
class SearchUsersViewModel @Inject constructor(
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SearchUsersState>(SearchUsersState.Idle)
    val state: StateFlow<SearchUsersState> = _state

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Mapa de uid -> si lo seguimos o no
    private val _followingMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val followingMap: StateFlow<Map<String, Boolean>> = _followingMap

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    // Busca usuarios por nombre
    fun searchUsers() {
        val query = _searchQuery.value
        if (query.isBlank()) return

        viewModelScope.launch {
            try {
                _state.value = SearchUsersState.Loading
                val users = socialRepository.searchUsers(query)
                _state.value = SearchUsersState.Success(users)

                // Comprobamos si ya seguimos a cada usuario encontrado
                val map = mutableMapOf<String, Boolean>()
                users.forEach { user ->
                    map[user.uid] = socialRepository.isFollowing(user.uid)
                }
                _followingMap.value = map
            } catch (e: Exception) {
                _state.value = SearchUsersState.Error(e.message ?: "Error en la búsqueda")
            }
        }
    }

    // Alterna entre seguir y dejar de seguir
    fun toggleFollow(targetUid: String) {
        viewModelScope.launch {
            try {
                val isFollowing = _followingMap.value[targetUid] ?: false
                if (isFollowing) {
                    socialRepository.unfollowUser(targetUid)
                } else {
                    socialRepository.followUser(targetUid)
                }
                // Actualizamos el mapa local
                _followingMap.value = _followingMap.value.toMutableMap().apply {
                    put(targetUid, !isFollowing)
                }
            } catch (e: Exception) {
                _state.value = SearchUsersState.Error(e.message ?: "Error al seguir usuario")
            }
        }
    }
}