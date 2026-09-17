package com.plyshka.medtimer.widgets

import android.text.SpannableStringBuilder
import android.text.Spanned
import com.plyshka.medtimer.database.MedicineRepository
import com.plyshka.medtimer.database.ReminderEventRepository
import com.plyshka.medtimer.di.ApplicationScope
import com.plyshka.medtimer.helpers.ReminderStringFormatter
import com.plyshka.medtimer.preferences.PreferencesDataSource
import com.plyshka.medtimer.reminders.TimeAccess
import com.plyshka.medtimer.reminders.scheduling.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class NextRemindersLineProvider @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val reminderEventRepository: ReminderEventRepository,
    private val preferencesDataSource: PreferencesDataSource,
    private val timeAccess: TimeAccess,
    private val reminderStringFormatter: ReminderStringFormatter,
    @param:ApplicationScope private val scope: CoroutineScope
) : WidgetLineProvider {
    private val scheduledReminders = scope.async {
        val medicinesWithReminders = medicineRepository.getFullAll()
        val reminderEvents = reminderEventRepository.getForScheduling(medicinesWithReminders)
        val reminderScheduler = ReminderScheduler(timeAccess, preferencesDataSource)

        reminderScheduler.schedule(medicinesWithReminders, reminderEvents)
    }

    override fun getWidgetLine(
        line: Int,
        isShort: Boolean
    ): Spanned {
        val scheduledReminder = runBlocking { scheduledReminders.await() }.getOrNull(line)

        return if (scheduledReminder != null)
            reminderStringFormatter.formatScheduledReminderForWidget(scheduledReminder, isShort)
        else SpannableStringBuilder()
    }
}