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

// Google Sign-In on iOS needs the GoogleSignIn SDK pod + a URL scheme registered
// in Info.plist (from the iOS OAuth client in Firebase). Not wired up yet — use
// email & password for now.
@Composable
actual fun GoogleSignInButton(
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit,
) {
    OutlinedButton(
        onClick = { onError("Google sign-in isn't available on iOS yet — use email & password.") },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text("Continue with Google (coming soon)")
    }
}
