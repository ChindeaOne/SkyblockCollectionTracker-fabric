package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.*;
import io.github.notenoughupdates.moulconfig.observer.Property;

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
    @ConfigOption(
            name = "Professional mining speed",
            desc = "Input your mining speed from Professional perk."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 755, minStep = 1)
    public Property<Integer> professionalMS = Property.of(0);

    @Expose
    @ConfigOption(
            name = "Strong Arm mining speed",
            desc = "Input your mining speed from Strong Arm perk."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 505, minStep = 1)
    public Property<Integer> strongArmMS = Property.of(0);

    @Expose
    @ConfigLink(owner = MiningStatsOverlay.class, field = "miningStatsOverlay")
    public Position miningStatsOverlayPosition = new Position(50, 150);
}