package com.plyshka.medtimer.widgets

import android.content.Context
import com.plyshka.medtimer.R
import javax.inject.Inject

class NextRemindersWidgetProvider @Inject constructor(private val nextRemindersLineProvider: NextRemindersLineProvider) : WidgetProvider() {
    override fun getWidgetImpl(context: Context): WidgetImpl {
        return WidgetImpl(
            context,
            nextRemindersLineProvider,
            WidgetIds(
                R.id.nextReminderWidget,
                R.layout.next_reminders_widget,
                R.layout.next_reminders_widget_small
            )
        )
    }
}
