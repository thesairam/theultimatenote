package com.theultimatenote.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.theultimatenote.app.data.model.KanbanBoard
import com.theultimatenote.app.data.model.KanbanColumn
import com.theultimatenote.app.data.model.Project
import com.theultimatenote.app.data.model.ProjectType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseProjectRepository : ProjectRepository {

    private val db = FirebaseFirestore.getInstance()
    private val projectsCol = db.collection("projects")
    private val boardsCol = db.collection("boards")

    override fun getProjects(userId: String): Flow<List<Project>> {
        return projectsCol
            .whereEqualTo("ownerId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toProject()
                }.sortedBy { it.createdAt }
            }
    }

    override fun getProject(projectId: String): Flow<Project?> {
        return projectsCol.document(projectId)
            .snapshots()
            .map { it.toProject() }
    }

    override suspend fun createProject(project: Project): String {
        val docRef = projectsCol.document()
        val projectWithId = project.copy(id = docRef.id)
        docRef.set(projectWithId.toMap()).await()

        val defaultColumns = listOf(
            KanbanColumn(id = "planning", name = "Planning", order = 0),
            KanbanColumn(id = "in_progress", name = "In Progress", order = 1),
            KanbanColumn(id = "review", name = "Review", order = 2),
            KanbanColumn(id = "done", name = "Done", order = 3),
        )
        val board = KanbanBoard(id = docRef.id, projectId = docRef.id, columns = defaultColumns)
        boardsCol.document(docRef.id).set(board.toMap()).await()

        return docRef.id
    }

    override suspend fun deleteProject(projectId: String) {
        val tasksSnapshot = projectsCol.document(projectId).collection("tasks").get().await()
        for (taskDoc in tasksSnapshot.documents) {
            taskDoc.reference.delete().await()
        }

        val notebooksSnapshot = db.collection("notebooks")
            .whereEqualTo("projectId", projectId)
            .get().await()
        for (notebookDoc in notebooksSnapshot.documents) {
            val pagesSnapshot = notebookDoc.reference.collection("pages").get().await()
            for (pageDoc in pagesSnapshot.documents) {
                pageDoc.reference.delete().await()
            }
            notebookDoc.reference.delete().await()
        }

        projectsCol.document(projectId).delete().await()
        boardsCol.document(projectId).delete().await()
    }

    override suspend fun createDefaultProjects(userId: String) {
        val existing = projectsCol
            .whereEqualTo("ownerId", userId)
            .whereEqualTo("type", ProjectType.DAILY.name)
            .get().await()

        if (existing.isEmpty) {
            val dailyRef = projectsCol.document()
            val daily = Project(
                id = dailyRef.id,
                name = "Daily",
                type = ProjectType.DAILY,
                ownerId = userId,
                createdAt = System.currentTimeMillis(),
                isDeletable = false,
            )
            dailyRef.set(daily.toMap()).await()

            val dailyBoard = KanbanBoard(
                id = dailyRef.id,
                projectId = dailyRef.id,
                columns = listOf(
                    KanbanColumn(id = "recurring", name = "Recurring", order = 0),
                    KanbanColumn(id = "temporary", name = "Temporary", order = 1),
                ),
            )
            boardsCol.document(dailyRef.id).set(dailyBoard.toMap()).await()

            val learningRef = projectsCol.document()
            val learning = Project(
                id = learningRef.id,
                name = "Learning",
                type = ProjectType.LEARNING,
                ownerId = userId,
                createdAt = System.currentTimeMillis(),
                isDeletable = false,
            )
            learningRef.set(learning.toMap()).await()

            val learningBoard = KanbanBoard(
                id = learningRef.id,
                projectId = learningRef.id,
                columns = listOf(
                    KanbanColumn(id = "path_1", name = "Learning Path 1", order = 0),
                    KanbanColumn(id = "path_2", name = "Learning Path 2", order = 1),
                    KanbanColumn(id = "completed", name = "Completed", order = 2),
                ),
            )
            boardsCol.document(learningRef.id).set(learningBoard.toMap()).await()
        }
    }

    override fun getBoard(projectId: String): Flow<KanbanBoard?> {
        return boardsCol.document(projectId)
            .snapshots()
            .map { doc ->
                if (!doc.exists()) return@map null
                val columns = (doc.get("columns") as? List<Map<String, Any>>)?.map { col ->
                    KanbanColumn(
                        id = col["id"] as? String ?: "",
                        name = col["name"] as? String ?: "",
                        order = (col["order"] as? Long)?.toInt() ?: 0,
                        taskIds = (col["taskIds"] as? List<String>) ?: emptyList(),
                    )
                }?.sortedBy { it.order } ?: emptyList()

                KanbanBoard(
                    id = doc.id,
                    projectId = doc.getString("projectId") ?: "",
                    columns = columns,
                )
            }
    }

    override suspend fun createBoard(board: KanbanBoard): String {
        boardsCol.document(board.projectId).set(board.toMap()).await()
        return board.projectId
    }

    override suspend fun addColumn(projectId: String, column: KanbanColumn) {
        val doc = boardsCol.document(projectId).get().await()
        val columns = (doc.get("columns") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
        columns.add(column.toMap())
        boardsCol.document(projectId).update("columns", columns).await()
    }

    override suspend fun deleteColumn(projectId: String, columnId: String) {
        val doc = boardsCol.document(projectId).get().await()
        val columns = (doc.get("columns") as? List<Map<String, Any>>)?.toMutableList() ?: return
        columns.removeAll { it["id"] == columnId }
        boardsCol.document(projectId).update("columns", columns).await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toProject(): Project? {
        if (!exists()) return null
        return Project(
            id = id,
            name = getString("name") ?: "",
            type = try { ProjectType.valueOf(getString("type") ?: "REGULAR") } catch (_: Exception) { ProjectType.REGULAR },
            ownerId = getString("ownerId") ?: "",
            notebookId = getString("notebookId") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
            isDeletable = getBoolean("isDeletable") ?: true,
        )
    }

    private fun Project.toMap() = mapOf(
        "id" to id,
        "name" to name,
        "type" to type.name,
        "ownerId" to ownerId,
        "notebookId" to notebookId,
        "createdAt" to createdAt,
        "isDeletable" to isDeletable,
    )

    private fun KanbanBoard.toMap() = mapOf(
        "id" to id,
        "projectId" to projectId,
        "columns" to columns.map { it.toMap() },
    )

    private fun KanbanColumn.toMap() = mapOf(
        "id" to id,
        "name" to name,
        "order" to order,
        "taskIds" to taskIds,
    )
}
