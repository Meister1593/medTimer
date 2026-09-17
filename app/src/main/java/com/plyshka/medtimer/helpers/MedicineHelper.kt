package com.plyshka.medtimer.helpers

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.core.text.bold
import androidx.core.text.color
import com.plyshka.medtimer.database.FullMedicine
import com.plyshka.medtimer.database.Medicine
import com.plyshka.medtimer.medicine.stockSettings.MedicineType
import com.plyshka.medtimer.medicine.stockSettings.toString
import com.plyshka.medtimer.model.UserPreferences
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale
import java.util.regex.Pattern

object MedicineHelper {
    private val CYCLIC_COUNT: Pattern = Pattern.compile(" (\\(\\d+/\\d+)\\)")

    fun normalizeMedicineName(medicineName: String): String {
        return CYCLIC_COUNT.matcher(medicineName).replaceAll("")
    }

    fun getMedicineName(
        medicine: Medicine,
        notification: Boolean,
        userPreferences: UserPreferences
    ): String {
        return if (userPreferences.hideMedicineName && notification) {
            medicine.name[0] + "*".repeat(medicine.name.length - 1)
        } else {
            medicine.name
        }
    }

    fun getStockIcons(fullMedicine: FullMedicine): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        if (fullMedicine.isOutOfStock) {
            builder.color(0xffcc0000.toInt()) { bold { append("⚠") } }
        }
        val expiredIcon = getExpiredIcon(fullMedicine)
        if (expiredIcon.isNotEmpty()) {
            if (builder.isNotEmpty()) {
                builder.append(" ")
            }
            builder.append(expiredIcon)
        }
        return builder
    }

    fun getExpiredIcon(fullMedicine: FullMedicine): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        if (fullMedicine.medicine.hasExpired()) {
            builder.color(0xffcc0000.toInt()) { bold { append("\uD83D\uDEAB") } }
        }
        return builder
    }

    fun formatType(type: Int, context: Context): String {
        return MedicineType.fromType(type)!!.toString(context)
    }

    fun parseType(typeIndex: String?): Int? {
        return typeIndex?.toInt()
    }

    fun formatAmount(amount: Double, unit: String): String {
        val numberFormat = NumberFormat.getInstance(Locale.US) // todo: put it into settings
        numberFormat.minimumFractionDigits = 0
        numberFormat.maximumFractionDigits = 2
        return numberFormat.format(amount) + if (unit.isEmpty()) "" else " $unit"
    }

    fun parseAmount(amount: String?): Double? {
        val numberRegex = Pattern.compile("(?:\\d|\\.\\d)[.,\\s\\d]*")
        val matcher = numberRegex.matcher(amount ?: "")

        return if (matcher.find() && matcher.group(0) != null) {
            val numberFormat = NumberFormat.getInstance(Locale.US) // todo: put it into settings
            try {
                numberFormat.parse(matcher.group(0)!!.replace(" ", ""))?.toDouble()
            } catch (_: ParseException) {
                null
            }
        } else {
            null
        }
    }
}
