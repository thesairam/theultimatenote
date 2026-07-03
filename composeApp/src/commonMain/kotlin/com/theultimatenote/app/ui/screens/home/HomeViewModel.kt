package com.theultimatenote.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.ChecklistItem
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

data class ProjectSummary(
    val project: Project,
    val totalTasks: Int,
    val completedTasks: Int,
    val activeTasks: Int,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val projectSummaries: List<ProjectSummary> = emptyList(),
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

    private val allProjectsWithTasks = allProjects.flatMapLatest { projects ->
        if (projects.isEmpty()) flowOf(emptyList())
        else combine(projects.map { project ->
            taskRepository.getTasksForProject(project.id).map { tasks ->
                project to tasks
            }
        }) { it.toList() }
    }

    val projects: StateFlow<List<Project>> = allProjects
        .map { it.filter { p -> !p.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val limitReached: StateFlow<String?> = _limitReached

    val uiState: StateFlow<HomeUiState> = combine(
        authRepository.currentUser,
        allProjectsWithTasks,
    ) { user, projectsWithTasks ->
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

        val summaries = projectsWithTasks
            .filter { (project, _) -> !project.isCompleted }
            .map { (project, tasks) ->
                val todayRelevant = tasks.filter {
                    it.isRecurring || !it.isCompletedToday || it.completedDate == today
                }
                ProjectSummary(
                    project = project,
                    totalTasks = todayRelevant.size,
                    completedTasks = todayRelevant.count { it.isCompletedToday },
                    activeTasks = todayRelevant.count { !it.isCompletedToday },
                )
            }

        val totalCompleted = summaries.sumOf { it.completedTasks }
        val totalAll = summaries.sumOf { it.totalTasks }

        HomeUiState(
            isLoading = false,
            userName = user?.displayName ?: "there",
            projectSummaries = summaries,
            completedCount = totalCompleted,
            totalCount = totalAll,
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

    private fun scheduleNotification(taskId: String, title: String, time: String) {
        val parts = time.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toIntOrNull() ?: return
            val minute = parts[1].toIntOrNull() ?: return
            notificationScheduler.scheduleTaskReminder(taskId, title, hour, minute)
        }
    }

    fun quickAddTask(
        title: String,
        projectId: String,
        isRecurring: Boolean = false,
        scheduledTime: String? = null,
        isUrgent: Boolean = false,
        isImportant: Boolean = true,
        description: String = "",
        checklist: List<ChecklistItem> = emptyList(),
    ) {
        if (title.isBlank() || projectId.isBlank()) return
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            val sub = subscriptionRepository.getSubscription(user.uid).first()
            if (sub.subscriptionTier == SubscriptionTier.FREE) {
                val totalActive = uiState.value.projectSummaries.sumOf { it.activeTasks }
                if (totalActive >= SubscriptionLimits.FREE_MAX_ACTIVE_TASKS) {
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
                    description = description.trim(),
                    projectId = projectId,
                    columnId = columnId,
                    isRecurring = isRecurring,
                    scheduledTime = scheduledTime,
                    isUrgent = isUrgent,
                    isImportant = isImportant,
                    checklist = checklist.filter { it.text.isNotBlank() },
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                )
            )
            if (isRecurring && scheduledTime != null) {
                scheduleNotification(taskId, title.trim(), scheduledTime)
            }
        }
    }
}
