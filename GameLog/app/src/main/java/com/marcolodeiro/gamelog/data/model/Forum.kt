package com.marcolodeiro.gamelog.data.model

// Modelo de hilo de foro
data class ForumThread(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val tag: String = "",           // Etiqueta: RPG, Acción, Noticias, etc.
    val timestamp: Long = System.currentTimeMillis(),
    val replyCount: Int = 0
)

// Modelo de respuesta en un hilo
data class ForumReply(
    val id: String = "",
    val threadId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)