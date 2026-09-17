package com.plyshka.medtimer.medicine.stockSettings

import android.content.Context
import com.plyshka.medtimer.database.FullMedicine
import com.plyshka.medtimer.database.MedicineRepository
import com.plyshka.medtimer.di.ApplicationScope
import com.plyshka.medtimer.helpers.EntityDataStore
import com.plyshka.medtimer.helpers.MedicineHelper
import com.plyshka.medtimer.helpers.TimeFormatter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MedicineDataStore @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted override var entity: FullMedicine,
    private val medicineRepository: MedicineRepository,
    private val timeFormatter: TimeFormatter,
    @param:ApplicationScope private val coroutineScope: CoroutineScope
) : EntityDataStore<FullMedicine>() {

    @AssistedFactory
    interface Factory {
        fun create(context: Context, entity: FullMedicine): MedicineDataStore
    }

    override val entityId: Int get() = entity.medicine.medicineId

    override fun getString(key: String?, defValue: String?): String? {
        return when (key) {
            "type" -> MedicineHelper.formatType(entity.medicine.type, context)
            "amount" -> MedicineHelper.formatAmount(entity.medicine.amount, "")
            "stock_unit" -> entity.medicine.unit
            "stock_refill_size" -> MedicineHelper.formatAmount(entity.medicine.refillSize, "")
            "production_date" -> timeFormatter.daysSinceEpochToDateString(entity.medicine.productionDate)
            "expiration_date" -> timeFormatter.daysSinceEpochToDateString(entity.medicine.expirationDate)
            else -> defValue
        }
    }

    override fun putString(key: String?, value: String?) {
        when (key) {
            "type" -> entity.medicine.type = MedicineHelper.parseType(value)!!
            "amount" -> MedicineHelper.parseAmount(value)?.let { entity.medicine.amount = it }
            "stock_unit" -> entity.medicine.unit = value!!
            "stock_refill_size" -> MedicineHelper.parseAmount(value)
                ?.let { entity.medicine.refillSizes = arrayListOf(it) }

            "production_date" -> entity.medicine.productionDate =
                timeFormatter.stringToLocalDate(value!!)!!.toEpochDay()

            "expiration_date" -> entity.medicine.expirationDate =
                timeFormatter.stringToLocalDate(value!!)!!.toEpochDay()
        }
        coroutineScope.launch {
            medicineRepository.update(entity.medicine)
        }
    }

    override fun putLong(key: String?, value: Long) {
        when (key) {
            "production_date" -> entity.medicine.productionDate = value
            "expiration_date" -> entity.medicine.expirationDate = value
        }
        coroutineScope.launch {
            medicineRepository.update(entity.medicine)
        }
    }
}
