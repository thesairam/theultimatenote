package com.theultimatenote.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.AuthResult
import com.theultimatenote.app.data.repository.AuthUser
import com.theultimatenote.app.data.security.InputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val resetEmailSent: Boolean = false,
)

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    sealed class AuthState {
        data object Loading : AuthState()
        data object Authenticated : AuthState()
        data object Unauthenticated : AuthState()
    }

    private val _authStateInitialized = MutableStateFlow(false)

    val authState: StateFlow<AuthState> = authRepository.currentUser
        .map { user ->
            _authStateInitialized.value = true
            if (user != null) AuthState.Authenticated else AuthState.Unauthenticated
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AuthState.Loading)

    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        val emailCheck = InputValidator.validateEmail(email)
        if (!emailCheck.isValid) {
            _uiState.value = AuthUiState(error = emailCheck.error)
            return
        }
        if (password.isBlank()) {
            _uiState.value = AuthUiState(error = "Please enter your password.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signIn(email.trim(), password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState()
                is AuthResult.Error -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        val emailCheck = InputValidator.validateEmail(email)
        if (!emailCheck.isValid) {
            _uiState.value = AuthUiState(error = emailCheck.error)
            return
        }
        val passwordCheck = InputValidator.validatePassword(password)
        if (!passwordCheck.isValid) {
            _uiState.value = AuthUiState(error = passwordCheck.error)
            return
        }
        val nameCheck = InputValidator.validateDisplayName(displayName)
        if (!nameCheck.isValid) {
            _uiState.value = AuthUiState(error = nameCheck.error)
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signUp(email.trim(), password, displayName.trim())) {
                is AuthResult.Success -> _uiState.value = AuthUiState()
                is AuthResult.Error -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }

    fun sendPasswordReset(email: String) {
        val emailCheck = InputValidator.validateEmail(email)
        if (!emailCheck.isValid) {
            _uiState.value = AuthUiState(error = emailCheck.error)
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.sendPasswordReset(email.trim())) {
                is AuthResult.Success -> _uiState.value = AuthUiState(resetEmailSent = true)
                is AuthResult.Error -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is AuthResult.Success -> _uiState.value = AuthUiState()
                is AuthResult.Error -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }

    fun setError(message: String) {
        _uiState.value = AuthUiState(error = message)
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearState() {
        _uiState.value = AuthUiState()
    }
}
