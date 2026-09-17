package com.plyshka.medtimer.database

open class DatabaseManager(
    private val medicineRepository: MedicineRepository,
    private val reminderRepository: ReminderRepository,
    private val reminderEventRepository: ReminderEventRepository,
    private val tagRepository: TagRepository
) {
    suspend fun deleteAll() {
        reminderRepository.deleteAll()
        medicineRepository.deleteAll()
        reminderEventRepository.deleteAll()
        tagRepository.deleteAll()
    }
}
