package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatClock
import io.github.chindeaone.collectiontracker.utils.ScoreboardUtils
import io.github.chindeaone.collectiontracker.utils.ScoreboardUtils.nextBuffTime
import kotlin.ranges.contains

/**
 * Another abstract class for Sky Mall, Lottery and Beekeeper overlays
 */
abstract class AbstractRotatingPerksOverlay: AbstractOverlay() {

    private var cachedLines: List<String> = emptyList()

    abstract val buffPrefix: String

    abstract val currentBuff: String?

    abstract val isIslandAllowed: Boolean

    override fun updateDimensions() {
        if (!isEnabled) return
        updateLinesIfNeeded()

        super.updateDimensions()
    }

    override val lines: List<String>
        get() {
            updateLinesIfNeeded()
            return cachedLines
        }

    private fun updateLinesIfNeeded() {
        if (!isIslandAllowed) {
            cachedLines = emptyList()
            return
        }

        cachedLines = listOf("$buffPrefix: $currentBuff", updateTimer())
    }

    private fun updateTimer(): String {
        val target = nextBuffTime ?: return "§aTime left: §cSoon"

        val timeLeft = target - EpochMark.now()

        if (timeLeft.isNegative()) {
            if (!ScoreboardUtils.checkTime) {
                ScoreboardUtils.checkTime = true
            }
            return "§aTime left: §cSoon"
        }

        if (timeLeft.inWholeSeconds in 0..5) {
            return "§aTime left: §cSoon"
        }

        return "§aTime left: ${timeLeft.formatClock()}"
    }
}