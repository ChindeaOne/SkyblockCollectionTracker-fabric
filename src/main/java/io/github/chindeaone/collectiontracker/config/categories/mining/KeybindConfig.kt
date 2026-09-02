package io.github.chindeaone.collectiontracker.config.categories.mining

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class KeybindConfig {
    @Expose
    @ConfigOption(name = "Enable Commissions keybinds", desc = "Set and use keys to quickly claim your commissions.")
    @ConfigEditorBoolean
    var enableCommissionsKeybinds: Boolean = false

    @Expose
    @ConfigOption(name = "Commission 1", desc = "Keybind to claim the 1st commission.")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_1)
    var commission1: Int = GLFW.GLFW_KEY_1

    @Expose
    @ConfigOption(name = "Commission 2", desc = "Keybind to claim the 2nd commission.")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_2)
    var commission2: Int = GLFW.GLFW_KEY_2

    @Expose
    @ConfigOption(name = "Commission 3", desc = "Keybind to claim the 3rd commission.")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_3)
    var commission3: Int = GLFW.GLFW_KEY_3

    @Expose
    @ConfigOption(name = "Commission 4", desc = "Keybind to claim the 4th commission.")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_4)
    var commission4: Int = GLFW.GLFW_KEY_4
}
