package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import io.github.chindeaone.collectiontracker.utils.ScreenColors
import io.github.chindeaone.collectiontracker.utils.render.scissor
import io.github.chindeaone.collectiontracker.utils.render.screen.core.BaseButton
import io.github.chindeaone.collectiontracker.utils.render.screen.core.BaseScrollableScreen
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

class WeightBreakdownScreen(
    oldScreen: AbstractContainerScreen<*>?,
    private val weightName: String,
    private val widgetBgColor: Int,
    private val weightEntries: Map<String, Float>,
    private val onBack: () -> Unit
): BaseScrollableScreen(oldScreen) {

    private val columnWidth: Int
        get() = panelWidth / 2

    private val widgetWidth = 120
    private val widgetHeight = 30
    private val verticalGap = 10

    private val totalWeight: Float
        get() = weightEntries["total"] ?: weightEntries["totalWeight"] ?: 0f

    override val contentHeight: Int
        get() = calculateContentHeight()

    override val screenTitle: Component
        get() = Component.literal("$weightName: %.2f".format(totalWeight)).withColor(ScreenColors.WIDGET_TITLE_TEXT.color)

    override fun initContent() {
        addRenderableWidget(BaseButton(panelLeft + 10, panelTop + 8, 80, 20, { Component.literal("Back") }) {
            onBack.invoke()
        })
    }

    private fun calculateContentHeight(): Int {
        val entryCount = weightEntries.count { it.key != "total" && it.key != "totalWeight" }
        val rows = (entryCount + 1) / 2
        return rows * (widgetHeight + verticalGap)
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(context, mouseX, mouseY, a)
        renderWeightEntries(context)
    }

    private fun renderWeightEntries(context: GuiGraphicsExtractor) {
        val entries = weightEntries.filterKeys { it != "total" && it != "totalWeight" }
        val midpoint = (entries.size + 1) / 2

        context.scissor(panelLeft, contentTop, panelRight, contentBottom) {
            entries.entries.forEachIndexed { index, (name, value) ->
                val column = if (index < midpoint) 0 else 1
                val row = if (index < midpoint) index else index - midpoint

                val x = centeredInColumn(column)
                val y = contentTop + row * (widgetHeight + verticalGap) - scrollOffset

                renderWeightEntry(context, name, value, x, y)
            }
        }
    }

    private fun renderWeightEntry(context: GuiGraphicsExtractor, name: String, value: Float, x: Int, y: Int) {
        val percentage = if (totalWeight == 0f) 0f else value / totalWeight * 100f

        context.fill(x, y, x + widgetWidth, y + widgetHeight, widgetBgColor)
        val centerX = x + widgetWidth / 2

        context.centeredText(font, Component.literal(name), centerX, y + 6, ScreenColors.WIDGET_TITLE_TEXT.color)
        context.centeredText(font, Component.literal("%.2f (%.2f%%)".format(value, percentage)), centerX, y + 6 + font.lineHeight, ScreenColors.WIDGET_VALUE_TEXT.color)
    }

    private fun centeredInColumn(column: Int): Int {
        val columnLeft = panelLeft + column * columnWidth
        return columnLeft + (columnWidth - widgetWidth) / 2
    }
}