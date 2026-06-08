package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcolodeiro.gamelog.data.model.NewsArticle
import com.marcolodeiro.gamelog.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NewsState {
    object Loading : NewsState()
    data class Success(val articles: List<NewsArticle>) : NewsState()
    data class Error(val message: String) : NewsState()
}

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<NewsState>(NewsState.Loading)
    val state: StateFlow<NewsState> = _state

    init {
        loadNews()
    }

    fun loadNews() {
        viewModelScope.launch {
            try {
                _state.value = NewsState.Loading
                val articles = newsRepository.getGamingNews()
                _state.value = NewsState.Success(articles)
            } catch (e: Exception) {
                _state.value = NewsState.Error(e.message ?: "Error al cargar noticias")
            }
        }
    }
}