package com.theultimatenote.app.ui.screens.notebooks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.Notebook
import com.theultimatenote.app.data.model.NotebookPage
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.NotebookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotebooksUiState(
    val selectedNotebook: Notebook? = null,
    val selectedPage: NotebookPage? = null,
    val isEditing: Boolean = false,
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class NotebooksViewModel(
    private val authRepository: AuthRepository,
    private val notebookRepository: NotebookRepository,
) : ViewModel() {

    val notebooks: StateFlow<List<Notebook>> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) notebookRepository.getNotebooks(user.uid) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(NotebooksUiState())
    val uiState: StateFlow<NotebooksUiState> = _uiState.asStateFlow()

    private val _pages = MutableStateFlow<List<NotebookPage>>(emptyList())
    val pages: StateFlow<List<NotebookPage>> = _pages.asStateFlow()

    fun selectNotebook(notebook: Notebook) {
        _uiState.value = _uiState.value.copy(selectedNotebook = notebook, selectedPage = null, isEditing = false)
        viewModelScope.launch {
            notebookRepository.getNotebookPages(notebook.id).collect {
                _pages.value = it
            }
        }
    }

    fun selectPage(page: NotebookPage) {
        _uiState.value = _uiState.value.copy(selectedPage = page, isEditing = true)
    }

    fun goBackToPages() {
        _uiState.value = _uiState.value.copy(selectedPage = null, isEditing = false)
    }

    fun goBackToNotebooks() {
        _uiState.value = _uiState.value.copy(selectedNotebook = null, selectedPage = null, isEditing = false)
        _pages.value = emptyList()
    }

    fun createNotebook(name: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.stateIn(viewModelScope).value ?: return@launch
            try {
                notebookRepository.createNotebook(
                    Notebook(
                        name = name,
                        ownerId = user.uid,
                        createdAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteNotebook(notebookId: String) {
        viewModelScope.launch {
            try {
                notebookRepository.deleteNotebook(notebookId)
                if (_uiState.value.selectedNotebook?.id == notebookId) {
                    goBackToNotebooks()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun createPage(title: String) {
        val notebook = _uiState.value.selectedNotebook ?: return
        viewModelScope.launch {
            try {
                val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                notebookRepository.createPage(
                    NotebookPage(
                        notebookId = notebook.id,
                        title = title,
                        content = "",
                        order = _pages.value.size,
                        createdAt = now,
                        updatedAt = now,
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun savePage(title: String, content: String) {
        val page = _uiState.value.selectedPage ?: return
        val updated = page.copy(title = title, content = content)
        viewModelScope.launch {
            try {
                notebookRepository.updatePage(updated)
                _uiState.value = _uiState.value.copy(selectedPage = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deletePage(page: NotebookPage) {
        viewModelScope.launch {
            try {
                notebookRepository.deletePage(page.notebookId, page.id)
                if (_uiState.value.selectedPage?.id == page.id) {
                    goBackToPages()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
