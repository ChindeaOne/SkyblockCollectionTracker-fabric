package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.enableForagingStatsOverlay
import io.github.chindeaone.collectiontracker.config.foragingStatsPosition
import io.github.chindeaone.collectiontracker.utils.parser.ForagingStatsParser

class ForagingStatsOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    override val overlayLabel: String = "Foraging Stats"

    override val position: Position get() = foragingStatsPosition

    override val isEnabled: Boolean get() = enableForagingStatsOverlay

    override fun updateDimensions() {
        if (!isEnabled) return

        val lines = lines
        if (lines === cachedLines) return

        cachedLines = lines
        super.updateDimensions()
    }

    override val lines: List<String> get() = ForagingStatsParser.getLines()
}
