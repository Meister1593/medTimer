package com.plyshka.medtimer.di

import android.content.Context
import androidx.room.Room
import com.plyshka.medtimer.database.DatabaseManager
import com.plyshka.medtimer.database.MedicineDao
import com.plyshka.medtimer.database.MedicineRepository
import com.plyshka.medtimer.database.MedicineRoomDatabase
import com.plyshka.medtimer.database.MedicineRoomDatabase.Migration22To23
import com.plyshka.medtimer.database.ReminderDao
import com.plyshka.medtimer.database.ReminderEventDao
import com.plyshka.medtimer.database.ReminderEventRepository
import com.plyshka.medtimer.database.ReminderRepository
import com.plyshka.medtimer.database.TagDao
import com.plyshka.medtimer.database.TagRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMedicineRoomDatabase(@ApplicationContext context: Context): MedicineRoomDatabase =
        Room.databaseBuilder(context, MedicineRoomDatabase::class.java, "medTimer")
            .addMigrations(Migration22To23)
            .build()

    @Provides
    fun provideMedicineDao(database: MedicineRoomDatabase): MedicineDao =
        database.medicineDao()

    @Provides
    fun provideReminderDao(database: MedicineRoomDatabase): ReminderDao =
        database.reminderDao()

    @Provides
    fun provideReminderEventDao(database: MedicineRoomDatabase): ReminderEventDao =
        database.reminderEventDao()

    @Provides
    fun provideTagDao(database: MedicineRoomDatabase): TagDao =
        database.tagDao()

    @Provides
    @Singleton
    fun provideMedicineRepository(
        medicineDao: MedicineDao,
        tagDao: TagDao
    ): MedicineRepository = MedicineRepository(medicineDao, tagDao)

    @Provides
    @Singleton
    fun provideReminderRepository(
        reminderDao: ReminderDao
    ): ReminderRepository = ReminderRepository(reminderDao)

    @Provides
    @Singleton
    fun provideReminderEventRepository(
        reminderEventDao: ReminderEventDao
    ): ReminderEventRepository = ReminderEventRepository(reminderEventDao)

    @Provides
    @Singleton
    fun provideTagRepository(
        tagDao: TagDao
    ): TagRepository = TagRepository(tagDao)

    @Provides
    @Singleton
    fun provideDatabaseManager(
        medicineRepository: MedicineRepository,
        reminderRepository: ReminderRepository,
        reminderEventRepository: ReminderEventRepository,
        tagRepository: TagRepository
    ): DatabaseManager = DatabaseManager(medicineRepository, reminderRepository, reminderEventRepository, tagRepository)
}
