package com.theultimatenote.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PomodoroSession(
    val id: String = "",
    val taskId: String = "",
    val projectId: String = "",
    val taskTitle: String = "",
    val startTime: Long = 0L,
    val durationMinutes: Int = 25,
    val completed: Boolean = false,
    val date: String = "",
)
