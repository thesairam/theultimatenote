package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.SubscriptionInfo
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getSubscription(userId: String): Flow<SubscriptionInfo>
    suspend fun updateSubscription(userId: String, info: SubscriptionInfo)
    suspend fun getTodayAiMessageCount(userId: String): Int
    suspend fun incrementAiMessageCount(userId: String)
}
