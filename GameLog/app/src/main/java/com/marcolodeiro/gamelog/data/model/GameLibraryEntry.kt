package com.marcolodeiro.gamelog.data.model

// Representa un juego guardado en la biblioteca del usuario en Firestore
data class GameLibraryEntry(
    val gameId: Int = 0,
    val gameName: String = "",
    val gameCover: String = "",      // URL de la portada
    val gameRating: Double? = null,
    val status: String = "",         // Estado: PLAYING, COMPLETED, etc.
    val userId: String = "",         // UID del usuario propietario
    val addedAt: Long = System.currentTimeMillis()
)