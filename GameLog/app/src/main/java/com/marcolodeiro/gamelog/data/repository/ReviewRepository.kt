package com.marcolodeiro.gamelog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.marcolodeiro.gamelog.data.model.Review
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Colección global de reseñas
    private val reviewsCollection = firestore.collection("reviews")

    // Publica una reseña
    suspend fun publishReview(gameId: Int, gameName: String, rating: Float, text: String) {
        val user = auth.currentUser ?: return
        val review = Review(
            id        = "${user.uid}_${gameId}",  // Un usuario solo puede reseñar un juego una vez
            gameId    = gameId,
            gameName  = gameName,
            userId    = user.uid,
            userName  = user.displayName ?: "Gamer",
            userPhoto = user.photoUrl?.toString() ?: "",
            rating    = rating,
            text      = text
        )
        reviewsCollection
            .document(review.id)
            .set(review)
            .await()
    }

    // Obtiene las reseñas de un juego concreto
    suspend fun getReviewsForGame(gameId: Int): List<Review> {
        return reviewsCollection
            .whereEqualTo("gameId", gameId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Review::class.java)
    }

    // Obtiene la reseña del usuario actual para un juego
    suspend fun getUserReview(gameId: Int): Review? {
        val uid = auth.currentUser?.uid ?: return null
        val doc = reviewsCollection
            .document("${uid}_${gameId}")
            .get()
            .await()
        return if (doc.exists()) doc.toObject(Review::class.java) else null
    }

    // Elimina la reseña del usuario actual
    suspend fun deleteReview(gameId: Int) {
        val uid = auth.currentUser?.uid ?: return
        reviewsCollection
            .document("${uid}_${gameId}")
            .delete()
            .await()
    }

    // Obtiene las reseñas de una lista de usuarios
    suspend fun getReviewsFromUsers(userIds: List<String>): List<Review> {
        if (userIds.isEmpty()) return emptyList()
        return reviewsCollection
            .whereIn("userId", userIds)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
            .toObjects(Review::class.java)
    }
}