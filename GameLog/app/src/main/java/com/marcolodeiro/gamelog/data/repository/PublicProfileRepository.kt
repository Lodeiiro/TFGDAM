package com.marcolodeiro.gamelog.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.marcolodeiro.gamelog.data.model.GameLibraryEntry
import com.marcolodeiro.gamelog.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublicProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Obtiene el perfil público de un usuario por su UID
    suspend fun getUserProfile(uid: String): User? {
        val doc = firestore.collection("users")
            .document(uid)
            .get()
            .await()
        return if (doc.exists()) doc.toObject(User::class.java) else null
    }

    // Obtiene la biblioteca pública de un usuario
    suspend fun getUserLibrary(uid: String): List<GameLibraryEntry> {
        return firestore.collection("users")
            .document(uid)
            .collection("library")
            .get()
            .await()
            .toObjects(GameLibraryEntry::class.java)
    }
}