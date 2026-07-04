package com.theultimatenote.app.ui.screens.home

import androidx.compose.runtime.Immutable
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
import kotlinx.coroutines.flow.catch
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

@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val starredProjects: List<Project> = emptyList(),
    val completedToday: Int = 0,
    val totalToday: Int = 0,
    val activeProjectCount: Int = 0,
    val completedProjectCount: Int = 0,
    val pomodoroSessionsToday: Int = 0,
    val pomodoroMinutesToday: Int = 0,
    val dailyTasksCompleted: Int = 0,
    val learningTasksCompleted: Int = 0,
    val projectTasksCompleted: Int = 0,
    val totalFocusMinutes: Int = 0,
    val totalPomodoroSessions: Int = 0,
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

    private val today: String
        get() = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

    private val cachedUser = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val allProjects = cachedUser
        .flatMapLatest { user ->
            if (user != null) projectRepository.getProjects(user.uid) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allProjectsWithTasks = allProjects.flatMapLatest { projects ->
        if (projects.isEmpty()) flowOf(emptyList())
        else combine(projects.map { project ->
            taskRepository.getTasksForProject(project.id).map { tasks ->
                project to tasks
            }
        }) { it.toList() }
    }

    private val pomodoroData = cachedUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList<PomodoroSession>() to emptyList<PomodoroSession>())
            else combine(
                pomodoroRepository.getSessionsForDate(user.uid, today).catch { emit(emptyList()) },
                pomodoroRepository.getSessions(user.uid).catch { emit(emptyList()) },
            ) { todaySessions, allSessions -> todaySessions to allSessions }
        }

    val projects: StateFlow<List<Project>> = allProjects
        .map { it.filter { p -> !p.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val limitReached: StateFlow<String?> = _limitReached

    val uiState: StateFlow<HomeUiState> = combine(
        cachedUser,
        allProjectsWithTasks,
        pomodoroData,
    ) { user, projectsWithTasks, (todaySessions, allPomSessions) ->
        val todayStr = today

        val activeProjects = projectsWithTasks.filter { !it.first.isCompleted }
        val starred = activeProjects
            .filter { it.first.isStarred }
            .map { it.first }

        val allTasks = activeProjects.flatMap { it.second }
        val todayRelevant = allTasks.filter {
            it.isRecurring || !it.isCompletedToday || it.completedDate == todayStr
        }

        val dailyTasks = activeProjects.filter { it.first.type == ProjectType.DAILY }.flatMap { it.second }
        val learningTasks = activeProjects.filter { it.first.type == ProjectType.LEARNING }.flatMap { it.second }
        val projectTasks = activeProjects.filter { it.first.type == ProjectType.REGULAR }.flatMap { it.second }

        val completedSessions = todaySessions.filter { it.completed }
        val allCompletedSessions = allPomSessions.filter { it.completed }

        HomeUiState(
            isLoading = false,
            userName = user?.displayName ?: "there",
            starredProjects = starred,
            completedToday = todayRelevant.count { it.isCompletedToday },
            totalToday = todayRelevant.size,
            activeProjectCount = activeProjects.size,
            completedProjectCount = projectsWithTasks.count { it.first.isCompleted },
            pomodoroSessionsToday = completedSessions.size,
            pomodoroMinutesToday = completedSessions.sumOf { it.durationMinutes },
            dailyTasksCompleted = dailyTasks.count { it.isCompletedToday },
            learningTasksCompleted = learningTasks.count { it.isCompletedToday },
            projectTasksCompleted = projectTasks.count { it.isCompletedToday },
            totalFocusMinutes = allCompletedSessions.sumOf { it.durationMinutes },
            totalPomodoroSessions = allCompletedSessions.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        resetRecurringTasksIfNeeded()
    }

    private fun resetRecurringTasksIfNeeded() {
        viewModelScope.launch {
            val projects = allProjects.value.ifEmpty { allProjects.first { it.isNotEmpty() } }
            projects.filter { it.type == ProjectType.DAILY || it.type == ProjectType.LEARNING }.forEach { project ->
                taskRepository.resetRecurringTasks(project.id)
            }
        }
    }

    fun savePomodoroSession(task: Task, durationMinutes: Int) {
        viewModelScope.launch {
            val user = cachedUser.value ?: return@launch
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
            val user = cachedUser.value ?: return@launch
            val sub = subscriptionRepository.getSubscription(user.uid).first()
            if (sub.subscriptionTier == SubscriptionTier.FREE) {
                val totalActive = uiState.value.totalToday - uiState.value.completedToday
                if (totalActive >= SubscriptionLimits.FREE_MAX_ACTIVE_TASKS) {
                    _limitReached.value = "You've reached the free limit of ${SubscriptionLimits.FREE_MAX_ACTIVE_TASKS} active tasks. Upgrade to Pro for unlimited tasks."
                    return@launch
                }
            }
            val project = allProjects.value.find { it.id == projectId }
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
