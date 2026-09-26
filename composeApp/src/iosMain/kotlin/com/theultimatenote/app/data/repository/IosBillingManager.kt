package com.theultimatenote.app.data.repository

/**
 * StoreKit billing isn't wired up yet (needs an Apple Developer account to
 * configure an in-app purchase / subscription product). Upgrade is a no-op
 * on iOS until then; all users stay on the free tier.
 */
class IosBillingManager : BillingManager {
    override fun launchUpgradeFlow() {
        // TODO: implement with StoreKit once App Store Connect subscriptions are set up.
    }

    override fun querySubscriptionStatus() {
        // TODO: implement with StoreKit once App Store Connect subscriptions are set up.
    }
}
