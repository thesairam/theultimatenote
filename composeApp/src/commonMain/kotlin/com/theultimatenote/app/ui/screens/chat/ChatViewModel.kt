package com.theultimatenote.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.ChatMessage
import com.theultimatenote.app.data.repository.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ChatViewModel(
    private val geminiService: GeminiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState(
        messages = listOf(
            ChatMessage(
                id = "welcome",
                role = "model",
                content = "Hi! I'm your AI assistant. I can help you brainstorm ideas, plan projects, or just chat. What's on your mind?",
                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            )
        )
    ))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            id = "user_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
            role = "user",
            content = text,
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            error = null,
        )

        viewModelScope.launch {
            val conversationHistory = _uiState.value.messages.filter { it.id != "welcome" }
            val reply = geminiService.chat(conversationHistory)

            val aiMessage = ChatMessage(
                id = "ai_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
                role = "model",
                content = reply,
                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            )

            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + aiMessage,
                isLoading = false,
            )
        }
    }
}
