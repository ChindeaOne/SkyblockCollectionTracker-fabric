package io.github.chindeaone.collectiontracker.gui

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker.configManager
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker.screenToOpen
import io.github.chindeaone.collectiontracker.config.ModConfig
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import net.minecraft.network.chat.Component

object GuiManager {

    var editor: MoulConfigEditor<ModConfig>? = null

    fun getEditorInstance() = editor ?: MoulConfigEditor(configManager.processor).also { editor = it }

    fun openConfigGui(search: String? = null) {
        println("[SCT]: Opening config gui")
        val editor = getEditorInstance()

        if (search != null) {
            editor.search(search)
        }
        openEditor(editor)
    }

    fun openEditor(editor: MoulConfigEditor<*>) {
        screenToOpen = MoulConfigScreenComponent(Component.empty(), GuiContext(GuiElementComponent(editor)), null)
    }
}