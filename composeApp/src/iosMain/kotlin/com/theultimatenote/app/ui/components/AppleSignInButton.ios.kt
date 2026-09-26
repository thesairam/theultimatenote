package com.theultimatenote.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Real "Sign in with Apple" needs the capability enabled in Xcode under a paid
// Apple Developer account. Stubbed until that's set up — use email & password.
@Composable
actual fun AppleSignInButton(
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit,
) {
    OutlinedButton(
        onClick = { onError("Sign in with Apple isn't set up yet — use email & password.") },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text("Sign in with Apple (coming soon)")
    }
}
