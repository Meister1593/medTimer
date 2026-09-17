package com.plyshka.medtimer.medicine

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import com.plyshka.medtimer.R
import com.plyshka.medtimer.database.Reminder
import com.plyshka.medtimer.database.ReminderRepository
import com.plyshka.medtimer.di.Dispatcher
import com.plyshka.medtimer.di.MedTimerDispatchers
import com.plyshka.medtimer.helpers.DeleteHelper
import com.plyshka.medtimer.helpers.TextInputDialogBuilder
import com.plyshka.medtimer.helpers.TimePickerDialogFactory
import com.google.android.material.timepicker.TimeFormat
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

class LinkedReminderHandling @AssistedInject constructor(
    @Assisted val reminder: Reminder,
    private val reminderRepository: ReminderRepository,
    @Assisted private val coroutineScope: CoroutineScope,
    @param:Dispatcher(MedTimerDispatchers.IO) private val dispatcher: CoroutineDispatcher,
    private val timePickerDialogFactory: TimePickerDialogFactory
) {
    @AssistedFactory
    interface Factory {
        fun create(reminder: Reminder, coroutineScope: CoroutineScope): LinkedReminderHandling
    }

    fun addLinkedReminder(fragmentActivity: FragmentActivity) {
        TextInputDialogBuilder(fragmentActivity).title(R.string.add_linked_reminder)
            .hint(R.string.create_reminder_dosage_hint).textSink { amount: String? ->
                this.createReminder(
                    fragmentActivity,
                    amount!!
                )
            }.show()
    }

    private fun createReminder(fragmentActivity: FragmentActivity, amount: String) {
        val linkedReminder = Reminder(reminder.medicineRelId).apply {
            this.amount = amount
            createdTimestamp = Instant.now().toEpochMilli() / 1000
            cycleStartDay = LocalDate.now().plusDays(1).toEpochDay()
            instructions = ""
            linkedReminderId = reminder.reminderId
        }

        timePickerDialogFactory.create(0, 0, R.string.linked_reminder_delay, TimeFormat.CLOCK_24H) { minutes: Int ->
            coroutineScope.launch {
                linkedReminder.timeInMinutes = minutes
                reminderRepository.create(linkedReminder)
                fragmentActivity.supportFragmentManager.popBackStack()
            }
        }.show(fragmentActivity.supportFragmentManager, TimePickerDialogFactory.DIALOG_TAG)
    }

    fun deleteReminder(context: Context, postYesAction: () -> Unit, postNoAction: () -> Unit) {
        DeleteHelper.deleteItem(context, R.string.are_you_sure_delete_reminder, {
            coroutineScope.launch(dispatcher) {
                internalDelete(reminder)
                Handler(Looper.getMainLooper()).post(postYesAction)
            }
        }, { Handler(Looper.getMainLooper()).post(postNoAction) })
    }

    private suspend fun internalDelete(
        reminder: Reminder
    ) {
        val reminders: List<Reminder> =
            reminderRepository.getLinked(reminder.reminderId)
        for (r in reminders) {
            internalDelete(r)
        }

        reminderRepository.delete(reminder.reminderId)
    }
}

