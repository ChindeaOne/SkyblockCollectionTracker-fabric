package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.utils.ScoreboardUtils
import io.github.chindeaone.collectiontracker.utils.ScoreboardUtils.nextBuffTime
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatTime
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
        val timeLeft = (nextBuffTime - System.currentTimeMillis()) / 1000
        if (timeLeft in 0..5) {
            return "§aTime left: §cSoon"
        }
        if (timeLeft < 0 && !ScoreboardUtils.checkTime) {
            ScoreboardUtils.checkTime = true
            return "§aTime left: §cSoon"
        }

        return "§aTime left: §e${formatTime(timeLeft)}"
    }
}