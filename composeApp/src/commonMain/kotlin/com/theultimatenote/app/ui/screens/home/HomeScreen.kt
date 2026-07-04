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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WorkHistory
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
import androidx.compose.ui.graphics.vector.ImageVector
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
            // Greeting
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

            // Starred project shortcuts
            if (uiState.starredProjects.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = goldColor,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Shortcuts",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                items(uiState.starredProjects, key = { "starred-${it.id}" }) { project ->
                    StarredProjectCard(
                        project = project,
                        goldColor = goldColor,
                        onClick = {
                            onNavigateToBoard(project.id, project.name, project.type.name)
                        },
                    )
                }
            }

            // Today's Progress
            item {
                Spacer(modifier = Modifier.height(4.dp))
                TodayProgressCard(uiState)
            }

            // Focus Sessions
            item { FocusCard(uiState) }

            // Task Breakdown
            item { BreakdownCard(uiState) }

            // Projects overview
            item { ProjectsOverviewCard(uiState, goldColor) }

            // All-time focus
            item { AllTimeFocusCard(uiState) }

            item { Spacer(modifier = Modifier.height(60.dp)) }
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
private fun StarredProjectCard(
    project: Project,
    goldColor: Color,
    onClick: () -> Unit,
) {
    val icon = when (project.type) {
        ProjectType.DAILY -> Icons.Default.CalendarToday
        ProjectType.LEARNING -> Icons.Default.School
        ProjectType.REGULAR -> Icons.Default.Folder
    }
    val isSpecial = project.type == ProjectType.DAILY || project.type == ProjectType.LEARNING

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSpecial) goldColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.75.dp, if (isSpecial) goldColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSpecial) goldColor else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Open →",
                style = MaterialTheme.typography.labelMedium,
                color = if (isSpecial) goldColor else MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun TodayProgressCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Today,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    text = "${uiState.completedToday}/${uiState.totalToday}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = {
                    if (uiState.totalToday > 0) uiState.completedToday.toFloat() / uiState.totalToday
                    else 0f
                },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            )
            if (uiState.totalToday > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val pct = (uiState.completedToday * 100) / uiState.totalToday
                Text(
                    text = "$pct% complete",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun FocusCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = "Focus Sessions Today",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatNumber(
                    value = "${uiState.pomodoroSessionsToday}",
                    label = "Sessions",
                    icon = Icons.Default.LocalFireDepartment,
                    color = MaterialTheme.colorScheme.error,
                )
                StatNumber(
                    value = "${uiState.pomodoroMinutesToday}m",
                    label = "Focus Time",
                    icon = Icons.Default.Timer,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun BreakdownCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text = "Completed Today",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatNumber(
                    value = "${uiState.dailyTasksCompleted}",
                    label = "Daily",
                    icon = Icons.Default.Today,
                    color = MaterialTheme.colorScheme.primary,
                )
                StatNumber(
                    value = "${uiState.learningTasksCompleted}",
                    label = "Learning",
                    icon = Icons.Default.School,
                    color = MaterialTheme.colorScheme.secondary,
                )
                StatNumber(
                    value = "${uiState.projectTasksCompleted}",
                    label = "Projects",
                    icon = Icons.Default.WorkHistory,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun ProjectsOverviewCard(uiState: HomeUiState, goldColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Projects",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatNumber(
                    value = "${uiState.activeProjectCount}",
                    label = "Active",
                    icon = Icons.Default.Folder,
                    color = MaterialTheme.colorScheme.primary,
                )
                StatNumber(
                    value = "${uiState.completedProjectCount}",
                    label = "Completed",
                    icon = Icons.Default.CheckCircle,
                    color = goldColor,
                )
            }
        }
    }
}

@Composable
private fun AllTimeFocusCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "All Time Focus",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatNumber(
                    value = "${uiState.totalPomodoroSessions}",
                    label = "Sessions",
                    icon = Icons.Default.LocalFireDepartment,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                val hours = uiState.totalFocusMinutes / 60
                val mins = uiState.totalFocusMinutes % 60
                val timeText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                StatNumber(
                    value = timeText,
                    label = "Total Focus",
                    icon = Icons.Default.Timer,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
private fun StatNumber(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.7f),
        )
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
