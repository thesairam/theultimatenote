package com.theultimatenote.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.theultimatenote.app.notifications.NotificationHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannels(this)
        NotificationHelper.scheduleMorningMotivation(this)
        requestNotificationPermission()
        scheduleRecurringTaskReminders()

        setContent {
            App()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun scheduleRecurringTaskReminders() {
        lifecycleScope.launch {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
            val db = FirebaseFirestore.getInstance()
            val projects = db.collection("projects")
                .whereEqualTo("ownerId", user.uid)
                .whereEqualTo("type", "DAILY")
                .get().await()

            projects.documents.forEach { projectDoc ->
                val tasks = projectDoc.reference.collection("tasks")
                    .whereEqualTo("isRecurring", true)
                    .get().await()

                tasks.documents.forEach { taskDoc ->
                    val time = taskDoc.getString("scheduledTime") ?: return@forEach
                    val parts = time.split(":")
                    if (parts.size == 2) {
                        val hour = parts[0].toIntOrNull() ?: return@forEach
                        val minute = parts[1].toIntOrNull() ?: return@forEach
                        NotificationHelper.scheduleTaskReminder(
                            this@MainActivity,
                            taskDoc.id,
                            taskDoc.getString("title") ?: "Task reminder",
                            hour,
                            minute,
                        )
                    }
                }
            }
        }
    }
}
