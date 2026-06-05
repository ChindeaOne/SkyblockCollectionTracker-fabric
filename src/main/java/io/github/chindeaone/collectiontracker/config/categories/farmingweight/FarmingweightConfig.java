package io.github.chindeaone.collectiontracker.config.categories.farmingweight;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class FarmingweightConfig {

    @Expose
    @ConfigOption(
            name = "Farming Weight Ranks in chat",
            desc = "Enable Farming Weight ranks in chat.\n§eNote: If you use Skyhanni's chat formatting, make sure you have at least §bPlayer Name §evisible!"
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
    public FarmingweightColorConfig farmingweightColorConfig = new FarmingweightColorConfig();
}

