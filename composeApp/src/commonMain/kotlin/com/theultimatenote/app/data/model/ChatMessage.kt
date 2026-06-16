package com.theultimatenote.app.data.model

data class ChatMessage(
    val id: String = "",
    val role: String = "user",
    val content: String = "",
    val timestamp: Long = 0L,
)
