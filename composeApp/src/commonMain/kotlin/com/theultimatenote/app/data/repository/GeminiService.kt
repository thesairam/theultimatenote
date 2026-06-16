package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.ChatMessage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
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

class GeminiService(private val apiKey: String) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun chat(messages: List<ChatMessage>): String {
        if (apiKey.isBlank()) {
            return "Gemini API key not configured. Add your key in the app settings to enable AI chat."
        }

        val contents = messages.map { msg ->
            GeminiContent(
                role = if (msg.role == "user") "user" else "model",
                parts = listOf(GeminiPart(text = msg.content)),
            )
        }

        return try {
            val response = client.post(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
            ) {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(contents = contents))
            }

            val geminiResponse = response.body<GeminiResponse>()
            geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: geminiResponse.error?.message
                ?: "No response from Gemini."
        } catch (e: Exception) {
            "Error: ${e.message ?: "Failed to connect to Gemini API"}"
        }
    }
}
