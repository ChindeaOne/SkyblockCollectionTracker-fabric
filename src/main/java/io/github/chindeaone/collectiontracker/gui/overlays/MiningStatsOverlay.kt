package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getMiningStatsPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isMiningStatsOverlayEnabled
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.parser.MiningStatsParser

class MiningStatsOverlay : AbstractOverlay() {
    private var lastLines: List<String> = emptyList()

    override val overlayLabel: String = "Mining Stats"

    override val position: Position get() = getMiningStatsPosition()

    override val isEnabled: Boolean get() = isMiningStatsOverlayEnabled()

    override fun updateDimensions() {
        if (!isEnabled) return

        val lines = lines
        if (lines === lastLines) return

        lastLines = lines
        super.updateDimensions()
    }

    override val lines: List<String> get() = MiningStatsParser.getLines()
}
