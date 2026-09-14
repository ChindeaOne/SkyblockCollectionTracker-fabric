package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import java.util.Locale

object NumbersUtils {

    private val UNITS = arrayOf("", "k", "M", "B", "T")

    fun formatValue(value: Long): String {
        if (value < 1000) {
            return value.toString()
        }

        return formatAbbreviated(value.toDouble())
    }

    fun formatNumber(number: Long): String {
        if (ConfigAccess.isExplicitValues()) {
            return String.format(Locale.ROOT, "%,d", number)
        }

        return formatValue(number)
    }

    fun formatFloat(number: Float): String {
        if (ConfigAccess.isExplicitValues()) {
            return String.format(Locale.ROOT, "%,.2f", number)
        }

        if (number < 1000) {
            return String.format(Locale.ROOT, "%.2f", number)
        }

        return formatAbbreviated(number.toDouble())
    }

    private fun formatAbbreviated(number: Double): String {
        var num = number
        var index = 0

        while (num >= 1000.0 && index < UNITS.size - 1) {
            num /= 1000.0
            index++
        }

        return String.format(Locale.ROOT, "%.2f", num)
            .trimEnd('0')
            .trimEnd('.') + UNITS[index]
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

    fun parseValue(value: String): Long? {
        val input = value.trim().lowercase()
        if (input.isEmpty()) return null

        val multiplier = when {
            input.endsWith("k") -> 1_000L
            input.endsWith("m") -> 1_000_000L
            input.endsWith("b") -> 1_000_000_000L
            else -> 1L
        }

        val number = if (multiplier > 1L) {
            input.dropLast(1)
        } else {
            input
        }

        return try {
            (number.toDouble() * multiplier).toLong()
        } catch (_: NumberFormatException) {
            null
        }
    }
}
