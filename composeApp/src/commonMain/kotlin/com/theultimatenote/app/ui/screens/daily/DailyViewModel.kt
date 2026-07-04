package com.theultimatenote.app.ui.screens.daily

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.KanbanBoard
import com.theultimatenote.app.data.model.Project
import com.theultimatenote.app.data.model.ProjectType
import com.theultimatenote.app.data.model.Task
import com.theultimatenote.app.data.model.SubscriptionLimits
import com.theultimatenote.app.data.model.SubscriptionTier
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.NotificationScheduler
import com.theultimatenote.app.data.repository.ProjectRepository
import com.theultimatenote.app.data.repository.SubscriptionRepository
import com.theultimatenote.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Immutable
data class DailyUiState(
    val isLoading: Boolean = true,
    val dailyProject: Project? = null,
    val learningProject: Project? = null,
    val dailyBoard: KanbanBoard? = null,
    val dailyTasks: List<Task> = emptyList(),
    val learningTasks: List<Task> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class DailyViewModel(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val notificationScheduler: NotificationScheduler,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _limitReached = MutableStateFlow<String?>(null)
    val limitReached = _limitReached.asStateFlow()
    fun dismissLimit() { _limitReached.value = null }

    private val cachedUser = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val allProjects = cachedUser
        .flatMapLatest { user ->
            if (user != null) projectRepository.getProjects(user.uid) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val specialProjects = allProjects
        .flatMapLatest { projects ->
            val daily = projects.find { it.type == ProjectType.DAILY }
            val learning = projects.find { it.type == ProjectType.LEARNING }
            if (daily == null && learning == null) flowOf(DailyUiState(isLoading = false))
            else {
                val flows = listOfNotNull(
                    daily?.let { d ->
                        combine(
                            projectRepository.getBoard(d.id),
                            taskRepository.getTasksForProject(d.id),
                        ) { board, tasks -> Triple(board, tasks, null as List<Task>?) }
                    },
                    learning?.let { l ->
                        taskRepository.getTasksForProject(l.id)
                            .flatMapLatest { tasks ->
                                flowOf(Triple(null as KanbanBoard?, null as List<Task>?, tasks))
                            }
                    },
                )
                if (flows.isEmpty()) flowOf(DailyUiState(isLoading = false))
                else combine(flows) { results ->
                    var board: KanbanBoard? = null
                    var dailyTasks: List<Task> = emptyList()
                    var learningTasks: List<Task> = emptyList()
                    for (r in results) {
                        if (r.first != null) board = r.first
                        if (r.second != null) dailyTasks = r.second!!
                        if (r.third != null) learningTasks = r.third!!
                    }
                    DailyUiState(
                        isLoading = false,
                        dailyProject = daily,
                        learningProject = learning,
                        dailyBoard = board,
                        dailyTasks = dailyTasks,
                        learningTasks = learningTasks,
                    )
                }
            }
        }

    val uiState: StateFlow<DailyUiState> = specialProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyUiState())

    fun addDailyTask(
        title: String,
        isRecurring: Boolean,
        scheduledTime: String? = null,
        isUrgent: Boolean? = null,
        isImportant: Boolean? = null,
    ) {
        val project = uiState.value.dailyProject ?: return
        if (title.isBlank()) return
        val columnId = if (isRecurring) "recurring" else "temporary"
        viewModelScope.launch {
            val user = cachedUser.value ?: return@launch
            val sub = subscriptionRepository.getSubscription(user.uid).first()
            if (sub.subscriptionTier == SubscriptionTier.FREE) {
                val state = uiState.value
                val totalActive = (state.dailyTasks + state.learningTasks).count { !it.isCompletedToday }
                if (totalActive >= SubscriptionLimits.FREE_MAX_ACTIVE_TASKS) {
                    _limitReached.value = "You've reached the free limit of ${SubscriptionLimits.FREE_MAX_ACTIVE_TASKS} active tasks. Upgrade to Pro for unlimited tasks."
                    return@launch
                }
            }
            val taskId = taskRepository.createTask(
                Task(
                    title = title.trim(),
                    projectId = project.id,
                    columnId = columnId,
                    isRecurring = isRecurring,
                    scheduledTime = scheduledTime,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                    isImportant = isImportant ?: isRecurring,
                    isUrgent = isUrgent ?: !isRecurring,
                )
            )
            if (isRecurring && scheduledTime != null) {
                scheduleNotification(taskId, title.trim(), scheduledTime)
            }
        }
    }

    fun addLearningTask(title: String, pathColumnId: String) {
        val project = uiState.value.learningProject ?: return
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepository.createTask(
                Task(
                    title = title.trim(),
                    projectId = project.id,
                    columnId = pathColumnId,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                )
            )
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
            val newCompleted = !task.isCompletedToday
            taskRepository.updateTask(
                task.copy(
                    isCompletedToday = newCompleted,
                    completedDate = if (newCompleted) today else null,
                )
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
            if (task.isRecurring && task.scheduledTime != null) {
                scheduleNotification(task.id, task.title, task.scheduledTime)
            } else {
                notificationScheduler.cancelTaskReminder(task.id)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            notificationScheduler.cancelTaskReminder(task.id)
            taskRepository.deleteTask(task.id, task.projectId)
        }
    }

    private fun scheduleNotification(taskId: String, title: String, time: String) {
        val parts = time.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toIntOrNull() ?: return
            val minute = parts[1].toIntOrNull() ?: return
            notificationScheduler.scheduleTaskReminder(taskId, title, hour, minute)
        }
    }
}
