package io.github.chindeaone.collectiontracker.utils.rendering.screen.core

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

abstract class BaseListScreen(
    oldScreen: AbstractContainerScreen<*>?
): BaseScrollableScreen(oldScreen) {

    protected val rowHeight = 28

    protected abstract val entryCount: Int

    override val contentHeight: Int
        get() = entryCount * rowHeight

    override fun initContent() {
        rebuildEntryWidgets()
    }

    protected abstract fun rebuildEntryWidgets()
}