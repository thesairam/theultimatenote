package com.theultimatenote.app.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun AppleSignInButton(
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit,
) {
    // Not shown on Android — Apple Sign-In is iOS only
}
