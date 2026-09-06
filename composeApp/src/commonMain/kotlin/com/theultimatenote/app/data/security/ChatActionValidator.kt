package com.theultimatenote.app.data.security

object ChatActionValidator {

    private val ALLOWED_ACTION_TYPES = setOf("create_task", "create_project")
    private val TIME_REGEX = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    fun isValidActionType(type: String): Boolean {
        return type in ALLOWED_ACTION_TYPES
    }

    fun isValidScheduledTime(time: String?): Boolean {
        if (time == null) return true
        return TIME_REGEX.matches(time)
    }

    fun isValidProjectName(name: String): Boolean {
        return name.isNotBlank() && name.length <= InputValidator.MAX_PROJECT_NAME_LENGTH
    }

    fun isValidTaskTitle(title: String): Boolean {
        return title.isNotBlank() && title.length <= InputValidator.MAX_TASK_TITLE_LENGTH
    }
}
