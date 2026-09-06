package com.theultimatenote.app.security

import com.theultimatenote.app.data.security.InputValidator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InputValidationSecurityTest {

    // ── Email Validation ──

    @Test
    fun validEmailAccepted() {
        assertTrue(InputValidator.validateEmail("user@example.com").isValid)
        assertTrue(InputValidator.validateEmail("user.name@domain.co.uk").isValid)
        assertTrue(InputValidator.validateEmail("user+tag@gmail.com").isValid)
    }

    @Test
    fun blankEmailRejected() {
        assertFalse(InputValidator.validateEmail("").isValid)
        assertFalse(InputValidator.validateEmail("   ").isValid)
    }

    @Test
    fun malformedEmailRejected() {
        assertFalse(InputValidator.validateEmail("not-an-email").isValid)
        assertFalse(InputValidator.validateEmail("@missing-local.com").isValid)
        assertFalse(InputValidator.validateEmail("missing-domain@").isValid)
        assertFalse(InputValidator.validateEmail("spaces in@email.com").isValid)
    }

    @Test
    fun oversizedEmailRejected() {
        // RFC 5321 max email length is 254 chars
        val longLocal = "a".repeat(243)
        val longEmail = "$longLocal@example.com" // 243 + 12 = 255 chars
        assertFalse(InputValidator.validateEmail(longEmail).isValid)
    }

    @Test
    fun emailWithScriptInjectionRejected() {
        assertFalse(InputValidator.validateEmail("<script>alert(1)</script>@evil.com").isValid)
    }

    // ── Password Validation ──

    @Test
    fun strongPasswordAccepted() {
        assertTrue(InputValidator.validatePassword("Str0ngPass").isValid)
        assertTrue(InputValidator.validatePassword("MyP4ssword!").isValid)
    }

    @Test
    fun blankPasswordRejected() {
        assertFalse(InputValidator.validatePassword("").isValid)
        assertFalse(InputValidator.validatePassword("   ").isValid)
    }

    @Test
    fun shortPasswordRejected() {
        assertFalse(InputValidator.validatePassword("Ab1").isValid)
        assertFalse(InputValidator.validatePassword("Abcdef1").isValid)
    }

    @Test
    fun passwordWithoutUppercaseRejected() {
        assertFalse(InputValidator.validatePassword("alllowercase1").isValid)
    }

    @Test
    fun passwordWithoutLowercaseRejected() {
        assertFalse(InputValidator.validatePassword("ALLUPPERCASE1").isValid)
    }

    @Test
    fun passwordWithoutDigitRejected() {
        assertFalse(InputValidator.validatePassword("NoDigitsHere").isValid)
    }

    @Test
    fun oversizedPasswordRejected() {
        val longPassword = "Aa1" + "x".repeat(200)
        assertFalse(InputValidator.validatePassword(longPassword).isValid)
    }

    // ── Display Name Validation ──

    @Test
    fun validDisplayNameAccepted() {
        assertTrue(InputValidator.validateDisplayName("John Doe").isValid)
        assertTrue(InputValidator.validateDisplayName("AB").isValid)
    }

    @Test
    fun singleCharDisplayNameRejected() {
        assertFalse(InputValidator.validateDisplayName("A").isValid)
    }

    @Test
    fun blankDisplayNameRejected() {
        assertFalse(InputValidator.validateDisplayName("").isValid)
        assertFalse(InputValidator.validateDisplayName("   ").isValid)
    }

    @Test
    fun oversizedDisplayNameRejected() {
        val longName = "a".repeat(InputValidator.MAX_DISPLAY_NAME_LENGTH + 1)
        assertFalse(InputValidator.validateDisplayName(longName).isValid)
    }

    // ── Task Title Validation ──

    @Test
    fun validTaskTitleAccepted() {
        assertTrue(InputValidator.validateTaskTitle("Buy groceries").isValid)
    }

    @Test
    fun blankTaskTitleRejected() {
        assertFalse(InputValidator.validateTaskTitle("").isValid)
    }

    @Test
    fun oversizedTaskTitleRejected() {
        val longTitle = "a".repeat(InputValidator.MAX_TASK_TITLE_LENGTH + 1)
        assertFalse(InputValidator.validateTaskTitle(longTitle).isValid)
    }

    // ── Project Name Validation ──

    @Test
    fun validProjectNameAccepted() {
        assertTrue(InputValidator.validateProjectName("My Project").isValid)
    }

    @Test
    fun blankProjectNameRejected() {
        assertFalse(InputValidator.validateProjectName("").isValid)
    }

    @Test
    fun oversizedProjectNameRejected() {
        val longName = "a".repeat(InputValidator.MAX_PROJECT_NAME_LENGTH + 1)
        assertFalse(InputValidator.validateProjectName(longName).isValid)
    }

    // ── Chat Message Validation ──

    @Test
    fun validChatMessageAccepted() {
        assertTrue(InputValidator.validateChatMessage("Hello, can you help me?").isValid)
    }

    @Test
    fun blankChatMessageRejected() {
        assertFalse(InputValidator.validateChatMessage("").isValid)
        assertFalse(InputValidator.validateChatMessage("   ").isValid)
    }

    @Test
    fun oversizedChatMessageRejected() {
        val longMessage = "a".repeat(InputValidator.MAX_CHAT_MESSAGE_LENGTH + 1)
        assertFalse(InputValidator.validateChatMessage(longMessage).isValid)
    }

    // ── Bio Validation ──

    @Test
    fun validBioAccepted() {
        assertTrue(InputValidator.validateBio("I love coding.").isValid)
        assertTrue(InputValidator.validateBio("").isValid) // Bio is optional
    }

    @Test
    fun oversizedBioRejected() {
        val longBio = "a".repeat(InputValidator.MAX_BIO_LENGTH + 1)
        assertFalse(InputValidator.validateBio(longBio).isValid)
    }

    // ── List Field Validation ──

    @Test
    fun validListAccepted() {
        assertTrue(InputValidator.validateListField(listOf("item1", "item2"), "hobbies").isValid)
    }

    @Test
    fun tooManyListItemsRejected() {
        val items = (1..InputValidator.MAX_LIST_ITEMS + 1).map { "item$it" }
        assertFalse(InputValidator.validateListField(items, "hobbies").isValid)
    }

    @Test
    fun oversizedListItemRejected() {
        val items = listOf("a".repeat(InputValidator.MAX_LIST_ITEM_LENGTH + 1))
        assertFalse(InputValidator.validateListField(items, "hobbies").isValid)
    }

    // ── Image File Validation ──

    @Test
    fun validImageFileNameAccepted() {
        assertTrue(InputValidator.validateImageFileName("photo.jpg").isValid)
        assertTrue(InputValidator.validateImageFileName("image.png").isValid)
        assertTrue(InputValidator.validateImageFileName("pic.webp").isValid)
    }

    @Test
    fun invalidImageExtensionRejected() {
        assertFalse(InputValidator.validateImageFileName("file.exe").isValid)
        assertFalse(InputValidator.validateImageFileName("script.js").isValid)
        assertFalse(InputValidator.validateImageFileName("doc.pdf").isValid)
        assertFalse(InputValidator.validateImageFileName("shell.sh").isValid)
    }

    @Test
    fun blankImageFileNameRejected() {
        assertFalse(InputValidator.validateImageFileName("").isValid)
    }

    @Test
    fun validImageSizeAccepted() {
        assertTrue(InputValidator.validateImageSize(1024).isValid) // 1 KB
        assertTrue(InputValidator.validateImageSize(5 * 1024 * 1024).isValid) // 5 MB
    }

    @Test
    fun oversizedImageRejected() {
        assertFalse(InputValidator.validateImageSize(InputValidator.MAX_IMAGE_SIZE_BYTES + 1).isValid)
    }

    @Test
    fun zeroSizeImageRejected() {
        assertFalse(InputValidator.validateImageSize(0).isValid)
        assertFalse(InputValidator.validateImageSize(-1).isValid)
    }

    // ── Notebook Validation ──

    @Test
    fun validNotebookNameAccepted() {
        assertTrue(InputValidator.validateNotebookName("My Journal").isValid)
    }

    @Test
    fun blankNotebookNameRejected() {
        assertFalse(InputValidator.validateNotebookName("").isValid)
    }

    @Test
    fun oversizedNotebookNameRejected() {
        val longName = "a".repeat(InputValidator.MAX_NOTEBOOK_NAME_LENGTH + 1)
        assertFalse(InputValidator.validateNotebookName(longName).isValid)
    }

    // ── Page Content Validation ──

    @Test
    fun validPageContentAccepted() {
        assertTrue(InputValidator.validatePageContent("Some content here.").isValid)
        assertTrue(InputValidator.validatePageContent("").isValid) // Empty page is valid
    }

    @Test
    fun oversizedPageContentRejected() {
        val longContent = "a".repeat(InputValidator.MAX_PAGE_CONTENT_LENGTH + 1)
        assertFalse(InputValidator.validatePageContent(longContent).isValid)
    }

    // ── Text Sanitization ──

    @Test
    fun sanitizeRemovesZeroWidthSpaces() {
        val input = "hello​world"
        val result = InputValidator.sanitizeText(input)
        assertFalse(result.contains("​"))
    }
}
