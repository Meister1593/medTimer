package com.plyshka.medtimer.medicine.advancedReminderPreferences

import com.plyshka.medtimer.database.Reminder
import com.plyshka.medtimer.database.ReminderRepository
import com.plyshka.medtimer.helpers.EntityViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : EntityViewModel<Reminder>() {

    override fun getFlow(id: Int): Flow<Reminder?> = reminderRepository.getFlow(id)
}
