package io.github.chindeaone.collectiontracker.config.categories.farmingweight;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.HashMap;
import java.util.Map;

public class FarmingweightColorConfig {

    @Expose
    @ConfigOption(
            name = "Enable Custom Rank Color",
            desc = "Allows you to customize your Farming Weight rank color in chat."
    )
    @ConfigEditorBoolean
    public boolean enableCustomColor = false;

    @Expose
    @ConfigOption(
            name = "Custom Rank Color",
            desc = "§eNote: If both Coleweight and Farming Weight use animated chroma, Coleweight determines the animation speed."
    )
    @ConfigEditorColour
    public ChromaColour customColor = ChromaColour.fromStaticRGB(0, 0, 0, 0);

    @Expose
    public Map<String, String> customColors = new HashMap<>();
}
