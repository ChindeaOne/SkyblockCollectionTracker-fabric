package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import io.github.chindeaone.collectiontracker.utils.render.screen.core.BaseScrollableScreen
import io.github.chindeaone.collectiontracker.utils.render.screen.core.LoadingWidget
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

abstract class BaseWeightScreen(
    oldScreen: AbstractContainerScreen<*>?,
    protected val playerName: String
): BaseScrollableScreen(oldScreen) {

    data class WeightSection(val title: String, val total: Float, val x: Int, val y: Int)
    val weightSections = mutableListOf<WeightSection>()

    protected val columnWidth: Int
        get() = panelWidth / 2

    protected val sectionWidth = 120
    protected val sectionHeight = 40
    protected val verticalGap = 20

    protected val leftX: Int
        get() = centeredInColumn(0)

    protected val rightX: Int
        get() = centeredInColumn(1)

    private var profileLoaded = false

    protected open fun isProfileInit(): Boolean = false

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

        renderWeightSections(context)
    }

    protected open fun renderWeightSections(context: GuiGraphicsExtractor) {}

    protected fun centeredInColumn(column: Int): Int {
        val columnLeft = panelLeft + column * columnWidth
        return columnLeft + (columnWidth - sectionWidth) / 2
    }

    protected open fun addLeftWidgets() {}
    protected open fun addRightWidgets() {}

    protected fun addWeightSection(title: String, total: Float, x: Int, y: Int) {
        weightSections += WeightSection(title, total, x, y)
    }

    override fun initButtons() {}

    override fun onClose() {
        weightSections.clear()
        super.onClose()
    }
}