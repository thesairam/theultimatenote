package com.theultimatenote.app.data.repository

interface BillingManager {
    fun launchUpgradeFlow()
    fun querySubscriptionStatus()
}
