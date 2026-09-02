package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.utils.ScoreboardUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils

/**
 * Another abstract class for Sky Mall, Lottery and Beekeeper overlays
 */
abstract class AbstractRotatingPerksOverlay: AbstractOverlay() {

    private var cachedLines: List<String> = emptyList()
    private var lastSecondsLeft: Long = -1L
    private var lastBuff: String? = null
    private var lastIslandAllowed: Boolean = false

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
        val islandAllowed = isIslandAllowed
        if (!islandAllowed) {
            if (cachedLines.isNotEmpty()) {
                cachedLines = emptyList()
                lastIslandAllowed = false
            }
            return
        }

        val secondsLeft = (ScoreboardUtils.nextBuffTime - System.currentTimeMillis()) / 1000
        val buff = currentBuff

        if (cachedLines.isNotEmpty() && secondsLeft == lastSecondsLeft && buff == lastBuff) {
            return
        }

        lastSecondsLeft = secondsLeft
        lastBuff = buff
        lastIslandAllowed = true

        val newLines = listOf(
            "$buffPrefix: $buff",
            StringUtils.updateTimer()
        )

        cachedLines = newLines
    }
}