package com.plyshka.medtimer.reminders

import android.util.Log
import com.plyshka.medtimer.LogTags
import com.plyshka.medtimer.database.FullMedicine
import com.plyshka.medtimer.database.MedicineRepository
import com.plyshka.medtimer.database.Reminder
import com.plyshka.medtimer.database.ReminderEvent
import com.plyshka.medtimer.database.ReminderEventRepository
import com.plyshka.medtimer.database.ReminderRepository
import com.plyshka.medtimer.database.Tag
import com.plyshka.medtimer.helpers.MedicineHelper
import com.plyshka.medtimer.reminders.notificationData.ProcessedNotificationData
import java.util.stream.Collectors
import javax.inject.Inject

class RefillProcessor @Inject constructor(
    private val notificationProcessor: NotificationProcessor,
    private val medicineRepository: MedicineRepository,
    private val reminderRepository: ReminderRepository,
    private val reminderEventRepository: ReminderEventRepository,
    private val timeAccess: TimeAccess
) {
    suspend fun processRefill(processedNotificationData: ProcessedNotificationData) {
        val reminderEvent = reminderEventRepository.get(processedNotificationData.reminderEventIds[0])!!
        val reminder = reminderRepository.get(reminderEvent.reminderId)
        val medicineId = reminder!!.medicineRelId
        processRefill(medicineId, reminderEvent)
    }

    suspend fun processRefill(medicineId: Int, reminderEvent: ReminderEvent? = null) {
        val medicine = medicineRepository.getFull(medicineId)!!

        val refillEvent = processRefillInternal(medicine)
        medicineRepository.update(medicine.medicine)
        reminderEventRepository.create(refillEvent)

        if (reminderEvent != null) {
            notificationProcessor.processReminderEventsInNotification(
                ProcessedNotificationData.fromReminderEvents(listOf(reminderEvent)),
                ReminderEvent.ReminderStatus.ACKNOWLEDGED
            )
        }
    }

    private fun processRefillInternal(medicine: FullMedicine): ReminderEvent {
        medicine.medicine.amount += medicine.medicine.refillSize
        Log.d(LogTags.STOCK_HANDLING, "Refill medicine ${medicine.medicine.name} with ${medicine.medicine.refillSize} to amount ${medicine.medicine.amount}")

        return buildReminderEvent(medicine)
    }

    fun buildReminderEvent(medicine: FullMedicine): ReminderEvent {
        val reminderEvent = ReminderEvent()
        reminderEvent.reminderId = -1
        reminderEvent.remindedTimestamp = timeAccess.now().toEpochMilli() / 1000
        reminderEvent.processedTimestamp = reminderEvent.remindedTimestamp
        reminderEvent.medicineName = medicine.medicine.name
        reminderEvent.color = medicine.medicine.color
        reminderEvent.useColor = medicine.medicine.useColor
        reminderEvent.status = ReminderEvent.ReminderStatus.ACKNOWLEDGED
        reminderEvent.iconId = medicine.medicine.iconId
        reminderEvent.askForAmount = false
        reminderEvent.tags = medicine.tags.stream().map { t: Tag? -> t!!.name }.collect((Collectors.toList()))
        reminderEvent.reminderType = Reminder.ReminderType.REFILL

        reminderEvent.amount = "${MedicineHelper.formatAmount(medicine.medicine.amount - medicine.medicine.refillSize, medicine.medicine.unit)} ➡ ${
            MedicineHelper.formatAmount(
                medicine.medicine.amount,
                medicine.medicine.unit
            )
        }"

        return reminderEvent
    }
}
