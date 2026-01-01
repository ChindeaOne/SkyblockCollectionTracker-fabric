package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.glfw.GLFW;

public class KeybindConfig {

    @Expose
    @ConfigOption(name = "Enable Commissions keybinds", desc = "Lets you use your number keys to quickly claim your commissions")
    @ConfigEditorBoolean
    public boolean enableCommissionsKeybinds = false;

    @Expose
    @ConfigOption(name = "Commission 1", desc = "Keybind to claim the first commission")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_1)
    public int commission1 = GLFW.GLFW_KEY_1;

    @Expose
    @ConfigOption(name = "Commission 2", desc = "Keybind to claim the second commission")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_2)
    public int commission2 = GLFW.GLFW_KEY_2;

    @Expose
    @ConfigOption(name = "Commission 3", desc = "Keybind to claim the third commission")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_3)
    public int commission3 = GLFW.GLFW_KEY_3;

    @Expose
    @ConfigOption(name = "Commission 4", desc = "Keybind to claim the fourth commission")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_4)
    public int commission4 = GLFW.GLFW_KEY_4;
}
