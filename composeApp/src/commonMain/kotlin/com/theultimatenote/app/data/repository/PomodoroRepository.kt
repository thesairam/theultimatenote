package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.PomodoroSession
import kotlinx.coroutines.flow.Flow

interface PomodoroRepository {
    fun getSessions(userId: String): Flow<List<PomodoroSession>>
    fun getSessionsForDate(userId: String, date: String): Flow<List<PomodoroSession>>
    suspend fun saveSession(userId: String, session: PomodoroSession): String
}
