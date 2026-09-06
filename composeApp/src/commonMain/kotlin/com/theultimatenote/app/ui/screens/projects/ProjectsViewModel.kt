package com.theultimatenote.app.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.Project
import com.theultimatenote.app.data.model.ProjectType
import com.theultimatenote.app.data.model.SubscriptionLimits
import com.theultimatenote.app.data.model.SubscriptionTier
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.NotebookRepository
import com.theultimatenote.app.data.repository.ProjectRepository
import com.theultimatenote.app.data.repository.SubscriptionRepository
import com.theultimatenote.app.data.security.InputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectsViewModel(
    private val projectRepository: ProjectRepository,
    private val authRepository: AuthRepository,
    private val notebookRepository: NotebookRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _limitReached = MutableStateFlow<String?>(null)
    val limitReached: StateFlow<String?> = _limitReached.asStateFlow()

    fun dismissLimit() { _limitReached.value = null }

    private val cachedUser = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val projects: StateFlow<List<Project>> = cachedUser
        .flatMapLatest { user ->
            if (user != null) projectRepository.getProjects(user.uid) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        ensureDefaultProjects()
    }

    fun createProject(name: String) {
        val nameCheck = InputValidator.validateProjectName(name)
        if (!nameCheck.isValid) return
        viewModelScope.launch {
            val user = cachedUser.value ?: return@launch
            val sub = subscriptionRepository.getSubscription(user.uid).first()
            if (sub.subscriptionTier == SubscriptionTier.FREE) {
                val regularCount = projects.value.count { it.type == ProjectType.REGULAR }
                if (regularCount >= SubscriptionLimits.FREE_MAX_PROJECTS) {
                    _limitReached.value = "You've reached the free limit of ${SubscriptionLimits.FREE_MAX_PROJECTS} projects. Upgrade to Pro for unlimited projects."
                    return@launch
                }
            }
            _isCreating.value = true
            val projectId = projectRepository.createProject(
                Project(
                    name = name.trim(),
                    type = ProjectType.REGULAR,
                    ownerId = user.uid,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                    isDeletable = true,
                )
            )
            notebookRepository.createDefaultNotebookForProject(
                projectId = projectId,
                projectName = name.trim(),
                ownerId = user.uid,
                sections = listOf("Planning", "In Progress", "Review", "Done"),
            )
            _isCreating.value = false
        }
    }

    fun toggleStar(project: Project) {
        viewModelScope.launch {
            projectRepository.toggleStarProject(project.id, !project.isStarred)
        }
    }

    fun completeProject(project: Project) {
        if (project.type != ProjectType.REGULAR) return
        viewModelScope.launch {
            projectRepository.completeProject(project.id)
        }
    }

    fun deleteProject(project: Project) {
        if (!project.isDeletable) return
        viewModelScope.launch {
            projectRepository.deleteProject(project.id)
        }
    }

    private fun ensureDefaultProjects() {
        viewModelScope.launch {
            val user = cachedUser.first { it != null } ?: return@launch
            projectRepository.createDefaultProjects(user.uid)
        }
    }
}
