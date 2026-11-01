package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.config.formattedValues
import java.util.Locale

object NumbersUtils {

    fun formatLong(value: Long): String {
        if (!formattedValues) {
            return String.format(Locale.US, "%,d", value)
        }

        if (value < 1000L) {
            return value.toString()
        }

        return formatCompact(value.toDouble())
    }

    fun formatDouble(value: Double): String {
        if (!formattedValues) {
            val formatted = String.format(Locale.US, "%,.2f", value)
            return if (formatted.endsWith(".00")) formatted.removeSuffix(".00") else formatted
        }

        return if (value < 1000.0) {
            val formatted = String.format(Locale.US, "%,.2f", value)
            if (formatted.endsWith(".00")) formatted.removeSuffix(".00") else formatted
        } else {
            formatCompact(value)
        }
    }

    private fun formatCompact(number: Double): String {
        val units = arrayOf("", "k", "M", "B", "T")
        var v = number
        var index = 0

        while (v >= 1000.0 && index < units.size - 1) {
            v /= 1000.0
            index++
        }

        return String.format(Locale.US, "%.2f", v)
            .trimEnd('0')
            .trimEnd('.') + units[index]
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
