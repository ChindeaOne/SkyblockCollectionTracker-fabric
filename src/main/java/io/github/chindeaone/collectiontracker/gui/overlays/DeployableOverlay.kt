package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getDeployablePosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isDeployableEnabled
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.parser.DeployableParser.buff
import io.github.chindeaone.collectiontracker.utils.parser.DeployableParser.buffColor
import io.github.chindeaone.collectiontracker.utils.parser.DeployableParser.isNear
import io.github.chindeaone.collectiontracker.utils.parser.DeployableParser.remainingTime

class DeployableOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    private var lastBuff: String = ""
    private var lastExpireTime: String = ""
    private var lastBuffColor: String = ""
    private var lastIsNear: Boolean = false

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
            if (cachedLines.isNotEmpty()) {
                cachedLines = emptyList()
            }
            return
        }

        val currentBuff = buff
        val expireTime = remainingTime
        val currentNear = isNear
        val currentBuffColor = buffColor

        if (currentBuff.isEmpty() || expireTime.isEmpty() || !currentNear) {
            if (cachedLines.isNotEmpty()) {
                cachedLines = emptyList()
                lastBuff = ""
                lastExpireTime = ""
                lastBuffColor = ""
                lastIsNear = false
            }
            return
        }

        if (cachedLines.isNotEmpty()
                && currentBuff == lastBuff
                && expireTime == lastExpireTime
                && currentBuffColor == lastBuffColor
                && lastIsNear) return

        var timeLeft: Int
        try {
            timeLeft = expireTime.replace("s", "").toInt()
        } catch (_: NumberFormatException) {
            if (cachedLines.isNotEmpty()) {
                cachedLines = emptyList()
            }
            return
        }

        lastBuff = currentBuff
        lastExpireTime = expireTime
        lastBuffColor = currentBuffColor
        lastIsNear = true

        val newLines = if (timeLeft <= 5) {
            listOf("$currentBuffColor$currentBuff §cSoon!")
        } else {
            listOf("$currentBuffColor$currentBuff §e${timeLeft}s")
        }

        cachedLines = newLines
    }
}
