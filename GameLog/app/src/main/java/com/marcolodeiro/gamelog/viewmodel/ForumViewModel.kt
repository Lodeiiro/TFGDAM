package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcolodeiro.gamelog.data.model.ForumReply
import com.marcolodeiro.gamelog.data.model.ForumThread
import com.marcolodeiro.gamelog.data.repository.ForumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ForumState {
    object Loading : ForumState()
    data class Success(val threads: List<ForumThread>) : ForumState()
    data class Error(val message: String) : ForumState()
}

sealed class ThreadState {
    object Loading : ThreadState()
    data class Success(
        val thread: ForumThread,
        val replies: List<ForumReply>
    ) : ThreadState()
    data class Error(val message: String) : ThreadState()
}

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val forumRepository: ForumRepository
) : ViewModel() {

    private val _forumState = MutableStateFlow<ForumState>(ForumState.Loading)
    val forumState: StateFlow<ForumState> = _forumState

    private val _threadState = MutableStateFlow<ThreadState>(ThreadState.Loading)
    val threadState: StateFlow<ThreadState> = _threadState

    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting

    init {
        loadThreads()
    }

    // Carga todos los hilos
    fun loadThreads() {
        viewModelScope.launch {
            try {
                _forumState.value = ForumState.Loading
                val threads = forumRepository.getThreads()
                _forumState.value = ForumState.Success(threads)
            } catch (e: Exception) {
                _forumState.value = ForumState.Error(e.message ?: "Error al cargar el foro")
            }
        }
    }

    // Crea un nuevo hilo
    fun createThread(title: String, content: String, tag: String) {
        viewModelScope.launch {
            try {
                _isPosting.value = true
                forumRepository.createThread(title, content, tag)
                loadThreads()
            } catch (e: Exception) {
                _forumState.value = ForumState.Error(e.message ?: "Error al crear el hilo")
            } finally {
                _isPosting.value = false
            }
        }
    }

    // Carga un hilo con sus respuestas
    fun loadThread(thread: ForumThread) {
        viewModelScope.launch {
            try {
                _threadState.value = ThreadState.Loading
                val replies = forumRepository.getReplies(thread.id)
                _threadState.value = ThreadState.Success(thread, replies)
            } catch (e: Exception) {
                _threadState.value = ThreadState.Error(e.message ?: "Error al cargar el hilo")
            }
        }
    }

    // Añade una respuesta
    fun addReply(threadId: String, content: String, thread: ForumThread) {
        viewModelScope.launch {
            try {
                _isPosting.value = true
                forumRepository.addReply(threadId, content)
                loadThread(thread)
            } catch (e: Exception) {
                _threadState.value = ThreadState.Error(e.message ?: "Error al responder")
            } finally {
                _isPosting.value = false
            }
        }
    }
}