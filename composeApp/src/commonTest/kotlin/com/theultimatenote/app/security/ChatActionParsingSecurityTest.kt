package com.theultimatenote.app.security

import com.theultimatenote.app.data.security.ChatActionValidator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class ChatActionParsingSecurityTest {

    // ── Action type validation ──

    @Test
    fun validActionTypesAccepted() {
        assertTrue(ChatActionValidator.isValidActionType("create_task"))
        assertTrue(ChatActionValidator.isValidActionType("create_project"))
    }

    @Test
    fun unknownActionTypesRejected() {
        assertFalse(ChatActionValidator.isValidActionType("delete_all"))
        assertFalse(ChatActionValidator.isValidActionType("admin_reset"))
        assertFalse(ChatActionValidator.isValidActionType("export_data"))
        assertFalse(ChatActionValidator.isValidActionType(""))
    }

    @Test
    fun actionTypeInjectionRejected() {
        assertFalse(ChatActionValidator.isValidActionType("create_task; rm -rf /"))
        assertFalse(ChatActionValidator.isValidActionType("create_task\"; DROP TABLE"))
    }

    // ── Scheduled time validation ──

    @Test
    fun validScheduledTimeAccepted() {
        assertTrue(ChatActionValidator.isValidScheduledTime("09:00"))
        assertTrue(ChatActionValidator.isValidScheduledTime("23:59"))
        assertTrue(ChatActionValidator.isValidScheduledTime("00:00"))
    }

    @Test
    fun invalidScheduledTimeRejected() {
        assertFalse(ChatActionValidator.isValidScheduledTime("25:00"))
        assertFalse(ChatActionValidator.isValidScheduledTime("12:60"))
        assertFalse(ChatActionValidator.isValidScheduledTime("abc"))
        assertFalse(ChatActionValidator.isValidScheduledTime(""))
        assertFalse(ChatActionValidator.isValidScheduledTime("9:00")) // not zero-padded
    }

    @Test
    fun nullScheduledTimeAccepted() {
        assertTrue(ChatActionValidator.isValidScheduledTime(null))
    }

    // ── Project name from AI response ──

    @Test
    fun oversizedProjectNameFromAiRejected() {
        val longName = "a".repeat(500)
        assertFalse(ChatActionValidator.isValidProjectName(longName))
    }

    @Test
    fun emptyProjectNameRejected() {
        assertFalse(ChatActionValidator.isValidProjectName(""))
    }

    @Test
    fun normalProjectNameAccepted() {
        assertTrue(ChatActionValidator.isValidProjectName("My Project"))
    }

    // ── Task title from AI response ──

    @Test
    fun oversizedTaskTitleFromAiRejected() {
        val longTitle = "a".repeat(1000)
        assertFalse(ChatActionValidator.isValidTaskTitle(longTitle))
    }

    @Test
    fun emptyTaskTitleRejected() {
        assertFalse(ChatActionValidator.isValidTaskTitle(""))
    }

    @Test
    fun normalTaskTitleAccepted() {
        assertTrue(ChatActionValidator.isValidTaskTitle("Buy groceries"))
    }
}
