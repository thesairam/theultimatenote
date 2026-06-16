package com.theultimatenote.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.theultimatenote.app.data.model.ChatAction
import com.theultimatenote.app.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseChatRepository : ChatRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun messagesCol(userId: String) =
        db.collection("users").document(userId).collection("chat_messages")

    override fun getMessages(userId: String): Flow<List<ChatMessage>> {
        return messagesCol(userId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc -> doc.toChatMessage() }
            }
    }

    override suspend fun saveMessage(userId: String, message: ChatMessage) {
        val docRef = if (message.id.isBlank()) messagesCol(userId).document()
            else messagesCol(userId).document(message.id)
        val msgWithId = message.copy(id = docRef.id)
        docRef.set(msgWithId.toMap()).await()
    }

    override suspend fun markActionExecuted(userId: String, messageId: String, actionIndex: Int) {
        val doc = messagesCol(userId).document(messageId).get().await()
        val actions = (doc.get("actions") as? List<Map<String, Any>>)?.toMutableList() ?: return
        if (actionIndex < actions.size) {
            val updated = actions[actionIndex].toMutableMap()
            updated["executed"] = true
            actions[actionIndex] = updated
            messagesCol(userId).document(messageId).update("actions", actions).await()
        }
    }

    override suspend fun clearHistory(userId: String) {
        val snapshot = messagesCol(userId).get().await()
        val batch = db.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toChatMessage(): ChatMessage? {
        if (!exists()) return null
        val actions = (get("actions") as? List<Map<String, Any>>)?.map { a ->
            ChatAction(
                type = a["type"] as? String ?: "",
                projectName = a["projectName"] as? String ?: "",
                title = a["title"] as? String ?: "",
                columnName = a["columnName"] as? String ?: "",
                isRecurring = a["isRecurring"] as? Boolean ?: false,
                scheduledTime = a["scheduledTime"] as? String,
                executed = a["executed"] as? Boolean ?: false,
            )
        } ?: emptyList()

        return ChatMessage(
            id = id,
            role = getString("role") ?: "user",
            content = getString("content") ?: "",
            timestamp = getLong("timestamp") ?: 0L,
            actions = actions,
        )
    }

    private fun ChatMessage.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "role" to role,
        "content" to content,
        "timestamp" to timestamp,
        "actions" to actions.map { it.toMap() },
    )

    private fun ChatAction.toMap(): Map<String, Any?> = mapOf(
        "type" to type,
        "projectName" to projectName,
        "title" to title,
        "columnName" to columnName,
        "isRecurring" to isRecurring,
        "scheduledTime" to scheduledTime,
        "executed" to executed,
    )
}
