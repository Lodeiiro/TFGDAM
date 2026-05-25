package com.marcolodeiro.gamelog.data.model

// Representa una actividad en el feed de un usuario
data class FeedItem(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val gameId: Int = 0,
    val gameName: String = "",
    val gameCover: String = "",
    val status: String = "",        // Estado que le ha puesto al juego
    val timestamp: Long = System.currentTimeMillis()
)