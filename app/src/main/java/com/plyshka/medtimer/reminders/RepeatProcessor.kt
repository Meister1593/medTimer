package com.plyshka.medtimer.reminders

import android.util.Log
import com.plyshka.medtimer.LogTags
import com.plyshka.medtimer.database.ReminderEventRepository
import com.plyshka.medtimer.reminders.notificationData.ReminderNotificationData
import javax.inject.Inject
import kotlin.time.Duration

class RepeatProcessor @Inject constructor(
    private val alarmProcessor: AlarmProcessor,
    private val reminderEventRepository: ReminderEventRepository,
    private val timeAccess: TimeAccess
) {
    suspend fun processRepeat(reminderNotificationData: ReminderNotificationData, repeatDelay: Duration) {
        reminderNotificationData.remindInstant = timeAccess.now().plusSeconds(repeatDelay.inWholeSeconds)

        Log.d(LogTags.REMINDER, "Repeating reminder $reminderNotificationData")
        alarmProcessor.setAlarmForReminderNotification(reminderNotificationData)

        for (reminderEventId in reminderNotificationData.reminderEventIds) {
            decreaseRemainingRepeats(reminderEventId)
        }
    }

    private suspend fun decreaseRemainingRepeats(reminderEventId: Int) {
        val reminderEvent = reminderEventRepository.get(reminderEventId) ?: return
        reminderEvent.remainingRepeats -= 1
        reminderEventRepository.update(reminderEvent)
    }
}
