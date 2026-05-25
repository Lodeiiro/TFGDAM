package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcolodeiro.gamelog.data.model.FeedItem
import com.marcolodeiro.gamelog.data.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FeedState {
    object Loading : FeedState()
    data class Success(val items: List<FeedItem>) : FeedState()
    data class Error(val message: String) : FeedState()
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _state = MutableStateFlow<FeedState>(FeedState.Loading)
    val state: StateFlow<FeedState> = _state

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            try {
                _state.value = FeedState.Loading
                val items = feedRepository.getFeed()
                _state.value = FeedState.Success(items)
            } catch (e: Exception) {
                _state.value = FeedState.Error(e.message ?: "Error al cargar el feed")
            }
        }
    }
}