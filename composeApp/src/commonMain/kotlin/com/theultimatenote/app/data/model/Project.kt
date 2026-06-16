package com.theultimatenote.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProjectType {
    REGULAR,
    DAILY,
    LEARNING,
}

@Serializable
data class Project(
    val id: String = "",
    val name: String = "",
    val type: ProjectType = ProjectType.REGULAR,
    val ownerId: String = "",
    val notebookId: String = "",
    val createdAt: Long = 0L,
    val isDeletable: Boolean = true,
)
