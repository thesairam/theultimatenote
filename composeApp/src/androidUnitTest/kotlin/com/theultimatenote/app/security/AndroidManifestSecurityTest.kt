package com.theultimatenote.app.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.io.File

class AndroidManifestSecurityTest {

    private val manifestContent: String by lazy {
        val manifestFile = findManifestFile()
        manifestFile?.readText() ?: error("AndroidManifest.xml not found")
    }

    private fun findManifestFile(): File? {
        val candidates = listOf(
            "src/androidMain/AndroidManifest.xml",
            "../src/androidMain/AndroidManifest.xml",
            "composeApp/src/androidMain/AndroidManifest.xml",
        )
        for (path in candidates) {
            val file = File(path)
            if (file.exists()) return file
        }
        val cwd = File(".").absolutePath
        val searchRoot = File(cwd).parentFile ?: File(".")
        return searchRoot.walkTopDown()
            .filter { it.name == "AndroidManifest.xml" && it.path.contains("androidMain") }
            .firstOrNull()
    }

    @Test
    fun manifestDisablesBackup() {
        // android:allowBackup="true" leaks app data when device is connected via ADB.
        // Should be false for apps storing sensitive user data.
        assertFalse(
            manifestContent.contains("android:allowBackup=\"true\""),
            "android:allowBackup should be false to prevent data exfiltration via ADB backup"
        )
    }

    @Test
    fun manifestDisablesCleartextTraffic() {
        // Ensure no cleartext (HTTP) traffic is allowed.
        // Either usesCleartextTraffic="false" should be present, or it should not be "true".
        assertFalse(
            manifestContent.contains("android:usesCleartextTraffic=\"true\""),
            "Cleartext traffic should not be explicitly enabled"
        )
    }

    @Test
    fun internalReceiversNotExported() {
        // NotificationReceiver should not be exported
        assertTrue(
            manifestContent.contains("NotificationReceiver") &&
                manifestContent.contains("android:exported=\"false\""),
            "Internal receivers should have android:exported=\"false\""
        )
    }

    @Test
    fun manifestDeclaresInternetPermission() {
        assertTrue(
            manifestContent.contains("android.permission.INTERNET"),
            "App needs INTERNET permission for Firebase and API calls"
        )
    }

    @Test
    fun manifestDoesNotRequestDangerousUnnecessaryPermissions() {
        val dangerousPermissions = listOf(
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_CALL_LOG",
            "CAMERA",
            "RECORD_AUDIO",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "READ_PHONE_STATE",
            "SEND_SMS",
            "READ_SMS",
        )
        for (perm in dangerousPermissions) {
            assertFalse(
                manifestContent.contains(perm),
                "Manifest should not request unnecessary dangerous permission: $perm"
            )
        }
    }
}
