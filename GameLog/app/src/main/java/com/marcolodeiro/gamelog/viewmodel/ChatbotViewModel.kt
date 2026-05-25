package com.marcolodeiro.gamelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Message(val text: String, val isFromUser: Boolean)

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    // Iniciamos la sesión de chat con un contexto inicial para guiar a Gemini
    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("Actúa como un recomendador de videojuegos experto en una red social llamada GameLog.") },
            content(role = "model") { text("¡Entendido! Estoy listo para recomendar los mejores videojuegos según los gustos, géneros y plataformas de los usuarios de GameLog. ¿De qué juegos o géneros te gustaría hablar hoy?") }
        )
    )

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Mensaje inicial del Bot para dar la bienvenida en la pantalla
        _messages.value = listOf(
            Message("¡Hola! Soy tu asistente de GameLog. ¿Qué tipo de juegos te gustaría que te recomiende hoy? 🎮", isFromUser = false)
        )
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(Message(userMessage, isFromUser = true))
        _messages.value = currentMessages

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userMessage)
                response.text?.let { botResponse ->
                    currentMessages.add(Message(botResponse, isFromUser = false))
                    _messages.value = currentMessages
                }
            } catch (e: Exception) {
                currentMessages.add(
                    Message("Lo siento, hubo un problema al generar la recomendación: ${e.localizedMessage}", isFromUser = false)
                )
                _messages.value = currentMessages
            } finally {
                _isLoading.value = false
            }
        }
    }
}