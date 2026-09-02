package io.github.chindeaone.collectiontracker.config.categories.coleweight

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class PrecisionMiningConfig {
    @Expose
    @ConfigOption(
        name = "Precision Mining Highlight",
        desc = "Highlights Precision Mining particles and prevents them from rendering."
    )
    @ConfigEditorBoolean
    var enablePrecisionMiningHighlight: Boolean = false

    @Expose
    @ConfigOption(
        name = "Line to Precision Mining",
        desc = "Draws a line to the highlighted particles.\n§eOnly works if Precision Mining Highlight is enabled."
    )
    @ConfigEditorBoolean
    var drawLineToPrecisionMining: Boolean = false
}
