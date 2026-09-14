package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getDeployablePosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isDeployableEnabled
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.parser.DeployableParser.buff
import io.github.chindeaone.collectiontracker.utils.parser.DeployableParser.buffColor
import io.github.chindeaone.collectiontracker.utils.parser.DeployableParser.isNear
import io.github.chindeaone.collectiontracker.utils.parser.DeployableParser.remainingTime

class DeployableOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    override val overlayLabel: String = "Lantern Deployable"

    override val position: Position get() = getDeployablePosition()

    override val isEnabled: Boolean get() = isDeployableEnabled()

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
        if (!isEnabled) {
            cachedLines = emptyList()
            return
        }

        if (ModLoader.clientTicks % 5L != 0L) return

        val currentBuff = buff
        val expireTime = remainingTime
        val currentNear = isNear
        val currentBuffColor = buffColor

        if (currentBuff.isEmpty() || expireTime.isEmpty() || !currentNear) {
            cachedLines = emptyList()
            return
        }

        var timeLeft: Int
        try {
            timeLeft = expireTime.replace("s", "").toInt()
        } catch (_: NumberFormatException) {
            cachedLines = emptyList()
            return
        }

        val newLines = if (timeLeft <= 5) {
            listOf("$currentBuffColor$currentBuff §cSoon!")
        } else {
            listOf("$currentBuffColor$currentBuff §e${timeLeft}s")
        }

        cachedLines = newLines
    }
}
