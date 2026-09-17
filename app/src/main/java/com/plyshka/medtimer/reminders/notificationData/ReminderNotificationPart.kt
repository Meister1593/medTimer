package com.plyshka.medtimer.reminders.notificationData

import com.plyshka.medtimer.database.FullMedicine
import com.plyshka.medtimer.database.Reminder
import com.plyshka.medtimer.database.ReminderEvent

class ReminderNotificationPart(val reminder: Reminder, val reminderEvent: ReminderEvent, val medicine: FullMedicine)