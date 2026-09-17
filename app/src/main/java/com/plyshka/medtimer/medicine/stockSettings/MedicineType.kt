package com.plyshka.medtimer.medicine.stockSettings

import android.content.Context
import com.plyshka.medtimer.R

enum class MedicineType(val value: Int) {
    PILL(0), GEL(1), PATCH(2), SINGLE_USE_INJECTION_VIAL(3), MULTI_USE_INJECTION_VIAL(4);

    companion object {
        private val actionMap = entries.associateBy { it.value }

        fun fromType(type: Int?): MedicineType? = actionMap[type]
    }
}

fun MedicineType.toString(context: Context): String {
    return when (this) {
        MedicineType.PILL -> context.getString(R.string.pill)
        MedicineType.GEL -> context.getString(R.string.gel)
        MedicineType.PATCH -> context.getString(R.string.patch)
        MedicineType.SINGLE_USE_INJECTION_VIAL -> context.getString(R.string.single_use_injection_vial)
        MedicineType.MULTI_USE_INJECTION_VIAL -> context.getString(R.string.multi_use_injection_vial)
    }
}