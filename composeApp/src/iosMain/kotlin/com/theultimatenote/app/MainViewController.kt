package com.theultimatenote.app

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    // Touch the lazy val once up front so the "not configured" console warning
    // (if any) shows immediately at launch rather than on first Firebase call.
    FirebaseInit.isConfigured
    App()
}
