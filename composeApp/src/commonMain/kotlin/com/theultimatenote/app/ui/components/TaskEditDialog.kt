package com.theultimatenote.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theultimatenote.app.data.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditDialog(
    task: Task,
    showRecurringToggle: Boolean = false,
    onSave: (Task) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var isRecurring by remember { mutableStateOf(task.isRecurring) }
    var scheduledTime by remember { mutableStateOf(task.scheduledTime) }
    var isUrgent by remember { mutableStateOf(task.isUrgent) }
    var isImportant by remember { mutableStateOf(task.isImportant) }
    var showTimePicker by remember { mutableStateOf(false) }

    val initHour = scheduledTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 8
    val initMinute = scheduledTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0
    val timePickerState = rememberTimePickerState(initialHour = initHour, initialMinute = initMinute, is24Hour = false)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (showRecurringToggle) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = isRecurring,
                            onClick = { isRecurring = true },
                            label = { Text("Recurring") },
                        )
                        FilterChip(
                            selected = !isRecurring,
                            onClick = {
                                isRecurring = false
                                scheduledTime = null
                            },
                            label = { Text("Temporary") },
                        )
                    }

                    if (isRecurring) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            TextButton(onClick = { showTimePicker = true }) {
                                Text(
                                    text = scheduledTime ?: "Set reminder time",
                                    color = if (scheduledTime != null) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (scheduledTime != null) {
                                TextButton(onClick = { scheduledTime = null }) {
                                    Text("Clear", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterChip(
                        selected = isUrgent,
                        onClick = { isUrgent = !isUrgent },
                        label = { Text("Urgent") },
                        leadingIcon = if (isUrgent) {
                            { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.padding(0.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.error,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.error,
                        ),
                    )
                    FilterChip(
                        selected = isImportant,
                        onClick = { isImportant = !isImportant },
                        label = { Text("Important") },
                        leadingIcon = if (isImportant) {
                            { Icon(Icons.Default.PriorityHigh, contentDescription = null, modifier = Modifier.padding(0.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            task.copy(
                                title = title.trim(),
                                description = description.trim(),
                                isRecurring = if (showRecurringToggle) isRecurring else task.isRecurring,
                                scheduledTime = if (showRecurringToggle && isRecurring) scheduledTime else task.scheduledTime,
                                columnId = if (showRecurringToggle) {
                                    if (isRecurring) "recurring" else "temporary"
                                } else task.columnId,
                                isUrgent = isUrgent,
                                isImportant = isImportant,
                            )
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Reminder Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    scheduledTime = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        )
    }
}
