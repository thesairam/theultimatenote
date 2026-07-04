package com.theultimatenote.app.ui.screens.stats

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.PomodoroSession
import com.theultimatenote.app.data.model.Project
import com.theultimatenote.app.data.model.ProjectType
import com.theultimatenote.app.data.model.Task
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.PomodoroRepository
import com.theultimatenote.app.data.repository.ProjectRepository
import com.theultimatenote.app.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
data class ProjectStats(
    val projectName: String,
    val projectType: ProjectType,
    val totalTasks: Int,
    val completedTasks: Int,
    val isCompleted: Boolean,
)

@Immutable
data class StatsUiState(
    val totalTasksToday: Int = 0,
    val completedToday: Int = 0,
    val totalTasksAllTime: Int = 0,
    val pomodoroSessionsToday: Int = 0,
    val pomodoroMinutesToday: Int = 0,
    val totalPomodoroSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
    val dailyTasksCompleted: Int = 0,
    val learningTasksCompleted: Int = 0,
    val projectTasksCompleted: Int = 0,
    val activeProjectCount: Int = 0,
    val completedProjectCount: Int = 0,
    val projectStats: List<ProjectStats> = emptyList(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val pomodoroRepository: PomodoroRepository,
) : ViewModel() {

    private val today: String
        get() = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

    private val cachedUser = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val allProjects = cachedUser
        .flatMapLatest { user ->
            if (user != null) projectRepository.getProjects(user.uid) else flowOf(emptyList())
        }

    private val allTasksByProject = allProjects.flatMapLatest { projects ->
        if (projects.isEmpty()) flowOf(emptyList<Triple<Project, ProjectType, List<Task>>>())
        else combine(projects.map { project ->
            taskRepository.getTasksForProject(project.id).map { tasks ->
                Triple(project, project.type, tasks)
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

    val uiState: StateFlow<StatsUiState> = combine(
        allTasksByProject,
        pomodoroData,
    ) { tasksByProject, (todaySessions, allPomSessions) ->
        val allTasks = tasksByProject.flatMap { it.third }
        val dailyTasks = tasksByProject.filter { it.second == ProjectType.DAILY }.flatMap { it.third }
        val learningTasks = tasksByProject.filter { it.second == ProjectType.LEARNING }.flatMap { it.third }
        val projectTasks = tasksByProject.filter { it.second == ProjectType.REGULAR }.flatMap { it.third }

        val completedSessions = todaySessions.filter { it.completed }
        val allCompletedSessions = allPomSessions.filter { it.completed }

        val todayStr = today
        val todayRelevant = allTasks.filter { it.isRecurring || !it.isCompletedToday || it.completedDate == todayStr }

        val perProjectStats = tasksByProject.map { (project, _, tasks) ->
            ProjectStats(
                projectName = project.name,
                projectType = project.type,
                totalTasks = tasks.size,
                completedTasks = tasks.count { it.isCompletedToday },
                isCompleted = project.isCompleted,
            )
        }

        StatsUiState(
            totalTasksToday = todayRelevant.size,
            completedToday = todayRelevant.count { it.isCompletedToday },
            totalTasksAllTime = allTasks.size,
            pomodoroSessionsToday = completedSessions.size,
            pomodoroMinutesToday = completedSessions.sumOf { it.durationMinutes },
            totalPomodoroSessions = allCompletedSessions.size,
            totalFocusMinutes = allCompletedSessions.sumOf { it.durationMinutes },
            dailyTasksCompleted = dailyTasks.count { it.isCompletedToday },
            learningTasksCompleted = learningTasks.count { it.isCompletedToday },
            projectTasksCompleted = projectTasks.count { it.isCompletedToday },
            activeProjectCount = tasksByProject.count { !it.first.isCompleted },
            completedProjectCount = tasksByProject.count { it.first.isCompleted },
            projectStats = perProjectStats,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    fun resetStats() {
        viewModelScope.launch {
            val userId = cachedUser.value?.uid ?: return@launch
            pomodoroRepository.clearAllSessions(userId)
        }
    }
}
