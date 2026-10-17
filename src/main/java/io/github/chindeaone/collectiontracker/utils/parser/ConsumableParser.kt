package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.fromNow
import java.util.EnumMap
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

object ConsumableParser {

    private val endTimes = EnumMap<Consumables, EpochMark>(Consumables::class.java)

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
        Consumables.entries.forEach { buff ->
            endTimes[buff] = ConfigAccess.getConsumableDuration(buff).seconds.fromNow()
        }
    }

    fun saveConsumableDurations() {
        Consumables.entries.forEach { buff ->
            val remaining = endTimes[buff]?.keepPositive?.inWholeSeconds ?: 0L
            ConfigHelper.setConsumableDuration(buff, remaining)
        }
    }

    fun getConsumable(name: String): Consumables? {
        return Consumables.entries.firstOrNull {
            it.displayName.equals(name, ignoreCase = true)
        }
    }

    fun getEndTime(buff: Consumables): EpochMark? {
        return endTimes[buff]
    }

    fun resetConsumable(buff: Consumables) {
        val endTime = 1.hours.fromNow()

        endTimes[buff] = endTime
        ConfigHelper.setConsumableDuration(buff, 1.hours.inWholeSeconds)
    }
}