package com.theultimatenote.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.User
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.AuthResult
import com.theultimatenote.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User = User(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val saveSuccess: Boolean = false,
    val accountDeleted: Boolean = false,
    val error: String? = null,
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val authUser = authRepository.currentUser.first()
            if (authUser != null) {
                userRepository.getUser(authUser.uid).collect { user ->
                    _uiState.value = _uiState.value.copy(
                        user = user ?: User(
                            id = authUser.uid,
                            email = authUser.email ?: "",
                            displayName = authUser.displayName ?: "",
                        ),
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun updateUser(user: User) {
        _uiState.value = _uiState.value.copy(user = user)
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveSuccess = false, error = null)
            try {
                userRepository.saveUser(_uiState.value.user)
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message ?: "Failed to save profile")
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            when (val result = authRepository.deleteAccount()) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isDeleting = false, accountDeleted = true)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isDeleting = false, error = result.message)
                }
            }
        }
    }
}
