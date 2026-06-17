package com.theultimatenote.app.ui.screens.daily

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.theultimatenote.app.data.model.Task
import com.theultimatenote.app.ui.components.TaskEditDialog
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen() {
    val viewModel: DailyViewModel = koinViewModel()
    val dailyTasks by viewModel.dailyTasks.collectAsState()
    val learningTasks by viewModel.learningTasks.collectAsState()
    val dailyProject by viewModel.dailyProject.collectAsState()
    val learningProject by viewModel.learningProject.collectAsState()

    var showAddTask by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var scheduledTime by remember { mutableStateOf<String?>(null) }
    val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        floatingActionButton = {
            if (dailyProject != null) {
                FloatingActionButton(
                    onClick = { showAddTask = true },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (showAddTask) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = newTaskTitle,
                                onValueChange = { newTaskTitle = it },
                                label = { Text("New daily task") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
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
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                IconButton(onClick = {
                                    viewModel.addDailyTask(newTaskTitle, isRecurring, scheduledTime)
                                    newTaskTitle = ""
                                    scheduledTime = null
                                    showAddTask = false
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    showAddTask = false
                                    newTaskTitle = ""
                                    scheduledTime = null
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Today's Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 0.75.dp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            val recurringTasks = dailyTasks.filter { it.isRecurring }
            val tempTasks = dailyTasks.filter { !it.isRecurring }

            if (recurringTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "↻ Recurring",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                items(recurringTasks, key = { "daily-${it.id}" }) { task ->
                    DailyTaskItem(
                        task = task,
                        onToggle = { viewModel.toggleTaskComplete(task) },
                        onDelete = { viewModel.deleteTask(task) },
                        onEdit = { viewModel.updateTask(it) },
                    )
                }
            }

            if (tempTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Temporary",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                items(tempTasks, key = { "temp-${it.id}" }) { task ->
                    DailyTaskItem(
                        task = task,
                        onToggle = { viewModel.toggleTaskComplete(task) },
                        onDelete = { viewModel.deleteTask(task) },
                        onEdit = { viewModel.updateTask(it) },
                    )
                }
            }

            if (dailyTasks.isEmpty()) {
                item {
                    Text(
                        text = "No daily tasks yet. Tap + to add one!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Learning",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 0.75.dp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            if (learningTasks.isNotEmpty()) {
                items(learningTasks, key = { "learn-${it.id}" }) { task ->
                    DailyTaskItem(
                        task = task,
                        onToggle = { viewModel.toggleTaskComplete(task) },
                        onDelete = { viewModel.deleteTask(task) },
                        onEdit = { viewModel.updateTask(it) },
                    )
                }
            } else {
                item {
                    Text(
                        text = "No learning tasks yet. Add them from the Learning project board.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Reminder Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    val h = timePickerState.hour
                    val m = timePickerState.minute
                    scheduledTime = "%02d:%02d".format(h, m)
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

@Composable
private fun DailyTaskItem(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (Task) -> Unit,
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showEditDialog = true },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = task.isCompletedToday,
                onCheckedChange = { onToggle() },
                modifier = Modifier.size(24.dp),
            )
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (task.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                    color = if (task.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (task.isRecurring || task.scheduledTime != null) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (task.isRecurring) {
                            Text(
                                text = "↻ Recurring",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                        if (task.scheduledTime != null) {
                            Text(
                                text = "⏰ ${task.scheduledTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }

    if (showEditDialog) {
        TaskEditDialog(
            task = task,
            showRecurringToggle = true,
            onSave = { updated -> onEdit(updated) },
            onDismiss = { showEditDialog = false },
        )
    }
}
