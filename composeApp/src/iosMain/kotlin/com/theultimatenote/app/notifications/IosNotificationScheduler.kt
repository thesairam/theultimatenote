package com.theultimatenote.app.notifications

import com.theultimatenote.app.data.repository.NotificationScheduler
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

class IosNotificationScheduler : NotificationScheduler {

    init {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
            completionHandler = { _, _ -> },
        )
    }

    override fun scheduleTaskReminder(taskId: String, taskTitle: String, hour: Int, minute: Int) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Task reminder")
            setBody(taskTitle)
        }

        val dateComponents = NSDateComponents().apply {
            this.hour = hour.toLong()
            this.minute = minute.toLong()
        }
        // Repeats daily at the given hour/minute, matching the Android reminder behavior.
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = true,
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = taskId,
            content = content,
            trigger = trigger,
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request, null)
    }

    override fun cancelTaskReminder(taskId: String) {
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(listOf(taskId))
    }
}
