package com.plyshka.medtimer.medicine.tags

import androidx.lifecycle.ViewModel
import com.plyshka.medtimer.database.FullMedicine
import com.plyshka.medtimer.database.MedicineRepository
import com.plyshka.medtimer.database.Tag
import com.plyshka.medtimer.database.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MedicineWithTagsViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val tagRepository: TagRepository
) : ViewModel() {
    fun getMedicineWithTags(medicineId: Int): Flow<FullMedicine?> =
        medicineRepository.getFullFlow(medicineId)

    val tags: Flow<List<Tag>> = tagRepository.getAllFlow()

    suspend fun associateTag(medicineId: Int, tagId: Int) {
        tagRepository.addMedicineTag(medicineId, tagId)
    }

    suspend fun disassociateTag(medicineId: Int, tagId: Int) {
        tagRepository.removeMedicineTag(medicineId, tagId)
    }
}