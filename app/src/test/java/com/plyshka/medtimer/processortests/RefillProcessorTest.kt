package com.plyshka.medtimer.processortests

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.SharedPreferences
import com.plyshka.medtimer.database.DatabaseManager
import com.plyshka.medtimer.database.MedicineDao
import com.plyshka.medtimer.database.MedicineRepository
import com.plyshka.medtimer.database.MedicineRoomDatabase
import com.plyshka.medtimer.database.Reminder
import com.plyshka.medtimer.database.ReminderDao
import com.plyshka.medtimer.database.ReminderEvent
import com.plyshka.medtimer.database.ReminderEventDao
import com.plyshka.medtimer.database.ReminderEventRepository
import com.plyshka.medtimer.database.ReminderRepository
import com.plyshka.medtimer.database.TagDao
import com.plyshka.medtimer.database.TagRepository
import com.plyshka.medtimer.di.DatabaseModule
import com.plyshka.medtimer.di.DatastoreModule
import com.plyshka.medtimer.di.TimeAccessModule
import com.plyshka.medtimer.preferences.PersistentDataDataSource
import com.plyshka.medtimer.preferences.PreferencesDataSource
import com.plyshka.medtimer.reminders.RefillProcessor
import com.plyshka.medtimer.reminders.TimeAccess
import com.plyshka.medtimer.reminders.notificationData.ProcessedNotificationData
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
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@UninstallModules(
    DatabaseModule::class,
    DatastoreModule::class,
    TimeAccessModule::class
)
class RefillProcessorTest {
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
    val boundMedicineRepository: MedicineRepository = reminderContext.repositoryFakes.medicineRepositoryMock

    @BindValue
    val boundReminderRepository: ReminderRepository = reminderContext.repositoryFakes.reminderRepositoryMock

    @BindValue
    val boundReminderEventRepository: ReminderEventRepository = reminderContext.repositoryFakes.reminderEventRepositoryMock

    @BindValue
    val boundPreferencesDataSource: PreferencesDataSource = reminderContext.preferencesDataSourceMock

    @BindValue
    val boundPersistentDataDataSource: PersistentDataDataSource = reminderContext.persistentDataDataSourceMock

    @BindValue
    val boundTimeAccess: TimeAccess = object : TimeAccess {
        override fun systemZone(): ZoneId = ZoneId.of("UTC")
        override fun localDate(): LocalDate = reminderContext.localDate
        override fun now(): Instant = reminderContext.instant
    }

    @BindValue
    val boundMedicineRoomDatabase: MedicineRoomDatabase = mock()

    @BindValue
    val boundMedicineDao: MedicineDao = mock()

    @BindValue
    val boundReminderDao: ReminderDao = mock()

    @BindValue
    val boundReminderEventDao: ReminderEventDao = mock()

    @BindValue
    val boundTagDao: TagDao = mock()

    @BindValue
    val boundTagRepository: TagRepository = mock()

    @BindValue
    val boundDatabaseManager: DatabaseManager = mock()

    @BindValue
    @com.plyshka.medtimer.di.DefaultPreferences
    val boundDefaultSharedPreferences: SharedPreferences = mock()

    @BindValue
    @com.plyshka.medtimer.di.MedTimerPreferencess
    val boundMedTimerSharedPreferences: SharedPreferences = mock()

    @Inject
    lateinit var refillProcessor: RefillProcessor

    @Test
    fun directRefill() {
        reminderContext.instant = Instant.ofEpochSecond(10)

        reminderContext.repositoryFakes.medicines.add(TestHelper.buildFullMedicine(1, "Test").medicine)
        reminderContext.repositoryFakes.medicines[0].refillSizes.add(10.0)
        reminderContext.repositoryFakes.medicines[0].amount = 100.0

        runBlocking {
            refillProcessor.processRefill(1)
        }

        assertEquals(110.0, reminderContext.repositoryFakes.medicines[0].amount)
        assertEquals(10, reminderContext.repositoryFakes.reminderEvents[0].processedTimestamp)
        assertEquals(10, reminderContext.repositoryFakes.reminderEvents[0].remindedTimestamp)
        assertEquals("100 ➡ 110", reminderContext.repositoryFakes.reminderEvents[0].amount)
        assertEquals(Reminder.ReminderType.REFILL, reminderContext.repositoryFakes.reminderEvents[0].reminderType)
    }

    @Test
    fun refillViaEvent() {
        reminderContext.repositoryFakes.medicines.add(TestHelper.buildFullMedicine(1, "Test").medicine)
        reminderContext.repositoryFakes.medicines[0].refillSizes.add(10.0)
        reminderContext.repositoryFakes.medicines[0].amount = 100.0
        reminderContext.repositoryFakes.reminders.add(TestHelper.buildReminder(1, 1, "1", 0, 1))
        reminderContext.repositoryFakes.reminderEvents.add(TestHelper.buildReminderEvent(1, 0, 1))

        runBlocking {
            refillProcessor.processRefill(ProcessedNotificationData(listOf(1)))
        }

        assertEquals(110.0, reminderContext.repositoryFakes.medicines[0].amount)
        assertEquals(ReminderEvent.ReminderStatus.ACKNOWLEDGED, reminderContext.repositoryFakes.reminderEvents[0].status)
    }
}