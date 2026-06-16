package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksForProject(projectId: String): Flow<List<Task>>
    fun getTasksForColumn(projectId: String, columnId: String): Flow<List<Task>>
    fun getTodayTasks(userId: String): Flow<List<Task>>
    suspend fun createTask(task: Task): String
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: String, projectId: String)
    suspend fun moveTask(taskId: String, projectId: String, newColumnId: String)
    suspend fun resetRecurringTasks(projectId: String)
}
