package io.github.chindeaone.collectiontracker.config.categories.coleweight;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PrecisionMiningConfig {

    @Expose
    @ConfigOption(
            name = "Precision Mining Highlight",
            desc = "Highlights Precision Mining particles and prevents them from rendering."
    )
    @ConfigEditorBoolean
    public boolean enablePrecisionMiningHighlight = false;

    @Expose
    @ConfigOption(
            name = "Enable Line to Precision Mining",
            desc = "Draws a line to the highlighted particles.\n§eOnly works if Precision Mining Highlight is enabled."
    )
    @ConfigEditorBoolean
    public boolean drawLineToPrecisionMining = false;
}
