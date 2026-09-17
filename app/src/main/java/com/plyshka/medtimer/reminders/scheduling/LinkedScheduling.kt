package com.plyshka.medtimer.reminders.scheduling

import com.plyshka.medtimer.database.Reminder
import com.plyshka.medtimer.database.ReminderEvent
import com.plyshka.medtimer.reminders.TimeAccess
import java.time.Instant

class LinkedScheduling(
    reminder: Reminder,
    reminderEventList: List<ReminderEvent>,
    timeAccess: TimeAccess
) : SchedulingBase(reminder, reminderEventList, timeAccess) {
    override fun getNextScheduledTime(): Instant? {
        val lastSourceReminderEvent: ReminderEvent? =
            findLastReminderEvent(reminder.linkedReminderId)
        val lastReminderEvent: ReminderEvent? =
            findLastReminderEvent()
        if (lastSourceReminderEvent != null &&
            lastSourceReminderEvent.processedTimestamp != 0L &&
            (lastReminderEvent == null
                    || lastSourceReminderEvent.processedTimestamp > lastReminderEvent.remindedTimestamp)
        ) {
            return Instant.ofEpochSecond(lastSourceReminderEvent.processedTimestamp).plusSeconds(
                reminder.timeInMinutes * 60L
            )
        }
        return null
    }

}
