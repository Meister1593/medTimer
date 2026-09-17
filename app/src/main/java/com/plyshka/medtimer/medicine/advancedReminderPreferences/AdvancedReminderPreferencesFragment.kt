package com.plyshka.medtimer.medicine.advancedReminderPreferences

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.preference.Preference
import com.plyshka.medtimer.database.Reminder
import com.plyshka.medtimer.database.ReminderRepository
import com.plyshka.medtimer.helpers.EntityDataStore
import com.plyshka.medtimer.helpers.EntityPreferencesFragment
import com.plyshka.medtimer.helpers.EntityViewModel
import javax.inject.Inject

abstract class AdvancedReminderPreferencesFragment(
    preferencesResId: Int,
    links: Map<String, (Int) -> NavDirections>,
    customOnClick: Map<String, (FragmentActivity, Preference) -> Unit>,
    simpleSummaryKeys: List<String>
) : EntityPreferencesFragment<Reminder>(preferencesResId, links, customOnClick, simpleSummaryKeys) {
    @Inject
    lateinit var reminderDataStoreFactory: ReminderDataStore.Factory

    @Inject
    lateinit var reminderRepository: ReminderRepository

    override suspend fun getEntityDataStore(
        requireArguments: Bundle
    ): EntityDataStore<Reminder> {
        val entityId = requireArguments.getInt("reminderId")
        val entity = reminderRepository.get(entityId)!!

        return reminderDataStoreFactory.create(entity)
    }

    private val reminderViewModel: ReminderViewModel by viewModels()

    override fun getEntityViewModel(): EntityViewModel<Reminder> = reminderViewModel

    override fun customSetup(entity: Reminder) {
        // Intentionally empty
    }
}