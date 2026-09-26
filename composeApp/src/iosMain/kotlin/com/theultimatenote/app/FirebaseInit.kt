package com.theultimatenote.app

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import platform.Foundation.NSBundle

/**
 * Firebase on iOS needs GoogleService-Info.plist to be added to the Xcode project
 * (from the Firebase console, as a new iOS app in the same project used for Android).
 * Until that file exists, we skip initialization instead of crashing on launch so the
 * rest of the UI is still reachable for testing.
 */
object FirebaseInit {
    val isConfigured: Boolean by lazy {
        val hasPlist = NSBundle.mainBundle.pathForResource("GoogleService-Info", ofType = "plist") != null
        if (hasPlist) {
            runCatching { Firebase.initialize() }.isSuccess
        } else {
            println("[TheUltimateNote] GoogleService-Info.plist not found — Firebase features are disabled until it's added to iosApp.")
            false
        }
    }
}
