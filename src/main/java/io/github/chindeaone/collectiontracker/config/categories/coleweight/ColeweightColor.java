package io.github.chindeaone.collectiontracker.config.categories.coleweight;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ColeweightColor {

    @Expose
    @ConfigOption(
        name = "Enable Custom Coleweight Rank Color",
        desc = "Allows you to customize your Coleweight rank color in chat."
    )
    @ConfigEditorBoolean
    public boolean enableCustomColor = false;

    @Expose
    @ConfigOption(name = "Rank Color", desc = "Set a custom color.")
    @ConfigEditorColour
    public ChromaColour customColor = ChromaColour.fromStaticRGB(0, 0, 0, 0);
}