package com.plyshka.medtimer

object ActivityCodes {
    const val EXTRA_SNOOZE_TIME: String = "com.plyshka.medtimer.SNOOZE_TIME"
    const val EXTRA_NOTIFICATION_ID: String = "com.plyshka.medtimer.NOTIFICATION_ID"
    const val EXTRA_AMOUNT: String = "com.plyshka.medtimer.AMOUNT"
    const val EXTRA_MEDICINE_ID: String = "com.plyshka.medtimer.MEDICINE_ID"
    const val EXTRA_REMINDER_EVENT_ID_LIST: String = "com.plyshka.medtimer.REMINDER_EVENT_ID_LIST"
    const val EXTRA_REMINDER_ID_LIST: String = "com.plyshka.medtimer.REMINDER_ID_LIST"
    const val EXTRA_REMIND_INSTANT: String = "com.plyshka.medtimer.REMIND_INSTANT"

    const val REMOTE_INPUT_SNOOZE_ACTION: String = "com.plyshka.medtimer.REMOTE_INPUT_SNOOZE_ACTION"
    const val REMOTE_INPUT_VARIABLE_AMOUNT_ACTION: String = "com.plyshka.medtimer.REMOTE_INPUT_VARIABLE_AMOUNT_ACTION"

    const val VARIABLE_AMOUNT_ACTIVITY: String = "com.plyshka.medtimer.VARIABLE_AMOUNT_ACTIVITY"
    const val CUSTOM_SNOOZE_ACTIVITY: String = "com.plyshka.medtimer.CUSTOM_SNOOZE_ACTIVITY"
}

enum class ProcessorCode(val action: String) {
    Reminder("com.plyshka.medtimer.REMINDER_ACTION"),
    Dismissed("com.plyshka.medtimer.DISMISSED_ACTION"),
    Taken("com.plyshka.medtimer.TAKEN_ACTION"),
    Snooze("com.plyshka.medtimer.SNOOZE_ACTION"),
    Acknowledged("com.plyshka.medtimer.ACKNOWLEDGED_ACTION"),
    Refill("com.plyshka.medtimer.REFILL_ACTION"),
    ShowReminderNotification("com.plyshka.medtimer.SHOW_REMINDER_NOTIFICATION"),
    StockHandling("com.plyshka.medtimer.STOCK_HANDLING"),
    Schedule("com.plyshka.medtimer.SCHEDULE");

    companion object {
        private val actionMap = entries.associateBy { it.action }

        fun fromAction(action: String?): ProcessorCode? = actionMap[action]
    }
}
