package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.categories.mining.KeybindConfig;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CommissionsConfig {

    @Expose
    @ConfigOption(
            name = "Enable Commissions Overlay",
            desc = "Toggles an overlay for mining commissions.")
    @ConfigEditorBoolean
    public boolean enableCommissionsOverlay = false;

    @Expose
    @ConfigLink(owner = CommissionsConfig.class, field = "enableCommissionsOverlay")
    public Position commissionsOverlayPosition = new Position(50, 50);

    @Expose
    @ConfigOption(
            name = "Enable Commissions Tracking",
            desc = "Toggles tracking for mining commissions."
    )
    @ConfigEditorBoolean
    public boolean enableCommissionsTracking =  false;

    @Expose
    @ConfigOption(name = "Commissions Keybinds", desc = "")
    @Accordion
    public KeybindConfig keybindConfig = new KeybindConfig();
}
