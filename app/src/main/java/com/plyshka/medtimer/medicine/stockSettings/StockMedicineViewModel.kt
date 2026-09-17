package com.plyshka.medtimer.medicine.stockSettings

import com.plyshka.medtimer.database.FullMedicine
import com.plyshka.medtimer.database.MedicineRepository
import com.plyshka.medtimer.helpers.EntityViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class StockMedicineViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository
) : EntityViewModel<FullMedicine>() {

    override fun getFlow(id: Int): Flow<FullMedicine?> = medicineRepository.getFullFlow(id)
}
