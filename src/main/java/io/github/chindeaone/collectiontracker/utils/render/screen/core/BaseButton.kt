package io.github.chindeaone.collectiontracker.utils.render.screen.core

import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.ScreenColors
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component

class BaseButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val labelProvider: () -> Component,
    onPress: OnPress,
) : Button.Plain(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION) {

    override fun extractContents(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        val border = if (isHovered) ScreenColors.BUTTON_HOVER.color else ScreenColors.BUTTON.color
        context.fill(x, y, x + width, y + 1, border)
        context.fill(x, y + height - 1, x + width, y + height, border)
        context.fill(x, y, x + 1, y + height, border)
        context.fill(x + width - 1, y, x + width, y + height, border)

        val label = labelProvider()
        context.centeredText(
            MinecraftUtils.font,
            label,
            x + width / 2,
            y + (height - MinecraftUtils.font.lineHeight) / 2,
            Colors.WHITE.color
        )
    }
}