package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcolodeiro.gamelog.data.model.Review
import com.marcolodeiro.gamelog.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ReviewState {
    object Idle : ReviewState()
    object Loading : ReviewState()
    object Success : ReviewState()
    data class Error(val message: String) : ReviewState()
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _userReview = MutableStateFlow<Review?>(null)
    val userReview: StateFlow<Review?> = _userReview

    private val _state = MutableStateFlow<ReviewState>(ReviewState.Idle)
    val state: StateFlow<ReviewState> = _state

    // Carga las reseñas de un juego y la del usuario actual
    fun loadReviews(gameId: Int) {
        viewModelScope.launch {
            try {
                _reviews.value = reviewRepository.getReviewsForGame(gameId)
                _userReview.value = reviewRepository.getUserReview(gameId)
            } catch (e: Exception) {
                _state.value = ReviewState.Error(e.message ?: "Error al cargar reseñas")
            }
        }
    }

    // Publica o actualiza la reseña del usuario
    fun publishReview(gameId: Int, gameName: String, rating: Float, text: String) {
        viewModelScope.launch {
            try {
                _state.value = ReviewState.Loading
                reviewRepository.publishReview(gameId, gameName, rating, text)
                _userReview.value = reviewRepository.getUserReview(gameId)
                _reviews.value = reviewRepository.getReviewsForGame(gameId)
                _state.value = ReviewState.Success
            } catch (e: Exception) {
                _state.value = ReviewState.Error(e.message ?: "Error al publicar reseña")
            }
        }
    }

    // Elimina la reseña del usuario
    fun deleteReview(gameId: Int) {
        viewModelScope.launch {
            try {
                reviewRepository.deleteReview(gameId)
                _userReview.value = null
                _reviews.value = reviewRepository.getReviewsForGame(gameId)
            } catch (e: Exception) {
                _state.value = ReviewState.Error(e.message ?: "Error al eliminar reseña")
            }
        }
    }
}