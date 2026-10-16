package io.github.chindeaone.collectiontracker.config.categories.farmingweight

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.ChromaColour.Companion.fromStaticRGB
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FarmingweightColorConfig {
    @Expose
    @ConfigOption(
        name = "Enable Custom Color",
        desc = "Use a custom color for your Farming Weight rank."
    )
    @ConfigEditorBoolean
    var enableCustomColor: Boolean = false

    @Expose
    @ConfigOption(
        name = "Set Custom Color",
        desc = "§eNote: If both Coleweight and Farming Weight use animated chroma, Coleweight takes animation speed priority."
    )
    @ConfigEditorColour
    var customColor: ChromaColour = fromStaticRGB(0, 0, 0, 0)

    @Expose
    var customColors: MutableMap<String, String> = mutableMapOf()
}
