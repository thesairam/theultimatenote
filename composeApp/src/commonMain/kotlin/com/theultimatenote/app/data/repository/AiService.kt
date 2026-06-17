package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.ChatMessage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// --- Groq (OpenAI-compatible) models ---

@Serializable
data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.7,
    @SerialName("max_tokens")
    val maxTokens: Int = 1024,
)

@Serializable
data class GroqMessage(
    val role: String,
    val content: String,
)

@Serializable
data class GroqResponse(
    val choices: List<GroqChoice>? = null,
    val error: GroqError? = null,
)

@Serializable
data class GroqChoice(
    val message: GroqMessage? = null,
)

@Serializable
data class GroqError(
    val message: String = "",
    val type: String = "",
)

// --- Gemini models ---

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerialName("system_instruction")
    val systemInstruction: GeminiSystemInstruction? = null,
)

@Serializable
data class GeminiSystemInstruction(
    val parts: List<GeminiPart>,
)

@Serializable
data class GeminiContent(
    val role: String,
    val parts: List<GeminiPart>,
)

@Serializable
data class GeminiPart(
    val text: String,
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null,
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
)

@Serializable
data class GeminiError(
    val message: String = "",
    val code: Int = 0,
)

// --- Multi-provider AI Service ---

class AiService(
    private val groqApiKey: String,
    private val geminiApiKeys: List<String>,
) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val groqModels = listOf(
        "llama-3.3-70b-versatile",
        "llama-3.1-8b-instant",
        "gemma2-9b-it",
    )

    private val geminiModels = listOf(
        "gemini-2.0-flash",
        "gemini-2.0-flash-lite",
        "gemini-flash-latest",
    )

    suspend fun chat(messages: List<ChatMessage>, systemContext: String? = null): String {
        // Try Groq first (primary)
        if (groqApiKey.isNotBlank()) {
            for (model in groqModels) {
                val result = tryGroq(model, messages, systemContext)
                if (result != null) return result
            }
        }

        // Fall back to Gemini (try each key × each model)
        val validGeminiKeys = geminiApiKeys.filter { it.isNotBlank() }
        for (key in validGeminiKeys) {
            for (model in geminiModels) {
                val result = tryGemini(model, messages, systemContext, key)
                if (result != null) return result
            }
        }

        if (groqApiKey.isBlank() && validGeminiKeys.isEmpty()) {
            return "No AI API keys configured. Add a Groq or Gemini key to enable chat."
        }
        return "All AI providers are currently unavailable. Please try again in a moment."
    }

    private suspend fun tryGroq(
        model: String,
        messages: List<ChatMessage>,
        systemContext: String?,
    ): String? {
        return try {
            val groqMessages = mutableListOf<GroqMessage>()
            if (systemContext != null) {
                groqMessages.add(GroqMessage(role = "system", content = systemContext))
            }
            messages.forEach { msg ->
                groqMessages.add(GroqMessage(
                    role = if (msg.role == "user") "user" else "assistant",
                    content = msg.content,
                ))
            }

            val response = client.post("https://api.groq.com/openai/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $groqApiKey")
                setBody(GroqRequest(model = model, messages = groqMessages))
            }

            val groqResponse = response.body<GroqResponse>()
            if (groqResponse.error != null) null
            else groqResponse.choices?.firstOrNull()?.message?.content
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun tryGemini(
        model: String,
        messages: List<ChatMessage>,
        systemContext: String?,
        apiKey: String,
    ): String? {
        return try {
            val contents = messages.map { msg ->
                GeminiContent(
                    role = if (msg.role == "user") "user" else "model",
                    parts = listOf(GeminiPart(text = msg.content)),
                )
            }
            val systemInstruction = systemContext?.let {
                GeminiSystemInstruction(parts = listOf(GeminiPart(text = it)))
            }

            val response = client.post(
                "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            ) {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(contents = contents, systemInstruction = systemInstruction))
            }

            val geminiResponse = response.body<GeminiResponse>()
            if (geminiResponse.error != null) null
            else geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (_: Exception) {
            null
        }
    }
}
