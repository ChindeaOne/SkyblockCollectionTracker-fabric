package io.github.chindeaone.collectiontracker.config.categories.farmingweight;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.HashMap;
import java.util.Map;

public class FarmingweightColor {

    @Expose
    @ConfigOption(
            name = "Custom Farming Weight Rank Color",
            desc = "Allows you to customize your Farming Weight rank color in chat."
    )
    @ConfigEditorBoolean
    public boolean enableCustomColor = false;

    @Expose
    @ConfigOption(name = "Rank Color", desc = "Set a custom color.")
    @ConfigEditorColour
    public ChromaColour customColor = ChromaColour.fromStaticRGB(0, 0, 0, 0);

    @Expose
    public Map<String, String> customColors = new HashMap<>();
}
