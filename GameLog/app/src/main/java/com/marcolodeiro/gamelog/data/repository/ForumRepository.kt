package com.marcolodeiro.gamelog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.marcolodeiro.gamelog.data.model.ForumReply
import com.marcolodeiro.gamelog.data.model.ForumThread
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForumRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val threadsCollection = firestore.collection("forum_threads")

    // Obtiene todos los hilos ordenados por fecha
    suspend fun getThreads(): List<ForumThread> {
        return threadsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
            .toObjects(ForumThread::class.java)
    }

    // Crea un nuevo hilo
    suspend fun createThread(title: String, content: String, tag: String) {
        val user = auth.currentUser ?: return
        val thread = ForumThread(
            id        = UUID.randomUUID().toString(),
            title     = title,
            content   = content,
            userId    = user.uid,
            userName  = user.displayName ?: "Gamer",
            userPhoto = user.photoUrl?.toString() ?: "",
            tag       = tag
        )
        threadsCollection.document(thread.id).set(thread).await()
    }

    // Obtiene las respuestas de un hilo
    suspend fun getReplies(threadId: String): List<ForumReply> {
        return threadsCollection
            .document(threadId)
            .collection("replies")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()
            .toObjects(ForumReply::class.java)
    }

    // Añade una respuesta a un hilo
    suspend fun addReply(threadId: String, content: String) {
        val user = auth.currentUser ?: return
        val reply = ForumReply(
            id        = UUID.randomUUID().toString(),
            threadId  = threadId,
            userId    = user.uid,
            userName  = user.displayName ?: "Gamer",
            userPhoto = user.photoUrl?.toString() ?: "",
            content   = content
        )
        threadsCollection
            .document(threadId)
            .collection("replies")
            .document(reply.id)
            .set(reply)
            .await()

        // Actualizamos el contador de respuestas
        threadsCollection
            .document(threadId)
            .update("replyCount", FieldValue.increment(1))
            .await()
    }
}