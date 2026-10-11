package com.theultimatenote.app.security

import kotlin.test.Test
import kotlin.test.assertTrue

class ApiSecurityTest {

    @Test
    fun freeAiMessageLimitIsReasonable() {
        val limit = com.theultimatenote.app.data.model.SubscriptionLimits.FREE_MAX_AI_MESSAGES_PER_DAY
        assertTrue(limit > 0, "AI message limit should be positive")
        assertTrue(limit <= 100, "AI message limit should not be excessively high for free tier")
    }

    @Test
    fun freeProjectLimitIsReasonable() {
        val limit = com.theultimatenote.app.data.model.SubscriptionLimits.FREE_MAX_PROJECTS
        assertTrue(limit > 0)
        assertTrue(limit <= 20)
    }

    @Test
    fun freeTaskLimitIsReasonable() {
        val limit = com.theultimatenote.app.data.model.SubscriptionLimits.FREE_MAX_ACTIVE_TASKS
        assertTrue(limit > 0)
        assertTrue(limit <= 500)
    }
}
