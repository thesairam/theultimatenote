package com.theultimatenote.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.theultimatenote.app.data.model.SubscriptionInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class FirebaseSubscriptionRepository : SubscriptionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCol = db.collection("users")

    override fun getSubscription(userId: String): Flow<SubscriptionInfo> {
        return usersCol.document(userId).collection("settings").document("subscription")
            .snapshots()
            .map { doc ->
                if (!doc.exists()) return@map SubscriptionInfo()
                SubscriptionInfo(
                    tier = doc.getString("tier") ?: "FREE",
                    purchaseToken = doc.getString("purchaseToken"),
                    expiresAt = doc.getLong("expiresAt") ?: 0L,
                )
            }
    }

    override suspend fun updateSubscription(userId: String, info: SubscriptionInfo) {
        usersCol.document(userId).collection("settings").document("subscription")
            .set(
                mapOf(
                    "tier" to info.tier,
                    "purchaseToken" to info.purchaseToken,
                    "expiresAt" to info.expiresAt,
                )
            ).await()
    }

    override suspend fun getTodayAiMessageCount(userId: String): Int {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val doc = usersCol.document(userId).collection("settings").document("ai_usage").get().await()
        if (!doc.exists()) return 0
        val date = doc.getString("date") ?: ""
        return if (date == today) (doc.getLong("count") ?: 0L).toInt() else 0
    }

    override suspend fun incrementAiMessageCount(userId: String) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val docRef = usersCol.document(userId).collection("settings").document("ai_usage")
        val doc = docRef.get().await()
        val currentDate = if (doc.exists()) doc.getString("date") ?: "" else ""
        val currentCount = if (doc.exists() && currentDate == today) (doc.getLong("count") ?: 0L).toInt() else 0
        docRef.set(mapOf("date" to today, "count" to currentCount + 1)).await()
    }
}
