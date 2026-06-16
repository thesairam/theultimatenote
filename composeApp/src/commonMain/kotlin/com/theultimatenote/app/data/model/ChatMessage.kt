package com.theultimatenote.app.data.model

data class ChatAction(
    val type: String = "",
    val projectName: String = "",
    val title: String = "",
    val columnName: String = "",
    val isRecurring: Boolean = false,
    val scheduledTime: String? = null,
    val executed: Boolean = false,
)

data class ChatMessage(
    val id: String = "",
    val role: String = "user",
    val content: String = "",
    val timestamp: Long = 0L,
    val actions: List<ChatAction> = emptyList(),
)
