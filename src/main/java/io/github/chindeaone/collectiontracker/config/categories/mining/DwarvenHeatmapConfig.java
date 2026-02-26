package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class DwarvenHeatmapConfig {

    @Expose
    @ConfigOption(
            name = "Enable Heatmap",
            desc = "Toggles the Dwarven Heatmap for Glacite Tunnels."
    )
    @ConfigEditorBoolean
    public boolean enableHeatmap = false;
}
