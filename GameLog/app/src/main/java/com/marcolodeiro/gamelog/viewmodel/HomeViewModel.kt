package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.marcolodeiro.gamelog.data.model.GameLibraryEntry
import com.marcolodeiro.gamelog.data.model.getImageUrl
import com.marcolodeiro.gamelog.data.repository.IgdbRepository
import com.marcolodeiro.gamelog.data.repository.LibraryRepository
import com.marcolodeiro.gamelog.ui.screens.detail.GameStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameRecommendation(
    val name: String,
    val coverUrl: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val auth: FirebaseAuth,
    private val generativeModel: com.google.ai.client.generativeai.GenerativeModel,
    private val igdbRepository: IgdbRepository
) : ViewModel() {

    private val _playing = MutableStateFlow(0)
    val playing: StateFlow<Int> = _playing

    private val _completed = MutableStateFlow(0)
    val completed: StateFlow<Int> = _completed

    private val _pending = MutableStateFlow(0)
    val pending: StateFlow<Int> = _pending

    private val _activeGames = MutableStateFlow<List<GameLibraryEntry>>(emptyList())
    val activeGames: StateFlow<List<GameLibraryEntry>> = _activeGames

    private val _recommendations = MutableStateFlow<List<GameRecommendation>>(emptyList())
    val recommendations: StateFlow<List<GameRecommendation>> = _recommendations

    private val _recommendationsLoading = MutableStateFlow(true)
    val recommendationsLoading: StateFlow<Boolean> = _recommendationsLoading

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            try {
                _recommendationsLoading.value = true
                android.util.Log.d("HOME", "Cargando biblioteca...")
                val library = libraryRepository.getLibrary()
                android.util.Log.d("HOME", "Biblioteca: ${library.size} juegos")
                _playing.value   = library.count { it.status == GameStatus.PLAYING.name }
                _completed.value = library.count { it.status == GameStatus.COMPLETED.name }
                _pending.value   = library.count { it.status == GameStatus.PENDING.name }
                _activeGames.value = library.filter { it.status == GameStatus.PLAYING.name }

                if (library.isNotEmpty()) {
                    android.util.Log.d("HOME", "Llamando Gemini...")
                    loadRecommendations(library)
                } else {
                    android.util.Log.d("HOME", "Biblioteca vacía")
                    _recommendationsLoading.value = false
                }
            } catch (e: Exception) {
                android.util.Log.e("HOME", "Error loadStats: ${e.message}")
                _recommendationsLoading.value = false
            }
        }
    }

    private suspend fun loadRecommendations(library: List<GameLibraryEntry>) {
        try {
            val gameNames = library.take(10).joinToString(", ") { it.gameName }
            android.util.Log.d("HOME", "Prompt con juegos: $gameNames")
            val prompt = """
            Basándote en estos videojuegos que ha jugado el usuario: $gameNames
            Recomiéndale exactamente 5 videojuegos que podría disfrutar.
            Responde SOLO con los nombres de los juegos, uno por línea, sin numeración ni explicación.
        """.trimIndent()
            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: ""
            android.util.Log.d("HOME", "Respuesta Gemini: $text")
            val names = text.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .take(5)

            val recsWithCovers = names.map { name ->
                try {
                    val results = igdbRepository.searchGames(name)
                    val cover = results.firstOrNull()?.cover?.getImageUrl() ?: ""
                    android.util.Log.d("HOME", "Portada para $name: $cover")
                    GameRecommendation(name = name, coverUrl = cover)
                } catch (e: Exception) {
                    android.util.Log.e("HOME", "Error portada $name: ${e.message}")
                    GameRecommendation(name = name)
                }
            }
            android.util.Log.d("HOME", "Recomendaciones finales: ${recsWithCovers.size}")
            _recommendations.value = recsWithCovers
        } catch (e: Exception) {
            android.util.Log.e("HOME", "Error Gemini: ${e.message}")
            _recommendations.value = emptyList()
        } finally {
            _recommendationsLoading.value = false
        }
    }
}