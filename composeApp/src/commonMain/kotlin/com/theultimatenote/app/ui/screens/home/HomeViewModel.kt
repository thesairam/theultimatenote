package com.theultimatenote.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.ProjectType
import com.theultimatenote.app.data.model.Task
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.ProjectRepository
import com.theultimatenote.app.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val dailyTasks: List<Task> = emptyList(),
    val learningTasks: List<Task> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val allProjects = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) projectRepository.getProjects(user.uid) else flowOf(emptyList())
        }

    private val dailyProject = allProjects.map { it.find { p -> p.type == ProjectType.DAILY } }
    private val learningProject = allProjects.map { it.find { p -> p.type == ProjectType.LEARNING } }

    private val dailyTasks = dailyProject.flatMapLatest { project ->
        if (project != null) taskRepository.getTasksForProject(project.id) else flowOf(emptyList())
    }

    private val learningTasks = learningProject.flatMapLatest { project ->
        if (project != null) taskRepository.getTasksForProject(project.id) else flowOf(emptyList())
    }

    val uiState: StateFlow<HomeUiState> = combine(
        authRepository.currentUser,
        dailyTasks,
        learningTasks,
    ) { user, daily, learning ->
        val allTasks = daily + learning
        HomeUiState(
            userName = user?.displayName ?: "there",
            dailyTasks = daily.filter { !it.isCompletedToday },
            learningTasks = learning.filter { !it.isCompletedToday },
            completedCount = allTasks.count { it.isCompletedToday },
            totalCount = allTasks.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        resetRecurringTasksIfNeeded()
    }

    private fun resetRecurringTasksIfNeeded() {
        viewModelScope.launch {
            val projects = allProjects.first()
            projects.filter { it.type == ProjectType.DAILY }.forEach { project ->
                taskRepository.resetRecurringTasks(project.id)
            }
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task.copy(isCompletedToday = !task.isCompletedToday))
        }
    }
}
