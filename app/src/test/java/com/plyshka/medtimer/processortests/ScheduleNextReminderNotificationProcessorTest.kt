package com.plyshka.medtimer.processortests

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import com.plyshka.medtimer.di.DatabaseModule
import com.plyshka.medtimer.di.DatastoreModule
import com.plyshka.medtimer.di.TimeAccessModule
import com.plyshka.medtimer.reminders.ScheduleNextReminderNotificationProcessor
import com.plyshka.medtimer.schedulertests.TestHelper
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@UninstallModules(
    DatabaseModule::class,
    DatastoreModule::class,
    TimeAccessModule::class
)
class ScheduleNextReminderNotificationProcessorTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    val reminderContext = TestReminderContext()

    @BindValue
    val boundAlarmManager: AlarmManager = reminderContext.alarmManagerMock

    @BindValue
    val boundNotificationManager: NotificationManager = reminderContext.notificationManagerFake.mock

    @BindValue
    val boundMedicineRepository: com.plyshka.medtimer.database.MedicineRepository = reminderContext.repositoryFakes.medicineRepositoryMock

    @BindValue
    val boundReminderRepository: com.plyshka.medtimer.database.ReminderRepository = reminderContext.repositoryFakes.reminderRepositoryMock

    @BindValue
    val boundReminderEventRepository: com.plyshka.medtimer.database.ReminderEventRepository = reminderContext.repositoryFakes.reminderEventRepositoryMock

    @BindValue
    val boundPreferencesDataSource: com.plyshka.medtimer.preferences.PreferencesDataSource = reminderContext.preferencesDataSourceMock

    @BindValue
    val boundPersistentDataDataSource: com.plyshka.medtimer.preferences.PersistentDataDataSource = reminderContext.persistentDataDataSourceMock

    @BindValue
    val boundTimeAccess: com.plyshka.medtimer.reminders.TimeAccess = object : com.plyshka.medtimer.reminders.TimeAccess {
        override fun systemZone(): java.time.ZoneId = java.time.ZoneId.of("UTC")
        override fun localDate(): java.time.LocalDate = reminderContext.localDate
        override fun now(): java.time.Instant = reminderContext.instant
    }

    @BindValue
    val boundMedicineRoomDatabase: com.plyshka.medtimer.database.MedicineRoomDatabase = org.mockito.Mockito.mock()

    @BindValue
    val boundMedicineDao: com.plyshka.medtimer.database.MedicineDao = org.mockito.Mockito.mock()

    @BindValue
    val boundReminderDao: com.plyshka.medtimer.database.ReminderDao = org.mockito.Mockito.mock()

    @BindValue
    val boundReminderEventDao: com.plyshka.medtimer.database.ReminderEventDao = org.mockito.Mockito.mock()

    @BindValue
    val boundTagDao: com.plyshka.medtimer.database.TagDao = org.mockito.Mockito.mock()

    @BindValue
    val boundTagRepository: com.plyshka.medtimer.database.TagRepository = org.mockito.Mockito.mock()

    @BindValue
    val boundDatabaseManager: com.plyshka.medtimer.database.DatabaseManager = org.mockito.Mockito.mock()

    @BindValue
    @com.plyshka.medtimer.di.DefaultPreferences
    val boundDefaultSharedPreferences: android.content.SharedPreferences = org.mockito.Mockito.mock()

    @BindValue
    @com.plyshka.medtimer.di.MedTimerPreferencess
    val boundMedTimerSharedPreferences: android.content.SharedPreferences = org.mockito.Mockito.mock()

    @Inject
    lateinit var scheduleNextReminderNotificationProcessor: ScheduleNextReminderNotificationProcessor

    @Test
    fun noReminder() {
        runBlocking {
            scheduleNextReminderNotificationProcessor.scheduleNextReminder()
        }

        verify(reminderContext.alarmManagerMock, times(1)).cancel(any<PendingIntent>())
        verify(reminderContext.alarmManagerMock, never()).setAndAllowWhileIdle(anyInt(), anyLong(), any())
    }

    @Test
    fun scheduleReminder() {
        reminderContext.repositoryFakes.medicines.add(TestHelper.buildFullMedicine(1, "Test").medicine)
        reminderContext.repositoryFakes.reminders.add(TestHelper.buildReminder(1, 1, "1", 600, 1))

        runBlocking {
            scheduleNextReminderNotificationProcessor.scheduleNextReminder()
        }

        verify(reminderContext.alarmManagerMock, times(1)).setAndAllowWhileIdle(anyInt(), eq(600 * 60 * 1000L), any())
    }
}