package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.skillPosition
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.utils.parser.SkillParser
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils.drawOverlayFrame
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils.renderSkillStringsWithTaming
import net.minecraft.client.gui.GuiGraphicsExtractor

class SkillOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()
    private var cachedSkillLines: List<String> = emptyList()
    private var cachedTamingLines: List<String> = emptyList()

    override val overlayLabel: String = "Skill Tracker"

    override val position: Position get() = skillPosition

    override val isEnabled: Boolean get() = SkillTrackingHandler.isTracking

    override fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled) return
        if (lines.isEmpty()) return

        drawOverlayFrame(context, position) { renderSkillStringsWithTaming(context, cachedSkillLines, cachedTamingLines) }
    }

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
            cachedSkillLines = emptyList()
            cachedTamingLines = emptyList()
            return
        }

        if (ModLoader.clientTicks % 5L != 0L) return

        val skillLines = mutableListOf<String>()
        val tamingLines = mutableListOf<String>()

        SkillParser.updateSkillLines(skillLines)
        SkillParser.updateTamingLines(tamingLines)

        cachedSkillLines = skillLines
        cachedTamingLines = tamingLines

        cachedLines = buildList {
            addAll(skillLines)
            if (tamingLines.isNotEmpty()) {
                addAll(tamingLines)
            }
        }
    }
}
