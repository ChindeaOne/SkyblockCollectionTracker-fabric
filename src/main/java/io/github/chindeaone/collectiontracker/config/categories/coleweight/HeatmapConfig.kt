package io.github.chindeaone.collectiontracker.config.categories.coleweight

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HeatmapConfig {
    @Expose
    @ConfigOption(
        name = "Enable Heatmap",
        desc = "Toggles the Heatmap for Glacite Tunnels.\n§eColor mapping: §2dark green §f> §alight green."
    )
    @ConfigEditorBoolean
    var enableHeatmap: Boolean = false

    @Expose
    @ConfigOption(
        name = "Heatmap Opacity",
        desc = "Adjust the opacity of the heatmap overlay. (0.0 - fully transparent, 1.0 - fully opaque)"
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 1f, minStep = 0.1f)
    var heatmapOpacity: Float = 1f
}
