package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.SubscriptionInfo
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable

@Serializable
private data class AiUsage(val date: String = "", val count: Long = 0)

class IosSubscriptionRepository : SubscriptionRepository {

    private val db = Firebase.firestore
    private val usersCol = db.collection("users")

    override fun getSubscription(userId: String): Flow<SubscriptionInfo> {
        return usersCol.document(userId).collection("settings").document("subscription")
            .snapshots
            .map { doc -> if (doc.exists) doc.data<SubscriptionInfo>() else SubscriptionInfo() }
    }

    override suspend fun updateSubscription(userId: String, info: SubscriptionInfo) {
        usersCol.document(userId).collection("settings").document("subscription").set(info)
    }

    override suspend fun getTodayAiMessageCount(userId: String): Int {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val doc = usersCol.document(userId).collection("settings").document("ai_usage").get()
        if (!doc.exists) return 0
        val usage = doc.data<AiUsage>()
        return if (usage.date == today) usage.count.toInt() else 0
    }

    override suspend fun incrementAiMessageCount(userId: String) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val docRef = usersCol.document(userId).collection("settings").document("ai_usage")
        val doc = docRef.get()
        val current = if (doc.exists) doc.data<AiUsage>() else AiUsage()
        if (current.date == today) {
            docRef.update("count" to FieldValue.increment(1))
        } else {
            docRef.set(AiUsage(date = today, count = 1))
        }
    }
}
