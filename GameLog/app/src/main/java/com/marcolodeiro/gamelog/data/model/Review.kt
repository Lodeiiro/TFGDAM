package com.marcolodeiro.gamelog.data.model

// Modelo de reseña de un juego
data class Review(
    val id: String = "",
    val gameId: Int = 0,
    val gameName: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val rating: Float = 0f,        // Puntuación del 1 al 10
    val text: String = "",         // Texto de la reseña
    val timestamp: Long = System.currentTimeMillis()
)