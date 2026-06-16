package com.theultimatenote.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val bio: String = "",
    val hobbies: List<String> = emptyList(),
    val areasOfFocus: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val idols: List<String> = emptyList(),
    val goals: List<String> = emptyList(),
    val currentLearningFocus: String = "",
)
