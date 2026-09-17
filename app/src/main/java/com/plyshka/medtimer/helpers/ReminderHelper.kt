package com.plyshka.medtimer.helpers

import com.plyshka.medtimer.database.FullMedicine
import com.plyshka.medtimer.database.Reminder
import com.plyshka.medtimer.database.ReminderRepository
import java.time.LocalDate


fun isReminderActive(reminder: Reminder): Boolean {
    var active = reminder.active
    if (reminder.periodStart != 0L) {
        active = active && LocalDate.now().toEpochDay() >= reminder.periodStart
    }
    if (reminder.periodEnd != 0L) {
        active = active && LocalDate.now().toEpochDay() <= reminder.periodEnd
    }
    return active
}

fun getActiveReminders(medicine: FullMedicine): List<Reminder> {
    return medicine.reminders.filter { isReminderActive(it) }
}

suspend fun setRemindersActive(reminders: List<Reminder>, reminderRepository: ReminderRepository, active: Boolean) {
    for (reminder in reminders) {
        if (!reminder.active && active && reminder.reminderType == Reminder.ReminderType.CONTINUOUS_INTERVAL) {
            // If reminder is activated again and an interval reminder, reset the interval start date to the current day in seconds since epoch
            reminder.intervalStart = TimeHelper.changeTimeStampDate(reminder.intervalStart, LocalDate.now())
        }
        reminder.active = active
    }
    reminderRepository.updateAll(reminders)
}
