/*
 * This kotlin object is derived from the SkyHanni mod.
 */
package io.github.chindeaone.collectiontracker.gui

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker.configManager
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker.screenToOpen
import io.github.chindeaone.collectiontracker.config.ModConfig
import io.github.chindeaone.collectiontracker.gui.overlays.ChangelogOverlay
import io.github.chindeaone.collectiontracker.gui.overlays.DummyOverlay
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

object GuiManager {

    var editor: MoulConfigEditor<ModConfig>? = null

    fun getEditorInstance() = editor ?: MoulConfigEditor(configManager.processor).also { editor = it }

    fun openConfigGui(search: String? = null) {
        val editor = getEditorInstance()

        if (search != null) {
            editor.search(search)
        }
        openEditor(editor)
    }

    fun openEditor(editor: MoulConfigEditor<*>) {
        screenToOpen = MoulConfigScreenComponent(Component.empty(), GuiContext(GuiElementComponent(editor)), null)
    }

    @JvmStatic
    fun openGuiPositionEditor() {
        OverlayManager.setGlobalRendering(false)

        val current = Minecraft.getInstance().screen
        val old = current as? AbstractContainerScreen<*>

        screenToOpen = DummyOverlay(old)
    }

    @JvmStatic
    fun openChangelog() {
        OverlayManager.setGlobalRendering(false)

        val current = Minecraft.getInstance().screen
        val old = current as? AbstractContainerScreen<*>

        screenToOpen = ChangelogOverlay(old)
    }
}