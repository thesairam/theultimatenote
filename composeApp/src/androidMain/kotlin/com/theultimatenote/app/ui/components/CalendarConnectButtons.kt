package com.theultimatenote.app.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun GoogleCalendarConnectButton(
    onConnected: (accessToken: String) -> Unit,
    onError: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch {
                try {
                    val email = FirebaseAuth.getInstance().currentUser?.email
                    if (email != null) {
                        val account = android.accounts.Account(email, "com.google")
                        val token = withContext(Dispatchers.IO) {
                            GoogleAuthUtil.getToken(
                                context,
                                account,
                                "oauth2:https://www.googleapis.com/auth/calendar.events",
                            )
                        }
                        onConnected(token)
                    } else {
                        onError("No Google account linked")
                    }
                } catch (e: Exception) {
                    onError(e.message ?: "Google Calendar authorization failed")
                }
            }
        } else {
            onError("Google Calendar authorization cancelled")
        }
    }

    OutlinedButton(
        onClick = {
            scope.launch {
                try {
                    val email = FirebaseAuth.getInstance().currentUser?.email
                    if (email == null) {
                        onError("Sign in with Google first to connect Calendar")
                        return@launch
                    }
                    val account = android.accounts.Account(email, "com.google")
                    val token = withContext(Dispatchers.IO) {
                        GoogleAuthUtil.getToken(
                            context,
                            account,
                            "oauth2:https://www.googleapis.com/auth/calendar.events",
                        )
                    }
                    onConnected(token)
                } catch (e: UserRecoverableAuthException) {
                    e.intent?.let { consentLauncher.launch(it) }
                        ?: onError("Calendar authorization requires user action")
                } catch (e: Exception) {
                    onError(e.message ?: "Google Calendar connection failed")
                }
            }
        },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Icon(Icons.Default.CalendarMonth, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Connect Google Calendar")
    }
}

@Composable
actual fun AppleCalendarConnectButton(
    onConnected: (String) -> Unit,
    onError: (String) -> Unit,
) {
    OutlinedButton(
        onClick = {
            onError("Apple Calendar is available on iOS only")
        },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        enabled = false,
    ) {
        Icon(Icons.Default.Event, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Apple Calendar (iOS only)")
    }
}
