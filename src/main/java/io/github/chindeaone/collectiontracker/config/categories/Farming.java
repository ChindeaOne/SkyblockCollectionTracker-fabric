package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.categories.farmingweight.Farmingweight;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Farming {

    @Expose
    @ConfigOption(name = "Farming Weight", desc = "")
    @Accordion
    public Farmingweight farmingweight = new Farmingweight();
}
