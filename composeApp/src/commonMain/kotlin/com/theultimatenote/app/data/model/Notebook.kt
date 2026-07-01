package com.theultimatenote.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Notebook(
    val id: String = "",
    val name: String = "",
    val projectId: String? = null,
    val ownerId: String = "",
    val createdAt: Long = 0L,
)

@Serializable
data class NotebookPage(
    val id: String = "",
    val notebookId: String = "",
    val title: String = "",
    val content: String = "",
    val order: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val imageUrls: List<String> = emptyList(),
)
