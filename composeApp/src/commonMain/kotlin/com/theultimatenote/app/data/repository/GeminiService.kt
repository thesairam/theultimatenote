package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.ChatMessage

@Deprecated("Use AiService instead — supports Groq + Gemini with automatic fallback")
class GeminiService(private val apiKey: String) {
    suspend fun chat(messages: List<ChatMessage>, systemContext: String? = null): String {
        val aiService = AiService(groqApiKey = "", geminiApiKeys = listOf(apiKey))
        return aiService.chat(messages, systemContext)
    }
}
