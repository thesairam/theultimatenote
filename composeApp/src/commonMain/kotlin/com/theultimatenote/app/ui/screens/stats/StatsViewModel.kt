package com.theultimatenote.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.PomodoroSession
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
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

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

    private val allProjects = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) projectRepository.getProjects(user.uid) else flowOf(emptyList())
        }

    private val allTasksByProject = allProjects.flatMapLatest { projects ->
        if (projects.isEmpty()) flowOf(emptyList<Pair<ProjectType, List<Task>>>())
        else combine(projects.map { project ->
            taskRepository.getTasksForProject(project.id).map { tasks ->
                project.type to tasks
            }
        }) { it.toList() }
    }

    private val todaysSessions = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) pomodoroRepository.getSessionsForDate(user.uid, today)
            else flowOf(emptyList())
        }.catch { emit(emptyList()) }

    private val allSessions = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) pomodoroRepository.getSessions(user.uid)
            else flowOf(emptyList())
        }.catch { emit(emptyList()) }

    val uiState: StateFlow<StatsUiState> = combine(
        allTasksByProject,
        todaysSessions,
        allSessions,
    ) { tasksByProject, todaySessions, allPomSessions ->
        val allTasks = tasksByProject.flatMap { it.second }
        val dailyTasks = tasksByProject.filter { it.first == ProjectType.DAILY }.flatMap { it.second }
        val learningTasks = tasksByProject.filter { it.first == ProjectType.LEARNING }.flatMap { it.second }
        val projectTasks = tasksByProject.filter { it.first == ProjectType.REGULAR }.flatMap { it.second }

        val completedSessions = todaySessions.filter { it.completed }
        val allCompletedSessions = allPomSessions.filter { it.completed }

        val todayStr = today
        val todayRelevant = allTasks.filter { it.isRecurring || !it.isCompletedToday || it.completedDate == todayStr }
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
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())
}
