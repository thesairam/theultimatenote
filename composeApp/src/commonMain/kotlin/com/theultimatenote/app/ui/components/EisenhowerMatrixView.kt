package com.theultimatenote.app.ui.components

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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.theultimatenote.app.data.model.KanbanColumn
import com.theultimatenote.app.data.model.Task

enum class Quadrant(val label: String, val isUrgent: Boolean, val isImportant: Boolean) {
    DO_FIRST("Do First", isUrgent = true, isImportant = true),
    SCHEDULE("Schedule", isUrgent = false, isImportant = true),
    QUICK_WIN("Quick Win", isUrgent = true, isImportant = false),
    LOW_PRIORITY("Low Priority", isUrgent = false, isImportant = false),
}

@Composable
fun EisenhowerMatrixView(
    tasks: List<Task>,
    columns: List<KanbanColumn>,
    onToggleComplete: (Task) -> Unit,
    onMoveQuadrant: (Task, Boolean, Boolean) -> Unit,
    onEditTask: (Task) -> Unit,
    isDailyProject: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val nonCompleted = tasks.filter { !it.isCompletedToday }

    Column(modifier = modifier.padding(8.dp)) {
        // Top row: Important
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            QuadrantCard(
                quadrant = Quadrant.DO_FIRST,
                tasks = nonCompleted.filter { it.isUrgent && it.isImportant },
                columns = columns,
                allTasks = nonCompleted,
                onToggleComplete = onToggleComplete,
                onMoveQuadrant = onMoveQuadrant,
                onEditTask = onEditTask,
                isDailyProject = isDailyProject,
                modifier = Modifier.weight(1f),
                accentColor = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.width(8.dp))
            QuadrantCard(
                quadrant = Quadrant.SCHEDULE,
                tasks = nonCompleted.filter { !it.isUrgent && it.isImportant },
                columns = columns,
                allTasks = nonCompleted,
                onToggleComplete = onToggleComplete,
                onMoveQuadrant = onMoveQuadrant,
                onEditTask = onEditTask,
                isDailyProject = isDailyProject,
                modifier = Modifier.weight(1f),
                accentColor = MaterialTheme.colorScheme.tertiary,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Bottom row: Not Important
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            QuadrantCard(
                quadrant = Quadrant.QUICK_WIN,
                tasks = nonCompleted.filter { it.isUrgent && !it.isImportant },
                columns = columns,
                allTasks = nonCompleted,
                onToggleComplete = onToggleComplete,
                onMoveQuadrant = onMoveQuadrant,
                onEditTask = onEditTask,
                isDailyProject = isDailyProject,
                modifier = Modifier.weight(1f),
                accentColor = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            QuadrantCard(
                quadrant = Quadrant.LOW_PRIORITY,
                tasks = nonCompleted.filter { !it.isUrgent && !it.isImportant },
                columns = columns,
                allTasks = nonCompleted,
                onToggleComplete = onToggleComplete,
                onMoveQuadrant = onMoveQuadrant,
                onEditTask = onEditTask,
                isDailyProject = isDailyProject,
                modifier = Modifier.weight(1f),
                accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Axis labels
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "← Urgent",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            )
            Text(
                text = "Not Urgent →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun QuadrantCard(
    quadrant: Quadrant,
    tasks: List<Task>,
    columns: List<KanbanColumn>,
    allTasks: List<Task>,
    onToggleComplete: (Task) -> Unit,
    onMoveQuadrant: (Task, Boolean, Boolean) -> Unit,
    onEditTask: (Task) -> Unit,
    isDailyProject: Boolean,
    modifier: Modifier = Modifier,
    accentColor: Color,
) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.06f),
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.75.dp, accentColor.copy(alpha = 0.3f)),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = quadrant.label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = accentColor,
                )
                Text(
                    text = "${tasks.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.7f),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(
                    if (isDailyProject) tasks.sortedBy { it.scheduledTime ?: "99:99" }
                    else tasks.sortedBy { it.createdAt },
                    key = { "matrix-${it.id}" },
                ) { task ->
                    MatrixTaskCard(
                        task = task,
                        columns = columns,
                        quadrant = quadrant,
                        onToggleComplete = { onToggleComplete(task) },
                        onMoveQuadrant = onMoveQuadrant,
                        onEditTask = onEditTask,
                        isDailyProject = isDailyProject,
                    )
                }
            }
        }
    }
}

@Composable
private fun MatrixTaskCard(
    task: Task,
    columns: List<KanbanColumn>,
    quadrant: Quadrant,
    onToggleComplete: () -> Unit,
    onMoveQuadrant: (Task, Boolean, Boolean) -> Unit,
    onEditTask: (Task) -> Unit,
    isDailyProject: Boolean,
) {
    var showMoveMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showEditDialog = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = task.isCompletedToday,
                    onCheckedChange = { onToggleComplete() },
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        textDecoration = if (task.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            // Chips row: kanban column tag + time + move
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val columnName = columns.find { it.id == task.columnId }?.name
                if (columnName != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    ) {
                        Text(
                            text = columnName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
                if (task.isRecurring) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                    ) {
                        Text(
                            text = "↻",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        )
                    }
                }
                if (task.scheduledTime != null) {
                    Text(
                        text = task.scheduledTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    TextButton(
                        onClick = { showMoveMenu = true },
                        modifier = Modifier.height(24.dp),
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move",
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = showMoveMenu,
                        onDismissRequest = { showMoveMenu = false },
                    ) {
                        Quadrant.entries.filter { it != quadrant }.forEach { target ->
                            DropdownMenuItem(
                                text = { Text(target.label, style = MaterialTheme.typography.bodySmall) },
                                onClick = {
                                    onMoveQuadrant(task, target.isUrgent, target.isImportant)
                                    showMoveMenu = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        TaskEditDialog(
            task = task,
            showRecurringToggle = isDailyProject,
            onSave = { updated -> onEditTask(updated) },
            onDismiss = { showEditDialog = false },
        )
    }
}
