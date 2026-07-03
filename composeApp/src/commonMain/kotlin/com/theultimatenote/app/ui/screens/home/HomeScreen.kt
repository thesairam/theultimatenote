package com.theultimatenote.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theultimatenote.app.data.model.ChecklistItem
import com.theultimatenote.app.data.model.Project
import com.theultimatenote.app.data.model.ProjectType
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToBoard: (projectId: String, projectName: String, projectType: String) -> Unit = { _, _, _ -> },
) {
    val viewModel: HomeViewModel = koinViewModel()
    val subscriptionViewModel: com.theultimatenote.app.ui.screens.subscription.SubscriptionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val limitReached by viewModel.limitReached.collectAsState()
    var showQuickAdd by remember { mutableStateOf(false) }

    val goldColor = Color(0xFFB8960C)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Connecting Dots",
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToChat) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI Chat",
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAdd = true },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Quick Add Task")
            }
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    Text(
                        text = "Good day,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${uiState.userName} ✨",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create tasks, habits, projects & notebooks - all dots connected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToStats() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (uiState.totalCount > 0) "${uiState.completedCount}/${uiState.totalCount} done today"
                                else "No tasks yet — tap + to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "View Dashboard →",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }

            val specialProjects = uiState.projectSummaries.filter {
                it.project.type == ProjectType.DAILY || it.project.type == ProjectType.LEARNING
            }
            val regularProjects = uiState.projectSummaries.filter {
                it.project.type == ProjectType.REGULAR
            }

            if (specialProjects.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(specialProjects, key = { "home-${it.project.id}" }) { summary ->
                    ProjectSummaryCard(
                        summary = summary,
                        isSpecial = true,
                        goldColor = goldColor,
                        onClick = {
                            onNavigateToBoard(summary.project.id, summary.project.name, summary.project.type.name)
                        },
                    )
                }
            }

            if (regularProjects.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Projects",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                items(regularProjects, key = { "home-${it.project.id}" }) { summary ->
                    ProjectSummaryCard(
                        summary = summary,
                        isSpecial = false,
                        goldColor = goldColor,
                        onClick = {
                            onNavigateToBoard(summary.project.id, summary.project.name, summary.project.type.name)
                        },
                    )
                }
            }

            if (uiState.projectSummaries.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No projects yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Tap + to add a task or create a project",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }

    if (showQuickAdd) {
        QuickAddTaskDialog(
            projects = projects,
            onAdd = { title, projectId, isRecurring, scheduledTime, isUrgent, isImportant, description, checklist ->
                viewModel.quickAddTask(title, projectId, isRecurring, scheduledTime, isUrgent, isImportant, description, checklist)
            },
            onDismiss = { showQuickAdd = false },
        )
    }

    limitReached?.let { reason ->
        com.theultimatenote.app.ui.components.UpgradeDialog(
            reason = reason,
            onUpgrade = { subscriptionViewModel.launchUpgradeFlow(); viewModel.dismissLimit() },
            onDismiss = { viewModel.dismissLimit() },
        )
    }
}

@Composable
private fun ProjectSummaryCard(
    summary: ProjectSummary,
    isSpecial: Boolean,
    goldColor: Color,
    onClick: () -> Unit,
) {
    val icon = when (summary.project.type) {
        ProjectType.DAILY -> Icons.Default.CalendarToday
        ProjectType.LEARNING -> Icons.Default.School
        ProjectType.REGULAR -> Icons.Default.Folder
    }

    val progress = if (summary.totalTasks > 0) {
        summary.completedTasks.toFloat() / summary.totalTasks
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSpecial) goldColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.75.dp, if (isSpecial) goldColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSpecial) goldColor else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.project.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isSpecial) {
                        Text(
                            text = "✦ Special Project",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = goldColor,
                        )
                    }
                }
                Text(
                    text = "Open →",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSpecial) goldColor else MaterialTheme.colorScheme.tertiary,
                )
            }

            if (summary.totalTasks > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = if (isSpecial) goldColor else MaterialTheme.colorScheme.tertiary,
                    trackColor = if (isSpecial) goldColor.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${summary.completedTasks}/${summary.totalTasks} done today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (summary.activeTasks > 0) {
                        Text(
                            text = "${summary.activeTasks} remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No tasks yet",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddTaskDialog(
    projects: List<Project>,
    onAdd: (String, String, Boolean, String?, Boolean, Boolean, String, List<ChecklistItem>) -> Unit,
    onDismiss: () -> Unit,
) {
    var taskTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedProject by remember { mutableStateOf(projects.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }
    var isRecurring by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var scheduledTime by remember { mutableStateOf<String?>(null) }
    var isUrgent by remember { mutableStateOf(false) }
    var isImportant by remember { mutableStateOf(true) }
    val checklistItems = remember { androidx.compose.runtime.mutableStateListOf<ChecklistItem>() }
    val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = false)

    val isDailyProject = selectedProject?.type == ProjectType.DAILY

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Add Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = "Checklist",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                checklistItems.forEachIndexed { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { checked ->
                                checklistItems[index] = item.copy(isChecked = checked)
                            },
                        )
                        OutlinedTextField(
                            value = item.text,
                            onValueChange = { text ->
                                checklistItems[index] = item.copy(text = text)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                        )
                        IconButton(
                            onClick = { checklistItems.removeAt(index) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                        }
                    }
                }
                TextButton(
                    onClick = {
                        checklistItems.add(
                            ChecklistItem(
                                id = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(),
                                text = "",
                                isChecked = false,
                            )
                        )
                    },
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add item")
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = selectedProject?.name ?: "Select project",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Project") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        projects.forEach { project ->
                            DropdownMenuItem(
                                text = { Text(project.name) },
                                onClick = {
                                    selectedProject = project
                                    expanded = false
                                    if (project.type != ProjectType.DAILY) {
                                        isRecurring = false
                                        scheduledTime = null
                                    }
                                },
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isDailyProject,
                    enter = expandVertically(tween(100)) + fadeIn(tween(100)),
                    exit = shrinkVertically(tween(150)) + fadeOut(tween(150)),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FilterChip(
                                selected = isRecurring,
                                onClick = {
                                    isRecurring = true
                                },
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

                        AnimatedVisibility(
                            visible = isRecurring,
                            enter = expandVertically(tween(100)) + fadeIn(tween(100)),
                            exit = shrinkVertically(tween(150)) + fadeOut(tween(150)),
                        ) {
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
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterChip(
                        selected = isUrgent,
                        onClick = { isUrgent = !isUrgent },
                        label = { Text("Urgent") },
                        leadingIcon = if (isUrgent) {
                            { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                    )
                    FilterChip(
                        selected = isImportant,
                        onClick = { isImportant = !isImportant },
                        label = { Text("Important") },
                        leadingIcon = if (isImportant) {
                            { Icon(Icons.Default.PriorityHigh, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (taskTitle.isNotBlank() && selectedProject != null) {
                        onAdd(taskTitle.trim(), selectedProject!!.id, isRecurring, scheduledTime, isUrgent, isImportant, description, checklistItems.toList())
                        onDismiss()
                    }
                },
                enabled = taskTitle.isNotBlank() && selectedProject != null,
            ) {
                Text("Add")
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
