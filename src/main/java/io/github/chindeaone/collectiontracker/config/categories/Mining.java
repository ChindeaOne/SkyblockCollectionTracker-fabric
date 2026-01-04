package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.categories.mining.CommissionsOverlay;
import io.github.chindeaone.collectiontracker.config.categories.mining.KeybindConfig;
import io.github.chindeaone.collectiontracker.config.categories.overlay.CommissionsOverlay;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Mining {

    @Expose
    @ConfigOption(name = "Commissions Keybinds", desc = "")
    @Accordion
    public KeybindConfig commissions = new KeybindConfig();

    @Expose
    @ConfigOption(name = "Commissions Overlay", desc = "")
    @Accordion
    public CommissionsOverlay commissionsOverlay = new CommissionsOverlay();
}
