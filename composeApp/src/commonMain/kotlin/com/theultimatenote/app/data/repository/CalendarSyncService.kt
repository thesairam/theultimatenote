package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.Task

interface CalendarSyncService {
    suspend fun isConnected(provider: String): Boolean
    suspend fun getAccessToken(provider: String): String?
    suspend fun createEvent(provider: String, accessToken: String, task: Task, projectName: String): String?
    suspend fun updateEvent(provider: String, accessToken: String, eventId: String, task: Task, projectName: String)
    suspend fun deleteEvent(provider: String, accessToken: String, eventId: String)
    suspend fun disconnect(provider: String)

    companion object {
        const val GOOGLE = "google"
        const val APPLE = "apple"
    }
}
