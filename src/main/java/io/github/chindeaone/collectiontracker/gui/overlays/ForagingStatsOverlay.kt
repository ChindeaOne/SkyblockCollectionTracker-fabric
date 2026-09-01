package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getForagingStatsPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isForagingStatsOverlayEnabled
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.HypixelUtils.isInSkyblock
import io.github.chindeaone.collectiontracker.utils.parser.ForagingStatsParser
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.drawOverlayFrame
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.renderStrings
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.max

class ForagingStatsOverlay : AbstractOverlay() {
    private val position = getForagingStatsPosition()

    override fun overlayLabel(): String {
        return "Foraging Stats"
    }

    override fun position(): Position {
        return position
    }

    override fun isEnabled(): Boolean {
        return isForagingStatsOverlayEnabled() && isInSkyblock
    }

    override fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled) return
        val lines = ForagingStatsParser.getLines()

        if (lines.isEmpty()) return

        drawOverlayFrame(context, position) { renderStrings(context, lines) }
    }

    override fun updateDimensions() {
        if (!isEnabled) return
        val lines = ForagingStatsParser.getLines()
        if (lines.isEmpty()) return

        val fr = Minecraft.getInstance().font
        var maxW = 0
        for (l in lines) maxW = max(maxW, fr.width(l))
        val h = fr.lineHeight * lines.size

        position.setDimensions(maxW, h)
    }
}
