package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.core.Position
import net.minecraft.client.gui.GuiGraphicsExtractor
import io.github.chindeaone.collectiontracker.config.titlePosition
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils

class TitleOverlay : AbstractOverlay() {
    override val overlayLabel: String = "Global Title"

    override val position: Position get() = titlePosition

    override val isEnabled: Boolean get() = true

    override fun render(context: GuiGraphicsExtractor) {
        RenderUtils.drawActiveTitle(context)
    }

    override fun updateDimensions() {}
}