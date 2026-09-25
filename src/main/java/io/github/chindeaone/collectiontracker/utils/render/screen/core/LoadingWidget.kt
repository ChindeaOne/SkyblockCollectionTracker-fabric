package io.github.chindeaone.collectiontracker.utils.render.screen.core

import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

class LoadingWidget(
    private val speed: Int = 50
) : AbstractWidget(
    0,
    0,
    MinecraftUtils.font.width("Loading Data..."),
    MinecraftUtils.font.lineHeight,
    Component.empty()
) {
    private var frames = 0L

    override fun extractWidgetRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        frames++

        val dots = ((frames / speed) % 4).toInt()
        val text = "Loading Data" + ".".repeat(dots)

        context.centeredText(
            MinecraftUtils.font,
            text,
            x + width / 2,
            y + (height - MinecraftUtils.font.lineHeight) / 2,
            Colors.WHITE.color
        )
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {}
}