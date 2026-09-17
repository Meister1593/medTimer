package com.plyshka.medtimer.reminders

import android.util.Log
import com.plyshka.medtimer.LogTags
import com.plyshka.medtimer.database.FullMedicine
import com.plyshka.medtimer.database.MedicineRepository
import com.plyshka.medtimer.database.ReminderEvent
import com.plyshka.medtimer.database.ReminderEventRepository
import com.plyshka.medtimer.preferences.PreferencesDataSource
import com.plyshka.medtimer.reminders.notificationData.ReminderNotificationData
import com.plyshka.medtimer.reminders.scheduling.ReminderScheduler
import com.plyshka.medtimer.reminders.scheduling.ScheduledReminder
import javax.inject.Inject

class ScheduleNextReminderNotificationProcessor @Inject constructor(
    private val alarmProcessor: AlarmProcessor,
    private val medicineRepository: MedicineRepository,
    private val reminderEventRepository: ReminderEventRepository,
    private val timeAccess: TimeAccess,
    private val preferencesDataSource: PreferencesDataSource
) {

    suspend fun scheduleNextReminder(processedEvents: List<ReminderEvent> = emptyList()) {
        val fullMedicines = medicineRepository.getFullAll()
        val reminderEvents = reminderEventRepository.getForScheduling(fullMedicines)
        val allEvents = (reminderEvents + processedEvents).distinctBy { it.reminderEventId }

        scheduleNextReminderInternal(fullMedicines, allEvents)
    }

    private fun scheduleNextReminderInternal(
        fullMedicines: List<FullMedicine>,
        reminderEvents: List<ReminderEvent>
    ) {
        val reminderScheduler = ReminderScheduler(timeAccess, preferencesDataSource)
        val scheduledReminders: List<ScheduledReminder> =
            reminderScheduler.schedule(fullMedicines, reminderEvents)
        if (scheduledReminders.isNotEmpty()) {
            val scheduledReminderNotificationData =
                ReminderNotificationData.fromScheduledReminders(
                    if (preferencesDataSource.preferences.value.combineNotifications) scheduledReminders else listOf(
                        scheduledReminders[0]
                    )
                )
            alarmProcessor.setAlarmForReminderNotification(scheduledReminderNotificationData)
        } else {
            Log.d(LogTags.REMINDER, "No reminders scheduled")
            alarmProcessor.cancelNextReminder()
        }
    }
}
