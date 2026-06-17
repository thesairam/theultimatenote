package com.theultimatenote.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

enum class PomodoroState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
}

@Composable
fun PomodoroTimerSheet(
    taskTitle: String,
    durationMinutes: Int = 25,
    onComplete: (actualMinutes: Int) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    var state by remember { mutableStateOf(PomodoroState.IDLE) }
    var remainingSeconds by remember { mutableIntStateOf(durationMinutes * 60) }
    val totalSeconds = durationMinutes * 60

    LaunchedEffect(state) {
        if (state == PomodoroState.RUNNING) {
            while (remainingSeconds > 0 && state == PomodoroState.RUNNING) {
                delay(1000L)
                if (state == PomodoroState.RUNNING) {
                    remainingSeconds--
                }
            }
            if (remainingSeconds <= 0) {
                state = PomodoroState.COMPLETED
                onComplete(durationMinutes)
            }
        }
    }

    Dialog(onDismissRequest = {
        if (state == PomodoroState.IDLE) onDismiss()
    }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Focus Session",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (state == PomodoroState.IDLE || state == PomodoroState.COMPLETED) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = taskTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                Text(
                    text = "%02d:%02d".format(minutes, seconds),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light,
                    color = when (state) {
                        PomodoroState.COMPLETED -> MaterialTheme.colorScheme.tertiary
                        PomodoroState.RUNNING -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    letterSpacing = 4.sp,
                )

                if (state == PomodoroState.COMPLETED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Session complete! Great focus.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (state) {
                        PomodoroState.IDLE -> {
                            FilledIconButton(
                                onClick = { state = PomodoroState.RUNNING },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(28.dp))
                            }
                        }
                        PomodoroState.RUNNING -> {
                            FilledIconButton(
                                onClick = { state = PomodoroState.PAUSED },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                ),
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(28.dp))
                            }
                            FilledIconButton(
                                onClick = {
                                    val elapsedMinutes = (totalSeconds - remainingSeconds) / 60
                                    onCancel()
                                },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                ),
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(28.dp))
                            }
                        }
                        PomodoroState.PAUSED -> {
                            FilledIconButton(
                                onClick = { state = PomodoroState.RUNNING },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(28.dp))
                            }
                            FilledIconButton(
                                onClick = { onCancel() },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                ),
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(28.dp))
                            }
                        }
                        PomodoroState.COMPLETED -> {
                            FilledIconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                ),
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Done", modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }

                if (state == PomodoroState.RUNNING || state == PomodoroState.PAUSED) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val progress = 1f - (remainingSeconds.toFloat() / totalSeconds)
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    )
                }
            }
        }
    }
}
