package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.ChatMessage
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IosChatRepository : ChatRepository {

    private val db = Firebase.firestore

    private fun messagesCol(userId: String) =
        db.collection("users").document(userId).collection("chat_messages")

    override fun getMessages(userId: String): Flow<List<ChatMessage>> {
        return messagesCol(userId)
            .orderBy("timestamp", Direction.ASCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.data<ChatMessage>() }
            }
    }

    override suspend fun saveMessage(userId: String, message: ChatMessage) {
        val docRef = if (message.id.isBlank()) messagesCol(userId).document(randomFirestoreId())
        else messagesCol(userId).document(message.id)
        val msgWithId = message.copy(id = docRef.id)
        docRef.set(msgWithId)
    }

    override suspend fun markActionExecuted(userId: String, messageId: String, actionIndex: Int) {
        val docRef = messagesCol(userId).document(messageId)
        val message = docRef.get().data<ChatMessage>()
        if (actionIndex < message.actions.size) {
            val updatedActions = message.actions.toMutableList()
            updatedActions[actionIndex] = updatedActions[actionIndex].copy(executed = true)
            docRef.set(message.copy(actions = updatedActions))
        }
    }

    override suspend fun clearHistory(userId: String) {
        val snapshot = messagesCol(userId).get()
        snapshot.documents.forEach { it.reference.delete() }
    }
}
