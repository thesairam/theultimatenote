package com.theultimatenote.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE,
}

@Serializable
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val projectId: String = "",
    val columnId: String = "",
    val isRecurring: Boolean = false,
    val isCompletedToday: Boolean = false,
    val completedDate: String? = null,
    val scheduledTime: String? = null,
    val dueDate: Long? = null,
    val createdAt: Long = 0L,
    val order: Int = 0,
    val isUrgent: Boolean = false,
    val isImportant: Boolean = false,
    val calendarEventId: String? = null,
    val calendarProvider: String? = null,
)
