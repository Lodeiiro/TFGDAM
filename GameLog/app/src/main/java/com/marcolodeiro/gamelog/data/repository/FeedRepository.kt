package com.marcolodeiro.gamelog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.marcolodeiro.gamelog.data.model.FeedItem
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val socialRepository: SocialRepository
) {
    // Obtiene el feed leyendo la biblioteca de cada usuario que seguimos
    suspend fun getFeed(): List<FeedItem> {
        val currentUid = auth.currentUser?.uid ?: return emptyList()

        // Obtenemos los usuarios que seguimos
        val following = socialRepository.getFollowing()
        val allUids = following.map { it.uid } + currentUid

        if (allUids.isEmpty()) return emptyList()

        val feedItems = mutableListOf<FeedItem>()

        // Para cada usuario que seguimos leemos su biblioteca
        allUids.forEach { uid ->
            try {
                // Buscamos el perfil del usuario para obtener su nombre y foto
                val userDoc = firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()

                val userName = userDoc.getString("displayName") ?: "Gamer"
                val userPhoto = userDoc.getString("photoUrl") ?: ""

                // Leemos los últimos 10 juegos añadidos por ese usuario
                val library = firestore.collection("users")
                    .document(uid)
                    .collection("library")
                    .orderBy("addedAt", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .await()

                library.documents.forEach { doc ->
                    feedItems.add(
                        FeedItem(
                            userId    = uid,
                            userName  = userName,
                            userPhoto = userPhoto,
                            gameId    = doc.getLong("gameId")?.toInt() ?: 0,
                            gameName  = doc.getString("gameName") ?: "",
                            gameCover = doc.getString("gameCover") ?: "",
                            status    = doc.getString("status") ?: "",
                            timestamp = doc.getLong("addedAt") ?: 0L
                        )
                    )
                }
            } catch (e: Exception) {
                // Si falla un usuario continuamos con el siguiente
            }
        }

        // Ordenamos todo por fecha descendente
        return feedItems.sortedByDescending { it.timestamp }
    }
}