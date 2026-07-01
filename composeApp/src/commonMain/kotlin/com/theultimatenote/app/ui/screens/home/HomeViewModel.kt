package com.theultimatenote.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.PomodoroSession
import com.theultimatenote.app.data.model.Project
import com.theultimatenote.app.data.model.ProjectType
import com.theultimatenote.app.data.model.Task
import com.theultimatenote.app.data.model.SubscriptionLimits
import com.theultimatenote.app.data.model.SubscriptionTier
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.NotificationScheduler
import com.theultimatenote.app.data.repository.PomodoroRepository
import com.theultimatenote.app.data.repository.ProjectRepository
import com.theultimatenote.app.data.repository.SubscriptionRepository
import com.theultimatenote.app.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class TaskWithProject(
    val task: Task,
    val projectName: String,
)

data class HomeUiState(
    val userName: String = "",
    val dailyTasks: List<Task> = emptyList(),
    val learningTasks: List<Task> = emptyList(),
    val projectTasks: List<TaskWithProject> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val notificationScheduler: NotificationScheduler,
    private val pomodoroRepository: PomodoroRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _limitReached = MutableStateFlow<String?>(null)

    fun dismissLimit() { _limitReached.value = null }

    private val allProjects = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) projectRepository.getProjects(user.uid) else flowOf(emptyList())
        }

    private val dailyProject = allProjects.map { it.find { p -> p.type == ProjectType.DAILY } }
    private val learningProject = allProjects.map { it.find { p -> p.type == ProjectType.LEARNING } }
    private val regularProjects = allProjects.map { it.filter { p -> p.type == ProjectType.REGULAR } }

    private val dailyTasks = dailyProject.flatMapLatest { project ->
        if (project != null) taskRepository.getTasksForProject(project.id) else flowOf(emptyList())
    }

    private val learningTasks = learningProject.flatMapLatest { project ->
        if (project != null) taskRepository.getTasksForProject(project.id) else flowOf(emptyList())
    }

    private val regularProjectTasks = regularProjects.flatMapLatest { projects ->
        if (projects.isEmpty()) flowOf(emptyList())
        else combine(projects.map { project ->
            taskRepository.getTasksForProject(project.id).map { tasks ->
                tasks.map { TaskWithProject(it, project.name) }
            }
        }) { arrays -> arrays.flatMap { it.toList() } }
    }

    val projects: StateFlow<List<Project>> = allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val limitReached: StateFlow<String?> = _limitReached

    val uiState: StateFlow<HomeUiState> = combine(
        authRepository.currentUser,
        dailyTasks,
        learningTasks,
        regularProjectTasks,
    ) { user, daily, learning, projectTasks ->
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val todayRelevantTasks = daily.filter { it.isRecurring || !it.isCompletedToday || it.completedDate == today } +
            learning.filter { it.isRecurring || !it.isCompletedToday || it.completedDate == today } +
            projectTasks.map { it.task }.filter { !it.isCompletedToday || it.completedDate == today }
        HomeUiState(
            userName = user?.displayName ?: "there",
            dailyTasks = daily.filter { !it.isCompletedToday },
            learningTasks = learning.filter { !it.isCompletedToday },
            projectTasks = projectTasks.filter { !it.task.isCompletedToday },
            completedCount = todayRelevantTasks.count { it.isCompletedToday },
            totalCount = todayRelevantTasks.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        resetRecurringTasksIfNeeded()
    }

    private fun resetRecurringTasksIfNeeded() {
        viewModelScope.launch {
            val projects = allProjects.first()
            projects.filter { it.type == ProjectType.DAILY || it.type == ProjectType.LEARNING }.forEach { project ->
                taskRepository.resetRecurringTasks(project.id)
            }
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
                val parts = task.scheduledTime.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toIntOrNull()
                    val minute = parts[1].toIntOrNull()
                    if (hour != null && minute != null) {
                        notificationScheduler.scheduleTaskReminder(task.id, task.title, hour, minute)
                    }
                }
            } else {
                notificationScheduler.cancelTaskReminder(task.id)
            }
        }
    }

    fun savePomodoroSession(task: Task, durationMinutes: Int) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
            pomodoroRepository.saveSession(
                userId = user.uid,
                session = PomodoroSession(
                    taskId = task.id,
                    projectId = task.projectId,
                    taskTitle = task.title,
                    startTime = Clock.System.now().toEpochMilliseconds(),
                    durationMinutes = durationMinutes,
                    completed = true,
                    date = today,
                ),
            )
        }
    }

    fun quickAddTask(
        title: String,
        projectId: String,
        isRecurring: Boolean = false,
        scheduledTime: String? = null,
        isUrgent: Boolean = false,
        isImportant: Boolean = true,
    ) {
        if (title.isBlank() || projectId.isBlank()) return
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            val sub = subscriptionRepository.getSubscription(user.uid).first()
            if (sub.subscriptionTier == SubscriptionTier.FREE) {
                val state = uiState.value
                val allTasksCount = state.dailyTasks.size + state.learningTasks.size + state.projectTasks.size
                if (allTasksCount >= SubscriptionLimits.FREE_MAX_ACTIVE_TASKS) {
                    _limitReached.value = "You've reached the free limit of ${SubscriptionLimits.FREE_MAX_ACTIVE_TASKS} active tasks. Upgrade to Pro for unlimited tasks."
                    return@launch
                }
            }
            val project = allProjects.first().find { it.id == projectId }
            val columnId = when (project?.type) {
                ProjectType.DAILY -> if (isRecurring) "recurring" else "temporary"
                ProjectType.LEARNING -> {
                    val board = projectRepository.getBoard(projectId).first()
                    board?.columns?.firstOrNull { it.id != "completed" }?.id ?: "path_1"
                }
                else -> {
                    val board = projectRepository.getBoard(projectId).first()
                    board?.columns?.minByOrNull { it.order }?.id ?: "planning"
                }
            }
            val taskId = taskRepository.createTask(
                Task(
                    title = title.trim(),
                    projectId = projectId,
                    columnId = columnId,
                    isRecurring = isRecurring,
                    scheduledTime = scheduledTime,
                    isUrgent = isUrgent,
                    isImportant = isImportant,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                )
            )
            if (isRecurring && scheduledTime != null) {
                val parts = scheduledTime.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toIntOrNull()
                    val minute = parts[1].toIntOrNull()
                    if (hour != null && minute != null) {
                        notificationScheduler.scheduleTaskReminder(taskId, title.trim(), hour, minute)
                    }
                }
            }
        }
    }
}
