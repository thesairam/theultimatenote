package com.theultimatenote.app.ui.screens.projects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.theultimatenote.app.data.model.KanbanColumn
import com.theultimatenote.app.data.model.Task
import com.theultimatenote.app.ui.components.EisenhowerMatrixView
import com.theultimatenote.app.ui.components.TaskEditDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanBoardScreen(
    viewModel: KanbanViewModel,
    projectName: String,
    onNavigateBack: () -> Unit,
    isDailyProject: Boolean = false,
) {
    val board by viewModel.board.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    var showMatrix by remember { mutableStateOf(false) }

    var draggedTask by remember { mutableStateOf<Task?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
    val columnPositions = remember { mutableMapOf<String, Pair<Offset, IntSize>>() }
    var hoveredColumnId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(projectName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showMatrix = !showMatrix }) {
                        Icon(
                            if (showMatrix) Icons.Default.ViewKanban else Icons.Default.GridView,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showMatrix) "Kanban" else "Eisenhower",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { innerPadding ->
        val columns = board?.columns ?: emptyList()

        if (columns.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Loading board...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else { Crossfade(targetState = showMatrix, animationSpec = tween(120)) { isMatrix ->
        if (isMatrix) {
            EisenhowerMatrixView(
                tasks = tasks,
                columns = columns,
                onToggleComplete = { task -> viewModel.toggleTaskComplete(task) },
                onMoveQuadrant = { task, urgent, important ->
                    viewModel.updateTask(task.copy(isUrgent = urgent, isImportant = important))
                },
                onEditTask = { task -> viewModel.updateTask(task) },
                isDailyProject = isDailyProject,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        } else {
            val tasksByColumn = remember(tasks) { tasks.groupBy { it.columnId } }
            var containerRootPos by remember { mutableStateOf(Offset.Zero) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .onGloballyPositioned { coords ->
                        containerRootPos = coords.positionInRoot()
                    },
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(columns, key = { it.id }) { column ->
                        val columnTasks = tasksByColumn[column.id] ?: emptyList()
                        val isHovered = hoveredColumnId == column.id && draggedTask != null && draggedTask?.columnId != column.id
                        KanbanColumnCard(
                            column = column,
                            tasks = columnTasks,
                            allColumns = columns,
                            isDailyProject = isDailyProject,
                            isDropTarget = isHovered,
                            onAddTask = { title, urgent, important -> viewModel.addTask(title, column.id, urgent, important) },
                            onMoveTask = { taskId, newColId -> viewModel.moveTask(taskId, newColId) },
                            onToggleComplete = { task -> viewModel.toggleTaskComplete(task) },
                            onDeleteTask = { taskId -> viewModel.deleteTask(taskId) },
                            onEditTask = { task -> viewModel.updateTask(task) },
                            onRegisterPosition = { pos, size -> columnPositions[column.id] = pos to size },
                            onStartDrag = { task, offset ->
                                draggedTask = task
                                dragStartOffset = offset
                                dragOffset = Offset.Zero
                            },
                            onDrag = { change ->
                                dragOffset += change
                                val fingerPos = dragStartOffset + dragOffset
                                hoveredColumnId = columnPositions.entries.find { (_, posSize) ->
                                    val (pos, size) = posSize
                                    fingerPos.x in pos.x..(pos.x + size.width) &&
                                        fingerPos.y in pos.y..(pos.y + size.height)
                                }?.key
                            },
                            onDrop = {
                                val target = hoveredColumnId
                                if (target != null && draggedTask != null && target != draggedTask?.columnId) {
                                    viewModel.moveTask(draggedTask!!.id, target)
                                }
                                draggedTask = null
                                dragOffset = Offset.Zero
                                hoveredColumnId = null
                            },
                            draggedTaskId = draggedTask?.id,
                        )
                    }
                }

                if (draggedTask != null) {
                    val pos = dragStartOffset + dragOffset - containerRootPos
                    Card(
                        modifier = Modifier
                            .width(240.dp)
                            .offset {
                                IntOffset(
                                    (pos.x - 120f).roundToInt(),
                                    (pos.y - 24f).roundToInt(),
                                )
                            }
                            .graphicsLayer {
                                alpha = 0.92f
                                scaleX = 1.04f
                                scaleY = 1.04f
                                shadowElevation = 16f
                                rotationZ = 1.5f
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    ) {
                        Text(
                            text = draggedTask!!.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        } // end Crossfade
        }
    }
}

@Composable
private fun KanbanColumnCard(
    column: KanbanColumn,
    tasks: List<Task>,
    allColumns: List<KanbanColumn>,
    isDailyProject: Boolean,
    isDropTarget: Boolean = false,
    onAddTask: (String, Boolean, Boolean) -> Unit,
    onMoveTask: (String, String) -> Unit,
    onToggleComplete: (Task) -> Unit,
    onDeleteTask: (String) -> Unit,
    onEditTask: (Task) -> Unit,
    onRegisterPosition: (Offset, IntSize) -> Unit = { _, _ -> },
    onStartDrag: (Task, Offset) -> Unit = { _, _ -> },
    onDrag: (Offset) -> Unit = {},
    onDrop: () -> Unit = {},
    draggedTaskId: String? = null,
) {
    var showAddTask by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskUrgent by remember { mutableStateOf(false) }
    var newTaskImportant by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isDropTarget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(100),
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isDropTarget) 2.dp else 0.75.dp,
        animationSpec = tween(100),
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isDropTarget) 0.6f else 0.4f,
        animationSpec = tween(100),
    )

    Card(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .onGloballyPositioned { coords ->
                onRegisterPosition(coords.positionInRoot(), coords.size)
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = bgAlpha),
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = column.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = " (${tasks.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { showAddTask = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add task",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            AnimatedVisibility(
                visible = showAddTask,
                enter = expandVertically(tween(100)) + fadeIn(tween(100)),
                exit = shrinkVertically(tween(150)) + fadeOut(tween(150)),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            placeholder = { Text("Task title") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium,
                        )
                        IconButton(onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                onAddTask(newTaskTitle, newTaskUrgent, newTaskImportant)
                                newTaskTitle = ""
                                newTaskUrgent = false
                                newTaskImportant = false
                                showAddTask = false
                            }
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showAddTask = false; newTaskTitle = ""; newTaskUrgent = false; newTaskImportant = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Priority:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FilterChip(
                            selected = newTaskUrgent,
                            onClick = { newTaskUrgent = !newTaskUrgent },
                            label = { Text("Urgent", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (newTaskUrgent) {
                                { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.error,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.error,
                            ),
                        )
                        FilterChip(
                            selected = newTaskImportant,
                            onClick = { newTaskImportant = !newTaskImportant },
                            label = { Text("Important", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (newTaskImportant) {
                                { Icon(Icons.Default.PriorityHigh, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(tasks, key = { it.id }) { task ->
                    val isDragged = draggedTaskId == task.id
                    var taskRootPos by remember { mutableStateOf(Offset.Zero) }
                    Box(
                        modifier = Modifier
                            .graphicsLayer { alpha = if (isDragged) 0.3f else 1f }
                            .onGloballyPositioned { coords ->
                                taskRootPos = coords.positionInRoot()
                            }
                            .pointerInput(task.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        onStartDrag(task, taskRootPos + offset)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onDrag(dragAmount)
                                    },
                                    onDragEnd = { onDrop() },
                                    onDragCancel = { onDrop() },
                                )
                            },
                    ) {
                        TaskCard(
                            task = task,
                            allColumns = allColumns,
                            isDailyProject = isDailyProject,
                            onMove = { newColId -> onMoveTask(task.id, newColId) },
                            onToggleComplete = { onToggleComplete(task) },
                            onDelete = { onDeleteTask(task.id) },
                            onEdit = onEditTask,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    allColumns: List<KanbanColumn>,
    isDailyProject: Boolean,
    onMove: (String) -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (Task) -> Unit,
) {
    var showMoveMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showEditDialog = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (task.checklist.isNotEmpty()) {
                val checked = task.checklist.count { it.isChecked }
                Text(
                    text = "☑ $checked/${task.checklist.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (checked == task.checklist.size) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onToggleComplete, modifier = Modifier.height(32.dp).width(32.dp)) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Toggle complete",
                        tint = if (task.isCompletedToday) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box {
                    TextButton(onClick = { showMoveMenu = true }) {
                        Text("Move", style = MaterialTheme.typography.labelSmall)
                    }
                    DropdownMenu(
                        expanded = showMoveMenu,
                        onDismissRequest = { showMoveMenu = false },
                    ) {
                        allColumns.filter { it.id != task.columnId }.forEach { col ->
                            DropdownMenuItem(
                                text = { Text(col.name) },
                                onClick = {
                                    onMove(col.id)
                                    showMoveMenu = false
                                },
                            )
                        }
                    }
                }
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.height(32.dp).width(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Task") },
            text = { Text("Delete \"${task.title}\"?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }

    if (showEditDialog) {
        TaskEditDialog(
            task = task,
            showRecurringToggle = isDailyProject,
            onSave = { updated -> onEdit(updated) },
            onDismiss = { showEditDialog = false },
        )
    }
}
