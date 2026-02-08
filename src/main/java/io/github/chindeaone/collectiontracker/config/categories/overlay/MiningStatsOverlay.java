package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.categories.mining.HotmPerks;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.*;

public class MiningStatsOverlay {

    @Expose
    @ConfigOption(
            name = "Mining Stats Overlay",
            desc = "Toggles an overlay for mining stats.\n§eYou need to have mining stats (e.g. Mining Speed, Mining Fortune etc) in your stats widget for this to work.")
    @ConfigEditorBoolean
    public boolean enableMiningStatsOverlay = false;

    @Expose
    @ConfigOption(
            name = "Only in mining specific areas",
            desc = "Allows the overlay to be rendered only in Mining Islands."
    )
    @ConfigEditorBoolean
    public boolean miningStatsOverlayInMiningIslandsOnly = false;

    @Expose
    @ConfigOption(
            name = "Show detailed fortune",
            desc = "Shows the fortune breakdown in the overlay."
    )
    @ConfigEditorBoolean
    public boolean showDetailedFortune = false;

    @Expose
    @ConfigOption(name = "HOTM perks", desc = "")
    @Accordion
    public HotmPerks hotmPerks = new HotmPerks();

    @Expose
    @ConfigLink(owner = MiningStatsOverlay.class, field = "miningStatsOverlay")
    public Position miningStatsOverlayPosition = new Position(50, 150);
}