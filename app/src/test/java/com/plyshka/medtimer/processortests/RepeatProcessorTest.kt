package com.plyshka.medtimer.processortests

import android.app.AlarmManager
import android.app.NotificationManager
import com.plyshka.medtimer.di.DatabaseModule
import com.plyshka.medtimer.di.DatastoreModule
import com.plyshka.medtimer.di.TimeAccessModule
import com.plyshka.medtimer.reminders.RepeatProcessor
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@UninstallModules(
    DatabaseModule::class,
    DatastoreModule::class,
    TimeAccessModule::class
)
class RepeatProcessorTest {
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
    lateinit var repeatProcessor: RepeatProcessor

    @Test
    fun repeat() {
        val reminderNotificationData = fillWithTwoReminders(reminderContext)

        runBlocking {
            repeatProcessor.processRepeat(reminderNotificationData, 10.seconds)
        }

        assertEquals(reminderContext.repositoryFakes.reminderEvents[0].remainingRepeats, -1)
        assertEquals(reminderContext.repositoryFakes.reminderEvents[1].remainingRepeats, -1)
        verify(reminderContext.alarmManagerMock, times(1)).setAndAllowWhileIdle(anyInt(), eq(10_000L), any())
    }
}