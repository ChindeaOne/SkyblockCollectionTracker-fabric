package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.categories.coleweight.ColeweightConfig;
import io.github.chindeaone.collectiontracker.config.categories.mining.*;
import io.github.chindeaone.collectiontracker.config.categories.mining.routes.MiningRoutesConfig;
import io.github.chindeaone.collectiontracker.config.categories.overlay.CommissionsConfig;
import io.github.chindeaone.collectiontracker.config.categories.overlay.MiningStatsConfig;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Mining {

    @Expose
    @ConfigOption(name = "Coleweight", desc = "")
    @Accordion
    public ColeweightConfig coleweightConfig = new ColeweightConfig();

    @Expose
    @ConfigOption(name = "Commissions", desc = "")
    @Accordion
    public CommissionsConfig commissionsConfig = new CommissionsConfig();

    @Expose
    @ConfigOption(name = "Mining Stats Overlay", desc = "")
    @Accordion
    public MiningStatsConfig miningStatsConfig = new MiningStatsConfig();

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
    public LanternDeployableConfig lanternDeployableConfig = new LanternDeployableConfig();

    @Expose
    @ConfigOption(name = "Temporary Buffs Tracker", desc = "")
    @Accordion
    public TemporaryBuffsConfig temporaryBuffsConfig = new TemporaryBuffsConfig();
}