package com.theultimatenote.app.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class KanbanBoard(
    val id: String = "",
    val projectId: String = "",
    val columns: List<KanbanColumn> = emptyList(),
)

@Immutable
@Serializable
data class KanbanColumn(
    val id: String = "",
    val name: String = "",
    val order: Int = 0,
    val taskIds: List<String> = emptyList(),
)
