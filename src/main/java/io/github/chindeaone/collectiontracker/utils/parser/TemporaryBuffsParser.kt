package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import java.util.EnumMap
import kotlin.time.Duration.Companion.hours

object TemporaryBuffsParser {

    private val HOUR = 1.hours.inWholeMilliseconds

    private val endTimes = EnumMap<TemporaryBuff, Long>(TemporaryBuff::class.java)

    enum class TemporaryBuff(val displayName: String, val overlayName: String) {
        REFINED_CACAO(
            displayName = "Refined Dark Cacao Truffle",
            overlayName = "§6Refined Dark Cacao Truffle",
        ),
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

    fun loadDurations() {
        val now = System.currentTimeMillis()

        TemporaryBuff.entries.forEach { buff ->
            endTimes[buff] = now + ConfigAccess.getDuration(buff)
        }
    }

    fun saveDurations() {
        val now = System.currentTimeMillis()

        TemporaryBuff.entries.forEach { buff ->
            val remaining = ((endTimes[buff] ?: 0L) - now).coerceAtLeast(0L)
            ConfigHelper.setDuration(buff, remaining)
        }
    }

    fun getConsumable(name: String): TemporaryBuff? {
        return TemporaryBuff.entries.firstOrNull {
            it.displayName.equals(name, ignoreCase = true)
        }
    }

    fun getEndTime(buff: TemporaryBuff): Long {
        return endTimes[buff] ?: 0L
    }

    fun resetConsumable(buff: TemporaryBuff) {
        val endTime = System.currentTimeMillis() + HOUR

        endTimes[buff] = endTime
        ConfigHelper.setDuration(buff, HOUR)
    }
}