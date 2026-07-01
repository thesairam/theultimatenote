package com.theultimatenote.app.ui.screens.notebooks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.Notebook
import com.theultimatenote.app.data.model.NotebookPage
import com.theultimatenote.app.data.model.SubscriptionLimits
import com.theultimatenote.app.data.model.SubscriptionTier
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.ImageStorageRepository
import com.theultimatenote.app.data.repository.NotebookRepository
import com.theultimatenote.app.data.repository.SubscriptionRepository
import kotlinx.datetime.Clock
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotebooksUiState(
    val selectedNotebook: Notebook? = null,
    val selectedPage: NotebookPage? = null,
    val isEditing: Boolean = false,
    val error: String? = null,
    val limitReached: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class NotebooksViewModel(
    private val authRepository: AuthRepository,
    private val notebookRepository: NotebookRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val imageStorageRepository: ImageStorageRepository,
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

    private var pagesCollectionJob: Job? = null

    fun selectNotebook(notebook: Notebook) {
        _uiState.value = _uiState.value.copy(selectedNotebook = notebook, selectedPage = null, isEditing = false)
        pagesCollectionJob?.cancel()
        pagesCollectionJob = viewModelScope.launch {
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

    fun dismissLimit() {
        _uiState.value = _uiState.value.copy(limitReached = null)
    }

    fun createPage(title: String) {
        val notebook = _uiState.value.selectedNotebook ?: return
        viewModelScope.launch {
            try {
                val user = authRepository.currentUser.stateIn(viewModelScope).value ?: return@launch
                val sub = subscriptionRepository.getSubscription(user.uid).first()
                if (sub.subscriptionTier == SubscriptionTier.FREE) {
                    val currentPageCount = _pages.value.size
                    if (currentPageCount >= SubscriptionLimits.FREE_MAX_NOTEBOOK_PAGES) {
                        _uiState.value = _uiState.value.copy(
                            limitReached = "You've reached the free limit of ${SubscriptionLimits.FREE_MAX_NOTEBOOK_PAGES} pages per notebook. Upgrade to Pro for unlimited pages."
                        )
                        return@launch
                    }
                }
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

    fun savePage(title: String, content: String, imageUrls: List<String> = emptyList()) {
        val page = _uiState.value.selectedPage ?: return
        val updated = page.copy(title = title, content = content, imageUrls = imageUrls)
        viewModelScope.launch {
            try {
                notebookRepository.updatePage(updated)
                _uiState.value = _uiState.value.copy(selectedPage = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun uploadImage(imageBytes: ByteArray, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            val fileName = "notebook_${Clock.System.now().toEpochMilliseconds()}.jpg"
            val url = imageStorageRepository.uploadImage(user.uid, imageBytes, fileName)
            onResult(url)
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
