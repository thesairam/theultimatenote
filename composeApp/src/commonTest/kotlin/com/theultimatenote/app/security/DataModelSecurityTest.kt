package com.theultimatenote.app.security

import com.theultimatenote.app.data.model.Project
import com.theultimatenote.app.data.model.ProjectType
import com.theultimatenote.app.data.model.Task
import com.theultimatenote.app.data.model.TaskStatus
import com.theultimatenote.app.data.model.User
import com.theultimatenote.app.data.model.Notebook
import com.theultimatenote.app.data.model.NotebookPage
import com.theultimatenote.app.data.model.ChecklistItem
import com.theultimatenote.app.data.model.KanbanBoard
import com.theultimatenote.app.data.model.KanbanColumn
import com.theultimatenote.app.data.model.SubscriptionInfo
import com.theultimatenote.app.data.model.SubscriptionTier
import com.theultimatenote.app.data.model.ChatMessage
import com.theultimatenote.app.data.model.ChatAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DataModelSecurityTest {

    // ── Default values should be safe ──

    @Test
    fun userDefaultsToEmptyNonNullFields() {
        val user = User()
        assertEquals("", user.id)
        assertEquals("", user.email)
        assertEquals("", user.displayName)
        assertEquals(null, user.photoUrl)
        assertEquals("", user.bio)
        assertTrue(user.hobbies.isEmpty())
        assertTrue(user.areasOfFocus.isEmpty())
        assertTrue(user.skills.isEmpty())
        assertTrue(user.idols.isEmpty())
        assertTrue(user.goals.isEmpty())
    }

    @Test
    fun taskDefaultsToSafeState() {
        val task = Task()
        assertEquals("", task.id)
        assertEquals("", task.title)
        assertEquals(TaskStatus.TODO, task.status)
        assertFalse(task.isRecurring)
        assertFalse(task.isCompletedToday)
        assertFalse(task.isUrgent)
        assertFalse(task.isImportant)
        assertTrue(task.checklist.isEmpty())
        assertTrue(task.imageUrls.isEmpty())
    }

    @Test
    fun projectDefaultsToRegularDeletableType() {
        val project = Project()
        assertEquals(ProjectType.REGULAR, project.type)
        assertTrue(project.isDeletable)
        assertFalse(project.isCompleted)
        assertFalse(project.isStarred)
    }

    @Test
    fun specialProjectsAreNotDeletable() {
        val daily = Project(type = ProjectType.DAILY, isDeletable = false)
        val learning = Project(type = ProjectType.LEARNING, isDeletable = false)
        assertFalse(daily.isDeletable)
        assertFalse(learning.isDeletable)
    }

    @Test
    fun notebookDefaultsToSafeState() {
        val notebook = Notebook()
        assertEquals("", notebook.id)
        assertEquals("", notebook.name)
        assertEquals(null, notebook.projectId)
    }

    @Test
    fun notebookPageDefaultsToSafeState() {
        val page = NotebookPage()
        assertEquals("", page.id)
        assertEquals("", page.title)
        assertEquals("", page.content)
        assertTrue(page.imageUrls.isEmpty())
    }

    @Test
    fun kanbanBoardDefaultsEmpty() {
        val board = KanbanBoard()
        assertTrue(board.columns.isEmpty())
    }

    @Test
    fun chatMessageDefaultsToUserRole() {
        val msg = ChatMessage()
        assertEquals("user", msg.role)
        assertEquals("", msg.content)
        assertTrue(msg.actions.isEmpty())
    }

    @Test
    fun chatActionDefaultsToEmptyType() {
        val action = ChatAction()
        assertEquals("", action.type)
        assertFalse(action.executed)
        assertFalse(action.isRecurring)
    }

    // ── Subscription defaults to FREE ──

    @Test
    fun subscriptionDefaultsToFreeTier() {
        val sub = SubscriptionInfo()
        assertEquals(SubscriptionTier.FREE, sub.subscriptionTier)
        assertEquals(0L, sub.expiresAt)
    }

    @Test
    fun subscriptionHandlesInvalidTierGracefully() {
        val sub = SubscriptionInfo(tier = "INVALID_TIER")
        assertEquals(SubscriptionTier.FREE, sub.subscriptionTier)
    }

    @Test
    fun subscriptionHandlesEmptyTierGracefully() {
        val sub = SubscriptionInfo(tier = "")
        assertEquals(SubscriptionTier.FREE, sub.subscriptionTier)
    }

    // ── Data immutability ──

    @Test
    fun taskCopyPreservesAllFields() {
        val original = Task(
            id = "t1",
            title = "Test",
            description = "Desc",
            status = TaskStatus.IN_PROGRESS,
            projectId = "p1",
            columnId = "c1",
            isRecurring = true,
            isCompletedToday = true,
            checklist = listOf(ChecklistItem(id = "cl1", text = "item", isChecked = true)),
        )
        val copy = original.copy(title = "Updated")
        assertEquals("Updated", copy.title)
        assertEquals(original.id, copy.id)
        assertEquals(original.status, copy.status)
        assertEquals(original.isRecurring, copy.isRecurring)
        assertEquals(original.checklist.size, copy.checklist.size)
    }

    @Test
    fun projectCopyPreservesSecurityFields() {
        val original = Project(
            id = "p1",
            name = "Test",
            type = ProjectType.DAILY,
            ownerId = "user1",
            isDeletable = false,
        )
        val copy = original.copy(name = "Updated")
        assertEquals(false, copy.isDeletable)
        assertEquals(ProjectType.DAILY, copy.type)
        assertEquals("user1", copy.ownerId)
    }

    // ── Enum safety ──

    @Test
    fun taskStatusEnumCoversExpectedValues() {
        val values = TaskStatus.entries
        assertTrue(values.contains(TaskStatus.TODO))
        assertTrue(values.contains(TaskStatus.IN_PROGRESS))
        assertTrue(values.contains(TaskStatus.DONE))
    }

    @Test
    fun projectTypeEnumCoversExpectedValues() {
        val values = ProjectType.entries
        assertTrue(values.contains(ProjectType.REGULAR))
        assertTrue(values.contains(ProjectType.DAILY))
        assertTrue(values.contains(ProjectType.LEARNING))
    }

    @Test
    fun subscriptionTierEnumCoversExpectedValues() {
        val values = SubscriptionTier.entries
        assertTrue(values.contains(SubscriptionTier.FREE))
        assertTrue(values.contains(SubscriptionTier.PRO))
    }
}
