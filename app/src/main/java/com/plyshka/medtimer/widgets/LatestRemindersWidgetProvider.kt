package com.plyshka.medtimer.widgets

import android.content.Context
import com.plyshka.medtimer.R
import javax.inject.Inject


class LatestRemindersWidgetProvider @Inject constructor(private val latestRemindersLineProvider: LatestRemindersLineProvider) : WidgetProvider() {
    override fun getWidgetImpl(context: Context): WidgetImpl {
        return WidgetImpl(
            context,
            latestRemindersLineProvider,
            WidgetIds(
                R.id.latestReminderWidget,
                R.layout.latest_reminders_widget,
                R.layout.latest_reminders_widget_small
            )
        )
    }
}
