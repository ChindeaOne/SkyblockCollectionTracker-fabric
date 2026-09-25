package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.ScreenColors
import io.github.chindeaone.collectiontracker.utils.render.screen.core.BaseScrollableScreen
import io.github.chindeaone.collectiontracker.utils.render.screen.core.LoadingWidget
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

abstract class BaseWeightScreen(
    oldScreen: AbstractContainerScreen<*>?,
    protected val playerName: String
): BaseScrollableScreen(oldScreen) {

    private data class WeightWidget(val title: String, val total: Float, val x: Int, val y: Int)
    private val weightWidgets = mutableListOf<WeightWidget>()

    protected val columnWidth: Int
        get() = panelWidth / 2

    protected val widgetWidth = 120
    protected val widgetHeight = 40
    protected val verticalGap = 20

    protected val leftX: Int
        get() = centeredInColumn(0)

    protected val rightX: Int
        get() = centeredInColumn(1)

    private var profileLoaded = false

    protected open fun isProfileInit(): Boolean = false

    protected abstract val weight: Float

    override fun initContent() {
        super.initContent()
        profileLoaded = isProfileInit()

        if (!profileLoaded) {
            showLoading()
            return
        }

        rebuildEntryWidgets()
    }

    private fun rebuildEntryWidgets() {
        addLeftWidgets()
        addRightWidgets()
    }

    override fun tick() {
        super.tick()

        if (!profileLoaded && isProfileInit()) {
            profileLoaded = true
            rebuildWidgets()
        }
    }

    protected fun showLoading() {
        if (isProfileInit()) return

        val loading = LoadingWidget()
        FrameLayout.centerInRectangle(loading, 0, 0, width, height)

        addRenderableWidget(loading)
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(context, mouseX, mouseY, a)

        if (!isProfileInit()) return

        renderWeightWidgets(context)
    }

    protected open fun renderWeightWidgets(context: GuiGraphicsExtractor) {
        weightWidgets.forEach { widget ->
            val percentage = widget.total / weight * 100f
            val component = Component.literal("%.2f (%.2f%%)".format(widget.total, percentage))

            context.fill(widget.x, widget.y, widget.x + widgetWidth, widget.y + widgetHeight, ScreenColors.BUTTON_HOVER.color)

            val centerX = widget.x + widgetWidth / 2

            context.centeredText(font, Component.literal(widget.title), centerX, widget.y + 6, Colors.WHITE.color)
            context.centeredText(font, component, centerX, widget.y + 6 + font.lineHeight, Colors.WHITE.color)
        }
    }

    protected fun centeredInColumn(column: Int): Int {
        val columnLeft = panelLeft + column * columnWidth
        return columnLeft + (columnWidth - widgetWidth) / 2
    }

    protected open fun addLeftWidgets() {}
    protected open fun addRightWidgets() {}

    protected fun addWeightSection(title: String, total: Float, x: Int, y: Int) {
        weightWidgets += WeightWidget(title, total, x, y)
    }

    override fun initButtons() {}

    override fun onClose() {
        weightWidgets.clear()
        super.onClose()
    }
}