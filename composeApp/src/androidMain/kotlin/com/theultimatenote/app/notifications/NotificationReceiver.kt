package com.theultimatenote.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "MORNING_MOTIVATION" -> {
                val quote = NotificationHelper.motivationQuotes.random()
                NotificationHelper.showMotivationQuote(context, quote)
            }
            "TASK_REMINDER" -> {
                val title = intent.getStringExtra("task_title") ?: "You have a task!"
                val id = intent.getIntExtra("notification_id", 0)
                NotificationHelper.showTaskReminder(context, title, id)
            }
        }
    }
}
