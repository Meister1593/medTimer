package com.plyshka.medtimer.widgets

import android.text.SpannableStringBuilder
import android.text.Spanned
import com.plyshka.medtimer.database.ReminderEventRepository
import com.plyshka.medtimer.di.ApplicationScope
import com.plyshka.medtimer.helpers.ReminderStringFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class LatestRemindersLineProvider @Inject constructor(
    private val reminderEventRepository: ReminderEventRepository,
    private val reminderStringFormatter: ReminderStringFormatter,
    @param:ApplicationScope private val scope: CoroutineScope
) : WidgetLineProvider {

    private val reminderEvents = scope.async {
        reminderEventRepository.getLastDays(7).reversed()
    }

    override fun getWidgetLine(
        line: Int,
        isShort: Boolean
    ): Spanned {
        val reminderEvent = runBlocking { reminderEvents.await() }.getOrNull(line)

        return if (reminderEvent != null)
            reminderStringFormatter.formatReminderForWidget(reminderEvent, isShort)
        else SpannableStringBuilder()
    }
}