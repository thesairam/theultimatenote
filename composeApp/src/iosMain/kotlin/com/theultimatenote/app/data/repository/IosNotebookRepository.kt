package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.Notebook
import com.theultimatenote.app.data.model.NotebookPage
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class IosNotebookRepository : NotebookRepository {

    private val db = Firebase.firestore
    private val notebooksCol = db.collection("notebooks")

    private fun pagesCol(notebookId: String) = notebooksCol.document(notebookId).collection("pages")

    override fun getNotebooks(ownerId: String): Flow<List<Notebook>> {
        return notebooksCol
            .where { "ownerId" equalTo ownerId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.data<Notebook>() }.sortedByDescending { it.createdAt }
            }
    }

    override fun getNotebookPages(notebookId: String): Flow<List<NotebookPage>> {
        return pagesCol(notebookId)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.data<NotebookPage>() }.sortedBy { it.order }
            }
    }

    override suspend fun createNotebook(notebook: Notebook): String {
        val docRef = notebooksCol.document(randomFirestoreId())
        val withId = notebook.copy(id = docRef.id)
        docRef.set(withId)
        return docRef.id
    }

    override suspend fun deleteNotebook(notebookId: String) {
        val pages = pagesCol(notebookId).get()
        pages.documents.forEach { it.reference.delete() }
        notebooksCol.document(notebookId).delete()
    }

    override suspend fun createPage(page: NotebookPage): String {
        val col = pagesCol(page.notebookId)
        val docRef = col.document(randomFirestoreId())
        val withId = page.copy(id = docRef.id)
        docRef.set(withId)
        return docRef.id
    }

    override suspend fun updatePage(page: NotebookPage) {
        pagesCol(page.notebookId).document(page.id)
            .set(page.copy(updatedAt = Clock.System.now().toEpochMilliseconds()))
    }

    override suspend fun deletePage(notebookId: String, pageId: String) {
        pagesCol(notebookId).document(pageId).delete()
    }

    override suspend fun createDefaultNotebookForProject(
        projectId: String,
        projectName: String,
        ownerId: String,
        sections: List<String>,
    ): String {
        val nowMillis = Clock.System.now().toEpochMilliseconds()
        val notebookId = createNotebook(
            Notebook(
                name = "$projectName Notes",
                projectId = projectId,
                ownerId = ownerId,
                createdAt = nowMillis,
            )
        )

        sections.forEachIndexed { index, section ->
            createPage(
                NotebookPage(
                    notebookId = notebookId,
                    title = "$section Notes",
                    content = "",
                    order = index,
                    createdAt = nowMillis,
                    updatedAt = nowMillis,
                )
            )
        }

        return notebookId
    }
}
