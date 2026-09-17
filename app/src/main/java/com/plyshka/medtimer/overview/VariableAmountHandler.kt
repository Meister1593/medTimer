package com.plyshka.medtimer.overview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.plyshka.medtimer.R
import com.plyshka.medtimer.database.ReminderEvent
import com.plyshka.medtimer.database.ReminderEventRepository
import com.plyshka.medtimer.di.Dispatcher
import com.plyshka.medtimer.di.MedTimerDispatchers
import com.plyshka.medtimer.helpers.TextInputDialogBuilder
import com.plyshka.medtimer.reminders.NotificationProcessor
import com.plyshka.medtimer.reminders.notificationData.ReminderNotificationData
import com.plyshka.medtimer.reminders.notificationData.ReminderNotificationFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class VariableAmountHandler @Inject constructor(
    private val reminderEventRepository: ReminderEventRepository,
    private val notificationProcessor: NotificationProcessor,
    private val reminderNotificationFactory: ReminderNotificationFactory,
    @param:Dispatcher(MedTimerDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun show(activity: AppCompatActivity, intent: Intent) {
        val reminderNotificationData = ReminderNotificationData.fromBundle(intent.extras!!)

        val reminderNotification = withContext(ioDispatcher) {
            reminderNotificationFactory.create(reminderNotificationData)
        } ?: return

        val reminderEvents = mutableListOf<ReminderEvent>()

        for (reminderNotificationPart in reminderNotification.reminderNotificationParts.reversed()) {
            if (!reminderNotificationPart.reminder.variableAmount) {
                reminderEvents.add(reminderNotificationPart.reminderEvent)
                continue
            }

            TextInputDialogBuilder(activity)
                .title(reminderNotificationPart.medicine.medicine.name)
                .hint(R.string.dosage)
                .initialText(reminderNotificationPart.reminder.amount)
                .textSink { amountLocal: String? ->
                    amountLocal?.let {
                        reminderNotificationPart.reminderEvent.amount = it
                        activity.lifecycleScope.launch(ioDispatcher) {
                            notificationProcessor.setReminderEventStatus(
                                ReminderEvent.ReminderStatus.TAKEN,
                                listOf(reminderNotificationPart.reminderEvent)
                            )
                        }
                    }
                }
                .cancelCallback {
                    activity.lifecycleScope.launch(ioDispatcher) {
                        touchReminderEvent(reminderNotificationPart.reminderEvent)
                    }
                }
                .show()
        }

        withContext(ioDispatcher) {
            notificationProcessor.setReminderEventStatus(
                ReminderEvent.ReminderStatus.TAKEN,
                reminderEvents,
            )
        }
    }

    private suspend fun touchReminderEvent(reminderEvent: ReminderEvent) {
        reminderEvent.processedTimestamp = Instant.now().epochSecond
        reminderEventRepository.update(reminderEvent)
    }
}
