package com.theultimatenote.app.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.io.File

class AndroidConfigSecurityTest {

    // ── ProGuard / Code obfuscation ──

    @Test
    fun proguardRulesFileExists() {
        val proguardFile = findFile("proguard-rules.pro")
        assertTrue(proguardFile != null && proguardFile.exists(), "ProGuard rules file should exist")
    }

    @Test
    fun proguardKeepsDataModels() {
        val content = findFile("proguard-rules.pro")?.readText() ?: ""
        assertTrue(
            content.contains("com.theultimatenote.app.data.model"),
            "ProGuard should keep data model classes for Firestore serialization"
        )
    }

    // ── Build config does not hardcode secrets ──

    @Test
    fun buildGradleDoesNotHardcodeApiKeys() {
        val buildFile = findFile("composeApp/build.gradle.kts")
            ?: findFile("build.gradle.kts")
        val content = buildFile?.readText() ?: ""

        // API keys should come from local.properties or project properties, not hardcoded
        assertFalse(
            content.contains(Regex("\"AIza[A-Za-z0-9_-]{35}\"")),
            "Build file should not contain hardcoded Google API keys"
        )
        assertFalse(
            content.contains(Regex("\"gsk_[A-Za-z0-9]{48,}\"")),
            "Build file should not contain hardcoded Groq API keys"
        )
    }

    @Test
    fun localPropertiesInGitignore() {
        val gitignoreFile = findFile(".gitignore")
        val content = gitignoreFile?.readText() ?: ""
        assertTrue(
            content.contains("local.properties"),
            "local.properties should be in .gitignore to prevent API key leaks"
        )
    }

    @Test
    fun googleServicesJsonInGitignore() {
        val gitignoreFile = findFile(".gitignore")
        val content = gitignoreFile?.readText() ?: ""
        assertTrue(
            content.contains("google-services.json"),
            "google-services.json should be in .gitignore to prevent Firebase config leaks"
        )
    }

    // ── Release build config ──

    @Test
    fun releaseBuildHasMinifyEnabled() {
        val buildFile = findFile("composeApp/build.gradle.kts")
            ?: findFile("build.gradle.kts")
        val content = buildFile?.readText() ?: ""
        assertTrue(
            content.contains("isMinifyEnabled = true"),
            "Release build should have code minification enabled"
        )
    }

    @Test
    fun releaseBuildHasShrinkResources() {
        val buildFile = findFile("composeApp/build.gradle.kts")
            ?: findFile("build.gradle.kts")
        val content = buildFile?.readText() ?: ""
        assertTrue(
            content.contains("isShrinkResources = true"),
            "Release build should shrink resources"
        )
    }

    private fun findFile(name: String): File? {
        val direct = File(name)
        if (direct.exists()) return direct
        val fromParent = File("../$name")
        if (fromParent.exists()) return fromParent
        val fromRoot = File("../../$name")
        if (fromRoot.exists()) return fromRoot
        val cwd = File(".").absoluteFile.parentFile ?: return null
        return cwd.walkTopDown().maxDepth(5)
            .filter { it.path.endsWith(name) }
            .firstOrNull()
    }
}
