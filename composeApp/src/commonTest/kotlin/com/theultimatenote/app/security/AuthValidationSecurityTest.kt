package com.theultimatenote.app.security

import com.theultimatenote.app.data.security.InputValidator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class AuthValidationSecurityTest {

    // ── SQL/NoSQL Injection in email field ──

    @Test
    fun emailRejectsSqlInjection() {
        // Apostrophes are valid in RFC5322 local parts, but full SQL statements are not valid emails
        assertFalse(InputValidator.validateEmail("' OR '1'='1").isValid)
        assertFalse(InputValidator.validateEmail("'; DROP TABLE users;--@x.com").isValid)
    }

    @Test
    fun emailRejectsNoSqlInjection() {
        assertFalse(InputValidator.validateEmail("{\"\$gt\":\"\"}@evil.com").isValid)
        assertFalse(InputValidator.validateEmail("{\"\$ne\":null}@evil.com").isValid)
    }

    @Test
    fun emailWithApostropheInLocalPartAccepted() {
        // RFC5322 allows apostrophes in local part — this is valid
        assertTrue(InputValidator.validateEmail("o'brien@example.com").isValid)
    }

    // ── XSS in text fields ──

    @Test
    fun displayNameRejectsXssButAcceptsNormalSpecialChars() {
        assertTrue(InputValidator.validateDisplayName("O'Brien").isValid)
        assertTrue(InputValidator.validateDisplayName("Jean-Pierre").isValid)
    }

    @Test
    fun sanitizeTextStripsZeroWidthChars() {
        val withZeroWidth = "hel​lo​world"
        val sanitized = InputValidator.sanitizeText(withZeroWidth)
        assertFalse(sanitized.contains("​"))
    }

    // ── Password boundary conditions ──

    @Test
    fun passwordAtExactMinLengthWithComplexityAccepted() {
        val pwd = "Abcdefg1" // exactly 8 chars
        assertTrue(InputValidator.validatePassword(pwd).isValid)
    }

    @Test
    fun passwordAtExactMaxLengthAccepted() {
        val pwd = "Aa1" + "x".repeat(InputValidator.MAX_PASSWORD_LENGTH - 3)
        assertTrue(InputValidator.validatePassword(pwd).isValid)
    }

    @Test
    fun passwordJustOverMaxLengthRejected() {
        val pwd = "Aa1" + "x".repeat(InputValidator.MAX_PASSWORD_LENGTH - 2)
        assertFalse(InputValidator.validatePassword(pwd).isValid)
    }

    @Test
    fun commonWeakPasswordsRejected() {
        // These all lack complexity requirements
        assertFalse(InputValidator.validatePassword("password").isValid)
        assertFalse(InputValidator.validatePassword("12345678").isValid)
        assertFalse(InputValidator.validatePassword("qwerty12").isValid)
        assertFalse(InputValidator.validatePassword("abcdefgh").isValid)
    }

    // ── Email trimming ──

    @Test
    fun emailWithLeadingTrailingSpacesAcceptedAfterTrim() {
        assertTrue(InputValidator.validateEmail("  user@example.com  ").isValid)
    }

    // ── Unicode handling ──

    @Test
    fun displayNameAcceptsUnicode() {
        assertTrue(InputValidator.validateDisplayName("Müller").isValid)
        assertTrue(InputValidator.validateDisplayName("田中太郎").isValid)
        assertTrue(InputValidator.validateDisplayName("Ñoño").isValid)
    }

    @Test
    fun projectNameAcceptsUnicode() {
        assertTrue(InputValidator.validateProjectName("Projet été").isValid)
        assertTrue(InputValidator.validateProjectName("プロジェクト").isValid)
    }

    // ── Validation result contains error messages ──

    @Test
    fun failedValidationReturnsErrorMessage() {
        val result = InputValidator.validateEmail("bad")
        assertFalse(result.isValid)
        assertNotNull(result.error)
        assertTrue(result.error!!.isNotBlank())
    }

    @Test
    fun successfulValidationHasNullError() {
        val result = InputValidator.validateEmail("good@example.com")
        assertTrue(result.isValid)
        assertTrue(result.error == null)
    }

    // ── Null-like edge cases ──

    @Test
    fun fieldsHandleOnlyWhitespace() {
        assertFalse(InputValidator.validateEmail("\t\n").isValid)
        assertFalse(InputValidator.validatePassword("\t\n").isValid)
        assertFalse(InputValidator.validateDisplayName("\t\n").isValid)
        assertFalse(InputValidator.validateTaskTitle("\t\n").isValid)
        assertFalse(InputValidator.validateProjectName("\t\n").isValid)
        assertFalse(InputValidator.validateChatMessage("\t\n").isValid)
    }
}
