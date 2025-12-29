package io.github.chindeaone.collectiontracker.config.version

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.notenoughupdates.moulconfig.common.RenderContext
import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import io.github.notenoughupdates.moulconfig.gui.GuiOptionEditor
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption

class VersionCheck(option: ProcessedOption) : GuiOptionEditor(option) {

    override fun render(context: RenderContext, x: Int, y: Int, width: Int) {
        val fr = context.minecraft.defaultFontRenderer

        val width = width - 20

        val widthRemaining = width - 10
        val currentVersion = "Version " + "§a" + SkyblockCollectionTracker.VERSION

        // Render the current version in green
        context.pushMatrix()
        context.translate(x.toFloat() + 10, y.toFloat())
        context.scale(2F, 2F)
        context.drawStringCenteredScaledMaxWidth(
            StructuredText.of(currentVersion), fr, widthRemaining / 4F, 10F, true, widthRemaining / 2, -1
        )
        context.popMatrix()

    }

    override fun getHeight(): Int {
        return 55
    }

    override fun mouseInput(x: Int, y: Int, width: Int, mouseX: Int, mouseY: Int): Boolean {
        return false
    }

    override fun keyboardInput(): Boolean {
        return false
    }
}