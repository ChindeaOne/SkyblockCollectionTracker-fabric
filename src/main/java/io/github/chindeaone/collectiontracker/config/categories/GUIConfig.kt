package io.github.chindeaone.collectiontracker.config.categories

import io.github.chindeaone.collectiontracker.gui.GuiManager
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class GUIConfig {
    @ConfigOption(name = "Edit GUI Location", desc = "Edit the position of all overlays.")
    @ConfigEditorButton(buttonText = "Edit")
    @Suppress("unused")
    var positions: Runnable = Runnable { GuiManager.openGuiPositionEditor() }
}
