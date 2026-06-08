package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcolodeiro.gamelog.data.model.FeedItem
import com.marcolodeiro.gamelog.data.model.Review
import com.marcolodeiro.gamelog.data.repository.FeedRepository
import com.marcolodeiro.gamelog.data.repository.ReviewRepository
import com.marcolodeiro.gamelog.data.repository.SocialRepository
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
    private val feedRepository: FeedRepository,
    private val reviewRepository: ReviewRepository,
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _state = MutableStateFlow<FeedState>(FeedState.Loading)
    val state: StateFlow<FeedState> = _state

    // Reseñas de los usuarios que seguimos
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    init {
        loadFeed()
        loadReviews()
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

    // Carga las reseñas de los usuarios que seguimos
    fun loadReviews() {
        viewModelScope.launch {
            try {
                val following = socialRepository.getFollowing()
                val followingIds = following.map { it.uid }

                if (followingIds.isEmpty()) {
                    _reviews.value = emptyList()
                    return@launch
                }

                // Obtenemos las reseñas de Firestore de los usuarios que seguimos
                val allReviews = reviewRepository.getReviewsFromUsers(followingIds)
                _reviews.value = allReviews.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                _reviews.value = emptyList()
            }
        }
    }


}