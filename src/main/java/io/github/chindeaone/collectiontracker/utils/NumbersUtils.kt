package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.config.ConfigAccess

object NumbersUtils {

    private val UNITS = arrayOf("", "k", "M", "B", "T")

    fun formatNumber(number: Long): String {
        if (number == 0L) return "0"

        if (ConfigAccess.isExplicitValues()) {
            return String.format("%,d", number)
        }

        if (number < 1000) {
            return String.format("%d", number)
        }

        var num = number.toFloat()
        var index = 0
        while (num >= 1000.0 && index < UNITS.size - 1) {
            num /= 1000.0f
            index++
        }

        return String.format("%.2f%s", num, UNITS[index])
    }

    fun formatFloat(number: Float): String {
        if (number == 0f) return "0"

        if (ConfigAccess.isExplicitValues()) {
            return String.format("%,.2f", number)
        }

        if (number < 1000) {
            return String.format("%.2f", number)
        }

        var num = number
        var index = 0
        while (num >= 1000.0 && index < UNITS.size - 1) {
            num /= 1000.0f
            index++
        }

        return String.format("%.2f%s", num, UNITS[index])
    }

    fun parseToSeconds(input: String): Long {
        val regex = "(\\d+)([dhms])".toRegex()
        val normalizedInput = input.lowercase().replace(" ", "")

        var seconds = 0L
        var found = false

        for (match in regex.findAll(normalizedInput)) {
            val (valueString, unitString) = match.destructured
            val value = valueString.toLong()

            seconds += when (unitString[0]) {
                'd' -> value * 86400
                'h' -> value * 3600
                'm' -> value * 60
                's' -> value
                else -> 0
            }
            found = true
        }

        return if (found) {
            seconds
        } else {
            normalizedInput.toLongOrNull() ?: -1
        }
    }
}
