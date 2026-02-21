package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.categories.coleweight.Coleweight;
import io.github.chindeaone.collectiontracker.config.categories.mining.HotmConfig;
import io.github.chindeaone.collectiontracker.config.categories.mining.LanternDeployable;
import io.github.chindeaone.collectiontracker.config.categories.mining.MiningRoutesConfig;
import io.github.chindeaone.collectiontracker.config.categories.mining.TemporaryBuffsConfig;
import io.github.chindeaone.collectiontracker.config.categories.overlay.CommissionsOverlay;
import io.github.chindeaone.collectiontracker.config.categories.overlay.MiningStatsOverlay;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Mining {

    @Expose
    @ConfigOption(name = "Coleweight", desc = "")
    @Accordion
    public Coleweight coleweight = new Coleweight();

    @Expose
    @ConfigOption(name = "Commissions Overlay", desc = "")
    @Accordion
    public CommissionsOverlay commissionsOverlay = new CommissionsOverlay();

    @Expose
    @ConfigOption(name = "Mining Stats Overlay", desc = "")
    @Accordion
    public MiningStatsOverlay miningStatsOverlay = new MiningStatsOverlay();

    @Expose
    @ConfigOption(name = "HOTM perks", desc = "")
    @Accordion
    public HotmConfig hotmConfig = new HotmConfig();

    @Expose
    @Category(name = "Mining Routes", desc = "")
    public MiningRoutesConfig miningRoutesConfig = new MiningRoutesConfig();

    @Expose
    @ConfigOption(name = "Lantern Deployable", desc = "")
    @Accordion
    public LanternDeployable lanternDeployable = new LanternDeployable();

    @Expose
    @ConfigOption(name = "Temporary Buffs Tracker", desc = "")
    @Accordion
    public TemporaryBuffsConfig temporaryBuffsConfig = new TemporaryBuffsConfig();
}