package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ForagingStatsOverlay {

    @Expose
    @ConfigOption(
            name = "Foraging Stats Overlay",
            desc = "Toggles an overlay for foraging stats.\n§eYou need to have foraging stats (e.g. Foraging Fortune, Sweep etc) in your stats widget for this to work."
    )
    @ConfigEditorBoolean
    public boolean enableForagingStatsOverlay = false;

    @Expose
    @ConfigOption(
            name = "Show detailed fortune",
            desc = "Shows the fortune breakdown in the overlay."
    )
    @ConfigEditorBoolean
    public boolean showDetailedFortune = false;

    @Expose
    @ConfigLink(owner = ForagingStatsOverlay.class, field = "foragingStatsOverlay")
    public Position foragingStatsOverlayPosition = new Position(50, 200);
}
