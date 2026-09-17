package io.github.chindeaone.collectiontracker.utils.render.screen.core

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

abstract class BaseListScreen(
    oldScreen: AbstractContainerScreen<*>?
): BaseScrollableScreen(oldScreen) {

    protected val rowHeight = 28

    protected abstract val entryCount: Int

    override val contentHeight: Int
        get() = entryCount * rowHeight

    protected abstract val message: Component

    override fun initContent() {
        rebuildEntryWidgets()
    }

    protected abstract fun rebuildEntryWidgets()
}