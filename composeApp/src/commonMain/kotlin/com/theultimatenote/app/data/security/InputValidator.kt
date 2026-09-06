package com.theultimatenote.app.data.security

object InputValidator {

    const val MAX_EMAIL_LENGTH = 254
    const val MAX_PASSWORD_LENGTH = 128
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_DISPLAY_NAME_LENGTH = 100
    const val MAX_TASK_TITLE_LENGTH = 500
    const val MAX_TASK_DESCRIPTION_LENGTH = 5000
    const val MAX_PROJECT_NAME_LENGTH = 200
    const val MAX_NOTEBOOK_NAME_LENGTH = 200
    const val MAX_PAGE_TITLE_LENGTH = 500
    const val MAX_PAGE_CONTENT_LENGTH = 100_000
    const val MAX_BIO_LENGTH = 1000
    const val MAX_LIST_ITEM_LENGTH = 200
    const val MAX_LIST_ITEMS = 50
    const val MAX_CHAT_MESSAGE_LENGTH = 10_000
    const val MAX_CHECKLIST_ITEMS = 100
    const val MAX_IMAGE_URLS = 20
    const val MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB

    private val EMAIL_REGEX = Regex(
        "^[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\$"
    )

    private val ALLOWED_IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "heic", "heif")

    private val ZERO_WIDTH_CHARS = Regex("[​‌‍﻿­⁠᠎]")

    data class ValidationResult(
        val isValid: Boolean,
        val error: String? = null,
    )

    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        if (trimmed.isBlank()) return ValidationResult(false, "Email is required.")
        if (trimmed.length > MAX_EMAIL_LENGTH) return ValidationResult(false, "Email is too long.")
        if (!EMAIL_REGEX.matches(trimmed)) return ValidationResult(false, "Please enter a valid email address.")
        return ValidationResult(true)
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) return ValidationResult(false, "Password is required.")
        if (password.length < MIN_PASSWORD_LENGTH) {
            return ValidationResult(false, "Password must be at least $MIN_PASSWORD_LENGTH characters.")
        }
        if (password.length > MAX_PASSWORD_LENGTH) {
            return ValidationResult(false, "Password is too long.")
        }
        if (!password.any { it.isUpperCase() }) {
            return ValidationResult(false, "Password must contain at least one uppercase letter.")
        }
        if (!password.any { it.isLowerCase() }) {
            return ValidationResult(false, "Password must contain at least one lowercase letter.")
        }
        if (!password.any { it.isDigit() }) {
            return ValidationResult(false, "Password must contain at least one number.")
        }
        return ValidationResult(true)
    }

    fun validateDisplayName(name: String): ValidationResult {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return ValidationResult(false, "Display name is required.")
        if (trimmed.length > MAX_DISPLAY_NAME_LENGTH) {
            return ValidationResult(false, "Display name is too long (max $MAX_DISPLAY_NAME_LENGTH characters).")
        }
        if (trimmed.length < 2) {
            return ValidationResult(false, "Display name must be at least 2 characters.")
        }
        return ValidationResult(true)
    }

    fun validateTaskTitle(title: String): ValidationResult {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return ValidationResult(false, "Task title is required.")
        if (trimmed.length > MAX_TASK_TITLE_LENGTH) {
            return ValidationResult(false, "Task title is too long (max $MAX_TASK_TITLE_LENGTH characters).")
        }
        return ValidationResult(true)
    }

    fun validateProjectName(name: String): ValidationResult {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return ValidationResult(false, "Project name is required.")
        if (trimmed.length > MAX_PROJECT_NAME_LENGTH) {
            return ValidationResult(false, "Project name is too long (max $MAX_PROJECT_NAME_LENGTH characters).")
        }
        return ValidationResult(true)
    }

    fun validateNotebookName(name: String): ValidationResult {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return ValidationResult(false, "Notebook name is required.")
        if (trimmed.length > MAX_NOTEBOOK_NAME_LENGTH) {
            return ValidationResult(false, "Notebook name is too long (max $MAX_NOTEBOOK_NAME_LENGTH characters).")
        }
        return ValidationResult(true)
    }

    fun validatePageContent(content: String): ValidationResult {
        if (content.length > MAX_PAGE_CONTENT_LENGTH) {
            return ValidationResult(false, "Page content is too long.")
        }
        return ValidationResult(true)
    }

    fun validateChatMessage(message: String): ValidationResult {
        val trimmed = message.trim()
        if (trimmed.isBlank()) return ValidationResult(false, "Message is required.")
        if (trimmed.length > MAX_CHAT_MESSAGE_LENGTH) {
            return ValidationResult(false, "Message is too long (max $MAX_CHAT_MESSAGE_LENGTH characters).")
        }
        return ValidationResult(true)
    }

    fun validateBio(bio: String): ValidationResult {
        if (bio.length > MAX_BIO_LENGTH) {
            return ValidationResult(false, "Bio is too long (max $MAX_BIO_LENGTH characters).")
        }
        return ValidationResult(true)
    }

    fun validateListField(items: List<String>, fieldName: String): ValidationResult {
        if (items.size > MAX_LIST_ITEMS) {
            return ValidationResult(false, "Too many $fieldName (max $MAX_LIST_ITEMS).")
        }
        items.forEachIndexed { index, item ->
            if (item.length > MAX_LIST_ITEM_LENGTH) {
                return ValidationResult(false, "$fieldName item ${index + 1} is too long.")
            }
        }
        return ValidationResult(true)
    }

    fun validateImageFileName(fileName: String): ValidationResult {
        if (fileName.isBlank()) return ValidationResult(false, "File name is required.")
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension !in ALLOWED_IMAGE_EXTENSIONS) {
            return ValidationResult(false, "Unsupported image format. Allowed: ${ALLOWED_IMAGE_EXTENSIONS.joinToString(", ")}.")
        }
        return ValidationResult(true)
    }

    fun validateImageSize(sizeBytes: Long): ValidationResult {
        if (sizeBytes <= 0) return ValidationResult(false, "Invalid file size.")
        if (sizeBytes > MAX_IMAGE_SIZE_BYTES) {
            return ValidationResult(false, "Image is too large (max ${MAX_IMAGE_SIZE_BYTES / (1024 * 1024)} MB).")
        }
        return ValidationResult(true)
    }

    fun sanitizeText(input: String): String {
        return ZERO_WIDTH_CHARS.replace(input, "").trim()
    }
}
