package com.theultimatenote.app.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.KanbanBoard
import com.theultimatenote.app.data.model.Task
import com.theultimatenote.app.data.model.TaskStatus
import com.theultimatenote.app.data.repository.ProjectRepository
import com.theultimatenote.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class KanbanViewModel(
    private val projectId: String,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    val board: StateFlow<KanbanBoard?> = projectRepository.getBoard(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val tasks: StateFlow<List<Task>> = taskRepository.getTasksForProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAddingTask = MutableStateFlow(false)
    val isAddingTask: StateFlow<Boolean> = _isAddingTask.asStateFlow()

    fun addTask(title: String, columnId: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            _isAddingTask.value = true
            taskRepository.createTask(
                Task(
                    title = title.trim(),
                    projectId = projectId,
                    columnId = columnId,
                    status = TaskStatus.TODO,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                )
            )
            _isAddingTask.value = false
        }
    }

    fun moveTask(taskId: String, newColumnId: String) {
        viewModelScope.launch {
            taskRepository.moveTask(taskId, projectId, newColumnId)
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task.copy(isCompletedToday = !task.isCompletedToday))
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId, projectId)
        }
    }
}
