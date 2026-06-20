package com.theultimatenote.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.theultimatenote.app.data.model.PomodoroSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebasePomodoroRepository : PomodoroRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun sessionsCol(userId: String) =
        db.collection("users").document(userId).collection("pomodoro_sessions")

    override fun getSessions(userId: String): Flow<List<PomodoroSession>> {
        return sessionsCol(userId)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toSession() }
            }
    }

    override fun getSessionsForDate(userId: String, date: String): Flow<List<PomodoroSession>> {
        return sessionsCol(userId)
            .whereEqualTo("date", date)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toSession() }
            }
    }

    override suspend fun saveSession(userId: String, session: PomodoroSession): String {
        val col = sessionsCol(userId)
        val docRef = col.document()
        val sessionWithId = session.copy(id = docRef.id)
        docRef.set(sessionWithId.toMap()).await()
        return docRef.id
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toSession(): PomodoroSession? {
        if (!exists()) return null
        return PomodoroSession(
            id = id,
            taskId = getString("taskId") ?: "",
            projectId = getString("projectId") ?: "",
            taskTitle = getString("taskTitle") ?: "",
            startTime = getLong("startTime") ?: 0L,
            durationMinutes = getLong("durationMinutes")?.toInt() ?: 25,
            completed = getBoolean("completed") ?: false,
            date = getString("date") ?: "",
        )
    }

    private fun PomodoroSession.toMap() = mapOf(
        "id" to id,
        "taskId" to taskId,
        "projectId" to projectId,
        "taskTitle" to taskTitle,
        "startTime" to startTime,
        "durationMinutes" to durationMinutes,
        "completed" to completed,
        "date" to date,
    )
}
