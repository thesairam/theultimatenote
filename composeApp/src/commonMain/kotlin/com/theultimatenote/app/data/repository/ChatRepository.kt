package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(userId: String): Flow<List<ChatMessage>>
    suspend fun saveMessage(userId: String, message: ChatMessage)
    suspend fun markActionExecuted(userId: String, messageId: String, actionIndex: Int)
    suspend fun clearHistory(userId: String)
}
