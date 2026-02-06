package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.categories.coleweight.Coleweight;
import io.github.chindeaone.collectiontracker.config.categories.overlay.CommissionsOverlay;
import io.github.chindeaone.collectiontracker.config.categories.overlay.MiningStatsOverlay;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Mining {

    @Expose
    @ConfigOption(name = "Commissions Overlay", desc = "")
    @Accordion
    public CommissionsOverlay commissionsOverlay = new CommissionsOverlay();

    @Expose
    @ConfigOption(name = "Mining Stats Overlay", desc = "")
    @Accordion
    public MiningStatsOverlay miningStatsOverlay = new MiningStatsOverlay();

    @Expose
    @ConfigOption(name = "Coleweight", desc = "")
    @Accordion
    public Coleweight coleweight = new Coleweight();
}