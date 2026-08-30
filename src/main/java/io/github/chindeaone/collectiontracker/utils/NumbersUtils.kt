package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.config.ConfigAccess

import java.util.Locale

object NumbersUtils {

    private val UNITS = arrayOf("", "k", "M", "B", "T")

    @JvmStatic
    fun formatNumber(number: Long): String {
        if (number == 0L) return "0"

        if (ConfigAccess.isExplicitValues()) {
            return String.format(Locale.US, "%,d", number)
        }

        if (number < 1000) {
            return String.format(Locale.US, "%d", number)
        }

        var num = number.toFloat()
        var index = 0
        while (num >= 1000.0 && index < UNITS.size - 1) {
            num /= 1000.0f
            index++
        }

        return String.format(Locale.US, "%.2f%s", num, UNITS[index])
    }

    @JvmStatic
    fun formatFloat(number: Float): String {
        if (number == 0f) return "0"

        if (ConfigAccess.isExplicitValues()) {
            return String.format(Locale.US, "%,.2f", number)
        }

        if (number < 1000) {
            return String.format(Locale.US, "%.2f", number)
        }

        var num = number
        var index = 0
        while (num >= 1000.0 && index < UNITS.size - 1) {
            num /= 1000.0f
            index++
        }

        return String.format(Locale.US, "%.2f%s", num, UNITS[index])
    }
}
