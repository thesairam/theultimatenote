package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.KanbanBoard
import com.theultimatenote.app.data.model.KanbanColumn
import com.theultimatenote.app.data.model.Project
import com.theultimatenote.app.data.model.ProjectType
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class IosProjectRepository : ProjectRepository {

    private val db = Firebase.firestore
    private val projectsCol = db.collection("projects")
    private val boardsCol = db.collection("boards")

    override fun getProjects(userId: String): Flow<List<Project>> {
        return projectsCol
            .where { "ownerId" equalTo userId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.data<Project>() }.sortedBy { it.createdAt }
            }
    }

    override fun getProject(projectId: String): Flow<Project?> {
        return projectsCol.document(projectId)
            .snapshots
            .map { if (it.exists) it.data<Project>() else null }
    }

    override suspend fun createProject(project: Project): String {
        val docRef = projectsCol.document(randomFirestoreId())
        val projectWithId = project.copy(id = docRef.id)
        docRef.set(projectWithId)

        val defaultColumns = listOf(
            KanbanColumn(id = "planning", name = "Planning", order = 0),
            KanbanColumn(id = "in_progress", name = "In Progress", order = 1),
            KanbanColumn(id = "review", name = "Review", order = 2),
            KanbanColumn(id = "done", name = "Done", order = 3),
        )
        val board = KanbanBoard(id = docRef.id, projectId = docRef.id, columns = defaultColumns)
        boardsCol.document(docRef.id).set(board)

        return docRef.id
    }

    override suspend fun toggleStarProject(projectId: String, isStarred: Boolean) {
        projectsCol.document(projectId).update("isStarred" to isStarred)
    }

    override suspend fun completeProject(projectId: String) {
        projectsCol.document(projectId).update(
            "isCompleted" to true,
            "completedAt" to Clock.System.now().toEpochMilliseconds(),
        )
    }

    override suspend fun deleteProject(projectId: String) {
        val tasksSnapshot = projectsCol.document(projectId).collection("tasks").get()
        for (taskDoc in tasksSnapshot.documents) {
            taskDoc.reference.delete()
        }

        val notebooksSnapshot = db.collection("notebooks")
            .where { "projectId" equalTo projectId }
            .get()
        for (notebookDoc in notebooksSnapshot.documents) {
            val pagesSnapshot = notebookDoc.reference.collection("pages").get()
            for (pageDoc in pagesSnapshot.documents) {
                pageDoc.reference.delete()
            }
            notebookDoc.reference.delete()
        }

        projectsCol.document(projectId).delete()
        boardsCol.document(projectId).delete()
    }

    override suspend fun createDefaultProjects(userId: String) {
        val existing = projectsCol
            .where { all("ownerId" equalTo userId, "type" equalTo ProjectType.DAILY.name) }
            .get()

        if (!existing.documents.isEmpty()) {
            existing.documents.forEach { doc ->
                if (doc.get<String?>("name") == "Daily") {
                    doc.reference.update("name" to "Daily Habits & Tasks")
                }
            }
            val learningDocs = projectsCol
                .where { all("ownerId" equalTo userId, "type" equalTo ProjectType.LEARNING.name) }
                .get()
            learningDocs.documents.forEach { doc ->
                if (doc.get<String?>("name") == "Learning") {
                    doc.reference.update("name" to "Learning Project")
                }
            }
            return
        }

        val nowMillis = Clock.System.now().toEpochMilliseconds()

        val dailyRef = projectsCol.document(randomFirestoreId())
        val daily = Project(
            id = dailyRef.id,
            name = "Daily Habits & Tasks",
            type = ProjectType.DAILY,
            ownerId = userId,
            createdAt = nowMillis,
            isDeletable = false,
        )
        dailyRef.set(daily)

        val dailyBoard = KanbanBoard(
            id = dailyRef.id,
            projectId = dailyRef.id,
            columns = listOf(
                KanbanColumn(id = "recurring", name = "Recurring", order = 0),
                KanbanColumn(id = "temporary", name = "Temporary", order = 1),
            ),
        )
        boardsCol.document(dailyRef.id).set(dailyBoard)

        val learningRef = projectsCol.document(randomFirestoreId())
        val learning = Project(
            id = learningRef.id,
            name = "Learning Project",
            type = ProjectType.LEARNING,
            ownerId = userId,
            createdAt = nowMillis,
            isDeletable = false,
        )
        learningRef.set(learning)

        val learningBoard = KanbanBoard(
            id = learningRef.id,
            projectId = learningRef.id,
            columns = listOf(
                KanbanColumn(id = "path_1", name = "Learning Path 1", order = 0),
                KanbanColumn(id = "path_2", name = "Learning Path 2", order = 1),
                KanbanColumn(id = "completed", name = "Completed", order = 2),
            ),
        )
        boardsCol.document(learningRef.id).set(learningBoard)
    }

    override fun getBoard(projectId: String): Flow<KanbanBoard?> {
        return boardsCol.document(projectId)
            .snapshots
            .map { if (it.exists) it.data<KanbanBoard>() else null }
    }

    override suspend fun createBoard(board: KanbanBoard): String {
        boardsCol.document(board.projectId).set(board)
        return board.projectId
    }

    override suspend fun addColumn(projectId: String, column: KanbanColumn) {
        val doc = boardsCol.document(projectId).get()
        val board = doc.data<KanbanBoard>()
        val updated = board.copy(columns = board.columns + column)
        boardsCol.document(projectId).set(updated)
    }

    override suspend fun deleteColumn(projectId: String, columnId: String) {
        val doc = boardsCol.document(projectId).get()
        val board = doc.data<KanbanBoard>()
        val updated = board.copy(columns = board.columns.filterNot { it.id == columnId })
        boardsCol.document(projectId).set(updated)
    }
}
