package io.github.chindeaone.collectiontracker.config.categories.coleweight

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.ChromaColour.Companion.fromStaticRGB
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ColeweightColorConfig {
    @Expose
    @ConfigOption(
        name = "Enable Custom Rank Color",
        desc = "Allows you to customize your Coleweight rank color in chat."
    )
    @ConfigEditorBoolean
    var enableCustomColor: Boolean = false

    @Expose
    @ConfigOption(
        name = "Custom Rank Color",
        desc = "§eNote: If both Coleweight and Farming Weight use animated chroma, Coleweight determines the animation speed."
    )
    @ConfigEditorColour
    var customColor: ChromaColour = fromStaticRGB(0, 0, 0, 0)

    @Expose
    var customColors: MutableMap<String, String> = mutableMapOf()
}