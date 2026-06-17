package com.theultimatenote.app.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun AppleSignInButton(
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit,
)
