package com.theultimatenote.app.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.firebase.auth.FirebaseAuth
import com.theultimatenote.app.data.model.SubscriptionInfo
import com.theultimatenote.app.data.model.SubscriptionTier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AndroidBillingManager(
    private val context: Context,
    private val subscriptionRepository: SubscriptionRepository,
) : BillingManager, PurchasesUpdatedListener {

    companion object {
        const val PRO_SUBSCRIPTION_ID = "pro_monthly"
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var productDetails: ProductDetails? = null

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().enablePrepaidPlans().build())
        .build()

    init {
        connectBillingClient()
    }

    private fun connectBillingClient() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    querySubscriptionStatus()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry on next launchUpgradeFlow call
            }
        })
    }

    private fun queryProductDetails() {
        val productList = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRO_SUBSCRIPTION_ID)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(productList))
            .build()

        billingClient.queryProductDetailsAsync(params) { result, detailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK && detailsList.isNotEmpty()) {
                productDetails = detailsList.first()
            }
        }
    }

    override fun querySubscriptionStatus() {
        if (!billingClient.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val activePurchase = purchases.firstOrNull { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                scope.launch {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                    if (activePurchase != null) {
                        subscriptionRepository.updateSubscription(
                            userId,
                            SubscriptionInfo(
                                tier = SubscriptionTier.PRO.name,
                                purchaseToken = activePurchase.purchaseToken,
                                expiresAt = 0L,
                            ),
                        )
                    }
                }
            }
        }
    }

    override fun launchUpgradeFlow() {
        val activity = context as? Activity
        if (activity == null || !billingClient.isReady) {
            if (!billingClient.isReady) connectBillingClient()
            return
        }

        val details = productDetails
        if (details == null) {
            queryProductDetails()
            return
        }

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    scope.launch {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                        subscriptionRepository.updateSubscription(
                            userId,
                            SubscriptionInfo(
                                tier = SubscriptionTier.PRO.name,
                                purchaseToken = purchase.purchaseToken,
                                expiresAt = 0L,
                            ),
                        )
                    }
                }
            }
        }
    }
}
