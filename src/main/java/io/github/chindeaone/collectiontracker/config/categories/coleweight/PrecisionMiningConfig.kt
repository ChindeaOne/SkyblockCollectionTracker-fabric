package io.github.chindeaone.collectiontracker.config.categories.coleweight

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class PrecisionMiningConfig {
    @Expose
    @ConfigOption(name = "Precision Mining Highlight", desc = "")
    @ConfigEditorBoolean
    var enablePrecisionMiningHighlight: Boolean = false

    @Expose
    @ConfigOption(
        name = "Line to Precision Mining",
        desc = "Draw a line from your cursor to the particle.\n§eNote: Only works if Precision Mining Highlight is enabled."
    )
    @ConfigEditorBoolean
    var drawLineToPrecisionMining: Boolean = false
}
