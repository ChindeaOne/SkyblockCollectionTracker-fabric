package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.ScreenColors
import io.github.chindeaone.collectiontracker.utils.render.drawRoundedOutline
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class WeightWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val title: String,
    val value: Float,
    val totalWeight: Float,
    val bgColor: Int,
    val onClick: () -> Unit
): AbstractWidget(x, y, width, height, Component.literal(title)) {

    override fun extractWidgetRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        context.fill(x, y, x + width, y + height, bgColor)
        context.drawRoundedOutline(x, y, width, height, 4, ScreenColors.PANEL_BORDER.color)

        val percentage = if (totalWeight > 0f) (value / totalWeight) * 100f else 0f
        val textValue = "%.2f (%.2f%%)".format(value, percentage)
        val centerX = x + width / 2

        context.centeredText(MinecraftUtils.font, Component.literal(title), centerX, y + 6, ScreenColors.WIDGET_TITLE_TEXT.color)
        context.centeredText(MinecraftUtils.font, Component.literal(textValue), centerX, y + 6 + MinecraftUtils.font.lineHeight, ScreenColors.WIDGET_VALUE_TEXT.color)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean){
        onClick.invoke()
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {
        defaultButtonNarrationText(output)
    }
}