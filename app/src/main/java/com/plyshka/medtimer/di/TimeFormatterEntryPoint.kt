package com.plyshka.medtimer.di

import com.plyshka.medtimer.helpers.TimeFormatter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Entry point for instrumented tests to access TimeFormatter without @HiltAndroidTest.
// Must live in the main source set so it is installed in the real app's SingletonComponent.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface TimeFormatterEntryPoint {
    fun timeFormatter(): TimeFormatter
}
