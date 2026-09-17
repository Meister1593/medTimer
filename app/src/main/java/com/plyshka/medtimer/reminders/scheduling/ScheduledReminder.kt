package com.plyshka.medtimer.reminders.scheduling

import com.plyshka.medtimer.database.FullMedicine
import com.plyshka.medtimer.database.Reminder
import java.time.Instant

data class ScheduledReminder(
    val medicine: FullMedicine,
    val reminder: Reminder,
    val timestamp: Instant
)
