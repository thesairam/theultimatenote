package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.Notebook
import com.theultimatenote.app.data.model.NotebookPage
import kotlinx.coroutines.flow.Flow

interface NotebookRepository {
    fun getNotebooks(ownerId: String): Flow<List<Notebook>>
    fun getNotebookPages(notebookId: String): Flow<List<NotebookPage>>
    suspend fun createNotebook(notebook: Notebook): String
    suspend fun deleteNotebook(notebookId: String)
    suspend fun createPage(page: NotebookPage): String
    suspend fun updatePage(page: NotebookPage)
    suspend fun deletePage(notebookId: String, pageId: String)
    suspend fun createDefaultNotebookForProject(projectId: String, projectName: String, ownerId: String, sections: List<String>): String
}
