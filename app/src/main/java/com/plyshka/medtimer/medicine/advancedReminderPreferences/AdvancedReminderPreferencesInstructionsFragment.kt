package com.plyshka.medtimer.medicine.advancedReminderPreferences

import com.plyshka.medtimer.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdvancedReminderPreferencesInstructionsFragment : AdvancedReminderPreferencesFragment(
    R.xml.advanced_reminder_settings_instructions,
    mapOf(),
    mapOf(),
    listOf("instructions")
)