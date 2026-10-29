package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.fromNow
import io.github.chindeaone.collectiontracker.utils.inventory.ItemUtils
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.ItemStack
import java.util.EnumMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object ConsumableParser {

    private val REMAINING_TIME_REGEX = Regex("""Remaining:\s*(?:(?:(\d+):)?(\d+):)?(\d+)""") // hours in case future consumables will stack
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
            it.displayName in name
        }
    }

    fun getEndTime(buff: Consumables): EpochMark? {
        return endTimes[buff]
    }

    fun updateDurationsFromScreen(screen: AbstractContainerScreen<*>) {
        val items = screen.menu.items

        for (row in 1..4) {
            for (col in 1..7) {
                val stack = items[row * 9 + col]
                if (stack.isEmpty) continue

                val consumable = getConsumable(stack.hoverName.string) ?: continue
                val remaining = parseRemainingDuration(stack) ?: continue

                endTimes[consumable] = remaining.fromNow()
            }
        }
    }

    private fun parseRemainingDuration(stack: ItemStack): Duration? {
        val remainingLine = ItemUtils.getTooltipLines(stack).firstOrNull { "Remaining" in it } ?: return null

        val match = REMAINING_TIME_REGEX.find(remainingLine) ?: return null
        val (hoursStr, minutesStr, secondsStr) = match.destructured

        val seconds = secondsStr.toIntOrNull() ?: 0
        val minutes = minutesStr.toIntOrNull() ?: 0
        val hours = hoursStr.toIntOrNull() ?: 0

        return hours.hours + minutes.minutes + seconds.seconds
    }

    fun resetConsumable(buff: Consumables) {
        val endTime = 1.hours.fromNow()

        endTimes[buff] = endTime
        ConfigHelper.setConsumableDuration(buff, 1.hours.inWholeSeconds)
    }
}