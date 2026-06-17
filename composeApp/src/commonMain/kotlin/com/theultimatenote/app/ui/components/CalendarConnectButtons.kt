package com.theultimatenote.app.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun GoogleCalendarConnectButton(
    onConnected: (accessToken: String) -> Unit,
    onError: (String) -> Unit,
)

@Composable
expect fun AppleCalendarConnectButton(
    onConnected: (String) -> Unit,
    onError: (String) -> Unit,
)
