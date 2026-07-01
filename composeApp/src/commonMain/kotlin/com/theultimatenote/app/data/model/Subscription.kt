package com.theultimatenote.app.data.model

import kotlinx.serialization.Serializable

enum class SubscriptionTier {
    FREE,
    PRO,
}

@Serializable
data class SubscriptionInfo(
    val tier: String = SubscriptionTier.FREE.name,
    val purchaseToken: String? = null,
    val expiresAt: Long = 0L,
) {
    val subscriptionTier: SubscriptionTier
        get() = try { SubscriptionTier.valueOf(tier) } catch (_: Exception) { SubscriptionTier.FREE }
}

object SubscriptionLimits {
    const val FREE_MAX_PROJECTS = 3
    const val FREE_MAX_ACTIVE_TASKS = 50
    const val FREE_MAX_NOTEBOOK_PAGES = 5
    const val FREE_MAX_AI_MESSAGES_PER_DAY = 10
}
