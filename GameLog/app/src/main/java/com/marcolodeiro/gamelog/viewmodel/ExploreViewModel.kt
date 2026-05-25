package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcolodeiro.gamelog.data.model.Game
import com.marcolodeiro.gamelog.data.repository.IgdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado posible de la pantalla de explorar
sealed class ExploreState {
    object Idle : ExploreState()                          // Estado inicial, sin búsqueda
    object Loading : ExploreState()                       // Cargando resultados
    data class Success(val games: List<Game>) : ExploreState()  // Juegos cargados correctamente
    data class Error(val message: String) : ExploreState()      // Error al cargar
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: IgdbRepository  // Repositorio que conecta con IGDB
) : ViewModel() {

    private val _state = MutableStateFlow<ExploreState>(ExploreState.Idle)
    val state: StateFlow<ExploreState> = _state

    // Texto de búsqueda actual
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Al crear el ViewModel cargamos los juegos populares directamente
    init {
        loadPopularGames()
    }

    // Carga los juegos más populares de IGDB
    fun loadPopularGames() {
        viewModelScope.launch {
            try {
                _state.value = ExploreState.Loading
                val games = repository.getPopularGames()
                _state.value = ExploreState.Success(games)
            } catch (e: Exception) {
                _state.value = ExploreState.Error(e.message ?: "Error al cargar juegos")
            }
        }
    }

    // Busca juegos por nombre
    fun searchGames(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            loadPopularGames()
            return
        }
        viewModelScope.launch {
            try {
                _state.value = ExploreState.Loading
                val games = repository.searchGames(query)
                _state.value = ExploreState.Success(games)
            } catch (e: Exception) {
                _state.value = ExploreState.Error(e.message ?: "Error en la búsqueda")
            }
        }
    }

    // Actualiza el texto del campo de búsqueda
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}