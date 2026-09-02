package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getForagingStatsPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isForagingStatsOverlayEnabled
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.parser.ForagingStatsParser

class ForagingStatsOverlay : AbstractOverlay() {
    private var lastLines: List<String> = emptyList()

    override val overlayLabel: String = "Foraging Stats"

    override val position: Position get() = getForagingStatsPosition()

    override val isEnabled: Boolean get() = isForagingStatsOverlayEnabled()

    override fun updateDimensions() {
        if (!isEnabled) return

        val lines = lines
        if (lines === lastLines) return

        lastLines = lines
        super.updateDimensions()
    }

    override val lines: List<String> get() = ForagingStatsParser.getLines()
}
