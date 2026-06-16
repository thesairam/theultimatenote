package com.theultimatenote.app.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.theultimatenote.app.R
import java.util.Calendar

object NotificationHelper {

    private const val CHANNEL_TASKS = "task_reminders"
    private const val CHANNEL_MOTIVATION = "motivation"
    private const val MORNING_ALARM_ID = 9999

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val taskChannel = NotificationChannel(
            CHANNEL_TASKS,
            "Task Reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Reminders for scheduled tasks"
        }

        val motivationChannel = NotificationChannel(
            CHANNEL_MOTIVATION,
            "Morning Motivation",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Daily motivational quotes"
        }

        manager.createNotificationChannels(listOf(taskChannel, motivationChannel))
    }

    fun showTaskReminder(context: Context, taskTitle: String, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_TASKS)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Task Reminder")
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
    }

    fun showMotivationQuote(context: Context, quote: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_MOTIVATION)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Good Morning!")
            .setContentText(quote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(quote))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(MORNING_ALARM_ID, notification)
    }

    fun scheduleMorningMotivation(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "MORNING_MOTIVATION"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, MORNING_ALARM_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent,
        )
    }

    fun scheduleTaskReminder(context: Context, taskId: String, taskTitle: String, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationId = taskId.hashCode()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "TASK_REMINDER"
            putExtra("task_title", taskTitle)
            putExtra("notification_id", notificationId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent,
        )
    }

    val motivationQuotes = listOf(
        "The only way to do great work is to love what you do. - Steve Jobs",
        "It does not matter how slowly you go as long as you do not stop. - Confucius",
        "Success is not final, failure is not fatal: it is the courage to continue that counts. - Winston Churchill",
        "Believe you can and you're halfway there. - Theodore Roosevelt",
        "The future belongs to those who believe in the beauty of their dreams. - Eleanor Roosevelt",
        "In the middle of difficulty lies opportunity. - Albert Einstein",
        "What you get by achieving your goals is not as important as what you become by achieving your goals. - Zig Ziglar",
        "The best time to plant a tree was 20 years ago. The second best time is now. - Chinese Proverb",
        "Your limitation—it's only your imagination.",
        "Push yourself, because no one else is going to do it for you.",
        "Great things never come from comfort zones.",
        "Dream it. Wish it. Do it.",
        "Stay focused and never give up.",
        "The harder you work for something, the greater you'll feel when you achieve it.",
    )
}
