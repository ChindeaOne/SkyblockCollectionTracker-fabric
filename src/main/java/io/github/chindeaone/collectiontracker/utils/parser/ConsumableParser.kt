package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import java.util.EnumMap
import kotlin.time.Duration.Companion.hours

object ConsumableParser {

    private val HOUR = 1.hours.inWholeMilliseconds

    private val endTimes = EnumMap<Consumables, Long>(Consumables::class.java)

    enum class Consumables(val displayName: String, val overlayName: String) {
        FILET_O_FORTUNE(
            displayName = "Filet O' Fortune",
            overlayName = "§9Filet O' Fortune",
        ),
        CHILLED_PRISTINE_POTATO(
            displayName = "Chilled Pristine Potato",
            overlayName = "§5Chilled Pristine Potato",
        ),
        POWDER_PIE(
            displayName = "Powder Pie",
            overlayName = "§aPowder Pie",
        ),
        FIESTA_FLASK(
            displayName = "Fiesta Flask",
            overlayName = "§6Fiesta Flask",
        )
    }

    fun loadConsumableDurations() {
        val now = System.currentTimeMillis()

        Consumables.entries.forEach { buff ->
            endTimes[buff] = now + ConfigAccess.getConsumableDuration(buff)
        }
    }

    fun saveConsumableDurations() {
        val now = System.currentTimeMillis()

        Consumables.entries.forEach { buff ->
            val remaining = ((endTimes[buff] ?: 0L) - now).coerceAtLeast(0L)
            ConfigHelper.setConsumableDuration(buff, remaining)
        }
    }

    fun getConsumable(name: String): Consumables? {
        return Consumables.entries.firstOrNull {
            it.displayName.equals(name, ignoreCase = true)
        }
    }

    fun getEndTime(buff: Consumables): Long {
        return endTimes[buff] ?: 0L
    }

    fun resetConsumable(buff: Consumables) {
        val endTime = System.currentTimeMillis() + HOUR

        endTimes[buff] = endTime
        ConfigHelper.setConsumableDuration(buff, HOUR)
    }
}