package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;

public class SkillOverlay {

    @Expose
    @ConfigOption(
            name = "Enable Taming Tracking",
            desc = "Toggles tracking for Taming as well.\n§eWon't work if you enable mid tracking!"
    )
    @ConfigEditorBoolean
    public boolean enableTamingTracking = false;

    @Expose
    @ConfigLink(owner = SkillOverlay.class, field = "skillOverlay")
    public Position skillOverlayPosition = new Position(50, 200);
}