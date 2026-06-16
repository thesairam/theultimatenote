package com.theultimatenote.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class KanbanBoard(
    val id: String = "",
    val projectId: String = "",
    val columns: List<KanbanColumn> = emptyList(),
)

@Serializable
data class KanbanColumn(
    val id: String = "",
    val name: String = "",
    val order: Int = 0,
    val taskIds: List<String> = emptyList(),
)
