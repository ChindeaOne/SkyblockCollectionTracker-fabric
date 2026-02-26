package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class DwarvenHeatmapConfig {

    @Expose
    @ConfigOption(
            name = "Enable Heatmap",
            desc = "Toggles the Heatmap for Glacite Tunnels.\n§eColor mapping: §0black §f> §9blue §f> §baqua §f> §cred §f> §2green"
    )
    @ConfigEditorBoolean
    public boolean enableHeatmap = false;
}