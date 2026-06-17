package com.theultimatenote.app.ui.screens.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.Project
import com.theultimatenote.app.data.model.ProjectType
import com.theultimatenote.app.data.model.Task
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.NotificationScheduler
import com.theultimatenote.app.data.repository.ProjectRepository
import com.theultimatenote.app.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalCoroutinesApi::class)
class DailyViewModel(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    private val allProjects = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) projectRepository.getProjects(user.uid) else flowOf(emptyList())
        }

    val dailyProject: StateFlow<Project?> = allProjects
        .map { projects -> projects.find { it.type == ProjectType.DAILY } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val learningProject: StateFlow<Project?> = allProjects
        .map { projects -> projects.find { it.type == ProjectType.LEARNING } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyTasks: StateFlow<List<Task>> = dailyProject
        .flatMapLatest { project ->
            if (project != null) taskRepository.getTasksForProject(project.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val learningTasks: StateFlow<List<Task>> = learningProject
        .flatMapLatest { project ->
            if (project != null) taskRepository.getTasksForProject(project.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDailyTask(title: String, isRecurring: Boolean, scheduledTime: String? = null) {
        val project = dailyProject.value ?: return
        if (title.isBlank()) return
        val columnId = if (isRecurring) "recurring" else "temporary"
        viewModelScope.launch {
            val taskId = taskRepository.createTask(
                Task(
                    title = title.trim(),
                    projectId = project.id,
                    columnId = columnId,
                    isRecurring = isRecurring,
                    scheduledTime = scheduledTime,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                )
            )
            if (isRecurring && scheduledTime != null) {
                scheduleNotification(taskId, title.trim(), scheduledTime)
            }
        }
    }

    fun addLearningTask(title: String, pathColumnId: String) {
        val project = learningProject.value ?: return
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
