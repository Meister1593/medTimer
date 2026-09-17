package com.plyshka.medtimer.medicine.advancedReminderPreferences

import android.content.Context
import com.plyshka.medtimer.R
import com.plyshka.medtimer.database.Reminder
import com.plyshka.medtimer.helpers.Interval
import com.plyshka.medtimer.medicine.editors.IntervalEditor
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditIntervalDialog(context: Context, reminder: Reminder, private val intervalUpdatedCallback: (Int) -> Unit) {
    private val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(R.string.interval)
        .setPositiveButton(android.R.string.ok) { _, _ -> intervalUpdatedCallback(intervalEditor.getMinutes()) }
        .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
        .setView(R.layout.include_edit_interval)
        .create()

    init {
        dialog.show()
    }

    private val intervalEditor: IntervalEditor = IntervalEditor(
        dialog.requireViewById(R.id.editIntervalTime),
        dialog.requireViewById(R.id.editIntervalTimeLayout),
        dialog.requireViewById(R.id.intervalUnit), reminder.timeInMinutes,
        if (reminder.reminderType == Reminder.ReminderType.WINDOWED_INTERVAL) 24 * 60 else Interval.MAX_INTERVAL_MINUTES
    )
}