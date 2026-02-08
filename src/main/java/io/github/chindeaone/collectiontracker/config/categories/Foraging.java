package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.categories.overlay.ForagingStatsOverlay;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Foraging {

    @Expose
    @ConfigOption(name = "Foraging Stats Overlay", desc = "")
    @Accordion
    public ForagingStatsOverlay foragingStatsOverlay = new ForagingStatsOverlay();
}