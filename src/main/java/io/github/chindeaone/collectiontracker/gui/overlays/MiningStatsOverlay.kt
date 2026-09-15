package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.enableMiningStatsOverlay
import io.github.chindeaone.collectiontracker.config.miningStatsPosition
import io.github.chindeaone.collectiontracker.utils.parser.MiningStatsParser

class MiningStatsOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    override val overlayLabel: String = "Mining Stats"

    override val position: Position get() = miningStatsPosition

    override val isEnabled: Boolean get() = enableMiningStatsOverlay

    override fun updateDimensions() {
        if (!isEnabled) return

        val lines = lines
        if (lines === cachedLines) return

        cachedLines = lines
        super.updateDimensions()
    }

    override val lines: List<String> get() = MiningStatsParser.getLines()
}
