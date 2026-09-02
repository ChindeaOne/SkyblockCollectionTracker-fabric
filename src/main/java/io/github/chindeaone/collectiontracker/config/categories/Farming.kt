package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.categories.farmingweight.FarmingweightConfig;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Farming {

    @Expose
    @ConfigOption(name = "Farming Weight", desc = "")
    @Accordion
    public FarmingweightConfig farmingweightConfig = new FarmingweightConfig();
}
