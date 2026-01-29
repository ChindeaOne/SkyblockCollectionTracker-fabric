package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MiningStatsOverlay {

    @Expose
    @ConfigOption(
            name = "Enable Mining Stats Overlay",
            desc = "Toggles an overlay for mining stats.\n§eYou need to have mining stats (e.g. Mining Speed, Mining Fortune etc) in your stats widget for this to work.")
    @ConfigEditorBoolean
    public boolean enableMiningStatsOverlay = false;

    @Expose
    @ConfigLink(owner = MiningStatsOverlay.class, field = "miningStatsOverlay")
    public Position miningStatsOverlayPosition = new Position(240, 50);
}
