package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.deployablePosition
import io.github.chindeaone.collectiontracker.config.enableDeployable
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatCompact
import io.github.chindeaone.collectiontracker.utils.world.deployable.DeployableTracker

class DeployableOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    override val overlayLabel: String = "Lantern Deployable"

    override val position: Position get() = deployablePosition

    override val isEnabled: Boolean get() = enableDeployable

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

        if (ModLoader.clientTicks % 2L != 0L) return

        val deployable = DeployableTracker.activeDeployable ?: run {
            cachedLines = emptyList()
            return
        }

        val expiryTime = deployable.expiryTime ?: return
        val timeLeft = expiryTime.timeUntil()

        cachedLines = listOf(
            if (timeLeft.inWholeSeconds < 5) {
                "${deployable.displayName} §cSoon!"
            } else {
                "${deployable.displayName} §e${timeLeft.formatCompact()}"
            }
        )
    }
}
