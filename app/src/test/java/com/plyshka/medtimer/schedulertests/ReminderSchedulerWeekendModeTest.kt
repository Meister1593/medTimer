package com.plyshka.medtimer.schedulertests

import com.plyshka.medtimer.database.FullMedicine
import com.plyshka.medtimer.model.UserPreferences
import com.plyshka.medtimer.reminders.TimeAccess
import com.plyshka.medtimer.schedulertests.ReminderSchedulerUnitTest.Companion.getScheduler
import com.plyshka.medtimer.schedulertests.ReminderSchedulerUnitTest.Companion.scheduler
import com.plyshka.medtimer.schedulertests.TestHelper.assertReminded
import com.plyshka.medtimer.schedulertests.TestHelper.buildFullMedicine
import com.plyshka.medtimer.schedulertests.TestHelper.buildReminder
import com.plyshka.medtimer.schedulertests.TestHelper.on
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.mockito.Mockito
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

internal class ReminderSchedulerWeekendModeTest {
    @Test
    fun weekendDaysEmpty() {
        val scheduler = scheduler

        val userPreferences = UserPreferences.default().copy(weekendTime = LocalTime.of(10, 0), weekendMode = true)
        val stateFlow = MutableStateFlow(userPreferences)

        Mockito.`when`(
            ReminderSchedulerUnitTest.preferencesDataSource.preferences
        ).thenReturn(stateFlow)

        val medicineWithReminders1 =
            buildFullMedicine(1, ReminderSchedulerUnitTest.TEST_1)
        val reminder1 = buildReminder(1, 1, "1", 16, 1)
        medicineWithReminders1.reminders.add(reminder1)

        val medicineList = mutableListOf<FullMedicine>()
        medicineList.add(medicineWithReminders1)
        val scheduledReminders = scheduler.schedule(medicineList, emptyList())
        assertReminded(scheduledReminders, on(1, 16), medicineWithReminders1.medicine, reminder1)
    }

    @Test
    fun weekendMode() {
        // 1.1.1970 is a Thursday
        val mockTimeAccess: TimeAccess = Mockito.mock()
        Mockito.`when`(mockTimeAccess.systemZone()).thenReturn(ZoneId.of("Z"))
        Mockito.`when`(mockTimeAccess.localDate()).thenReturn(LocalDate.EPOCH)
        val scheduler = getScheduler(mockTimeAccess)

        val userPreferences = UserPreferences.default().copy(
            weekendTime = LocalTime.of(10, 0),
            weekendMode = true,
            weekendDays = setOf(DayOfWeek.SATURDAY.value.toString(), DayOfWeek.SUNDAY.value.toString())
        )
        val stateFlow = MutableStateFlow(userPreferences)

        Mockito.`when`(
            ReminderSchedulerUnitTest.preferencesDataSource.preferences
        ).thenReturn(stateFlow)

        val medicineWithReminders1 =
            buildFullMedicine(1, ReminderSchedulerUnitTest.TEST_1)
        val reminder1 = buildReminder(1, 1, "1", 16, 1)
        medicineWithReminders1.reminders.add(reminder1)
        val medicineList = mutableListOf<FullMedicine>()
        medicineList.add(medicineWithReminders1)

        var scheduledReminders = scheduler.schedule(medicineList, emptyList())
        assertReminded(scheduledReminders, on(1, 16), medicineWithReminders1.medicine, reminder1)

        Mockito.`when`(mockTimeAccess.localDate()).thenReturn(LocalDate.EPOCH.plusDays(2))

        scheduledReminders = scheduler.schedule(medicineList, emptyList())
        assertReminded(
            scheduledReminders, on(3, 10 * 60), medicineWithReminders1.medicine, reminder1
        )
    }
}
