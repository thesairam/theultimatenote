package com.theultimatenote.app.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.Project
import com.theultimatenote.app.data.model.ProjectType
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.AuthUser
import com.theultimatenote.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ProjectsViewModel(
    private val projectRepository: ProjectRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    val projects: StateFlow<List<Project>> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) projectRepository.getProjects(user.uid) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createProject(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            _isCreating.value = true
            projectRepository.createProject(
                Project(
                    name = name.trim(),
                    type = ProjectType.REGULAR,
                    ownerId = user.uid,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                    isDeletable = true,
                )
            )
            _isCreating.value = false
        }
    }

    fun deleteProject(project: Project) {
        if (!project.isDeletable) return
        viewModelScope.launch {
            projectRepository.deleteProject(project.id)
        }
    }

    fun ensureDefaultProjects() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            projectRepository.createDefaultProjects(user.uid)
        }
    }
}
