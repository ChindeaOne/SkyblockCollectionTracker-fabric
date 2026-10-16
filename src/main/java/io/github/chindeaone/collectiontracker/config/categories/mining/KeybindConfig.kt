package io.github.chindeaone.collectiontracker.config.categories.mining

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import com.mojang.blaze3d.platform.InputConstants

class KeybindConfig {
    @Expose
    @ConfigOption(
        name = "Enable Commissions keybinds",
        desc = "Set and use keys to quickly claim your commissions."
    )
    @ConfigEditorBoolean
    var enableCommissionsKeybinds: Boolean = false

    @Expose
    @ConfigOption(name = "Commission 1 Keybind", desc = "")
    @ConfigEditorKeybind(defaultKey = InputConstants.KEY_1)
    var commission1: Int = InputConstants.KEY_1

    @Expose
    @ConfigOption(name = "Commission 2 Keybind", desc = "")
    @ConfigEditorKeybind(defaultKey = InputConstants.KEY_2)
    var commission2: Int = InputConstants.KEY_2

    @Expose
    @ConfigOption(name = "Commission 3 Keybind", desc = "")
    @ConfigEditorKeybind(defaultKey = InputConstants.KEY_3)
    var commission3: Int = InputConstants.KEY_3

    @Expose
    @ConfigOption(name = "Commission 4 Keybind", desc = "")
    @ConfigEditorKeybind(defaultKey = InputConstants.KEY_4)
    var commission4: Int = InputConstants.KEY_4
}
