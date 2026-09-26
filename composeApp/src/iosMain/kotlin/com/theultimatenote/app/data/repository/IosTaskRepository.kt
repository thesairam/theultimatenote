package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.Task
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class IosTaskRepository : TaskRepository {

    private val db = Firebase.firestore

    private fun tasksCol(projectId: String) = db.collection("projects").document(projectId).collection("tasks")

    override fun getTasksForProject(projectId: String): Flow<List<Task>> {
        return tasksCol(projectId)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.data<Task>() }.sortedBy { it.order }
            }
    }

    override fun getTasksForColumn(projectId: String, columnId: String): Flow<List<Task>> {
        return tasksCol(projectId)
            .where { "columnId" equalTo columnId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.data<Task>() }.sortedBy { it.order }
            }
    }

    override fun getTodayTasks(userId: String): Flow<List<Task>> {
        return db.collection("projects")
            .where { "ownerId" equalTo userId }
            .snapshots
            .map { projectsSnapshot ->
                projectsSnapshot.documents.flatMap { projectDoc ->
                    val tasksSnapshot = projectDoc.reference.collection("tasks")
                        .where { "isCompletedToday" equalTo false }
                        .get()
                    tasksSnapshot.documents.mapNotNull { it.data<Task>() }
                }.sortedBy { it.order }
            }
    }

    override suspend fun createTask(task: Task): String {
        val col = tasksCol(task.projectId)
        val docRef = col.document(randomFirestoreId())
        val taskWithId = task.copy(id = docRef.id)
        docRef.set(taskWithId)
        return docRef.id
    }

    override suspend fun updateTask(task: Task) {
        tasksCol(task.projectId).document(task.id).set(task)
    }

    override suspend fun deleteTask(taskId: String, projectId: String) {
        tasksCol(projectId).document(taskId).delete()
    }

    override suspend fun moveTask(taskId: String, projectId: String, newColumnId: String) {
        tasksCol(projectId).document(taskId).update("columnId" to newColumnId)
    }

    override suspend fun resetRecurringTasks(projectId: String) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val snapshot = tasksCol(projectId)
            .where { all("isRecurring" equalTo true, "isCompletedToday" equalTo true) }
            .get()

        for (doc in snapshot.documents) {
            val completedDate = doc.get<String?>("completedDate")
            if (completedDate != null && completedDate < today) {
                doc.reference.update(
                    "isCompletedToday" to false,
                    "completedDate" to null,
                )
            }
        }
    }
}
