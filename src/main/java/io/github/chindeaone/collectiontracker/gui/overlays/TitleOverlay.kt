package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.HypixelUtils.isInSkyblock
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.drawActiveTitle
import net.minecraft.client.gui.GuiGraphicsExtractor

class TitleOverlay : AbstractOverlay() {
    override fun overlayLabel(): String {
        return "Global Title"
    }

    override fun position(): Position? {
        return null
    }

    override fun isEnabled(): Boolean {
        return isInSkyblock
    }

    override fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled) return
        drawActiveTitle(context)
    }

    override fun updateDimensions() {}
}