package com.marcolodeiro.gamelog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.marcolodeiro.gamelog.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val currentUid get() = auth.currentUser?.uid ?: ""

    // Busca usuarios por nombre en Firestore
    suspend fun searchUsers(query: String): List<User> {
        return firestore.collection("users")
            .get()
            .await()
            .toObjects(User::class.java)
            .filter { user ->
                user.uid != currentUid && // Excluimos al usuario actual
                        user.displayName.contains(query, ignoreCase = true)
            }
    }

    // Sigue a un usuario
    suspend fun followUser(targetUid: String) {
        // Añadimos al seguido en nuestra lista de following
        firestore.collection("users")
            .document(currentUid)
            .collection("following")
            .document(targetUid)
            .set(mapOf("uid" to targetUid, "since" to System.currentTimeMillis()))
            .await()

        // Añadimos al seguidor en la lista de followers del otro usuario
        firestore.collection("users")
            .document(targetUid)
            .collection("followers")
            .document(currentUid)
            .set(mapOf("uid" to currentUid, "since" to System.currentTimeMillis()))
            .await()
    }

    // Deja de seguir a un usuario
    suspend fun unfollowUser(targetUid: String) {
        firestore.collection("users")
            .document(currentUid)
            .collection("following")
            .document(targetUid)
            .delete()
            .await()

        firestore.collection("users")
            .document(targetUid)
            .collection("followers")
            .document(currentUid)
            .delete()
            .await()
    }

    // Comprueba si seguimos a un usuario
    suspend fun isFollowing(targetUid: String): Boolean {
        return firestore.collection("users")
            .document(currentUid)
            .collection("following")
            .document(targetUid)
            .get()
            .await()
            .exists()
    }

    // Obtiene la lista de usuarios que seguimos
    suspend fun getFollowing(): List<User> {
        val followingIds = firestore.collection("users")
            .document(currentUid)
            .collection("following")
            .get()
            .await()
            .documents
            .map { it.id }

        if (followingIds.isEmpty()) return emptyList()

        return firestore.collection("users")
            .whereIn("uid", followingIds)
            .get()
            .await()
            .toObjects(User::class.java)
    }

    // Obtiene la biblioteca pública de otro usuario
    suspend fun getUserLibrary(targetUid: String): List<com.marcolodeiro.gamelog.data.model.GameLibraryEntry> {
        return firestore.collection("users")
            .document(targetUid)
            .collection("library")
            .get()
            .await()
            .toObjects(com.marcolodeiro.gamelog.data.model.GameLibraryEntry::class.java)
    }
}