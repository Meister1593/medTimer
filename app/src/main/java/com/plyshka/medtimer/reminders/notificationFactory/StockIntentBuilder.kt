package com.plyshka.medtimer.reminders.notificationFactory

import android.app.PendingIntent
import android.content.Context
import com.plyshka.medtimer.reminders.getAcknowledgedActionIntent
import com.plyshka.medtimer.reminders.getRefillActionIntent
import com.plyshka.medtimer.reminders.notificationData.ProcessedNotificationData
import com.plyshka.medtimer.reminders.notificationData.ReminderNotification

class StockIntentBuilder(private val context: Context, private val reminderNotification: ReminderNotification) {
    val processedNotificationData = ProcessedNotificationData.fromReminderNotificationData(reminderNotification.reminderNotificationData)

    val pendingAcknowledged = getAcknowledgedPendingIntent()
    val pendingRefill = getRefillPendingIntent()

    private fun getAcknowledgedPendingIntent(): PendingIntent {
        val notifyAcknowledged = getAcknowledgedActionIntent(context, processedNotificationData)
        return PendingIntent.getBroadcast(
            context,
            reminderNotification.reminderNotificationData.notificationId,
            notifyAcknowledged,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getRefillPendingIntent(): PendingIntent {
        val notifyRefill = getRefillActionIntent(context, processedNotificationData)
        return PendingIntent.getBroadcast(
            context,
            reminderNotification.reminderNotificationData.notificationId,
            notifyRefill,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
