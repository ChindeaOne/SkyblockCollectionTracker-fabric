package io.github.chindeaone.collectiontracker.utils.rendering.screen.core

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

abstract class BaseListScreen(
    oldScreen: AbstractContainerScreen<*>?
) : BaseScreen(oldScreen) {

    private var scrollOffset = 0

    protected val rowHeight = 28

    protected val contentTop: Int
        get() = panelTop() + 60

    protected val contentBottom: Int
        get() = panelBottom() - 40

    private val visibleHeight: Int
        get() = contentBottom - contentTop

    protected val currentScrollOffset: Int
        get() = scrollOffset

    protected abstract val entryCount: Int

    private val maxScrollOffset: Int
        get() = ((entryCount * rowHeight) - visibleHeight).coerceAtLeast(0)

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (mouseX >= panelLeft() && mouseX < panelRight() && mouseY >= contentTop && mouseY < contentBottom) {
            scrollOffset = (scrollOffset - scrollY.toInt() * 10).coerceIn(0, maxScrollOffset)

            rebuildWidgets()
            return true
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    protected abstract fun rebuildEntryWidgets()
}