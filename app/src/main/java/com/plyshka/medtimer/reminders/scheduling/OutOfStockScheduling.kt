package com.plyshka.medtimer.reminders.scheduling

import com.plyshka.medtimer.database.Medicine
import com.plyshka.medtimer.database.Reminder
import com.plyshka.medtimer.database.ReminderEvent
import com.plyshka.medtimer.reminders.TimeAccess
import java.time.Instant

class OutOfStockScheduling(
    reminder: Reminder,
    val medicine: Medicine,
    reminderEventList: List<ReminderEvent>,
    timeAccess: TimeAccess
) : SchedulingBase(reminder, reminderEventList, timeAccess) {
    override fun getNextScheduledTime(): Instant? {
        if (reminder.outOfStockReminderType == Reminder.OutOfStockReminderType.DAILY && medicine.amount <= reminder.outOfStockThreshold) {
            return getNextNotRemindedDay()
        }
        return null
    }
}