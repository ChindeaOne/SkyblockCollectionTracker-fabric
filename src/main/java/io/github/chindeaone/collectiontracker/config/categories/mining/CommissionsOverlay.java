package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CommissionsOverlay {

    @Expose
    @ConfigOption(
            name = "Enable Commissions Overlay",
            desc = "Toggles the display of the commissions overlay on the screen")
    @ConfigEditorBoolean
    public boolean enableCommissionsOverlay = false;

    @Expose
    @ConfigLink(owner = CommissionsOverlay.class, field = "commissionsOverlay")
    public Position commissionsOverlayPosition = new Position(50, 50);
}
