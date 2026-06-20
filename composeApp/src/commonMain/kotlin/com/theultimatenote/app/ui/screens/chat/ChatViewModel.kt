package com.theultimatenote.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.ChatAction
import com.theultimatenote.app.data.model.ChatMessage
import com.theultimatenote.app.data.model.ProjectType
import com.theultimatenote.app.data.model.Task
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.ChatRepository
import com.theultimatenote.app.data.repository.AiService
import com.theultimatenote.app.data.repository.ProjectRepository
import com.theultimatenote.app.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isHistoryLoaded: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val aiService: AiService,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var userId: String? = null

    init {
        loadChatHistory()
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            userId = user?.uid
            if (userId == null) return@launch

            chatRepository.getMessages(userId!!).collect { messages ->
                val displayMessages = if (messages.isEmpty()) {
                    listOf(welcomeMessage())
                } else {
                    messages
                }
                _uiState.value = _uiState.value.copy(
                    messages = displayMessages,
                    isHistoryLoaded = true,
                )
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || userId == null) return

        val userMessage = ChatMessage(
            role = "user",
            content = text,
            timestamp = Clock.System.now().toEpochMilliseconds(),
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            error = null,
        )

        viewModelScope.launch {
            try {
                chatRepository.saveMessage(userId!!, userMessage)

                val systemContext = buildSystemContext()
                val conversationHistory = _uiState.value.messages
                    .filter { it.id != "welcome" }

                val rawReply = aiService.chat(conversationHistory, systemContext)

                val (cleanContent, actions) = parseActions(rawReply)

                val aiMessage = ChatMessage(
                    role = "model",
                    content = cleanContent,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    actions = actions,
                )

                chatRepository.saveMessage(userId!!, aiMessage)

                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to get response. Please try again.",
                )
            }
        }
    }

    fun executeAction(messageId: String, actionIndex: Int, action: ChatAction) {
        if (userId == null) return
        viewModelScope.launch {
            when (action.type) {
                "create_task" -> executeCreateTask(action)
                "create_project" -> executeCreateProject(action)
            }
            chatRepository.markActionExecuted(userId!!, messageId, actionIndex)
        }
    }

    fun clearHistory() {
        if (userId == null) return
        viewModelScope.launch {
            chatRepository.clearHistory(userId!!)
        }
    }

    private suspend fun executeCreateTask(action: ChatAction) {
        val user = authRepository.currentUser.first() ?: return
        val projects = projectRepository.getProjects(user.uid).first()
        val project = projects.find {
            it.name.equals(action.projectName, ignoreCase = true)
        } ?: return

        val columnId = when (project.type) {
            ProjectType.DAILY -> if (action.isRecurring) "recurring" else "temporary"
            ProjectType.LEARNING -> {
                val board = projectRepository.getBoard(project.id).first()
                val matched = if (action.columnName.isNotBlank()) {
                    board?.columns?.find { it.name.equals(action.columnName, ignoreCase = true) }?.id
                } else null
                matched
                    ?: board?.columns?.firstOrNull { it.id != "completed" }?.id
                    ?: "path_1"
            }
            ProjectType.REGULAR -> {
                val board = projectRepository.getBoard(project.id).first()
                val matched = if (action.columnName.isNotBlank()) {
                    board?.columns?.find { it.name.equals(action.columnName, ignoreCase = true) }?.id
                } else null
                matched
                    ?: board?.columns?.minByOrNull { it.order }?.id
                    ?: "planning"
            }
        }

        taskRepository.createTask(
            Task(
                title = action.title,
                projectId = project.id,
                columnId = columnId,
                isRecurring = action.isRecurring,
                scheduledTime = action.scheduledTime,
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    private suspend fun executeCreateProject(action: ChatAction) {
        val user = authRepository.currentUser.first() ?: return
        projectRepository.createProject(
            com.theultimatenote.app.data.model.Project(
                name = action.projectName,
                type = ProjectType.REGULAR,
                ownerId = user.uid,
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    private suspend fun buildSystemContext(): String {
        val user = authRepository.currentUser.first() ?: return ""
        val projects = projectRepository.getProjects(user.uid).first()

        val sb = StringBuilder()
        sb.appendLine("You are the AI assistant for \"The Ultimate Note\" — a productivity app combining Kanban boards, journaling, daily planning, and learning tracking.")
        sb.appendLine()
        sb.appendLine("The user's current projects and tasks:")
        sb.appendLine()

        for (project in projects) {
            val board = projectRepository.getBoard(project.id).first()
            val tasks = taskRepository.getTasksForProject(project.id).first()
            val columns = board?.columns?.sortedBy { it.order } ?: emptyList()

            sb.appendLine("- ${project.name} (${project.type.name})")
            sb.appendLine("  Columns: ${columns.joinToString(", ") { it.name }}")
            if (tasks.isNotEmpty()) {
                sb.appendLine("  Tasks:")
                for (task in tasks.take(10)) {
                    val col = columns.find { it.id == task.columnId }?.name ?: "?"
                    val status = if (task.isCompletedToday) " [done today]" else ""
                    val recurring = if (task.isRecurring) " (recurring)" else ""
                    sb.appendLine("    - ${task.title} [$col]$status$recurring")
                }
                if (tasks.size > 10) sb.appendLine("    ... and ${tasks.size - 10} more")
            }
        }

        sb.appendLine()
        sb.appendLine("ACTIONS: When the conversation naturally leads to creating a task or project, suggest it to the user. If they agree or you think it's helpful, include an action block in your response using this exact format:")
        sb.appendLine()
        sb.appendLine("<<ACTION>>{\"type\":\"create_task\",\"projectName\":\"PROJECT_NAME\",\"title\":\"TASK_TITLE\",\"columnName\":\"COLUMN_NAME\",\"isRecurring\":false}<<END_ACTION>>")
        sb.appendLine("<<ACTION>>{\"type\":\"create_project\",\"projectName\":\"NEW_PROJECT_NAME\"}<<END_ACTION>>")
        sb.appendLine()
        sb.appendLine("Rules for actions:")
        sb.appendLine("- Use exact project names from the list above")
        sb.appendLine("- For Daily tasks, set isRecurring true/false and optionally scheduledTime as \"HH:mm\"")
        sb.appendLine("- For new projects, use type create_project")
        sb.appendLine("- Always ask or confirm before including an action — don't create things without the user's intent")
        sb.appendLine("- You can include multiple actions in one response")
        sb.appendLine("- Place actions at the end of your message, after the conversational text")
        sb.appendLine()
        sb.appendLine("Be helpful, concise, and proactive about suggesting organization. If the user discusses ideas, suggest tasks or projects that would help them stay organized.")

        return sb.toString()
    }

    private fun parseActions(rawText: String): Pair<String, List<ChatAction>> {
        val actions = mutableListOf<ChatAction>()
        var cleanText = rawText

        val actionRegex = Regex("<<ACTION>>(.*?)<<END_ACTION>>", RegexOption.DOT_MATCHES_ALL)
        val matches = actionRegex.findAll(rawText)

        for (match in matches) {
            cleanText = cleanText.replace(match.value, "")
            val jsonStr = match.groupValues[1].trim()
            try {
                val action = parseActionJson(jsonStr)
                if (action != null) actions.add(action)
            } catch (_: Exception) {
                // Skip malformed actions
            }
        }

        return cleanText.trim() to actions
    }

    private fun parseActionJson(json: String): ChatAction? {
        fun extractString(key: String): String {
            val regex = Regex("\"$key\"\\s*:\\s*\"([^\"]*?)\"")
            return regex.find(json)?.groupValues?.get(1) ?: ""
        }
        fun extractBool(key: String): Boolean {
            val regex = Regex("\"$key\"\\s*:\\s*(true|false)")
            return regex.find(json)?.groupValues?.get(1) == "true"
        }

        val type = extractString("type")
        if (type.isBlank()) return null

        return ChatAction(
            type = type,
            projectName = extractString("projectName"),
            title = extractString("title"),
            columnName = extractString("columnName"),
            isRecurring = extractBool("isRecurring"),
            scheduledTime = extractString("scheduledTime").ifBlank { null },
        )
    }

    private fun welcomeMessage() = ChatMessage(
        id = "welcome",
        role = "model",
        content = "Hi! I'm your AI assistant. I can help you brainstorm ideas, plan projects, create tasks, and organize your work. What's on your mind?",
        timestamp = Clock.System.now().toEpochMilliseconds(),
    )
}
