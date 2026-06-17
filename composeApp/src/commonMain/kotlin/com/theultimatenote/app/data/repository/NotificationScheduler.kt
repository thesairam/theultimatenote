package com.theultimatenote.app.data.repository

interface NotificationScheduler {
    fun scheduleTaskReminder(taskId: String, taskTitle: String, hour: Int, minute: Int)
    fun cancelTaskReminder(taskId: String)
}
