package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.PomodoroSession
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IosPomodoroRepository : PomodoroRepository {

    private val db = Firebase.firestore

    private fun sessionsCol(userId: String) =
        db.collection("users").document(userId).collection("pomodoro_sessions")

    override fun getSessions(userId: String): Flow<List<PomodoroSession>> {
        return sessionsCol(userId)
            .orderBy("startTime", Direction.DESCENDING)
            .snapshots
            .map { snapshot -> snapshot.documents.mapNotNull { it.data<PomodoroSession>() } }
    }

    override fun getSessionsForDate(userId: String, date: String): Flow<List<PomodoroSession>> {
        return sessionsCol(userId)
            .where { "date" equalTo date }
            .snapshots
            .map { snapshot -> snapshot.documents.mapNotNull { it.data<PomodoroSession>() } }
    }

    override suspend fun saveSession(userId: String, session: PomodoroSession): String {
        val col = sessionsCol(userId)
        val docRef = col.document(randomFirestoreId())
        val sessionWithId = session.copy(id = docRef.id)
        docRef.set(sessionWithId)
        return docRef.id
    }

    override suspend fun clearAllSessions(userId: String) {
        val sessions = sessionsCol(userId).get()
        sessions.documents.forEach { it.reference.delete() }
    }
}
