package com.marcolodeiro.gamelog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.marcolodeiro.gamelog.data.model.Game
import com.marcolodeiro.gamelog.data.model.GameLibraryEntry
import com.marcolodeiro.gamelog.data.model.getImageUrl
import com.marcolodeiro.gamelog.ui.screens.detail.GameStatus
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.marcolodeiro.gamelog.data.model.FeedItem

@Singleton
class LibraryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Referencia a la colección de biblioteca del usuario actual
    private fun libraryCollection() = firestore
        .collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("library")

    // Añade o actualiza un juego en la biblioteca
    suspend fun addGameToLibrary(game: Game, status: GameStatus) {
        val entry = GameLibraryEntry(
            gameId    = game.id,
            gameName  = game.name,
            gameCover = game.cover?.getImageUrl() ?: "",
            gameRating = game.rating,
            status    = status.name,
            userId    = auth.currentUser?.uid ?: ""
        )
        // Usamos el gameId como ID del documento para evitar duplicados
        libraryCollection()
            .document(game.id.toString())
            .set(entry)
            .await()
    }

    // Obtiene todos los juegos de la biblioteca del usuario
    suspend fun getLibrary(): List<GameLibraryEntry> {
        return libraryCollection()
            .get()
            .await()
            .toObjects(GameLibraryEntry::class.java)
    }

    // Elimina un juego de la biblioteca
    suspend fun removeGameFromLibrary(gameId: Int) {
        libraryCollection()
            .document(gameId.toString())
            .delete()
            .await()
    }

    // Comprueba si un juego ya está en la biblioteca
    suspend fun getGameStatus(gameId: Int): GameStatus? {
        val doc = libraryCollection()
            .document(gameId.toString())
            .get()
            .await()
        return if (doc.exists()) {
            val statusStr = doc.getString("status") ?: return null
            GameStatus.valueOf(statusStr)
        } else null
    }

    // Publica la actividad en el feed global cuando el usuario añade un juego
    suspend fun publishFeedItem(game: Game, status: GameStatus) {
        val user = auth.currentUser ?: return
        val feedItem = com.marcolodeiro.gamelog.data.model.FeedItem(
            id        = "${user.uid}_${game.id}_${System.currentTimeMillis()}",
            userId    = user.uid,
            userName  = user.displayName ?: "Gamer",
            userPhoto = user.photoUrl?.toString() ?: "",
            gameId    = game.id,
            gameName  = game.name,
            gameCover = game.cover?.getImageUrl() ?: "",
            status    = status.name,
            timestamp = System.currentTimeMillis()
        )
        // Guardamos en la colección global de feed
        firestore.collection("feed")
            .document(feedItem.id)
            .set(feedItem)
            .await()
    }

}