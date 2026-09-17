package com.plyshka.medtimer.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.plyshka.medtimer.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RepeatRemindersPreferencesFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var preferencesDataSource: PreferencesDataSource

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = preferencesDataSource

        setPreferencesFromResource(R.xml.repeat_reminders_preferences, rootKey)
    }
}
