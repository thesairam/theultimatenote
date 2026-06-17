package com.theultimatenote.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.theultimatenote.app.data.model.Task
import com.theultimatenote.app.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class FirebaseTaskRepository : TaskRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun tasksCol(projectId: String) = db.collection("projects").document(projectId).collection("tasks")

    override fun getTasksForProject(projectId: String): Flow<List<Task>> {
        return tasksCol(projectId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toTask() }.sortedBy { it.order }
            }
    }

    override fun getTasksForColumn(projectId: String, columnId: String): Flow<List<Task>> {
        return tasksCol(projectId)
            .whereEqualTo("columnId", columnId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toTask() }.sortedBy { it.order }
            }
    }

    override fun getTodayTasks(userId: String): Flow<List<Task>> {
        return db.collectionGroup("tasks")
            .whereEqualTo("isCompletedToday", false)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toTask() }.sortedBy { it.order }
            }
    }

    override suspend fun createTask(task: Task): String {
        val col = tasksCol(task.projectId)
        val docRef = col.document()
        val taskWithId = task.copy(id = docRef.id)
        docRef.set(taskWithId.toMap()).await()
        return docRef.id
    }

    override suspend fun updateTask(task: Task) {
        tasksCol(task.projectId).document(task.id).set(task.toMap()).await()
    }

    override suspend fun deleteTask(taskId: String, projectId: String) {
        tasksCol(projectId).document(taskId).delete().await()
    }

    override suspend fun moveTask(taskId: String, projectId: String, newColumnId: String) {
        tasksCol(projectId).document(taskId).update("columnId", newColumnId).await()
    }

    override suspend fun resetRecurringTasks(projectId: String) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val snapshot = tasksCol(projectId)
            .whereEqualTo("isRecurring", true)
            .whereEqualTo("isCompletedToday", true)
            .get().await()

        val batch = db.batch()
        snapshot.documents.forEach { doc ->
            val completedDate = doc.getString("completedDate")
            if (completedDate != null && completedDate < today) {
                batch.update(doc.reference, mapOf(
                    "isCompletedToday" to false,
                    "completedDate" to null,
                ))
            }
        }
        batch.commit().await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toTask(): Task? {
        if (!exists()) return null
        return Task(
            id = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            status = try { TaskStatus.valueOf(getString("status") ?: "TODO") } catch (_: Exception) { TaskStatus.TODO },
            projectId = getString("projectId") ?: "",
            columnId = getString("columnId") ?: "",
            isRecurring = getBoolean("isRecurring") ?: false,
            isCompletedToday = getBoolean("isCompletedToday") ?: false,
            completedDate = getString("completedDate"),
            scheduledTime = getString("scheduledTime"),
            dueDate = getLong("dueDate"),
            createdAt = getLong("createdAt") ?: 0L,
            order = getLong("order")?.toInt() ?: 0,
            isUrgent = getBoolean("isUrgent") ?: false,
            isImportant = getBoolean("isImportant") ?: false,
        )
    }

    private fun Task.toMap() = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "status" to status.name,
        "projectId" to projectId,
        "columnId" to columnId,
        "isRecurring" to isRecurring,
        "isCompletedToday" to isCompletedToday,
        "completedDate" to completedDate,
        "scheduledTime" to scheduledTime,
        "dueDate" to dueDate,
        "createdAt" to createdAt,
        "order" to order,
        "isUrgent" to isUrgent,
        "isImportant" to isImportant,
    )
}
