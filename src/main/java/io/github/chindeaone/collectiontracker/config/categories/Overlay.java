package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.categories.overlay.OverlayList;
import io.github.chindeaone.collectiontracker.config.categories.overlay.OverlaySingle;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Overlay {

    @Expose
    @ConfigOption(name = "Simple Overlay", desc = "")
    @Accordion
    public OverlaySingle overlaySingle = new OverlaySingle();

    @Expose
    @ConfigOption(name = "Overlay List", desc = "Choose between multiple collections to track.")
    @Accordion
    public OverlayList overlayList = new OverlayList();

    @Expose
    @ConfigOption(
            name = "Overlay Text Color",
            desc = "Toggle this to enable color-coded text."
    )
    @ConfigEditorBoolean
    public boolean overlayTextColor = false;
}
