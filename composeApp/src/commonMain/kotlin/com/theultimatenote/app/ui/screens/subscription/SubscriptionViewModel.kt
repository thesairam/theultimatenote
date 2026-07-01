package com.theultimatenote.app.ui.screens.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theultimatenote.app.data.model.SubscriptionInfo
import com.theultimatenote.app.data.model.SubscriptionLimits
import com.theultimatenote.app.data.model.SubscriptionTier
import com.theultimatenote.app.data.repository.AuthRepository
import com.theultimatenote.app.data.repository.BillingManager
import com.theultimatenote.app.data.repository.SubscriptionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SubscriptionUiState(
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val showUpgradeDialog: Boolean = false,
    val upgradeReason: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModel(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val billingManager: BillingManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    val subscription: StateFlow<SubscriptionInfo> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) subscriptionRepository.getSubscription(user.uid) else flowOf(SubscriptionInfo())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubscriptionInfo())

    val isPro: StateFlow<Boolean> = subscription
        .map { it.subscriptionTier == SubscriptionTier.PRO }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun canCreateProject(currentProjectCount: Int): Boolean {
        if (subscription.value.subscriptionTier == SubscriptionTier.PRO) return true
        return currentProjectCount < SubscriptionLimits.FREE_MAX_PROJECTS
    }

    fun canCreateTask(currentActiveTaskCount: Int): Boolean {
        if (subscription.value.subscriptionTier == SubscriptionTier.PRO) return true
        return currentActiveTaskCount < SubscriptionLimits.FREE_MAX_ACTIVE_TASKS
    }

    fun canCreateNotebookPage(currentPageCount: Int): Boolean {
        if (subscription.value.subscriptionTier == SubscriptionTier.PRO) return true
        return currentPageCount < SubscriptionLimits.FREE_MAX_NOTEBOOK_PAGES
    }

    suspend fun canSendAiMessage(): Boolean {
        if (subscription.value.subscriptionTier == SubscriptionTier.PRO) return true
        val userId = authRepository.currentUser.first()?.uid ?: return false
        val count = subscriptionRepository.getTodayAiMessageCount(userId)
        return count < SubscriptionLimits.FREE_MAX_AI_MESSAGES_PER_DAY
    }

    suspend fun recordAiMessage() {
        val userId = authRepository.currentUser.first()?.uid ?: return
        subscriptionRepository.incrementAiMessageCount(userId)
    }

    fun showUpgradeDialog(reason: String) {
        _uiState.value = _uiState.value.copy(showUpgradeDialog = true, upgradeReason = reason)
    }

    fun dismissUpgradeDialog() {
        _uiState.value = _uiState.value.copy(showUpgradeDialog = false, upgradeReason = "")
    }

    fun launchUpgradeFlow() {
        billingManager.launchUpgradeFlow()
    }
}
