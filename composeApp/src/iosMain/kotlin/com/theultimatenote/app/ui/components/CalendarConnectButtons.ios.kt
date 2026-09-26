package com.theultimatenote.app.ui.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Google Calendar OAuth and EventKit (Apple Calendar) sync aren't wired up on
// iOS yet — both are follow-ups, not blockers for the first testable build.
@Composable
actual fun GoogleCalendarConnectButton(
    onConnected: (accessToken: String) -> Unit,
    onError: (String) -> Unit,
) {
    OutlinedButton(
        onClick = { onError("Google Calendar sync isn't available on iOS yet") },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Icon(Icons.Default.CalendarMonth, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Connect Google Calendar (coming soon)")
    }
}

@Composable
actual fun AppleCalendarConnectButton(
    onConnected: (String) -> Unit,
    onError: (String) -> Unit,
) {
    OutlinedButton(
        onClick = { onError("Apple Calendar sync isn't available yet") },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Icon(Icons.Default.Event, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Connect Apple Calendar (coming soon)")
    }
}
