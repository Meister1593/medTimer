package com.plyshka.medtimer.overview.actions

import com.plyshka.medtimer.database.ReminderEvent
import com.plyshka.medtimer.database.ReminderEventRepository
import com.plyshka.medtimer.di.Dispatcher
import com.plyshka.medtimer.di.MedTimerDispatchers
import com.plyshka.medtimer.helpers.TimeFormatter
import com.plyshka.medtimer.reminders.ReminderNotificationProcessor
import com.plyshka.medtimer.reminders.scheduling.ScheduledReminder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderEventCreator @Inject constructor(
    private val reminderEventRepository: ReminderEventRepository,
    private val timeFormatter: TimeFormatter,
    @param:Dispatcher(MedTimerDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getOrCreateReminderEvent(scheduledReminder: ScheduledReminder, reminderTimeStamp: Long): ReminderEvent = withContext(ioDispatcher) {
        var reminderEvent = reminderEventRepository.get(scheduledReminder.reminder.reminderId, scheduledReminder.timestamp.epochSecond)
        if (reminderEvent != null) {
            return@withContext reminderEvent
        }

        reminderEvent = ReminderNotificationProcessor.buildReminderEvent(
            reminderTimeStamp, scheduledReminder.medicine, scheduledReminder.reminder, reminderEventRepository, timeFormatter
        )
        reminderEvent.reminderEventId = reminderEventRepository.create(reminderEvent).toInt()
        return@withContext reminderEvent
    }
}
