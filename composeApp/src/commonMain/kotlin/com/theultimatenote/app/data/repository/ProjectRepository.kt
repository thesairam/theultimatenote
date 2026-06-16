package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.KanbanBoard
import com.theultimatenote.app.data.model.KanbanColumn
import com.theultimatenote.app.data.model.Project
import com.theultimatenote.app.data.model.ProjectType
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getProjects(userId: String): Flow<List<Project>>
    fun getProject(projectId: String): Flow<Project?>
    suspend fun createProject(project: Project): String
    suspend fun deleteProject(projectId: String)
    suspend fun createDefaultProjects(userId: String)

    fun getBoard(projectId: String): Flow<KanbanBoard?>
    suspend fun createBoard(board: KanbanBoard): String
    suspend fun addColumn(projectId: String, column: KanbanColumn)
    suspend fun deleteColumn(projectId: String, columnId: String)
}
