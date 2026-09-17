package com.plyshka.medtimer.preferences

import android.os.Bundle
import com.plyshka.medtimer.R
import com.takisoft.preferencex.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var preferencesDataSource: PreferencesDataSource

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = preferencesDataSource

        setPreferencesFromResource(R.xml.alarm_settings, rootKey)
    }
}