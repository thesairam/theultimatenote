package com.theultimatenote.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.theultimatenote.app.data.model.Notebook
import com.theultimatenote.app.data.model.NotebookPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseNotebookRepository : NotebookRepository {

    private val db = FirebaseFirestore.getInstance()
    private val notebooksCol = db.collection("notebooks")

    private fun pagesCol(notebookId: String) = notebooksCol.document(notebookId).collection("pages")

    override fun getNotebooks(ownerId: String): Flow<List<Notebook>> {
        return notebooksCol
            .whereEqualTo("ownerId", ownerId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toNotebook() }.sortedByDescending { it.createdAt }
            }
    }

    override fun getNotebookPages(notebookId: String): Flow<List<NotebookPage>> {
        return pagesCol(notebookId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toPage() }.sortedBy { it.order }
            }
    }

    override suspend fun createNotebook(notebook: Notebook): String {
        val docRef = notebooksCol.document()
        val withId = notebook.copy(id = docRef.id)
        docRef.set(withId.toMap()).await()
        return docRef.id
    }

    override suspend fun deleteNotebook(notebookId: String) {
        val pages = pagesCol(notebookId).get().await()
        val batch = db.batch()
        pages.documents.forEach { batch.delete(it.reference) }
        batch.delete(notebooksCol.document(notebookId))
        batch.commit().await()
    }

    override suspend fun createPage(page: NotebookPage): String {
        val col = pagesCol(page.notebookId)
        val docRef = col.document()
        val withId = page.copy(id = docRef.id)
        docRef.set(withId.toMap()).await()
        return docRef.id
    }

    override suspend fun updatePage(page: NotebookPage) {
        pagesCol(page.notebookId).document(page.id)
            .set(page.copy(updatedAt = System.currentTimeMillis()).toMap())
            .await()
    }

    override suspend fun deletePage(notebookId: String, pageId: String) {
        pagesCol(notebookId).document(pageId).delete().await()
    }

    override suspend fun createDefaultNotebookForProject(
        projectId: String,
        projectName: String,
        ownerId: String,
        sections: List<String>,
    ): String {
        val notebookId = createNotebook(
            Notebook(
                name = "$projectName Notes",
                projectId = projectId,
                ownerId = ownerId,
                createdAt = System.currentTimeMillis(),
            )
        )

        sections.forEachIndexed { index, section ->
            createPage(
                NotebookPage(
                    notebookId = notebookId,
                    title = "$section Notes",
                    content = "",
                    order = index,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }

        return notebookId
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toNotebook(): Notebook? {
        if (!exists()) return null
        return Notebook(
            id = id,
            name = getString("name") ?: "",
            projectId = getString("projectId"),
            ownerId = getString("ownerId") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPage(): NotebookPage? {
        if (!exists()) return null
        return NotebookPage(
            id = id,
            notebookId = getString("notebookId") ?: "",
            title = getString("title") ?: "",
            content = getString("content") ?: "",
            order = getLong("order")?.toInt() ?: 0,
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L,
        )
    }

    private fun Notebook.toMap() = mapOf(
        "id" to id,
        "name" to name,
        "projectId" to projectId,
        "ownerId" to ownerId,
        "createdAt" to createdAt,
    )

    private fun NotebookPage.toMap() = mapOf(
        "id" to id,
        "notebookId" to notebookId,
        "title" to title,
        "content" to content,
        "order" to order,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
    )
}
