package io.github.chindeaone.collectiontracker.config.categories.coleweight;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class HeatmapConfig {

    @Expose
    @ConfigOption(
            name = "Enable Heatmap",
            desc = "Toggles the Heatmap for Glacite Tunnels.\n§eColor mapping: §0black §f> §9blue §f> §baqua §f> §cred §f> §2green"
    )
    @ConfigEditorBoolean
    public boolean enableHeatmap = false;

    @Expose
    @ConfigOption(
            name = "Heatmap Opacity",
            desc = "Adjust the opacity of the heatmap overlay. (0.0 - fully transparent, 1.0 - fully opaque)"
    )
    @ConfigEditorSlider(minValue = 0.0f, maxValue = 1.0f, minStep = 0.1f)
    public Property<Float> heatmapOpacity = Property.of(1.0f);
}
