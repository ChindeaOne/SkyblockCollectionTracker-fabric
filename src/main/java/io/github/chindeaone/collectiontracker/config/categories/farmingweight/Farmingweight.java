package io.github.chindeaone.collectiontracker.config.categories.farmingweight;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Farmingweight {

    @Expose
    @ConfigOption(
            name = "Farming Weight ranks in chat",
            desc = "Enable Farming Weight ranks in chat."
    )
    @ConfigEditorBoolean
    public boolean farmingweightRankingInChat = false;

    @Expose
    @ConfigOption(
            name = "Farming Islands Only",
            desc = "Show Farming Weight ranks in chat only when on Farming Islands."
    )
    @ConfigEditorBoolean
    public boolean onlyOnFarmingIslands = false;

    @Expose
    @ConfigOption(name = "Custom Farming Weight Rank Color", desc = "")
    @Accordion
    public FarmingweightColor farmingweightColor = new FarmingweightColor();
}

