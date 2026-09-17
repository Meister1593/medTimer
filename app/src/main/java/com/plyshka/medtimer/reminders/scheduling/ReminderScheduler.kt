package com.plyshka.medtimer.reminders.scheduling

import com.plyshka.medtimer.database.FullMedicine
import com.plyshka.medtimer.database.ReminderEvent
import com.plyshka.medtimer.preferences.PreferencesDataSource
import com.plyshka.medtimer.reminders.TimeAccess

class ReminderScheduler(private val timeAccess: TimeAccess, private val dataSource: PreferencesDataSource) {
    fun schedule(fullMedicineWithTagsAndReminders: List<FullMedicine>, reminderEvents: List<ReminderEvent>): List<ScheduledReminder> {
        val scheduledReminders = mutableListOf<ScheduledReminder>()

        for (fullMedicine in fullMedicineWithTagsAndReminders) {

            for (reminder in fullMedicine.reminders) {
                if (!reminder.active) {
                    continue
                }

                val scheduling = SchedulingFactory().create(reminder, fullMedicine.medicine, reminderEvents, timeAccess, dataSource)
                val reminderScheduledTime = scheduling.getNextScheduledTime()

                if (reminderScheduledTime != null) {
                    scheduledReminders.add(ScheduledReminder(fullMedicine, reminder, reminderScheduledTime))
                }
            }
        }

        scheduledReminders.sortWith(Comparator.comparing(ScheduledReminder::timestamp))

        return scheduledReminders
    }

}
